package tecsun.cjw.systemupdate;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Response;
import tecsun.cjw.systemupdate.base.BaseApplication;
import tecsun.cjw.systemupdate.been.DownloadEvent;
import tecsun.cjw.systemupdate.been.DownloadInfo;
import tecsun.cjw.systemupdate.http.CommonCallback;
import tecsun.cjw.systemupdate.http.NetManager;
import tecsun.cjw.systemupdate.http.OkHttpUtil;
import tecsun.cjw.systemupdate.http.download.DownloadManager;
import tecsun.cjw.systemupdate.utils.SPUtils;
import tecsun.cjw.systemupdate.utils.SerializeUtils;
import tecsun.cjw.systemupdate.utils.UI;
import tecsun.cjw.systemupdate.utils.xml.SaxUpdateXmlParser;
import tecsun.cjw.systemupdate.utils.xml.SystemModel;
import tecsun.cjw.systemupdate.view.BaseCustomDialog;
import tecsun.cjw.systemupdate.view.ContentDialog;
import tecsun.cjw.systemupdate.view.RoundProgress;
import tecsun.cjw.systemupdate.view.catloading.CatLoadingView;

import static tecsun.cjw.systemupdate.been.DownloadEvent.EVENT_PAUSE_2;
import static tecsun.cjw.systemupdate.been.DownloadEvent.EVENT_SUCCESS_4;
import static tecsun.cjw.systemupdate.been.DownloadEvent.EVENT_SUCCESS_TO_UPDATE_5;

public class MainActivity extends AppCompatActivity implements DownloadManager.DownloadObserver {

	public static final int BT_STATE_CHECK_UPDATE = 1; // 检查更新
	public static final int BT_STATE_START_DOWNLOAD = 2; // 开始下载
	public static final int BT_STATE_PAUSE = 3; // 暂停下载
	public static final int BT_STATE_SUCCESS = 4; // 下载成功

	@BindView(R.id.tv_version_name)
	TextView mTvVersionName;
	@BindView(R.id.tv_tip)
	TextView mTvTip;
	@BindView(R.id.tv_version_description)
	TextView mTvVersionDescription;
	@BindView(R.id.rp_download)
	RoundProgress mRpDownload;
	@BindView(R.id.bt_download)
	Button mBtDownload;

