package tecsun.cjw.systemupdate.base;

public class DownloadEvent {
	public final static int EVENT_DOWNLOAD_1 = 1;
	public final static int EVENT_PAUSE_2 = 2;
	public final static int EVENT_CANCEL_3 = 3;
	public int what;//1.下载，2.暂停，3取消

	public DownloadEvent(int what) {
		this.what = what;
	}
}
