package tecsun.cjw.systemupdate.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtils {

	public static void putString(String key, String str) {
		SharedPreferences.Editor sp = UI.getContext().getSharedPreferences("tecsun_system_update", Context.MODE_PRIVATE).edit();
		sp.putString(key, str);
		sp.apply();
	}

	public static String getString(String key) {
		SharedPreferences sp = UI.getContext().getSharedPreferences("tecsun_system_update", Context.MODE_PRIVATE);
		return sp.getString(key, "");
	}
	public static void putInt(String key, int str) {
		SharedPreferences.Editor sp = UI.getContext().getSharedPreferences("tecsun_system_update", Context.MODE_PRIVATE).edit();
		sp.putInt(key, str);
		sp.apply();
	}

	public static int getInt(String key) {
		SharedPreferences sp = UI.getContext().getSharedPreferences("tecsun_system_update", Context.MODE_PRIVATE);
		return sp.getInt(key, 0);
	}

	public static void clear() {
		SharedPreferences preferences = UI.getContext().getSharedPreferences("tecsun_system_update", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.clear();
		editor.apply();
	}

}
