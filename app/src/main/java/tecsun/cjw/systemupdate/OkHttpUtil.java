package tecsun.cjw.systemupdate;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpUtil {
  private static OkHttpUtil sOkHttpUtil;
  private OkHttpClient mClient;
  private ConcurrentHashMap<String, Call> mCalls =new ConcurrentHashMap<>();//用来存放各个下载的请求

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
    mClient.newBuilder().connectTimeout(10, TimeUnit.SECONDS);//连接超时
    mClient.newBuilder().readTimeout(10, TimeUnit.SECONDS);//读取超时
    mClient.newBuilder().writeTimeout(10, TimeUnit.SECONDS);//写入超时
  }

  public void doHttp(String url) {
    Request request = new Request.Builder().url(url).build();
    Call call = mClient.newCall(request);
    mCalls.put(url,call);//记录请求
    call.enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {

      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        System.out.println("成功"+Thread.currentThread().getId());
      }
    });
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
