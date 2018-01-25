package tecsun.cjw.systemupdate.http.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import tecsun.cjw.systemupdate.been.DownloadInfo;
import tecsun.cjw.systemupdate.http.OkHttpUtil;
import tecsun.cjw.systemupdate.utils.UI;

public class DownloadManager {

	public static final int STATE_UNDO = 0; // 未下载
	public static final int STATE_WAITING = 1; // 等待下载
	public static final int STATE_DOWNLOADING = 2; // 正在下载
	public static final int STATE_PAUSE = 3; // 暂停下载
	public static final int STATE_FAIL = 4; // 下载失败
	public static final int STATE_SUCCESS = 5; // 下载成功
	public static final int STATE_CANCEL = 6; // 取消下载

	// 被观察者-观察者 1对多

	// 观察者集合
	private ArrayList<DownloadObserver> observerList = new ArrayList<DownloadObserver>();
	// downloadInfo对象集合
	private ConcurrentHashMap<String, DownloadInfo> downloadInfoMap = new ConcurrentHashMap<>();
	// 线程对象集合
	private ConcurrentHashMap<String, DownloadTask> downloadTaskMap = new ConcurrentHashMap<>();

	private static DownloadManager downloadManager = new DownloadManager();

	public static DownloadManager getInstance() {
		return downloadManager;
	}

	/**
	 * 开始下载
	 */
	public synchronized void download(final DownloadInfo downloadInfo) {
		downloadInfo.currentState = STATE_WAITING;// 切换为等待下载
		notifyDownloadStateChanged(downloadInfo);// 通知所有观察者，下载状态改变

		OkHttpUtil.getInstance().doHttp(downloadInfo.url, new Callback() {//获取contentLength
			@Override
			public void onResponse(Call call, Response response) {
				try {
					if (response.code() != 200) {
						System.out.println("响应码-" + response.code() + "-" + response.message());
						UI.showToast("响应码-" + response.code() + "-" + response.message());
						return;
					}
					// 获取资源大小
					downloadInfo.contentLength = response.body().contentLength();
					System.out.println(downloadInfo.name + "-downloadInfo.contentLength-" + downloadInfo.contentLength);

					DownloadTask downloadTask = new DownloadTask(downloadInfo);
					ThreadManager.getInstance().execute(downloadTask);// 开始下载
					downloadInfoMap.put(downloadInfo.url, downloadInfo);// 添加到下载对象集合里面
					downloadTaskMap.put(downloadInfo.url, downloadTask);// 添加到下载线程集合里面

					response.body().close();
				} catch (Exception e) {
					e.printStackTrace();
					UI.showToast("下载出错-" + e.getMessage());
				}
			}

			@Override
			public void onFailure(Call call, IOException e) {
				e.printStackTrace();
				UI.showToast(e.getMessage());
			}
		});
	}

	/**
	 * 下载暂停
	 */
	public synchronized void pause(DownloadInfo downloadInfo) {
		if (downloadInfo != null) {
			if (downloadInfo.currentState == STATE_WAITING || downloadInfo.currentState == STATE_DOWNLOADING) {
				downloadInfo.currentState = STATE_PAUSE;
				notifyDownloadStateChanged(downloadInfo);// 通知所有观察者，下载状态改变

				DownloadTask downloadTask = downloadTaskMap.get(downloadInfo.url);
				if (downloadTask != null) {
					//					OkHttpUtil.getInstance().cancel(downloadInfo.url);
					ThreadManager.getInstance().cancel(downloadTask);
				}
				downloadTaskMap.remove(downloadInfo.url);
				System.out.println(downloadInfo.name + "下载暂停");
			}
		}
	}

	/**
	 * 取消下载
	 */
	public synchronized void cancel(DownloadInfo downloadInfo) {
		System.out.println("取消下载");
		if (downloadInfo != null) {
			downloadInfo.currentPos = 0;
			downloadInfo.currentState = STATE_CANCEL;// 切换为取消下载
			notifyDownloadStateChanged(downloadInfo);// 通知所有观察者，下载状态改变

			DownloadTask downloadTask = downloadTaskMap.get(downloadInfo.url);
			if (downloadTask != null) {
				//				OkHttpUtil.getInstance().cancel(downloadInfo.url);
				ThreadManager.getInstance().cancel(downloadTask);
			}
			downloadTaskMap.remove(downloadInfo.url);
			downloadInfoMap.remove(downloadInfo.url);

			File file = new File(downloadInfo.filePath);// 获取文件路径
			file.delete();// 删除文件
		}
	}

	/**
	 * 下载线程
	 */
	private class DownloadTask implements Runnable {

		private DownloadInfo downloadInfo;

		DownloadTask(DownloadInfo downloadInfo) {
			this.downloadInfo = downloadInfo;
		}

		@Override
		public void run() {
			//判断本地是否已经有下载过的文件
			long downloadLength = 0;
			long contentLength = downloadInfo.contentLength;
			File file = new File(downloadInfo.filePath);
			DownloadCallback downloadCallback = new DownloadCallback(downloadInfo);

			if (file.exists()) {//存在
				downloadLength = file.length();//得到下载内容的大小
				if (contentLength == downloadLength) {
					downloadInfo.currentState = STATE_SUCCESS;
					//// TODO: 2018/1/23 0023  测试
					file.delete();
					OkHttpUtil.getInstance().downloadFileByRange(downloadInfo.url, 0, contentLength, downloadCallback);
				} else {//断点续传
					OkHttpUtil.getInstance().downloadFileByRange(downloadInfo.url, downloadLength, contentLength, downloadCallback);
				}
			} else {//不存在，从头开始下载
				OkHttpUtil.getInstance().downloadFileByRange(downloadInfo.url, 0, contentLength, downloadCallback);
			}
		}
	}

