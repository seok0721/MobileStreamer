package localhost.activity;

import java.util.ArrayList;

import localhost.mobilestreamer.R;
import localhost.webrtc.CameraView;
import localhost.webrtc.PeerConnectionType;
import localhost.webrtc.PeerConnectionWrapper;
import localhost.webrtc.SocketEvent;
import localhost.webrtc.SocketThread;
import localhost.webrtc.SocketThread.EventListener;

import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaConstraints.KeyValuePair;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnection.IceServer;
import org.webrtc.PeerConnection.SignalingState;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SessionDescription.Type;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoRendererGui.ScalingType;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 다른 사용자들과 비디오/오디오를 송/수신하는 액티비티입니다.
 * 
 * 사용자 인증에 성공하면 액티비티를 시작합니다.
 */
public class ShootingActivity extends Activity implements OnClickListener, EventListener {
	/**
	 * 로그 출력에 사용할 태그
	 */
	private static final String TAG = ShootingActivity.class.getName();
	/**
	 * 소켓 스레드
	 */
	private SocketThread socketThread = SocketThread.getInstance();
	/**
	 * 접속할 채널 입력 뷰
	 */
	private EditText edtChannel;
	/**
	 * 카메라 출력 뷰
	 */
	private CameraView mCameraView;
	/**
	 * 방 접속 버튼 뷰
	 */
	private Button btnEnterChannel;
	/**
	 * 방 생성 버튼 뷰
	 */
	private Button btnCreateChannel;
	/**
	 * WebRTC를 사용할 때 커넥션을 생성하는 팩토리
	 */
	private PeerConnectionFactory factory;
	/**
	 * 전면 카메라와 마이크를 입력받을 로컬 스트림
	 */
	private MediaStream mLocalStream;
	/**
	 * 실제 오디오 데이터를 입력받는 오디오 소스
	 */
	private AudioSource mAudioSource;
	/**
	 * 오디오 소스를 미디어 스트림에 추가하기 위해서 중개자 역할을 하는 오디오 트랙
	 */
	private AudioTrack mAudioTrack;
	/**
	 * 카메라 전/후방 선택 및 각도를 설정하여 실시간 영상을 받아오는 비디오 캡쳐러
	 */
	private VideoCapturer mVideoCapturer;
	/**
	 * 실제 비디오 데이터를 입력받는 소스
	 */
	private VideoSource mVideoSource;
	/**
	 * 비디오 소스를 미디어 스트림에 추가하기 위해서 중개자 역할을 하는 비디오 트랙
	 */
	private VideoTrack mVideoTrack;
	/**
	 * 로컬 스트림을 화면에 출력할 로컬 렌더러
	 */
	private VideoRenderer.Callbacks localRenderer;
	/**
	 * 원격 스트림을 화면에 출력할 원격 렌더러, 4분할된 화면에서 총 3개를 사용
	 */
	private VideoRenderer.Callbacks[] remoteRenderers = new VideoRenderer.Callbacks[3];
	/**
	 * 원격 사용자들과 비디오/오디오 데이터를 송/수신하기 위한 커넥션, 렌더러와 맞춰 총 3개를 사용
	 */
	private PeerConnectionWrapper[] connections = new PeerConnectionWrapper[3];
	/**
	 * 공인 아이피 주소를 가져올 stun서버와 방화벽이나 다른 네트워크 사이에서 중개자 역할을 할 turn
	 * 서버의 주소를 보관하는 ice 서버 리스트
	 * + 참고1) STUN = Session Traversal Utilities for NAT
	 * + 참고2) TURN = Traversal Using Relays around NAT
	 * + 참고3) ICE = Interactive Connectivity Establishment
	 */
	private ArrayList<IceServer> iceServerList;
	/**
	 * 피어 커넥션을 생성할 때 필수 또는 추가 옵션을 주기 위한 제약정보
	 * 현재는 비디오를 송/수신할 때 스트림에 보안 기능을 추가 할 것인지 말 것인지에 대한 옵션만 있음.
	 * 예제를 따라서 한 것이기 때문에 자세한 내용은 잘...
	 */
	private MediaConstraints mediaConstraints;

	private Object mLock = new Object();

