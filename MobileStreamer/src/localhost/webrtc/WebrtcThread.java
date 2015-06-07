package localhost.webrtc;

import localhost.Global;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class WebrtcThread implements IWebrtcThread {

	public interface OnConnectListener {
		void onSuccess();
		void onFailure(String message);
	}

	public interface OnSignUpListener {
		void onSuccess();
		void onFailure(String message);
	}

	public interface OnSignInListener {
		void onSuccess();
		void onFailure(String message);
	}

	private static final String TAG = Global.TAG;
	private static WebrtcThread mInstance;
	private OnConnectListener mOnConnectListener;
	private OnSignUpListener mOnSignUpListener;
	private OnSignInListener mOnSignInListener;
	private HandlerThread mEventThread;
	private EventHandler mEventHandler;
	private SocketThread mSocketThread;
	private PeerConnectionThread mPeerConnectionThread;
	private Context mContext;
	private Object mListenerLock;
	private String mHost;

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

	@Override
	public void attachSignalingServer() {
		if(mSocketThread.isConnected()) {
			Toast.makeText(mContext, "Already socket is connected.", Toast.LENGTH_SHORT).show();
			return;
		}

		mSocketThread.connect(mHost);
	}

	@Override
	public void detachSignalingServer() {
		if(!mSocketThread.isConnected()) {
			Toast.makeText(mContext, "Socket is not connected.", Toast.LENGTH_SHORT).show();
			return;
		}

		mSocketThread.disconnect();
	}

	@Override
	public void enterChannel(final String name) {
		mSocketThread.enterChannel(name);
	}

	@Override
	public void leaveChannel() {

	}

	@Override
	public void createChannel(String name) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deleteChannel(String name) {
		// TODO Auto-generated method stub
	}

	@Override
	public void signUp(String email, String name, String password, String base64Image) {
		mSocketThread.signUp(email, name, password, base64Image);
	}

	@Override
	public void signIn(String email, String password) {
		mSocketThread.signIn(email, password);
	}

	private WebrtcThread() {
		mListenerLock = new Object();
		mOnConnectListener = null;
		mOnSignUpListener = null;
		mOnSignInListener = null;
		mEventThread = new HandlerThread(getClass().getName());
		mEventThread.start();
		mEventHandler = new EventHandler(mEventThread.getLooper());
		mSocketThread = new SocketThread(mEventHandler);
		mPeerConnectionThread = new PeerConnectionThread(mEventHandler);
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
					mOnConnectListener.onSuccess();
				}
				break;
			case MSG_FAILURE_ATTACH_SERVER:
				if(mOnConnectListener != null) {
					mOnConnectListener.onFailure((String)msg.obj);
				}
				break;
			case MSG_SUCCESS_SIGN_UP:
				if(mOnSignUpListener != null) {
					mOnSignUpListener.onSuccess();
				}
				break;
			case MSG_FAILURE_SIGN_UP:
				if(mOnSignUpListener != null) {
					mOnSignUpListener.onFailure((String)msg.obj);
				}
				break;
			case MSG_SUCCESS_SIGN_IN:
				if(mOnSignInListener != null) {
					mOnSignInListener.onSuccess();
				}
				break;
			case MSG_FAILURE_SIGN_IN:
				if(mOnSignInListener != null) {
					mOnSignInListener.onFailure((String)msg.obj);
				}
				break;
			default:
				Log.w(TAG, "unknown message");
				break;
			}
		}
	}
}