package tecsun.cjw.systemupdate.http;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.net.ConnectivityManagerCompat;

public class NetManager {
	private NetManager() {
	}

	/**
	 * 检查当前WIFI是否连接，两层意思——是否连接，连接是不是WIFI
	 *
	 * @param context
	 * @return true表示当前网络处于连接状态，且是WIFI，否则返回false
	 */
	public static boolean isWifiConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null && info.isConnected() && ConnectivityManager.TYPE_WIFI == info.getType()) {
			return true;
		}
		return false;
	}

	/**
	 * 检查当前GPRS是否连接，两层意思——是否连接，连接是不是GPRS
	 *
	 * @param context
	 * @return true表示当前网络处于连接状态，且是GPRS，否则返回false
	 */
	public static boolean isGprsConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null && info.isConnected() && ConnectivityManager.TYPE_MOBILE == info.getType()) {
			return true;
		}
		return false;
	}

	/**
	 * 检查当前是否连接
	 *
	 * @param context
	 * @return true表示当前网络处于连接状态，否则返回false
	 */
	public static boolean isConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			return true;
		}
		return false;
	}

	/**
	 * 对大数据传输时，需要调用该方法做出判断，如果流量敏感，应该提示用户
	 *
	 * @param context
	 * @return true表示流量敏感，false表示不敏感
	 */
	public static boolean isActiveNetworkMetered(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return ConnectivityManagerCompat.isActiveNetworkMetered(cm);
	}

	public static Intent registerReceiver(Context context, ConnectivityChangeReceiver receiver) {
		return context.registerReceiver(receiver, ConnectivityChangeReceiver.FILTER);
	}

	public static void unregisterReceiver(Context context, ConnectivityChangeReceiver receiver) {
		context.unregisterReceiver(receiver);
	}

	public abstract static class ConnectivityChangeReceiver extends BroadcastReceiver {

		public static final IntentFilter FILTER = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

		@Override
		public final void onReceive(Context context, Intent intent) {
			if (isConnected(context)) {
				onConnected();
			} else {
				onDisconnected();
			}
		}

		public abstract void onDisconnected();

		public abstract void onConnected();
	}
}
