package tecsun.cjw.systemupdate.http;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import tecsun.cjw.systemupdate.utils.UI;

public abstract class CommonCallback implements Callback{
	@Override
	public void onResponse(final Call call, final Response response) throws IOException {
		UI.runOnUIThread(new Runnable() {
			@Override
			public void run() {
				_onResponse(call,response);
			}
		});
	}

	@Override
	public void onFailure(final Call call, final IOException e) {
		UI.runOnUIThread(new Runnable() {
			@Override
			public void run() {
				_onFailure(call,e);
			}
		});
	}

	public abstract void _onResponse(Call call, Response response);

	public abstract void _onFailure(Call call, IOException e);
}
