package localhost.activity;

import localhost.mobilestreamer.R;
import localhost.webrtc.SocketEvent;
import localhost.webrtc.SocketThread;
import localhost.webrtc.SocketThread.EventListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * 스플래쉬 액티비티는 앱을 처음 실행할 때 시그널링 서버와 접속하는 역할을 합니다.
 * 
 * 서버와 연결이 정상적으로 되면 사인-인 화면으로 이동하며, 실패 시 앱을 종료합니다.
 */
public class SplashActivity extends Activity implements EventListener {
	/**
	 * 시그널링 서버의 주소를 입력합니다. socket.io를 사용하기 때문에 HTTP 프로토콜을 이용해 접속합니다.
	 */
	private static final String serverAddress = "http://172.27.34.228:4450";
	/**
	 * socket.io를 사용할 스레드입니다. 싱글톤으로 구현되었기 때문에 처음 호출 시에만 초기화합니다.
	 */
	private SocketThread socketThread = SocketThread.getInstance();

	/**
	 * 액티비티를 생성합니다. 뷰를 그린 후 소켓에 접속하며,
	 * 액티비티 자체를 이벤트 리스너로 추가합니다.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		socketThread.addListener(this);
		socketThread.attachServer(serverAddress);
	}

	/**
	 * 서버에서 이벤트를 받았을 때 호출합니다.
	 * 
	 * 접속에 성공 또는 실패 이벤트를 처리합니다.
	 */
	@Override
	public void onListen(int event, int code, Object data) {
		switch(event) {
		case SocketEvent.MSG_ATTACH_SERVER: // 서버 접속 이벤트
			onAttachServer(code);
			break;
		}
	}

	/**
	 * 서버 접속 이벤트가 발생하였을 때 호출합니다.
	 * 
	 * @param code 이벤트 결과 코드
	 */
	private void onAttachServer(int code) {
		socketThread.removeListener(this);

		switch(code) {
		case SocketEvent.SUCCESS:
			// 성공 시 사인-인 액티비티 실행
			startActivity(new Intent(this, SignInActivity.class));
			break;
		case SocketEvent.FAILURE:
			// 실패 시 메시지 띄우기
			Toast.makeText(this, "서버 접속에 실패했습니다.", Toast.LENGTH_SHORT).show();
			break;
		}

		finish();
	}
}