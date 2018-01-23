package tecsun.cjw.systemupdate;

import android.widget.Toast;

import com.cjw.zhiyue.domain.DownloadInfo;
import com.cjw.zhiyue.domain.DownloadSongInfo;
import com.cjw.zhiyue.utils.SPUtils;
import com.cjw.zhiyue.utils.UIUtils;

import org.xutils.common.Callback;
import org.xutils.common.Callback.Cancelable;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static android.R.attr.x;

public class DownloadManager {

	private static final int STATE_UNDO = 0; // 未下载
	private static final int STATE_WAITING = 1; // 等待下载
	private static final int STATE_DOWNLOADING = 2; // 正在下载
	private static final int STATE_PAUSE = 3; // 暂停下载
	private static final int STATE_FAIL = 4; // 下载失败
	private static final int STATE_SUCCESS = 5; // 下载成功
	private static final int STATE_CANCEL = 6; // 取消下载

	// 被观察者-观察者 1对多

	// 观察者集合
	private ArrayList<DownloadObserver> observerList = new ArrayList<DownloadObserver>();
	// downloadInfo对象集合,HashMap线程不安全,用ConcurrentHashMap
	private ConcurrentHashMap<String, DownloadInfo> downloadInfoMap = new ConcurrentHashMap<>();
	// 线程对象集合
	private ConcurrentHashMap<String, DownloadTask> downloadTaskMap = new ConcurrentHashMap<>();
	// Cancelable对象集合
	private ConcurrentHashMap<String, Cancelable> CancelableMap = new ConcurrentHashMap<>();

	private static DownloadManager downloadManager = new DownloadManager();

	public static DownloadManager getInstance() {
		return downloadManager;
	}

	/**
	 * 开始下载
	 */
	public synchronized void download(DownloadInfo downloadInfo) {

		downloadInfo.currentState = STATE_WAITING;// 切换为等待下载
//		SPUtils.saveDownloadInfo(downloadInfo.song_id, downloadInfo);// 本地存储downloadInfo的信息

		notifyDownloadStateChanged(downloadInfo);// 通知所有观察者，下载状态改变

		downloadInfoMap.put(downloadInfo.downloadUrl, downloadInfo);// 添加到下载对象集合里面

		DownloadTask downloadTask = new DownloadTask(downloadInfo);
		ThreadManager.getInstance().execute(downloadTask);// 开始下载
		downloadTaskMap.put(downloadInfo.downloadUrl, downloadTask);// 添加到下载线程集合里面
	}

	/**
	 * 下载暂停
	 */
	public synchronized void pause(DownloadInfo dInfo) {
		DownloadInfo downloadInfo = null;

//		if (dsInfo == null && dInfo != null) {
//			downloadInfo = downloadInfoMap.get(dInfo.song_id);
//			if (downloadInfo == null) {
//				downloadInfo = SPUtils.readDownloadInfo(dInfo.song_id);
//			}
//
//		} else if (dsInfo != null && dInfo == null) {
//			downloadInfo = downloadInfoMap.get(dsInfo.songinfo.song_id);
//		}

		if (downloadInfo != null) {
			if (downloadInfo.currentState == STATE_WAITING || downloadInfo.currentState == STATE_DOWNLOADING) {
				downloadInfo.currentState = STATE_PAUSE;
//				SPUtils.saveDownloadInfo(downloadInfo.song_id, downloadInfo);// 本地存储downloadInfo的信息

				notifyDownloadStateChanged(downloadInfo);// 通知所有观察者，下载状态改变

				DownloadTask downloadTask = downloadTaskMap.get(downloadInfo.downloadUrl);
				if (downloadTask != null)
					ThreadManager.getInstance().cancel(downloadTask);

//				Cancelable cancelable = CancelableMap.get(downloadInfo.downloadUrl);
//				if (cancelable != null)
//					cancelable.cancel();

				System.out.println(downloadInfo.downloadName + "下载暂停");
//				Toast.makeText(UIUtils.getContext(), "下载暂停", Toast.LENGTH_SHORT).show();
			}
		}

	}

	/**
	 * 取消下载
	 */
	public synchronized void cancel(DownloadInfo downloadInfo) {

		downloadInfo.currentPos = 0;
		downloadInfo.currentState = STATE_CANCEL;// 切换为取消下载
//		SPUtils.saveDownloadInfo(downloadInfo.downloadUrl, downloadInfo);// 本地存储downloadInfo的信息

		notifyDownloadStateChanged(downloadInfo);// 通知所有观察者，下载状态改变

		DownloadTask downloadTask = downloadTaskMap.get(downloadInfo.downloadUrl);
		if (downloadTask != null) {
			ThreadManager.getInstance().cancel(downloadTask);
		}

//		Cancelable cancelable = CancelableMap.get(downloadInfo.song_id);
//		if (cancelable != null)
//			cancelable.cancel();

		File file = new File(downloadInfo.downloadPath + ".tmp");// 获取文件路径
		file.delete();// 删除文件

//		Toast.makeText(UIUtils.getContext(), "下载取消", Toast.LENGTH_SHORT).show();
	}

	/**
	 * 下载线程
	 */
	class DownloadTask implements Runnable {

		private DownloadInfo downloadInfo;

		public DownloadTask(DownloadInfo downloadInfo) {
			this.downloadInfo = downloadInfo;
		}

