package localhost.activity;

import localhost.mobilestreamer.R;
import localhost.webrtc.SocketEvent;
import localhost.webrtc.SocketThread;
import localhost.webrtc.SocketThread.EventListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class SplashActivity extends Activity implements EventListener {

	private SocketThread socketThread = SocketThread.getInstance();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		socketThread.addListener(this);
		socketThread.attachServer("http://192.168.0.2:4450");
		// socketThread.attachServer("http://192.168.1.24:4450");
	}

	@Override
	public void onListen(int event, int code, Object data) {
		switch(event) {
		case SocketEvent.MSG_ATTACH_SERVER:
			onAttachServer(code);
			break;
		}
	}

	private void onAttachServer(int result) {
		socketThread.removeListener(this);

		switch(result) {
		case SocketEvent.SUCCESS:
			startActivity(new Intent(this, SignInActivity.class));
			finish();
			break;
		case SocketEvent.FAILURE:
			Toast.makeText(this, "서버 접속에 실패했습니다.", Toast.LENGTH_SHORT).show();
			finish();
			break;
		}
	}
}