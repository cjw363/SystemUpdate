package tecsun.cjw.systemupdate;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tecsun.cjw.systemupdate.base.DownloadEvent;
import tecsun.cjw.systemupdate.been.DownloadInfo;
import tecsun.cjw.systemupdate.http.download.DownloadManager;

import static tecsun.cjw.systemupdate.base.DownloadEvent.EVENT_CANCEL_3;
import static tecsun.cjw.systemupdate.base.DownloadEvent.EVENT_DOWNLOAD_1;
import static tecsun.cjw.systemupdate.base.DownloadEvent.EVENT_PAUSE_2;

public class SystemUpdateService extends Service {

	private DownloadInfo mDownloadInfo = null;

	@Override
	public void onCreate() {
		super.onCreate();
		//注册事件
		EventBus.getDefault().register(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			mDownloadInfo = (DownloadInfo) intent.getSerializableExtra("download_info");
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
	public void onDestroy() {
		super.onDestroy();
		//取消注册事件
		EventBus.getDefault().unregister(this);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
