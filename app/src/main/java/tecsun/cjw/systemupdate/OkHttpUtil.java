package tecsun.cjw.systemupdate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class OkHttpUtil {
	private final static long CONNECT_TIMEOUT_30 = 30;//超时时间，秒
	private final static long READ_TIMEOUT_30 = 30;//读取时间，秒
	private final static long WRITE_TIMEOUT_30 = 30;//写入时间，秒

	private static OkHttpUtil sOkHttpUtil;
	private OkHttpClient mClient;
	private ConcurrentHashMap<String, Call> mCalls = new ConcurrentHashMap<>();//用来存放各个下载的请求

	public static OkHttpUtil getInstance() {
		if (sOkHttpUtil == null) {
			synchronized (OkHttpUtil.class) {
				if (sOkHttpUtil == null) {
					sOkHttpUtil = new OkHttpUtil();
				}
			}
		}
		return sOkHttpUtil;
	}

	private OkHttpUtil() {
		mClient = new OkHttpClient();
		mClient.newBuilder().connectTimeout(CONNECT_TIMEOUT_30, TimeUnit.SECONDS);//连接超时
		mClient.newBuilder().readTimeout(READ_TIMEOUT_30, TimeUnit.SECONDS);//读取超时
		mClient.newBuilder().writeTimeout(WRITE_TIMEOUT_30, TimeUnit.SECONDS);//写入超时
	}

	/**
	 * 通用网络请求
	 *
	 * @param url 下载链接
	 * @param callback 回调
	 */
	public void doHttp(String url, Callback callback) {
		Request request = new Request.Builder().url(url).build();
		Call call = mClient.newCall(request);
		mCalls.put(url, call);//记录请求
		call.enqueue(callback);
	}

	/**
	 * @param url        下载链接
	 * @param startIndex 下载起始位置
	 * @param endIndex   结束为止
	 * @param callback   回调
	 */
	public void downloadFileByRange(String url, long startIndex, long endIndex, Callback callback) {
		// 创建一个Request
		// 设置分段下载的头信息。 Range:做分段数据请求,断点续传指示下载的区间。格式: Range bytes=0-1024或者bytes:0-1024
		Request request = new Request.Builder().header("RANGE", "bytes=" + startIndex + "-" + endIndex).url(url).build();
		Call call = mClient.newCall(request);
		mCalls.put(url, call);//记录请求
		call.enqueue(callback);
	}

	/**
	 * 取消请求
	 */
	public void cancel(String url) {
		Call call = mCalls.get(url);
		if (call != null) {
			call.cancel();//取消
		}
		mCalls.remove(url);
	}
}
