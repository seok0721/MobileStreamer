package localhost.webrtc;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * node.js로 만든 소켓 서버와 실제로 통신하는 클래스입니다.
 */
public class SocketThread {
	/**
	 * 이벤트가 발생했을 때 이벤트를 처리할 리스너 인터페이스
	 * 
	 * event는 이벤트 타입, code는 결과 코드, data는 실제 데이터이다.
	 */
	public interface EventListener {
		public void onListen(int event, int code, Object data);
	}
	/**
	 * 로그에 사용할 태그
	 */
	private static final String TAG = SocketThread.class.getName();
	/**
	 * 객체를 하나만 쓰기 위한 싱글톤 인스턴스
	 */
	private static SocketThread instance;
	/**
	 * 이벤트를 처리할 리스너 리스트
	 */
	private List<EventListener> listenerList = new LinkedList<EventListener>();
	/**
	 * +주의) 백그라운드 스레드는 뷰를 업데이트 할 수 없으므로 별도의 UI 핸들러 스레드를 생성
	 */
	private HandlerThread eventThread;
	/**
	 * 이벤트를 처리할 핸들러 생성
	 */
	private EventHandler eventHandler;
	/**
	 * 서버와 접속하는 소켓
	 */
	private SocketIO socket;

	/**
	 * 어느 액티비티에서든 소켓스레드를 사용할 수 있도록 하는 싱글톤 객체 리턴 함수
	 */
	public static SocketThread getInstance() {
		if(instance == null) {
			instance  = new SocketThread();
		}

		return instance;
	}

	/**
	 * 이벤트 리스너 추가, 이 프로젝트에선 액티비티 참조를 추가함
	 */
	public void addListener(EventListener listener) {
		listenerList.add(listener);
	}

	/**
	 * 이벤트 리스너 삭제, 이 프로젝트에선 액티비티 참조만 제거함
	 */
	public void removeListener(EventListener listener) {
		listenerList.remove(listener);
	}

	/**
	 * 서버에 연결 시도
	 */
	public void attachServer(final String url) {
		try {
			socket = new SocketIO(url, new SocketHandler());
		} catch (MalformedURLException e) { // 연결 하기 전 에러!! URL 포맷이 잘못되었음
			eventHandler.obtainMessage(SocketEvent.MSG_ATTACH_SERVER, SocketEvent.FAILURE, 0, e.getMessage()).sendToTarget();
		}
	}

	/**
	 * 서버와 연결 해제
	 */
	public void detachServer() {
		socket.disconnect();
	}

	/**
	 * 회원 가입
	 * 
	 * @param email 아이디로 사용할 이메일 주소
	 * @param name 사용자 이름
	 * @param password 비밀번호
	 * @param base64Image 썸네일로 사용할 base64 이미지
	 */
	public void signUp(final String email, final String name, final String password, final String base64Image) {
		try {
			JSONObject data = new JSONObject();
			data.put("email", email);
			data.put("name", name);
			data.put("passwd", password);
			data.put("thumbnail", base64Image);

			socket.emit("signUp", data);
		} catch(JSONException e) {
			eventHandler.obtainMessage(SocketEvent.MSG_SIGN_UP, SocketEvent.FAILURE, 0, e.getMessage()).sendToTarget();
		}
	}

	/**
	 * 회원 인증
	 * 
	 * @param email 아이디로 사용하는 이메일 주소
	 * @param password 비밀번호
	 */
	public void signIn(final String email, final String password) {
		try {
			JSONObject data = new JSONObject();
			data.put("email", email);
			data.put("passwd", password);

			socket.emit("signIn", data);
		} catch(JSONException e) {
			eventHandler.obtainMessage(SocketEvent.MSG_SIGN_IN, SocketEvent.FAILURE, 0, e.getMessage()).sendToTarget();
		}
	}

	/**
	 * 채널 생성
	 */
	public void createChannel() {
		socket.emit("createChannel");
	}

	/**
	 * 채널 삭제
	 */
	public void deleteChannel() {
		socket.emit("deleteChannel");
	}

	/**
	 * 채널 입장
	 * 
	 * @param email 채널 아이디, 이 프로젝트에선 이메일로 구분합니다.
	 */
	public void enterChannel(final String email) {
		try {
			JSONObject data = new JSONObject();
			data.put("email", email);

			socket.emit("enterChannel", data);
		} catch(JSONException e) {
			eventHandler.obtainMessage(SocketEvent.MSG_ENTER_CHANNEL, SocketEvent.FAILURE, 0, e.getMessage()).sendToTarget();
		}
	}

	/**
	 * 채널 탈퇴
	 */
	public void leaveChannel() {
		socket.emit("leaveChannel");
	}

	/**
	 * 요청자의 세션 전송
	 * 
	 * @param sdp 세션 정보(IP 주소, 중개 서버 리스트, 비디오 소켓, 오디오 소켓, 코덱, 보안 등등...)
	 */
	public void sendOffer(final String sdp) {
		try {
			JSONObject data = new JSONObject();
			data.put("sdp", sdp);

			socket.emit("offer", data);
		} catch(JSONException e) {
			eventHandler.obtainMessage(SocketEvent.MSG_SEND_OFFER, SocketEvent.FAILURE, 0, e.getMessage()).sendToTarget();
		}
	}

