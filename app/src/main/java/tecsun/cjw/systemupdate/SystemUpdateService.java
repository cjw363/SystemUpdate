package tecsun.cjw.systemupdate;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import tecsun.cjw.systemupdate.base.BaseApplication;
import tecsun.cjw.systemupdate.been.DownloadEvent;
import tecsun.cjw.systemupdate.been.DownloadInfo;
import tecsun.cjw.systemupdate.http.NetManager;
import tecsun.cjw.systemupdate.http.OkHttpUtil;
import tecsun.cjw.systemupdate.http.download.DownloadManager;
import tecsun.cjw.systemupdate.utils.FileUtil;
import tecsun.cjw.systemupdate.utils.SPUtils;
import tecsun.cjw.systemupdate.utils.SerializeUtils;
import tecsun.cjw.systemupdate.utils.UI;
import tecsun.cjw.systemupdate.utils.xml.SaxUpdateXmlParser;
import tecsun.cjw.systemupdate.utils.xml.SystemModel;

import static tecsun.cjw.systemupdate.been.DownloadEvent.EVENT_CANCEL_3;
import static tecsun.cjw.systemupdate.been.DownloadEvent.EVENT_DOWNLOAD_1;
import static tecsun.cjw.systemupdate.been.DownloadEvent.EVENT_PAUSE_2;
import static tecsun.cjw.systemupdate.been.DownloadEvent.EVENT_SUCCESS_4;
import static tecsun.cjw.systemupdate.been.DownloadEvent.EVENT_SUCCESS_TO_UPDATE_5;

public class SystemUpdateService extends Service implements DownloadManager.DownloadObserver {

	public static final String INTENT_ACTION_CHECK_SYSTEM_UPDATE = "intent.action.CHECK_SYSTEM_UPDATE";
	public static final String INTENT_ACTION_SYSTEM_DOWNLOAD = "intent.action.SYSTEM_DOWNLOAD";

	private NotificationCompat.Builder mNotifBuilder;
	private int preProgress = 0;//用来记录之前的下载进度，总进度为100，如果相同则不需要频繁更新，避免卡顿
	private List<DownloadInfo> mDownloads;

