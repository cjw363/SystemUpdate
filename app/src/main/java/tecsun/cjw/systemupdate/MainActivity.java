package tecsun.cjw.systemupdate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

//    System.out.println("主线程"+Thread.currentThread().getId());
    OkHttpUtil.getInstance().doHttp("http://cpzx.e-tecsun.com:8037/update/TA/update_cjw.xml");
  }
}
