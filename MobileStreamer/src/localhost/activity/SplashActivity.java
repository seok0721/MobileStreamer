package localhost.activity;

import localhost.mobilestreamer.R;
import localhost.webrtc.SocketThread;
import localhost.webrtc.WebrtcThread;
import localhost.webrtc.WebrtcThread.OnConnectListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class SplashActivity extends Activity implements OnConnectListener {

	private SocketThread socketThread = SocketThread.getInstance();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		socketThread.attachServer("http://192.168.0.2:4450");
		socketThread.attachServer("http://192.168.1.24:4450");

		// mWebrtcThread = WebrtcThread.getInstance();
		// mWebrtcThread.setContext(getApplicationContext());
		// mWebrtcThread.setOnConnectListener(this);
		// mWebrtcThread.setHost("http://192.168.0.2:4450");
		// mWebrtcThread.setHost("http://192.168.1.24:4450");
		// mWebrtcThread.attachSignalingServer();
	}

	@Override
	public void onSuccess(int event) {
		startActivity(new Intent(this, SignInActivity.class));
		finish();
	}

	@Override
	public void onFailure(int event, String message) {
		Toast.makeText(this, "서버 접속에 실패했습니다.", Toast.LENGTH_SHORT).show();
		finish();
	}
}