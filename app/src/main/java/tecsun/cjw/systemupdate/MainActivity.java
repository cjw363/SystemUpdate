package tecsun.cjw.systemupdate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import tecsun.cjw.systemupdate.base.DownloadEvent;
import tecsun.cjw.systemupdate.been.DownloadInfo;
import tecsun.cjw.systemupdate.http.download.DownloadManager;

import static tecsun.cjw.systemupdate.base.DownloadEvent.EVENT_CANCEL_3;
import static tecsun.cjw.systemupdate.base.DownloadEvent.EVENT_PAUSE_2;

public class MainActivity extends AppCompatActivity {

	@BindView(R.id.pg_download)
	ProgressBar mPgDownload;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		DownloadManager.getInstance().registerObserver(new DownloadManager.DownloadObserver() {
			@Override
			public void onDownloadStateChanged(DownloadInfo downloadInfo) {
				switch (downloadInfo.currentState) {
					case DownloadManager.STATE_UNDO:
						break;
					case DownloadManager.STATE_WAITING:
						break;
					case DownloadManager.STATE_DOWNLOADING:
						mPgDownload.setProgress((int) (downloadInfo.getProgress() * 100));
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
		});
	}

	public void onDownload(View v) {
		DownloadInfo mDownloadInfo = new DownloadInfo();
		mDownloadInfo.name = "update.zip";
		mDownloadInfo.url = "http://cpzx.e-tecsun.com:8037/update/TA/TecSun TA V1.2.8 Build20170227/update.zip";
		//"http://cpzx.e-tecsun.com:8037/update/TA/update_cjw.xml"
		//		String currVersion = android.os.Build.DISPLAY;
		mDownloadInfo.filePath = Environment.getDownloadCacheDirectory().toString() + "/" + mDownloadInfo.name;
		System.out.println("filePath--" + mDownloadInfo.filePath);

		Intent intent = new Intent(MainActivity.this, SystemUpdateService.class);
		intent.putExtra("download_info", mDownloadInfo);
		startService(intent);
	}

	public void onPause(View v) {
		EventBus.getDefault().post(new DownloadEvent(EVENT_PAUSE_2));
	}

	public void onCancel(View v) {
		EventBus.getDefault().post(new DownloadEvent(EVENT_CANCEL_3));
	}
}