	@Override
	public void onCreate() {
		super.onCreate();
		//注册事件
		EventBus.getDefault().register(this);
		DownloadManager.getInstance().registerObserver(this);//注册观察者
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String action = intent.getAction();
			if (INTENT_ACTION_CHECK_SYSTEM_UPDATE.equals(action)) {//接受的是检查更新命令
				if (NetManager.isConnected(this)) {
					checkSystemUpdate();//检查更新系统版本
				}
			} else if (INTENT_ACTION_SYSTEM_DOWNLOAD.equals(action)) {//接受的是下载命令
				SystemModel.Target target = (SystemModel.Target) intent.getSerializableExtra("target");
				SPUtils.putString("target", SerializeUtils.serialize(target));
				if (target != null) {
					//下载任务列表
					mDownloads = new ArrayList<>();
					/**
					 * 保存的文件名是版本号-如update.zip:TecSun TA V1.2.12 Build20171130-update.zip
					 */
					mDownloads.add(createDownloadInfo(BaseApplication.command, target.getAddr(), "/recovery/" + target
					  .getName() + "-"));///recovery/
					mDownloads.add(createDownloadInfo(BaseApplication.updateZip, target.getAddr(), "/" + target
					  .getName() + "-"));
					DownloadManager.getInstance().download(mDownloads);
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	public DownloadInfo createDownloadInfo(String name, String url, String path) {
		DownloadInfo downloadInfo = new DownloadInfo();
		downloadInfo.name = name;
		downloadInfo.url = url + name;
		downloadInfo.filePath = Environment.getDownloadCacheDirectory()
		  .toString() + path + downloadInfo.name;
		return downloadInfo;
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEvent(DownloadEvent event) {
		switch (event.what) {
			case EVENT_DOWNLOAD_1:
				if (mDownloads != null) DownloadManager.getInstance().download(mDownloads);
				break;
			case EVENT_PAUSE_2:
				if (mDownloads != null) DownloadManager.getInstance().pause(mDownloads);
				break;
			case EVENT_CANCEL_3:
				if (mDownloads != null) DownloadManager.getInstance().cancel(mDownloads);
				break;
			case EVENT_SUCCESS_TO_UPDATE_5:
				rebootToUpdate(mDownloads);
				break;
		}
	}

	@Override
	public void onDownloadStateChanged(DownloadInfo downloadInfo) {
		switch (downloadInfo.currentState) {
			case DownloadManager.STATE_UNDO:
				break;
			case DownloadManager.STATE_WAITING:
				getNotificationManager().notify(1, getNotification("等待下载...", 0).build());
				break;
			case DownloadManager.STATE_DOWNLOADING:
				float progress = (downloadInfo.currentPos / (float) DownloadManager.getInstance()
				  .getTotalContentLength());
				int currProgress = (int) (progress * 100);
				if (preProgress < currProgress) {
					System.out.println(currProgress);
					SPUtils.putInt("progress",currProgress);
					SPUtils.putInt("state",DownloadManager.STATE_DOWNLOADING);
					getNotificationManager().notify(1, getNotification("下载中...", currProgress).build());
				}
				preProgress = currProgress;
				break;
			case DownloadManager.STATE_PAUSE:
				SPUtils.putInt("state",DownloadManager.STATE_PAUSE);
				getNotificationManager().notify(1, getNotification("下载暂停...", 0).build());
				break;
			case DownloadManager.STATE_FAIL:
				SPUtils.putInt("state",DownloadManager.STATE_PAUSE);
				getNotificationManager().notify(1, getNotification("下载失败...", 0).build());
				break;
			case DownloadManager.STATE_SUCCESS:
				if (mDownloads != null) {
					if (checkIsAllDownloaded(mDownloads)) {//所有下载任务已完成
						SPUtils.putInt("progress",100);
						SPUtils.putInt("state",DownloadManager.STATE_SUCCESS);
						getNotificationManager().notify(1, getNotification("下载成功...", 100).build());
						UI.showToast("下载成功");
						EventBus.getDefault().post(new DownloadEvent(EVENT_SUCCESS_4));
					}
				}
				break;
			case DownloadManager.STATE_CANCEL:
				getNotificationManager().cancel(1);
				break;
		}
	}

	private void rebootToUpdate(List<DownloadInfo> downloads) {
		if (downloads != null) {
			if (reNameSystemUpdateFile(downloads)) {//重命名合法文件成功
				System.out.println("重命名合法文件成功");
				//					PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE); //重启到fastboot模式
				//					pManager.reboot("recovery");
			}
		}
	}

	private boolean checkIsAllDownloaded(List<DownloadInfo> downloads) {
		if (downloads == null || downloads.isEmpty()) return false;
		for (DownloadInfo downloadInfo : downloads) {
			//			if (downloadInfo.currentState != DownloadManager.BT_STATE_SUCCESS) return false;
			if (downloadInfo.contentLength == null) return false;
			if (downloadInfo.contentLength != new File(downloadInfo.filePath).length())
				return false;//大小不一致
		}
		return true;
	}

	private boolean reNameSystemUpdateFile(List<DownloadInfo> downloads) {
		if (downloads == null || downloads.isEmpty()) return false;
		for (DownloadInfo downloadInfo : downloads) {
			if (!FileUtil.reNameSystemUpdateFile(downloadInfo.filePath)) return false;//有一重名失败就false
		}
		return true;
	}


	private void checkSystemUpdate() {
		OkHttpUtil.getInstance().doHttp(BaseApplication.systemUpdateUrl, new Callback() {
			@Override
			public void onResponse(Call call, Response response) {
				try {
					List<SystemModel> systemModels = new SaxUpdateXmlParser().parse(SystemUpdateService.this, response
					  .body()
					  .byteStream());
					String currVersion = android.os.Build.DISPLAY;
					for (SystemModel system : systemModels) {
						if (currVersion.equals(system.getName())) {
							List<SystemModel.Target> targetList = system.getTagetList();
							if (targetList != null && targetList.size() > 0) {
								SystemModel.Target target = targetList.get(0);//取第一个
								System.out.println(target.getName());
								getNotificationManager().notify(1, getNotification("发现新版本", 0).build());
								return;
							} else {
								//暂无更新版本
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("检查更新失败");
				}
			}

			@Override
			public void onFailure(Call call, IOException e) {
				e.printStackTrace();
				System.out.println("检查更新失败");
			}
		});
	}

	/**
	 * 获取NotificationManager的实例，对通知进行管理
	 *
	 * @return
	 */
	private NotificationManager getNotificationManager() {
		return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	/**
	 * @param title
	 * @param progress
	 * @return
	 */
	private NotificationCompat.Builder getNotification(String title, int progress) {
		if (mNotifBuilder == null) {
			Intent intent = new Intent(this, MainActivity.class);
			//PendingIntent是等待的Intent,这是跳转到一个Activity组件。当用户点击通知时，会跳转到MainActivity
			PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
			/**
			 * 几乎Android系统的每一个版本都会对通知这部分功能进行获多或少的修改，API不稳定行问题在通知上面凸显的尤其严重。
			 * 解决方案是：用support库中提供的兼容API。support-v4库中提供了一个NotificationCompat类，使用它可以保证我们的
			 * 程序在所有的Android系统版本中都能正常工作。
			 */
			mNotifBuilder = new NotificationCompat.Builder(this);
			//设置通知的小图标
			mNotifBuilder.setSmallIcon(R.mipmap.ic_launcher);
			//设置通知的大图标，当下拉系统状态栏时，就可以看到设置的大图标
			mNotifBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
			//当通知被点击的时候，跳转到MainActivity中
			mNotifBuilder.setContentIntent(pi);
		}
		//设置通知的标题
		mNotifBuilder.setContentTitle(title);
		if (progress > 0) {
			//当progress大于或等于0时，才需要显示下载进度
			mNotifBuilder.setContentText(progress + "%");
			mNotifBuilder.setProgress(100, progress, false);
		}
		return mNotifBuilder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//取消注册事件
		EventBus.getDefault().unregister(this);
		DownloadManager.getInstance().unRegisterObserver(this);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
