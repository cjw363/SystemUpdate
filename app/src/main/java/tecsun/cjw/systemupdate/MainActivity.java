package tecsun.cjw.systemupdate;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import tecsun.cjw.systemupdate.base.BaseApplication;
import tecsun.cjw.systemupdate.base.DownloadEvent;
import tecsun.cjw.systemupdate.been.DownloadInfo;
import tecsun.cjw.systemupdate.http.OkHttpUtil;
import tecsun.cjw.systemupdate.http.download.DownloadManager;
import tecsun.cjw.systemupdate.utils.UI;
import tecsun.cjw.systemupdate.utils.xml.SaxUpdateXmlParser;
import tecsun.cjw.systemupdate.utils.xml.SystemModel;

import static tecsun.cjw.systemupdate.base.DownloadEvent.EVENT_CANCEL_3;
import static tecsun.cjw.systemupdate.base.DownloadEvent.EVENT_PAUSE_2;

public class MainActivity extends AppCompatActivity implements DownloadManager.DownloadObserver {

	@BindView(R.id.pg_download)
	ProgressBar mPgDownload;
	@BindView(R.id.tv_version_name)
	TextView mTvVersionName;
	@BindView(R.id.tv_tip)
	TextView mTvTip;
	@BindView(R.id.tv_version_description)
	TextView mTvVersionDescription;

	private int preProgress = 0;//用来记录之前的下载进度，总进度为100，如果相同则不需要频繁更新，避免卡顿
	private SystemModel.Target mTarget;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		checkSystemUpdate();//检查更新系统版本
		DownloadManager.getInstance().registerObserver(this);//注册观察者
	}

	private void checkSystemUpdate() {
		OkHttpUtil.getInstance().doHttp(BaseApplication.systemUpdateUrl, new Callback() {
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				try {
					List<SystemModel> systemModels = new SaxUpdateXmlParser().parse(MainActivity.this, response.body().byteStream());
					String currVersion = Build.DISPLAY;
					for (SystemModel system : systemModels) {
						if (currVersion.equals(system.getName())) {
							List<SystemModel.Target> targetList = system.getTagetList();
							if (targetList != null && targetList.size() > 0) {
								final SystemModel.Target target = targetList.get(0);//取第一个
								UI.runOnUIThread(new Runnable() {
									@Override
									public void run() {
										showNewSystemUpdate(target);
									}
								});
								return;
							} else {
								//暂无更新版本
								mTvVersionName.setText(currVersion);
								mTvTip.setText("(暂无新版本)");
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
		mTvVersionName.setText(target.getName());
		mTvTip.setText("(发现新版本)");
		mTvVersionDescription.setText(target.getDescription());

		mTarget=target;
	}

	public void onDownload(View v) {
		if(mTarget!=null){
			Intent intent = new Intent(MainActivity.this, SystemUpdateService.class);
			intent.setAction(SystemUpdateService.INTENT_ACTION_SYSTEM_DOWNLOAD);
			intent.putExtra("target", mTarget);
			startService(intent);
		}
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
				int currProgress = (int) (progress * 100);
				if (preProgress < currProgress) {
					mPgDownload.setProgress((int) (progress * 100));
				}
				preProgress = currProgress;
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
