package tecsun.cjw.systemupdate.utils;

import java.io.File;

public class FileUtil {
	/**
	 * 系统升级文件重命名
	 * /cache/TecSun TA V1.2.12 Build20171130-update.zip
	 */
	public static boolean reNameSystemUpdateFile(String pathName) {
		try {
			File file = new File(pathName);
			if (!file.exists()) {
				return false;//重命名文件不存在
			} else {
				int indexOf = pathName.lastIndexOf("/");
				String path = pathName.substring(0, indexOf);
				String oleFileName = pathName.substring(indexOf+1, pathName.length());//TecSun TA V1.2.12 Build20171130-update.zip或者可能是update.zip
				if (oleFileName.contains("-")) {
					String[] split2 = oleFileName.split("-");
					String newFileName = split2[split2.length - 1];//update.zip
					return reNameFile(pathName, path + "/" + newFileName);
				} else {//已经修改好的
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	/** */
	/**
	 * 文件重命名
	 *
	 * @param oldName 原来的文件名
	 * @param newName 新文件名
	 */
	public static boolean reNameFile(String oldName, String newName) {
		try {
			if (!oldName.equals(newName)) {//新的文件名和以前文件名不同时,才有必要进行重命名
				File oldFile = new File(oldName);
				File newFile = new File(newName);
				if (!oldFile.exists()) {
					return false;//重命名文件不存在
				}
				if(newFile.exists()){
					newFile.delete();
				}
				oldFile.renameTo(newFile);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