	private int preProgress = 0;//用来记录之前的下载进度，总进度为100，如果相同则不需要频繁更新，避免卡顿
	private SystemModel.Target mTarget;
	private BaseCustomDialog mDialog;
	private CatLoadingView mLoadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		initView();
		DownloadManager.getInstance().registerObserver(this);//注册观察者
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		startService(new Intent(MainActivity.this, SystemUpdateService.class));
	}

	private void initView() {
		mTarget = (SystemModel.Target) SerializeUtils.deSerialize(SPUtils.getString("target"));
		if (mTarget != null) {
			mTvVersionName.setText(mTarget.getName());
			mTvTip.setText("(新版本)");
			mTvVersionDescription.setText(mTarget.getDescription());

			int progress = SPUtils.getInt("progress");
			int state = SPUtils.getInt("state");
			mRpDownload.setProgress(progress);
			switch (state) {
				case DownloadManager.STATE_DOWNLOADING:
					mBtDownload.setTag(BT_STATE_PAUSE);
					mBtDownload.setText("暂停下载");
					break;
				case DownloadManager.STATE_PAUSE:
					mBtDownload.setTag(BT_STATE_START_DOWNLOAD);
					mBtDownload.setText("继续下载");
					break;
				case DownloadManager.STATE_SUCCESS:
					mBtDownload.setTag(BT_STATE_SUCCESS);
					mBtDownload.setText("重启升级");
					break;
			}
		} else {
			mTvVersionName.setText(Build.DISPLAY);
			mTvTip.setText("(当前版本)");
			mBtDownload.setTag(BT_STATE_CHECK_UPDATE);
			mBtDownload.setText("检查更新");
		}
	}

	public void onDownload(View v) {
		switch ((Integer) v.getTag()) {
			case BT_STATE_CHECK_UPDATE:
				if (NetManager.isConnected(this)) {
					checkSystemUpdate();//检查更新系统版本
				} else {
					mDialog = new ContentDialog.Builder(this).setContent("网络异常！")
					  .setSingleButton()
					  .build();
					mDialog.showDialog();
				}
				break;
			case BT_STATE_START_DOWNLOAD:
				if (mTarget != null) {
					Intent intent = new Intent(MainActivity.this, SystemUpdateService.class);
					intent.setAction(SystemUpdateService.INTENT_ACTION_SYSTEM_DOWNLOAD);
					intent.putExtra("target", mTarget);
					startService(intent);
				}
				break;
			case BT_STATE_PAUSE:
				EventBus.getDefault().post(new DownloadEvent(EVENT_PAUSE_2));
				mBtDownload.setTag(BT_STATE_START_DOWNLOAD);
				mBtDownload.setText("继续下载");
				break;
			case BT_STATE_SUCCESS:
				if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
				mDialog = new ContentDialog.Builder(this).setContent("是否立即重启升级系统")
				  .setOkListener(new View.OnClickListener() {
					  @Override
					  public void onClick(View view) {
						  EventBus.getDefault().post(new DownloadEvent(EVENT_SUCCESS_TO_UPDATE_5));
						  mDialog.dismiss();
					  }
				  })
				  .build();
				mDialog.showDialog();
				break;
		}
	}

	private void checkSystemUpdate() {
		mLoadingDialog = new CatLoadingView();
		mLoadingDialog.show(getSupportFragmentManager(), "CatLoadingView");
		OkHttpUtil.getInstance().doHttp(BaseApplication.systemUpdateUrl, new CommonCallback() {
			@Override
			public void _onResponse(Call call, Response response) {
				try {
					List<SystemModel> systemModels = new SaxUpdateXmlParser().parse(MainActivity.this, response
					  .body()
					  .byteStream());
					String currVersion = Build.DISPLAY;
					mLoadingDialog.dismiss();
					for (SystemModel system : systemModels) {
						if (currVersion.equals(system.getName())) {
							List<SystemModel.Target> targetList = system.getTagetList();
							if (targetList != null && targetList.size() > 0) {
								SystemModel.Target target = targetList.get(0);//取第一个
								showNewSystemUpdate(target);
								return;
							} else {
								//暂无更新版本
								showNonSystemUpdate();
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("检查更新失败");
					mLoadingDialog.dismiss();
				}
			}

			@Override
			public void _onFailure(Call call, IOException e) {
				e.printStackTrace();
				mLoadingDialog.dismiss();
				mDialog = new ContentDialog.Builder(MainActivity.this).setContent("检查更新失败")
				  .setSingleButton()
				  .build();
				mDialog.showDialog();
			}
		});
	}

	private void showNonSystemUpdate() {
		UI.runOnUIThread(new Runnable() {
			@Override
			public void run() {
				mTvVersionName.setText(Build.DISPLAY);
				mDialog = new ContentDialog.Builder(MainActivity.this).setContent("暂无新版本")
				  .setSingleButton()
				  .build();
				mDialog.showDialog();
				mBtDownload.setEnabled(false);
				mBtDownload.setClickable(false);
			}
		});
	}

	private void showNewSystemUpdate(final SystemModel.Target target) {
		UI.runOnUIThread(new Runnable() {
			@Override
			public void run() {
				mTvVersionName.setText(target.getName());
				mTvTip.setText("(发现新版本)");
				mTvVersionDescription.setText(target.getDescription());
				mBtDownload.setTag(BT_STATE_START_DOWNLOAD);
				mBtDownload.setText("开始下载");
			}
		});
		mTarget = target;
	}

	@Override
	public void onDownloadStateChanged(DownloadInfo downloadInfo) {
		switch (downloadInfo.currentState) {
			case DownloadManager.STATE_UNDO:
				break;
			case DownloadManager.STATE_WAITING:
				mBtDownload.setTag(BT_STATE_PAUSE);
				mBtDownload.setText("暂停下载");
				break;
			case DownloadManager.STATE_DOWNLOADING:
				float progress = (downloadInfo.currentPos / (float) DownloadManager.getInstance()
				  .getTotalContentLength());
				int currProgress = (int) (progress * 100);
				if (preProgress < currProgress) {
					mRpDownload.setProgress((int) (progress * 100));
				}
				preProgress = currProgress;
				break;
			case DownloadManager.STATE_PAUSE:
				break;
			case DownloadManager.STATE_FAIL:
				mBtDownload.setTag(BT_STATE_START_DOWNLOAD);
				mBtDownload.setText("继续下载");
				if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
				mDialog = new ContentDialog.Builder(this).setContent(downloadInfo.message)
				  .setSingleButton()
				  .build();
				mDialog.showDialog();
				break;
			case DownloadManager.STATE_SUCCESS:
				break;
			case DownloadManager.STATE_CANCEL:
				break;
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEvent(DownloadEvent event) {
		switch (event.what) {
			case EVENT_SUCCESS_4:
				mRpDownload.setProgress(100);
				mBtDownload.setTag(BT_STATE_SUCCESS);
				mBtDownload.setText("重启升级");

				if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
				mDialog = new ContentDialog.Builder(this).setContent("下载成功是否立即重启升级系统")
				  .setOkListener(new View.OnClickListener() {
					  @Override
					  public void onClick(View view) {
						  EventBus.getDefault().post(new DownloadEvent(EVENT_SUCCESS_TO_UPDATE_5));
						  mDialog.dismiss();
					  }
				  })
				  .build();
				mDialog.showDialog();
				break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DownloadManager.getInstance().unRegisterObserver(this);
		EventBus.getDefault().unregister(this);
	}
}
