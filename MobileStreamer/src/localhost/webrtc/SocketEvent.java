package localhost.webrtc;

public class SocketEvent {

	public static final int SUCCESS = 0;

	public static final int FAILURE = -1;

	public static final int MSG_ATTACH_SERVER = 0;

	public static final int MSG_DETACH_SERVER = 1;

	public static final int MSG_SIGN_UP = 2;

	public static final int MSG_SIGN_IN = 3;

	public static final int MSG_SIGN_OUT = 4;

	public static final int MSG_CREATE_CHANNEL = 5;

	public static final int MSG_DELETE_CHANNEL = 6;

	public static final int MSG_ENTER_CHANNEL = 7;

	public static final int MSG_LEAVE_CHANNEL = 8;

	public static final int MSG_SEND_OFFER = 9;

	public static final int MSG_RECEIVE_OFFER = 10;

	public static final int MSG_SEND_ANSWER = 11;

	public static final int MSG_RECEIVE_ANSWER = 12;

	public static final int MSG_UNKNOWN = 99;

	private SocketEvent() {}
}