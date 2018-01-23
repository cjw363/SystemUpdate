package tecsun.cjw.systemupdate;

import android.os.Environment;

import java.io.File;
import java.io.Serializable;

public class DownloadInfo implements Serializable {

	private static final String ZHIYUE = "com.cjw.zhiyue";
	private static final String DOWNLOAD = "Download";


	public Long fileLength;// 文件大小
	public String downloadUrl;// 下载链接
	public String downloadPath;// 文件下载路径
	public String downloadName;//文件名

	public long currentPos;// 当前下载位置progress
	public int currentState;// 当前下载状态


	public float getProgress() {//获取下载进度0-1
		if (fileLength == 0) {
			return 0;
		}

		return currentPos / (float) fileLength;
	}

	/**
	 * 获取文件下载路径
	 */
	public String getFilePath() {
		StringBuffer sb = new StringBuffer();
		String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		sb.append(path);
		sb.append(File.separator);
		sb.append(ZHIYUE);
		sb.append(File.separator);
		sb.append(DOWNLOAD);

		if (createDir(sb.toString())) {
			return sb.toString() + File.separator + downloadName;
		}
		return null;
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
