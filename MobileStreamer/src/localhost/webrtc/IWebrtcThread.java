package localhost.webrtc;

/**
 * @author seok0721@gmail.com
 * 
 * IWebrtcHandler does manage connection to signaling server,
 * channel and peer connections. 
 */
public interface IWebrtcThread {

	public static final int MSG_SOCKET_ERROR = 0;
	public static final int MSG_CHANNEL_ERROR = 1;

	/**
	 * Connect to signaling server that uses node.js with socket.io.
	 */
	public void attachSignalingServer(final String url);

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
}