	/**
	 * 응답자의 세션 전송
	 * 
	 * @param sdp 세션 정보(IP 주소, 중개 서버 리스트, 비디오 소켓, 오디오 소켓, 코덱, 보안 등등...)
	 */
	public void sendAnswer(final String socketId, final String sdp) {
		try {
			JSONObject data = new JSONObject();
			data.put("socketId", socketId);
			data.put("sdp", sdp);

			socket.emit("answer", data);
		} catch(JSONException e) {
			eventHandler.obtainMessage(SocketEvent.MSG_SEND_ANSWER, SocketEvent.FAILURE, 0, e.getMessage()).sendToTarget();
		}
	}

	/**
	 * 소켓 스레드를 생성할 때 뷰를 업데이트 할 수 있는 이벤트 스레드 생성
	 */
	private SocketThread() {
		eventThread = new HandlerThread(TAG); // 솔직히 이 스레드가 왜 뷰를 업데이트 할 수 있는지 모르겠음...
		eventThread.start();
		eventHandler = new EventHandler(eventThread.getLooper());
	}

	/**
	 * 서버와의 이벤트를 처리하는 콜백 핸들러입니다.
	 */
	private class SocketHandler implements IOCallback {

		@Override
		public void on(String event, IOAcknowledge ack, Object... args) {
			switch(event) {
			case "signUp":
				handleMessage(SocketEvent.MSG_SIGN_UP, (JSONObject)args[0]);
				break;
			case "signIn":
				handleMessage(SocketEvent.MSG_SIGN_IN, (JSONObject)args[0]);
				break;
			case "signOut":
				handleMessage(SocketEvent.MSG_SIGN_OUT, (JSONObject)args[0]);
				break;
			case "createChannel":
				handleMessage(SocketEvent.MSG_CREATE_CHANNEL, (JSONObject)args[0]);
				break;
			case "deleteChannel":
				handleMessage(SocketEvent.MSG_DELETE_CHANNEL, (JSONObject)args[0]);
				break;
			case "enterChannel":
				handleMessage(SocketEvent.MSG_ENTER_CHANNEL, (JSONObject)args[0]);
				break;
			case "leaveChannel":
				handleMessage(SocketEvent.MSG_LEAVE_CHANNEL, (JSONObject)args[0]);
				break;
			case "offer":
				eventHandler.obtainMessage(SocketEvent.MSG_RECEIVE_OFFER, SocketEvent.SUCCESS, 0, (JSONObject)args[0]).sendToTarget();
				break;
			case "answer":
				eventHandler.obtainMessage(SocketEvent.MSG_RECEIVE_ANSWER, SocketEvent.SUCCESS, 0, (JSONObject)args[0]).sendToTarget();
				break;
			default:
				eventHandler.sendEmptyMessage(SocketEvent.MSG_UNKNOWN);
				break;
			}
		}

		/**
		 * 서버 접속 성공 이벤트
		 */
		@Override
		public void onConnect() {
			Log.i(TAG, "onConnect");
			eventHandler.obtainMessage(SocketEvent.MSG_ATTACH_SERVER, SocketEvent.SUCCESS, 0).sendToTarget();
		}

		/**
		 * 서버 접속 해제 이벤트
		 */
		@Override
		public void onDisconnect() {
			Log.i(TAG, "onDisconnect");
			eventHandler.obtainMessage(SocketEvent.MSG_DETACH_SERVER, SocketEvent.SUCCESS, 0).sendToTarget();
		}

		/**
		 * 서버 접속 에러 이벤트
		 */
		@Override
		public void onError(SocketIOException e) {
			Log.i(TAG, "onError");
			eventHandler.obtainMessage(SocketEvent.MSG_ATTACH_SERVER, SocketEvent.FAILURE, 0).sendToTarget();
		}

		/**
		 * 발생 아니합니다.
		 */
		@Override
		public void onMessage(String event, IOAcknowledge ack) {
			Log.i(TAG, "onMessage");
		}

		/**
		 * 이것 또한 발생 아니합니다.
		 */
		@Override
		public void onMessage(JSONObject data, IOAcknowledge ack) {
			Log.i(TAG, "onMessage");
		}
	}

	/**
	 * 서버에서 보낸 emit 메시지를 이 함수에서 처리합니다.
	 * 
	 * @param event 이벤트 문자열
	 * @param data 이벤트 데이터, 서버와 클라 모두 JSONObject로 통일합니다.
	 */
	private void handleMessage(int event, JSONObject data) {
		try {
			int code = data.getInt("code");
			String message = data.getString("message");

			switch(code) {
			case SocketEvent.SUCCESS:
				eventHandler.obtainMessage(event, code).sendToTarget();
				break;
			case SocketEvent.FAILURE:
				eventHandler.obtainMessage(event, code, 0, message).sendToTarget();
				break;
			default:
				eventHandler.sendEmptyMessage(SocketEvent.MSG_UNKNOWN);
				break;
			}
		} catch(JSONException e) {
			eventHandler.obtainMessage(SocketEvent.MSG_UNKNOWN, e.getMessage()).sendToTarget();
		}
	}

	/**
	 * 이벤트를 처리하는 클래스입니다.
	 */
	private class EventHandler extends Handler {

		public EventHandler(Looper looper) {
			super(looper);
		}

		/**
		 * IOCallback에서 이벤트를 수신받으면 각 리스너의 함수를 호출합니다.
		 */
		@Override
		public void handleMessage(Message msg) {
			for(EventListener listener : listenerList) {
				listener.onListen(msg.what, msg.arg1, msg.obj);
			}
		}
	}
}