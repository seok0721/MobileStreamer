package localhost.webrtc;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoRenderer;
import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.PeerConnection.IceGatheringState;
import org.webrtc.PeerConnection.Observer;
import org.webrtc.PeerConnection.SignalingState;
import org.webrtc.SessionDescription.Type;

import android.util.Log;

public class PeerConnectionWrapper implements SdpObserver, Observer {

	private static final String TAG = PeerConnectionWrapper.class.getName();
	private SocketThread socketThread = SocketThread.getInstance();
	private PeerConnection connection;
	private PeerConnectionType type;
	private SessionDescription session;
	private String socketId;
	private VideoRenderer.Callbacks renderer;
	private MediaStream localStream;

	public MediaStream getLocalStream() {
		return localStream;
	}

	public void setLocalStream(MediaStream localStream) {
		this.localStream = localStream;
	}

	public void setConnection(PeerConnection connection) {
		this.connection = connection;
	}

	public PeerConnection getConnection() {
		return connection;
	}

	public PeerConnectionType getType() {
		return type;
	}

	public void setType(PeerConnectionType type) {
		this.type = type;
	}

	public VideoRenderer.Callbacks getRenderer() {
		return renderer;
	}

	public void setRenderer(VideoRenderer.Callbacks renderer) {
		this.renderer = renderer;
	}

	public String getSocketId() {
		return socketId;
	}

	public void setSocketId(String socketId) {
		this.socketId = socketId;
	}

	@Override
	public void onCreateFailure(String error) {
		Log.i(TAG, "onCreateFailure"); // 커넥션이 총 3개이기 때문에 실패 이벤트가 발생하는건 못봤습니다
	}

	@Override
	public void onCreateSuccess(SessionDescription session) {
		Log.i(TAG, "onCreateSuccess");
		this.session = session;
		switch(type) {
		case Offerer: // 요청자가 오퍼(로컬스트림정보)를 생성하면 로컬 커넥션에 바로 설정합니다.
			connection.setLocalDescription(this, session);
			break;
		case Answerer: // 응답자도 앤서(로컬스트림정보)를 생성하면 로컬 커넥션에 바로 설장합니다.
			connection.setLocalDescription(this, session);
			break;
		}
	}

	@Override
	public void onSetFailure(String error) {
		Log.i(TAG, "onSetFailure"); // 라이브러리에서 생성하는 정보를 그대로 사용해서 에러가 나지 않았습니다.
	}

	@Override
	public void onSetSuccess() {
		Log.i(TAG, "onSetSuccess");
		switch(type) {
		case Offerer:
			if(session.type == Type.OFFER) {
				// 요청자가 로컬 스트림 세션을 설정하면 NAT Traversal을 하기 때문에 아래에 있는 onIceGatheringChange 이벤트 발생을 대기합니다.
			} else {
				// 요청자가 원격 스트림 세션을 설정하면 연결이 완료된 것이기 때문에 아무것도 하지 않습니다.
			}
			break;
		case Answerer:
			if(session.type == Type.OFFER) {
				// 응답자가 원격 스트림 세션을 설정하면 응답에 사용할 세션을 생성합니다.
				connection.createAnswer(this, new MediaConstraints());
			} else {
				// 응답자가 로컬 스트림 세션을 설정하면 NAT Traversal을 하기 때문에 아래에 있는 onIceGatheringChange 이벤트 발생을 대기합니다.
			}
			break;
		}
	}

	@Override
	public void onAddStream(MediaStream media) {
		Log.i(TAG, "onAddStream, " + media.videoTracks.size());

		// 원격지에서 비디오/오디오 스트림을 받아옵니다.
		// 현재 스트림은 하나씩만 사용하기 때문에 인덱스 0만 추가합니다. 
		media.videoTracks.get(0).addRenderer(new VideoRenderer(renderer));
	}

	@Override
	public void onDataChannel(DataChannel arg0) {
		Log.i(TAG, "onDataChannel"); // 자막같은걸 전송할 때 사용하는데, 여기선 쓰이지 않습니다.
	}

	@Override
	public void onIceCandidate(IceCandidate iceCadidate) {
		Log.i(TAG, "onIceCandidate, " + iceCadidate.sdp);
		// 공인 아이피 주소, 중개 서버 주소를 찾은 경우 추가합니다. 
		connection.addIceCandidate(iceCadidate);
	}

	@Override
	public void onIceConnectionChange(IceConnectionState state) {
		Log.i(TAG, "onIceConnectionChange, " + state.name());
	}

	/**
	 * 이 함수가 핵심입니다. setLocalDescription을 호출하면 NAT Traversal이 시작됩니다.
	 * 
	 * iceGatheringState는 서버에서 주소를 수집하기 때문에 GATHERING 상태로 이벤트가 발생하며,
	 * 모든 작업이 완료되면 COMPLETE상태의 이벤트가 발생합니다.
	 */
	@Override
	public void onIceGatheringChange(IceGatheringState state) {
		Log.i(TAG, "onIceGatheringChange, " + state.name());
		switch(type) {
		case Offerer:
			// 요청자가 공인아이피 주소와 중개 서버 주소를 모두 수집하였고
			// 현재 로컬 세션을 가지고 있으면 스트림 송/수신 요청을 전송합니다.
			if(state == IceGatheringState.COMPLETE && connection.signalingState() == SignalingState.HAVE_LOCAL_OFFER) {
				socketThread.sendOffer(connection.getLocalDescription().description);
			}
			break;
		case Answerer:
			// 응답자가 공인아이피 주소와 중개 서버 주소를 모두 수집하였고
			// 현재 로컬 세션과 원격 세션을 둘 다 가지고 있으면 요청자에게 응답을 전송합니다.
			if(state == IceGatheringState.COMPLETE && connection.signalingState() == SignalingState.STABLE) {
				socketThread.sendAnswer(socketId, connection.getLocalDescription().description);
			}
			break;
		}
	}

	@Override
	public void onRemoveStream(MediaStream media) {
		Log.i(TAG, "onRemoveStream");
		// TODO 처리하기
		connection.removeStream(media);
	}

	@Override
	public void onRenegotiationNeeded() {
		Log.i(TAG, "onRenegotiationNeeded");
	}

	@Override
	public void onSignalingChange(SignalingState state) {
		// TODO 처리하기
		Log.i(TAG, "onSignalingChange, " + state.name());
	}
}