package localhost.webrtc;

public class SocketEvent {
	/**
	 * 이벤트 처리 성공
	 */
	public static final int SUCCESS = 0;
	/**
	 * 이벤트 처리 실패
	 */
	public static final int FAILURE = -1;
	/**
	 * 서버 접속 이벤트
	 */
	public static final int MSG_ATTACH_SERVER = 0;
	/**
	 * 서버 접속 해제 이벤트
	 */
	public static final int MSG_DETACH_SERVER = 1;
	/**
	 * 회원 가입 이벤트
	 */
	public static final int MSG_SIGN_UP = 2;
	/**
	 * 로그인 이벤트
	 */
	public static final int MSG_SIGN_IN = 3;
	/**
	 * 로그아웃 이벤트
	 */
	public static final int MSG_SIGN_OUT = 4;
	/**
	 * 채널 생성 이벤트
	 */
	public static final int MSG_CREATE_CHANNEL = 5;
	/**
	 * 채널 삭제 이벤트
	 */
	public static final int MSG_DELETE_CHANNEL = 6;
	/**
	 * 채널 입장 이벤트
	 */
	public static final int MSG_ENTER_CHANNEL = 7;
	/**
	 * 채널 탈퇴 이벤트
	 */
	public static final int MSG_LEAVE_CHANNEL = 8;
	/**
	 * 요청 전송 이벤트
	 */
	public static final int MSG_SEND_OFFER = 9;
	/**
	 * 요청 수신 이벤트
	 */
	public static final int MSG_RECEIVE_OFFER = 10;
	/**
	 * 응답 전송 이벤트
	 */
	public static final int MSG_SEND_ANSWER = 11;
	/**
	 * 응답 수신 이벤트
	 */
	public static final int MSG_RECEIVE_ANSWER = 12;
	/**
	 * 알 수 없는 이벤트
	 */
	public static final int MSG_UNKNOWN = 99;

	private SocketEvent() {}
}