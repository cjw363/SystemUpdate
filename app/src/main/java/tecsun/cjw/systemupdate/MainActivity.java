package tecsun.cjw.systemupdate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import tecsun.cjw.systemupdate.base.DownloadEvent;
import tecsun.cjw.systemupdate.been.DownloadInfo;
import tecsun.cjw.systemupdate.http.OkHttpUtil;
import tecsun.cjw.systemupdate.http.download.DownloadManager;
import tecsun.cjw.systemupdate.utils.xml.SaxUpdateXmlParser;
import tecsun.cjw.systemupdate.utils.xml.SystemModel;

import static tecsun.cjw.systemupdate.base.DownloadEvent.EVENT_CANCEL_3;
import static tecsun.cjw.systemupdate.base.DownloadEvent.EVENT_PAUSE_2;

public class MainActivity extends AppCompatActivity implements DownloadManager.DownloadObserver {

	private static String systemUpdateUrl = "http://cpzx.e-tecsun.com:8037/update/TA/update_cjw.xml";
	private static String command = "command";
	private static String updateZip = "update.zip";

	@BindView(R.id.pg_download)
	ProgressBar mPgDownload;
	private DownloadInfo mDownloadInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		checkSystemUpdate();//检查更新系统版本
		DownloadManager.getInstance().registerObserver(this);//注册观察者
	}

	private void checkSystemUpdate() {
		OkHttpUtil.getInstance().doHttp(systemUpdateUrl, new Callback() {
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				try {
					List<SystemModel> systemModels = new SaxUpdateXmlParser().parse(MainActivity.this, response.body().byteStream());
					String currVersion = android.os.Build.DISPLAY;
					for (SystemModel system : systemModels) {
						if (currVersion.equals(system.getName())) {
							List<SystemModel.Target> targetList = system.getTagetList();
							if (targetList != null && targetList.size() > 0) {
								SystemModel.Target target = targetList.get(0);//取第一个
								showNewSystemUpdate(target);
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
				System.out.println("检查更新失败");
			}
		});
	}

	private void showNewSystemUpdate(SystemModel.Target target) {
		System.out.println("发现新版本" + target.getName());
		mDownloadInfo = new DownloadInfo();
		mDownloadInfo.name = updateZip;
		mDownloadInfo.url = target.getAddr() + mDownloadInfo.name;
		mDownloadInfo.filePath = Environment.getDownloadCacheDirectory().toString() + "/" + mDownloadInfo.name;
	}

	public void onDownload(View v) {
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

	@Override
	public void onDownloadStateChanged(DownloadInfo downloadInfo) {
		switch (downloadInfo.currentState) {
			case DownloadManager.STATE_UNDO:
				break;
			case DownloadManager.STATE_WAITING:
				break;
			case DownloadManager.STATE_DOWNLOADING:
				float progress = (downloadInfo.currentPos / (float) DownloadManager.getInstance().getTotalContentLength());
				mPgDownload.setProgress((int) (progress * 100));
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DownloadManager.getInstance().unRegisterObserver(this);
	}
}
