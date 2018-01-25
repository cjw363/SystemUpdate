package tecsun.cjw.systemupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 开机监听广播，启动服务检查更新
 */
public class BootCompletedReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent intentService = new Intent(context, SystemUpdateService.class);
			intentService.setAction(SystemUpdateService.INTENT_ACTION_CHECK_SYSTEM_UPDATE);
			context.startService(intentService);
		}
	}
}
