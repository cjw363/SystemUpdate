package tecsun.cjw.systemupdate.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;

import java.util.Stack;

public class BaseApplication extends Application {
	public static String systemUpdateUrl = "http://cpzx.e-tecsun.com:8037/update/TA/update_cjw.xml";
	private static Context context;
	private static Handler handler;
	private static int mainThreadId;
	private static Stack<Activity> mStack = new Stack<>();

	@Override
	public void onCreate() {
		super.onCreate();

		context = getApplicationContext();
		handler = new Handler();
		mainThreadId = android.os.Process.myTid();
	}

	public static Context getContext() {
		return context;
	}

	public static Handler getHandler() {
		return handler;
	}

	public static int getMainThreadId() {
		return mainThreadId;
	}

	public static void putActivity(Activity activity) {
		mStack.push(activity);
	}

	public static void removeActivity() {
		mStack.pop();
	}

	public static Activity getActivity() {
		return mStack.get(mStack.size() - 1);
	}
}
