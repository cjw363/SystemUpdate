package tecsun.cjw.systemupdate.been;

import java.io.File;
import java.io.Serializable;

public class DownloadInfo implements Serializable {
	public Long contentLength;// 文件大小
	public String url;// 下载链接
	public String filePath;// 文件保存路径
	public String name;//文件名
	public long currentPos;// 当前下载位置progress
	public int currentState;// 当前下载状态

	public float getProgress() {//获取下载进度0-1
		if (contentLength == 0) {
			return 0;
		}
		return currentPos / (float) contentLength;
	}

	/**
	 * 创建下载的文件夹，并判断是否创建成功，没有会创建
	 */
	private boolean createDir(String dir) {
		File dirFile = new File(dir);//文件夹
		if (!dirFile.exists() || !dirFile.isDirectory()) return dirFile.mkdirs();

		return true;
	}
}