		@Override
		public void run() {
			System.out.println(downloadInfo.downloadName + "开始下载");

			downloadInfo.currentState = STATE_DOWNLOADING;
			SPUtils.saveDownloadInfo(downloadInfo.song_id, downloadInfo);// 本地存储downloadInfo的信息

			notifyDownloadStateChanged(downloadInfo);// 通知所有观察者，下载状态改变

			File file = new File(downloadInfo.downloadPath);

			RequestParams params = new RequestParams(downloadInfo.downloadUrl);// 设置下载地址
			params.setAutoResume(true);// 设置是否在下载是自动断点续传
			params.setAutoRename(false);// 设置是否根据头信息自动命名文件
			params.setSaveFilePath(file.getPath());// 设置文件下载后的位置
			params.setCancelFast(true);// 是否可以被立即停止.

			Cancelable cancelable = x.http().get(params, new DownloadCallback(downloadInfo));

			CancelableMap.put(downloadInfo.song_id, cancelable);

		}

	}

	private class DownloadCallback implements Callback.ProgressCallback<File> {

		private DownloadInfo downloadInfo;

		private File mFile;

		public DownloadCallback(DownloadInfo downloadInfo) {
			this.downloadInfo = downloadInfo;
			mFile = new File(downloadInfo.downloadPath);// 获取文件路径
		}

		@Override
		public void onSuccess(File file) {
			// 下载成功
			if (mFile.length() == downloadInfo.fileLength) {
				// 下载成功
				downloadInfo.currentState = STATE_SUCCESS;
				SPUtils.saveDownloadInfo(downloadInfo.song_id, downloadInfo);// 本地存储downloadInfo的信息

				notifyDownloadStateChanged(downloadInfo);// 通知观察者，下载进度发生变化

				System.out.println(downloadInfo.downloadName + "下载成功");
				Toast.makeText(UIUtils.getContext(), "下载成功", Toast.LENGTH_SHORT).show();
			}

			// 下载成功，移除任务
			downloadTaskMap.remove(downloadInfo.song_id);
		}

		@Override
		public void onLoading(long total, long current, boolean isDownloading) {
			// 下载中
			downloadInfo.currentPos = current;// 更新下载进度
			SPUtils.saveDownloadInfo(downloadInfo.song_id, downloadInfo);// 本地存储downloadInfo的信息

			notifyDownloadProgressChanged(downloadInfo);// 通知观察者，下载进度发生变化

			System.out.println(downloadInfo.downloadName + "currentPos" + downloadInfo.currentPos);

		}

		@Override
		public void onError(Throwable arg0, boolean arg1) {
			// 下载失败
			mFile.delete();// 删除无效文件
			downloadInfo.currentPos = 0;
			downloadInfo.currentState = STATE_FAIL;
			SPUtils.saveDownloadInfo(downloadInfo.song_id, downloadInfo);// 本地存储downloadInfo的信息

			notifyDownloadStateChanged(downloadInfo);// 通知所有观察者，下载状态改变

			// 下载失败，移除任务
			downloadTaskMap.remove(downloadInfo.song_id);

			System.out.println(downloadInfo.downloadName + "下载失败");
			Toast.makeText(UIUtils.getContext(), "下载失败", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancelled(CancelledException arg0) {}

		@Override
		public void onFinished() {
		}

		@Override
		public void onStarted() {
			Toast.makeText(UIUtils.getContext(), "下载开始", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onWaiting() {
		}
	}

	/**
	 * 删除下载文件
	 */
	public synchronized void delete(DownloadInfo downloadInfo) {
		File file = new File(downloadInfo.downloadPath);// 获取文件路径
		file.delete();

		downloadInfo.currentPos = 0;
		downloadInfo.currentState = STATE_UNDO;

		SPUtils.saveDownloadInfo(downloadInfo.song_id, downloadInfo);// 本地存储downloadInfo的信息

		Toast.makeText(UIUtils.getContext(), "歌曲已删除", Toast.LENGTH_SHORT).show();
	}

	/**
	 * 通知所有观察者下载状态改变
	 */
	public void notifyDownloadStateChanged(DownloadInfo info) {
		for (DownloadObserver observer : observerList) {
			observer.onDownloadStateChanged(info);
		}
	}

	/**
	 * 通知所有观察者下载进程改变
	 */
	public void notifyDownloadProgressChanged(DownloadInfo info) {
		for (DownloadObserver observer : observerList) {
			observer.onDownloadProgressChanged(info);
		}
	}

	/**
	 * 注册观察者，添加观察者
	 */
	public void registObserver(DownloadObserver observer) {
		if (observer != null && !observerList.contains(observer)) {
			observerList.add(observer);
		}
	}

	/**
	 * 取消观察者，移除
	 */
	public void unregistObserver(DownloadObserver observer) {
		if (observer != null && observerList.contains(observer)) {
			observerList.remove(observer);
		}
	}

	/**
	 * 观察者接口
	 */
	public interface DownloadObserver {
		public abstract void onDownloadStateChanged(DownloadInfo downloadInfo);

		public abstract void onDownloadProgressChanged(DownloadInfo downloadInfo);
	}

	/**
	 * 获取下载对象
	 */
	public DownloadInfo getDownloadInfo(String song_id) {
		return downloadInfoMap.get(song_id);
	}

	/**
	 * 判断当前DownloadInfo是否有下载线程
	 */
	public boolean isHasDownloadTask(String song_id) {
		if (downloadTaskMap.get(song_id) != null)
			return true;
		else
			return false;
	}
}
