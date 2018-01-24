package tecsun.cjw.systemupdate;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tecsun.cjw.systemupdate.base.DownloadEvent;
import tecsun.cjw.systemupdate.been.DownloadInfo;
import tecsun.cjw.systemupdate.http.download.DownloadManager;

import static tecsun.cjw.systemupdate.base.DownloadEvent.EVENT_CANCEL_3;
import static tecsun.cjw.systemupdate.base.DownloadEvent.EVENT_DOWNLOAD_1;
import static tecsun.cjw.systemupdate.base.DownloadEvent.EVENT_PAUSE_2;

public class SystemUpdateService extends Service implements DownloadManager.DownloadObserver {

	private DownloadInfo mDownloadInfo = null;

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
			mDownloadInfo = (DownloadInfo) intent.getSerializableExtra("download_info");
			if (mDownloadInfo != null)
				if (!DownloadManager.getInstance().hasDownloadTask(mDownloadInfo.url))//当前没有此下载任务
					DownloadManager.getInstance().download(mDownloadInfo);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEvent(DownloadEvent event) {
		switch (event.what) {
			case EVENT_DOWNLOAD_1:
				if (mDownloadInfo != null) DownloadManager.getInstance().download(mDownloadInfo);
				break;
			case EVENT_PAUSE_2:
				if (mDownloadInfo != null) DownloadManager.getInstance().pause(mDownloadInfo);
				break;
			case EVENT_CANCEL_3:
				if (mDownloadInfo != null) DownloadManager.getInstance().cancel(mDownloadInfo);
				break;
		}
	}

	@Override
	public void onDownloadStateChanged(DownloadInfo downloadInfo) {
		switch (downloadInfo.currentState) {
			case DownloadManager.STATE_UNDO:
				break;
			case DownloadManager.STATE_WAITING:
//				startForeground(1, getNotification("Downloading...", 0));
				break;
			case DownloadManager.STATE_DOWNLOADING:
				float progress = (downloadInfo.currentPos / (float) DownloadManager.getInstance().getTotalContentLength());
				getNotificationManager().notify(1, getNotification("Downloading...", (int) (progress * 100)));
				break;
			case DownloadManager.STATE_PAUSE:
				break;
			case DownloadManager.STATE_FAIL:
				break;
			case DownloadManager.STATE_SUCCESS:
				break;
			case DownloadManager.STATE_CANCEL:
				break;
		}
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
	private Notification getNotification(String title, int progress) {
		Intent intent = new Intent(this, MainActivity.class);
		//PendingIntent是等待的Intent,这是跳转到一个Activity组件。当用户点击通知时，会跳转到MainActivity
		PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
		/**
		 * 几乎Android系统的每一个版本都会对通知这部分功能进行获多或少的修改，API不稳定行问题在通知上面凸显的尤其严重。
		 * 解决方案是：用support库中提供的兼容API。support-v4库中提供了一个NotificationCompat类，使用它可以保证我们的
		 * 程序在所有的Android系统版本中都能正常工作。
		 */
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		//设置通知的小图标
		builder.setSmallIcon(R.mipmap.ic_launcher);
		//设置通知的大图标，当下拉系统状态栏时，就可以看到设置的大图标
		builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
		//当通知被点击的时候，跳转到MainActivity中
		builder.setContentIntent(pi);
		//设置通知的标题
		builder.setContentTitle(title);
		if (progress > 0) {
			//当progress大于或等于0时，才需要显示下载进度
			builder.setContentText(progress + "%");
			builder.setProgress(100, progress, false);
		}
		return builder.build();
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
