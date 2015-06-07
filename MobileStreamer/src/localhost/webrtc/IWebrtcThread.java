package localhost.webrtc;

/**
 * @author seok0721@gmail.com
 * 
 * IWebrtcHandler does manage connection to signaling server,
 * channel and peer connections. 
 */
public interface IWebrtcThread {

	public static final int RET_SUCCESS = 0;
	public static final int RET_FAILURE = -1;

	public static final int MSG_SOCKET_ERROR = 0;
	public static final int MSG_UNKNOWN = 0;
	public static final int MSG_CHANNEL_ERROR = 1;
	public static final int MSG_SUCCESS_SIGN_UP = 2;
	public static final int MSG_FAILURE_SIGN_UP = 3;
	public static final int MSG_SUCCESS_SIGN_IN = 4;
	public static final int MSG_FAILURE_SIGN_IN = 5;
	public static final int MSG_SUCCESS_ATTACH_SERVER = 6;
	public static final int MSG_FAILURE_ATTACH_SERVER = 7;

	/**
	 * Connect to signaling server that uses node.js with socket.io.
	 */
	public void attachSignalingServer();

	/**
	 * Disconnect from signaling server.
	 */
	public void detachSignalingServer();

	/**
	 * Enter the channel specific name.
	 */
	public void enterChannel(final String name);

	/**
	 * Leave the current channel.
	 */
	public void leaveChannel();

	/**
	 * Create new channel.
	 */
	public void createChannel(final String name);

	/**
	 * Destroy current channel if user is host.
	 */
	public void deleteChannel(final String name);

	public void signUp(final String email, final String name,
			final String password, final String base64Image);

	public void signIn(final String email, final String password);
}