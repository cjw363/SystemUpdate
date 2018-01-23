package tecsun.cjw.systemupdate;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import tecsun.cjw.systemupdate.been.DownloadInfo;
import tecsun.cjw.systemupdate.http.download.DownloadManager;

public class MainActivity extends AppCompatActivity {

  @BindView(R.id.pg_download)
  ProgressBar mPgDownload;
  private DownloadInfo mDownloadInfo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    // 获取当前系统版本号 TecSun TA V1.2.10 Build20170728
    String currVersion = android.os.Build.DISPLAY;

    System.out.println(currVersion);
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
    mDownloadInfo = new DownloadInfo();
    mDownloadInfo.name = "update.zip";
    mDownloadInfo.url = "http://cpzx.e-tecsun.com:8037/update/TA/TecSun TA V1.2.8 Build20170227/update.zip";
    mDownloadInfo.filePath = Environment.getDownloadCacheDirectory().toString() + "/" + mDownloadInfo.name;
    System.out.println("filePath--" + mDownloadInfo.filePath);

    DownloadManager.getInstance().download(mDownloadInfo);
  }

  public void onPause(View v) {
    if(mDownloadInfo!=null)
    DownloadManager.getInstance().pause(mDownloadInfo);
  }

  public void onCancel(View v) {
    if(mDownloadInfo!=null)
      DownloadManager.getInstance().cancel(mDownloadInfo);
  }
}