	/**
	 * 슈팅 액티비티를 초기화 합니다.
	 * 
	 * 원격 사용자들과 접속하기 위해서 환경 변수 및 로컬 스트림을 초기화합니다.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shooting);

		// 뷰 초기화 작업 실행
		edtChannel = (EditText) findViewById(R.id.edtChannel);
		mCameraView = (CameraView)findViewById(R.id.surfaceView);
		btnEnterChannel = (Button) findViewById(R.id.btnEnterChannel);
		btnEnterChannel.setOnClickListener(this);
		btnCreateChannel = (Button) findViewById(R.id.btnCreateChannel);
		btnCreateChannel.setOnClickListener(this);

		// WebRTC 라이브러리 초기화, EGLContext는 하드웨어 가속에 사용한다고 합니다.
		PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true, VideoRendererGui.getEGLContext());
		factory = new PeerConnectionFactory(); // 피어 커넥션 팩토리 생성
		mLocalStream = factory.createLocalMediaStream(String.format("%s", TAG)); // 로컬 스트림 생성

		// 오디오 트랙 초기화
		mAudioSource = factory.createAudioSource(new MediaConstraints());
		mAudioTrack = factory.createAudioTrack(String.format("%sa", TAG), mAudioSource); // 인자는 레이블입니다.

		// 비디오 트랙 초기화
		mVideoCapturer = VideoCapturer.create("Camera 1, Facing front, Orientation 270"); // logcat에서 가능한 옵션들을 모두 확인할 수 있습니다.
		mVideoSource = factory.createVideoSource(mVideoCapturer, new MediaConstraints());
		mVideoTrack = factory.createVideoTrack(String.format("%sv", TAG), mVideoSource); // 인자는 레이블입니다.

		// 카메라 뷰에 렌더러를 장착
		VideoRendererGui.setView(mCameraView, new Runnable() {

			@Override
			public void run() {
				// 카메라가 초기화 되면 호출되는 것 같은데... 딱히 하는 일이 없음.
			}
		});

		// 카메라뷰를 액티비티 화면에 맞게 설정합니다.
		// 지금은 필요 없는 것 같은데,,, 시간 상 예전 프로젝트에 사용한 그대로 카피캣
		mCameraView.initialize(this);

		// 화면을 4개로 분할합니다. 인자값은 차례대로 top, left, width(%), height(%)입니다.
		// 로컬 비디오는 좌상에, 원격 비디오는 우상, 좌하, 우하에 배치합니다.
		localRenderer = VideoRendererGui.create(2, 2, 47, 40, ScalingType.SCALE_FILL, true);
		remoteRenderers[0] = VideoRendererGui.create(51, 2, 47, 40, ScalingType.SCALE_FILL, true);
		remoteRenderers[1] = VideoRendererGui.create(2, 44, 47, 40, ScalingType.SCALE_FILL, true);
		remoteRenderers[2] = VideoRendererGui.create(51, 44, 47, 40, ScalingType.SCALE_FILL, true);

		// 로컬 비디오를 렌더러에 출력합니다.
		mVideoTrack.addRenderer(new VideoRenderer(localRenderer));

		// 원격지에 비디오와 오디오를 전송할 수 있도록 스트림에 비디오 오디오 트랙을 추가합니다.
		mLocalStream.addTrack(mAudioTrack);
		mLocalStream.addTrack(mVideoTrack);

		// 아이스 서버를 초기화합니다. 현재는 구글에서 제공하는 stun서버만 사용합니다.
		// turn서버를 추가하려면 양식은 "turn:<서버아아피>:<서버포트>" 형태로 추가하세요.
		iceServerList = new ArrayList<IceServer>();
		iceServerList.add(new IceServer("stun:stun.l.google.com:19302"));

		// 미디어 스트림 전송 방식에 보안 옵션을 추가합니다.
		mediaConstraints = new MediaConstraints();
		mediaConstraints.optional.add(new KeyValuePair("DtlsSrtpKeyAgreement", "true"));

		// 원격 접속에 사용될 각 커넥션을 초기화 합니다.
		// 래퍼 클래스 자체가 콜백이기 때문에 아래에서 인자로 사용됩니다.
		for(int i = 0; i < connections.length; i++) {
			connections[i] = new PeerConnectionWrapper(); // 커넥션과 콜백 함수를 연결하기 위한 래퍼 클래스 생성
			// 커넥션 생성 및 커넥션에 콜백함수 등록
			PeerConnection connection = factory.createPeerConnection(iceServerList, mediaConstraints, connections[i]);
			connection.addStream(mLocalStream); // 로컬 스트림 추가
			connections[i].setConnection(connection); // 콜백 함수에 커넥션 등록
			connections[i].setRenderer(remoteRenderers[i]); // 콜백 함수에 렌더러 등록
			connections[i].setLocalStream(mLocalStream);
			connections[i].setFactory(factory);
			connections[i].setIceServerList(iceServerList);
			connections[i].setConstraints(mediaConstraints);
		}

		// 현재 액티비티를 이벤트 리스너로 추가합니다.
		socketThread.addListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		for(int i = 0; i < connections.length; i++) {
			connections[i].getConnection().close();
		}

		SocketThread.getInstance().detachServer();
	}

	/**
	 * 액티비티에 있는 버튼이 눌렸을 때 호출합니다.
	 */
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btnEnterChannel:
			onClickBtnEnterChannel(v);
			break;
		case R.id.btnCreateChannel:
			onClickBtnCreateChannel(v);	
			break;
		}
	}

	/**
	 * 방 접속 버튼이 눌렸을 때 호출합니다.
	 */
	private void onClickBtnEnterChannel(View v) {
		String channel = edtChannel.getText().toString().trim();

		if(channel.length() == 0) {
			Toast.makeText(this, "채널을 입력하세요.", Toast.LENGTH_SHORT).show();
			return;
		}

		socketThread.enterChannel(channel);
	}

	/**
	 * 방 생성 버튼이 눌렸을 때 호출합니다.
	 */
	private void onClickBtnCreateChannel(View v) {
		socketThread.createChannel();
	}

	/**
	 * 서버에서 방 생성 결과, 방 접속 결과, "Offer"수신, "Answer"수신 이벤트가 발생했을 때 호출합니다. 
	 */
	@Override
	public void onListen(int event, int code, Object data) {
		switch(event) {
		case SocketEvent.MSG_CREATE_CHANNEL:
			onCreateChannel(code, data);
			break;
		case SocketEvent.MSG_ENTER_CHANNEL:
			onEnterChannel(code, data);
			break;
		case SocketEvent.MSG_RECEIVE_OFFER:
			onReceiveOffer(code, (JSONObject)data);
			break;
		case SocketEvent.MSG_RECEIVE_ANSWER:
			onReceiveAnswer(code, (JSONObject)data);
			break;
		}
	}

	/**
	 * 채널 생성 결과 이벤트를 받았을 때 호출합니다.
	 * 
	 * @param code 결과 코드
	 * @param data 에러 메시지
	 */
	private void onCreateChannel(int code, Object data) {
		switch(code) {
		case SocketEvent.SUCCESS:
			Toast.makeText(this, "채널을 생성하였습니다.", Toast.LENGTH_SHORT).show();
			break;
		case SocketEvent.FAILURE:
			Toast.makeText(this, (String)data, Toast.LENGTH_SHORT).show();
			break;
		}
	}

	/**
	 * 채널 접속 결과 이벤트를 받았을 때 호출합니다.
	 * 
	 * @param code 결과 코드
	 * @param data 에러 메시지
	 */
	private void onEnterChannel(int code, Object data) {
		switch(code) {
		case SocketEvent.SUCCESS:
			Toast.makeText(this, "방에 접속하였습니다.", Toast.LENGTH_SHORT).show();

			// 커넥션 배열에서 접속 가능한 커넥션을 찾습니다.
			for(int i = 0; i < connections.length; i++) {
				PeerConnection connection = connections[0].getConnection();

				// 로컬 스트림 정보와 원격 스트림 정보가 없으면 사용 가능한 커넥션입니다.
				if(connection.getLocalDescription() == null && connection.getRemoteDescription() == null) {
					// 방에 접속하자마자 원격 사용자들에게 스트림 수신 요청을 보내기 때문에 타입은 "요청자"입니다.
					connections[0].setType(PeerConnectionType.Offerer);
					// 원격 사용자들에게 보낼 로컬 스트림 정보를 생성합니다.
					connection.createOffer(connections[i], new MediaConstraints());
					return;
				}
			}

			Toast.makeText(this, "사용 가능한 커넥션이 없습니다..", Toast.LENGTH_SHORT).show();
			break;
		case SocketEvent.FAILURE:
			Toast.makeText(this, (String)data, Toast.LENGTH_SHORT).show();
			break;
		}
	}

	/**
	 * 스트림 송/수신 요청 이벤트를 받았을 때 호출합니다.
	 * 
	 * @param code 결과 코드
	 * @param data 요청자의 소켓 아이디와 스트림 정보인 SDP(Session Description Protocol)를 수신받습니다.
	 */
	private void onReceiveOffer(int code, JSONObject data) {
		try {
			String socketId = data.getString("socketId"); // 요청자의 소켓 아이디
			String sdp = data.getString("sdp"); // 요청자가 보낸 스트림 정보

			for(int i = 0; i < connections.length; i++) {
				PeerConnection connection = connections[i].getConnection();

				// 요청자로부터 요청을 받았을 때 사용가능한 커넥션을 확인합니다. 로컬 및 리모트 디스크립션이 없으면 사용 가능합니다. 
				if(connection.getLocalDescription() == null && connection.getRemoteDescription() == null) {
					// 요청자가 보낸 정보이기 때문에 OFFER 타입을 가진 세션 디스크립션을 생성합니다.
					SessionDescription session = new SessionDescription(Type.OFFER, sdp);
					connections[i].setType(PeerConnectionType.Answerer); // 요청자의 요청에 "응답"을 하는 것이기 때문에 커넥션은 "응답자"가 됩니다.
					connections[i].setSocketId(socketId); // 요청자의 소켓 아이디를 설정합니다.
					connections[i].setSession(session);
					connection.setRemoteDescription(connections[i], session); // 요청자가 보낸 디스크립션을 설정합니다.
					return;
				}
			}

			throw new Exception("no connection available.");
		} catch(Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	/**
	 * 스트림 송/수신 응답 이벤트를 받았을 때 호출합니다.
	 * 
	 * @param code 결과 코드
	 * @param data 응답자의 스트림 정보인 SDP(Session Description Protocol)를 수신받습니다.
	 */
	private void onReceiveAnswer(int code, JSONObject data) {
		Log.i(TAG, "handle answer");
		boolean isSuccess = false;
		try {
			String sdp = data.getString("sdp");

			// 요청자의 커넥션을 찾습니다.
			synchronized(mLock) {
				for(int i = 0; i < connections.length; i++) {
					PeerConnection connection = connections[i].getConnection();
					boolean isWaiter = (connection.getLocalDescription() == null && connection.getRemoteDescription() == null);
					// 요청자의 커넥션의 상태는 "로컬 요청을 가지고 있음" 입니다.

					// 같은 방에서 다른 사용자에게 응답을 받은 경우
					if(isWaiter) {
						SessionDescription session = new SessionDescription(Type.ANSWER, sdp);
						connections[i].setType(PeerConnectionType.Waiter);
						connections[i].setSession(session);
						connection.setLocalDescription(connections[i], PeerConnectionWrapper.offerSession);
						//connection.createOffer(connections[i], new MediaConstraints());
						//connection.setRemoteDescription(connections[i], session); // 응답자의 세션을 설정해서 연결을 완료시킵니다.
						isSuccess = true;
						break;
					}

					if(connection.signalingState() == SignalingState.HAVE_LOCAL_OFFER) {
						// 응답자가 보낸 스트림 정보이기 때문에 타입은 "ANSWER" 입니다.
						SessionDescription session = new SessionDescription(Type.ANSWER, sdp);
						connections[i].setSession(session);
						connection.setRemoteDescription(connections[i], session); // 응답자의 세션을 설정해서 연결을 완료시킵니다.
						isSuccess = true;
						break;
					}
				}
			}

			if(!isSuccess) {
				throw new Exception("사용 가능한 커넥션이 없습니다.");
			}
		} catch(Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
}