package localhost.activity;

import localhost.mobilestreamer.R;
import localhost.webrtc.WebrtcThread;
import localhost.webrtc.WebrtcThread.OnConnectListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class SplashActivity extends Activity implements OnConnectListener {

	private WebrtcThread mWebrtcThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		mWebrtcThread = WebrtcThread.getInstance();
		mWebrtcThread.setContext(getApplicationContext());
		mWebrtcThread.setOnConnectListener(this);
		mWebrtcThread.setHost("http://192.168.0.2:4450");
		mWebrtcThread.attachSignalingServer();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onSuccess() {
		startActivity(new Intent(this, SignInActivity.class));
		Toast.makeText(this, "서버에 접속하였습니다.", Toast.LENGTH_SHORT).show();
		finish();
	}

	@Override
	public void onFailure(String message) {
		Toast.makeText(this, "서버 접속에 실패했습니다.", Toast.LENGTH_SHORT).show();
		finish();
	}
}