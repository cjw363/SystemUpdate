package tecsun.cjw.systemupdate.been;

public class DownloadEvent {
	public final static int EVENT_DOWNLOAD_1 = 1;
	public final static int EVENT_PAUSE_2 = 2;
	public final static int EVENT_CANCEL_3 = 3;
	public final static int EVENT_SUCCESS_4 = 4;
	public final static int EVENT_SUCCESS_TO_UPDATE_5 = 5;
	public final static int EVENT_DOWNLOADING_PROGRESS_6 = 6;
	public int what;//1.下载，2.暂停，3取消
	public int value;

	public DownloadEvent(int what) {
		this.what = what;
	}
}
