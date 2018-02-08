package tecsun.cjw.systemupdate.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class BitmapUtil {
	public static Bitmap setImgSize(Bitmap bm, int newWidth, int newHeight) {
		// 获得图片的宽高.
		int width = bm.getWidth();
		int height = bm.getHeight();
		// 计算缩放比例.
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 取得想要缩放的matrix参数.
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// 得到新的图片.
		return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
	}

	public static Bitmap setImgSize(Bitmap bm, float scale) {
		// 获得图片的宽高.
		int width = bm.getWidth();
		int height = bm.getHeight();
		// 取得想要缩放的matrix参数.
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		// 得到新的图片.
		return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
	}
}
