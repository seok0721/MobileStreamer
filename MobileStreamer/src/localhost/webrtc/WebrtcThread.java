package localhost.webrtc;

import java.util.ArrayList;
import java.util.List;

import localhost.Global;
import localhost.webrtc.PeerConnectionBuilder.PeerConnectionWrapper;

import org.webrtc.MediaConstraints;
import org.webrtc.MediaConstraints.KeyValuePair;
import org.webrtc.PeerConnection.IceServer;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SessionDescription.Type;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class WebrtcThread {

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
	public static final int MSG_SUCCESS_CREATE_CHANNEL = 8;
	public static final int MSG_FAILURE_CREATE_CHANNEL = 9;
	public static final int MSG_SUCCESS_ENTER_CHANNEL = 10;
	public static final int MSG_FAILURE_ENTER_CHANNEL = 11;

	public static final int MSG_OFFER = 12;

	public static final int NOTIFY_SIGN_UP = 1;
	public static final int NOTIFY_SIGN_IN = 2;
	public static final int NOTIFY_CONNECT = 3;
	public static final int NOTIFY_CREATE_CHANNEL = 4;
	public static final int NOTIFY_ENTER_CHANNEL = 5;

	public interface OnEnterChannelListener {
		void onSuccess(int event);
		void onFailure(int event, String message);
	}

	public interface OnCreateChannelListener {
		void onSuccess(int event);
		void onFailure(int event, String message);
	}

	public interface OnConnectListener {
		void onSuccess(int event);
		void onFailure(int event, String message);
	}

	public interface OnSignUpListener {
		void onSuccess(int event);
		void onFailure(int event, String message);
	}

	public interface OnSignInListener {
		void onSuccess(int event);
		void onFailure(int event, String message);
	}

	private static final String TAG = Global.TAG;
	private static WebrtcThread mInstance;
	private OnConnectListener mOnConnectListener;
	private OnSignUpListener mOnSignUpListener;
	private OnSignInListener mOnSignInListener;
	private OnCreateChannelListener mOnCreateChannelListener;
	private OnEnterChannelListener mOnEnterChannelListener;
	private HandlerThread mEventThread;
	private EventHandler mEventHandler;
	private SocketThread mSocketThread;
	private Context mContext;
	private Object mListenerLock;
	private String mHost;
	private PeerConnectionFactory mFactory;
	private PeerConnectionWrapper[] mPeerConnectionList;
	private PeerConnectionBuilder mBuilder;

	private VideoRenderer.Callbacks mRenderer1;
	private VideoRenderer.Callbacks mRenderer2;
	private VideoRenderer.Callbacks mRenderer3;
	private VideoRenderer.Callbacks mRenderer4;

	public static WebrtcThread getInstance() {
		if(mInstance == null) {
			mInstance = new WebrtcThread();
		}

		return mInstance;
	}

	public void setHost(String host) {
		mHost = host;
	}

	public void setContext(Context context) {
		mContext = context;
	}

	public void setOnCreateChannelListener(OnCreateChannelListener listener) {
		synchronized(mListenerLock) {
			mOnCreateChannelListener = listener;
		}
	}

	public void setOnEnterChannelListener(OnEnterChannelListener listener) {
		synchronized(mListenerLock) {
			mOnEnterChannelListener = listener;
		}
	}

	public void setOnConnectListener(OnConnectListener listener) {
		synchronized(mListenerLock) {
			mOnConnectListener = listener;
		}
	}

	public void setOnSignUpListener(OnSignUpListener listener) {
		synchronized(mListenerLock) {
			mOnSignUpListener = listener;
		}
	}

	public void setOnSignInListener(OnSignInListener listener) {
		synchronized(mListenerLock) {
			mOnSignInListener = listener;
		}
	}

	public boolean isConnected() {
		return mSocketThread.isConnected();
	}

	public void attachSignalingServer() {
		if(mSocketThread.isConnected()) {
			Toast.makeText(mContext, "Already socket is connected.", Toast.LENGTH_SHORT).show();
			return;
		}

		mSocketThread.attachServer(mHost);
	}

	public void detachSignalingServer() {
		if(!mSocketThread.isConnected()) {
			Toast.makeText(mContext, "Socket is not connected.", Toast.LENGTH_SHORT).show();
			return;
		}

		mSocketThread.detachServer();
	}

	public void signUp(String email, String name, String password, String base64Image) {
		mSocketThread.signUp(email, name, password, base64Image);
	}

	public void signIn(String email, String password) {
		mSocketThread.signIn(email, password);
	}

	public void createChannel() {
		mSocketThread.createChannel();
	}

	public void deleteChannel() {
		mSocketThread.deleteChannel();
	}

	public void enterChannel(final String email) {
		mSocketThread.enterChannel(email);
	}

	public void leaveChannel() {
		mSocketThread.leaveChannel();
	}

	public void sendOffer() {
		mBuilder.setType(PeerConnectionType.OFFERER);

		for(int i = 0; i < mPeerConnectionList.length; i++) {
			if(mPeerConnectionList[i] == null) {
				PeerConnectionWrapper wrapper = mBuilder.build();
				wrapper.createOffer();
				return;
			}
		}
	}

	public void sendAnswer() {
		mBuilder.setType(PeerConnectionType.OFFERER);

		for(int i = 0; i < mPeerConnectionList.length; i++) {
			PeerConnectionWrapper wrapper = mPeerConnectionList[i];

			if(wrapper != null && wrapper.hasLocalOffer()) {
				wrapper.createAnswer();
				return;
			}
		}
	}

	public void receiveOffer(String socketId, String sdp) {
		mBuilder.setType(PeerConnectionType.ANSWERER);

		for(int i = 0; i < mPeerConnectionList.length; i++) {
			if(mPeerConnectionList[i] == null) {
				PeerConnectionWrapper handler = mBuilder.build();
				handler.setRemoteOffer(socketId, new SessionDescription(Type.OFFER, sdp));
				return;
			}
		}
	}

	public void receiveAnswer(String sdp) {
		for(int i = 0; i < mPeerConnectionList.length; i++) {
			PeerConnectionWrapper wrapper = mPeerConnectionList[i];

			if(wrapper.hasLocalOffer()) {
				wrapper.setRemoteAnswer(new SessionDescription(Type.ANSWER, sdp));
			}
		}
	}

	public void init(Context context, GLSurfaceView view) {
		mFactory = new PeerConnectionFactory();
		mBuilder.setFactory(mFactory);

		mRenderer1 = VideoRendererGui.create(0, 0, 150, 200);
		//mRenderer2 = VideoRendererGui.create(0, 154, 150, 200);
		//mRenderer3 = VideoRendererGui.create(204, 0, 150, 200);
		//mRenderer4 = VideoRendererGui.create(204, 154, 150, 200);
	}

	private WebrtcThread() {
		mListenerLock = new Object();
		mOnConnectListener = null;
		mOnSignUpListener = null;
		mOnSignInListener = null;
		mOnCreateChannelListener = null;
		mPeerConnectionList = new PeerConnectionWrapper[4];

		mEventThread = new HandlerThread(getClass().getName());
		mEventThread.start();
		mEventHandler = new EventHandler(mEventThread.getLooper());

		mSocketThread = new SocketThread(mEventHandler);

		List<IceServer> mIceServerList = new ArrayList<IceServer>();
		mIceServerList.add(new IceServer("stun:stun.l.google.com:19302"));

		MediaConstraints mMediaConstraints = new MediaConstraints();
		mMediaConstraints.optional.add(new KeyValuePair("DtlsSrtpKeyAgreement", "true"));

		mBuilder = new PeerConnectionBuilder();
		mBuilder.setIceServers(mIceServerList);
		mBuilder.setMediaConstraints(mMediaConstraints);
	}

	private class EventHandler extends Handler {

		public EventHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_SUCCESS_ATTACH_SERVER:
				if(mOnConnectListener != null) {
					mOnConnectListener.onSuccess(NOTIFY_CONNECT);
				}
				break;
			case MSG_FAILURE_ATTACH_SERVER:
				if(mOnConnectListener != null) {
					mOnConnectListener.onFailure(NOTIFY_CONNECT, (String)msg.obj);
				}
				break;
			case MSG_SUCCESS_SIGN_UP:
				if(mOnSignUpListener != null) {
					mOnSignUpListener.onSuccess(NOTIFY_SIGN_UP);
				}
				break;
			case MSG_FAILURE_SIGN_UP:
				if(mOnSignUpListener != null) {
					mOnSignUpListener.onFailure(NOTIFY_SIGN_UP, (String)msg.obj);
				}
				break;
			case MSG_SUCCESS_SIGN_IN:
				if(mOnSignInListener != null) {
					mOnSignInListener.onSuccess(NOTIFY_SIGN_IN);
				}
				break;
			case MSG_FAILURE_SIGN_IN:
				if(mOnSignInListener != null) {
					mOnSignInListener.onFailure(NOTIFY_SIGN_IN, (String)msg.obj);
				}
				break;
			case MSG_SUCCESS_CREATE_CHANNEL:
				if(mOnCreateChannelListener != null) {
					mOnCreateChannelListener.onSuccess(NOTIFY_CREATE_CHANNEL);
				}
				break;
			case MSG_FAILURE_CREATE_CHANNEL:
				if(mOnCreateChannelListener != null) {
					mOnCreateChannelListener.onFailure(NOTIFY_CREATE_CHANNEL, (String)msg.obj);
				}
				break;
			case MSG_SUCCESS_ENTER_CHANNEL:
				if(mOnEnterChannelListener != null) {
					mOnEnterChannelListener.onSuccess(NOTIFY_ENTER_CHANNEL);
				}
				break;
			case MSG_FAILURE_ENTER_CHANNEL:
				if(mOnEnterChannelListener != null) {
					mOnEnterChannelListener.onFailure(NOTIFY_ENTER_CHANNEL, (String)msg.obj);
				}
				break;
			default:
				Log.w(TAG, "unknown message");
				break;
			}
		}
	}
}