	private class DownloadCallback implements Callback {
		private DownloadInfo downloadInfo;

		private File mFile;

		DownloadCallback(DownloadInfo downloadInfo) {
			this.downloadInfo = downloadInfo;
			mFile = new File(downloadInfo.filePath);// 获取文件路径
		}

		@Override
		public void onResponse(Call call, Response response) {
			try {
				if (response != null) {
					System.out.println(downloadInfo.name + "开始下载");

					downloadInfo.currentState = STATE_DOWNLOADING;
					notifyDownloadStateChanged(downloadInfo);// 通知所有观察者，下载状态改变

					InputStream in = response.body().byteStream();
					RandomAccessFile savedFile = new RandomAccessFile(mFile, "rw");
					long total = savedFile.length();
					savedFile.seek(total);//跳过已经下载的字节

					byte[] b = new byte[1024<<2];
					int len;
					while (downloadInfo.currentState == STATE_DOWNLOADING && (len = in.read(b)) != -1) {
						total += len;
						savedFile.write(b, 0, len);
						downloadInfo.currentPos = total;//更新下载进度
						System.out.println(total);
						notifyDownloadStateChanged(downloadInfo);// 通知观察者，下载进度发生变化
					}
					response.body().close();

					if (downloadInfo.currentState != STATE_PAUSE && downloadInfo.currentState != STATE_CANCEL) {
						if (mFile.length() == downloadInfo.contentLength) {
							// 下载成功
							downloadInfo.currentState = STATE_SUCCESS;
							notifyDownloadStateChanged(downloadInfo);// 通知观察者，下载进度发生变化

							System.out.println(downloadInfo.name + "下载成功");
						} else {//下载结束的文件大小不对
							//下载失败
							downloadInfo.currentState = STATE_FAIL;
							notifyDownloadStateChanged(downloadInfo);// 通知观察者，下载进度发生变化
							delete(downloadInfo);//删除文件
							System.out.println(downloadInfo.name + "下载失败");
						}
					}
					// 下载成功，移除任务
					downloadTaskMap.remove(downloadInfo.url);
				}
			} catch (Exception e) {
				// 下载失败，移除任务
				downloadTaskMap.remove(downloadInfo.url);

				e.printStackTrace();
				UI.showToast(e.getMessage());
			} finally {
				if (response != null) response.body().close();
			}
		}

		@Override
		public void onFailure(Call call, IOException e) {
			e.printStackTrace();
			mFile.delete();// 删除无效文件
			downloadInfo.currentState = STATE_FAIL;

			notifyDownloadStateChanged(downloadInfo);// 通知所有观察者，下载状态改变

			// 下载失败，移除任务
			downloadTaskMap.remove(downloadInfo.url);
			System.out.println(downloadInfo.name + "下载失败");
		}
	}

	/**
	 * 删除下载文件
	 */
	public synchronized void delete(DownloadInfo downloadInfo) {
		File file = new File(downloadInfo.filePath);// 获取文件路径
		file.delete();

		downloadInfo.currentPos = 0;
		downloadInfo.currentState = STATE_UNDO;
	}

	/**
	 * 通知所有观察者下载状态改变
	 */
	private void notifyDownloadStateChanged(final DownloadInfo info) {
		UI.runOnUIThread(new Runnable() {
			@Override
			public void run() {
				for (DownloadObserver observer : observerList) {
					observer.onDownloadStateChanged(info);
				}
			}
		});
	}

	/**
	 * 注册观察者，添加观察者
	 */
	public void registerObserver(DownloadObserver observer) {
		if (observer != null && !observerList.contains(observer)) {
			observerList.add(observer);
		}
	}

	/**
	 * 取消观察者，移除
	 */
	public void unRegisterObserver(DownloadObserver observer) {
		if (observer != null && observerList.contains(observer)) {
			observerList.remove(observer);
		}
	}

	/**
	 * 观察者接口
	 */
	public interface DownloadObserver {
		void onDownloadStateChanged(DownloadInfo downloadInfo);
	}

	/**
	 * 获取下载对象
	 */
	public DownloadInfo getDownloadInfo(String url) {
		return downloadInfoMap.get(url);
	}

	/**
	 * 判断当前DownloadInfo是否有下载线程
	 */
	public boolean hasDownloadTask(String url) {
		if (downloadTaskMap.get(url) != null) return true;
		else return false;
	}

	public synchronized ConcurrentHashMap<String, DownloadInfo> getDownloadInfoMap() {
		return downloadInfoMap;
	}

	public synchronized Long getTotalContentLength() {
		Iterator iter = downloadInfoMap.entrySet().iterator();
		long total = 0;
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			DownloadInfo downloadInfo = (DownloadInfo) entry.getValue();
			total += downloadInfo.contentLength;
		}
		return total;
	}
}
