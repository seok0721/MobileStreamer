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

	private static final String TAG = Global.TAG;
	private HandlerThread mEventThread;
	private EventHandler mEventHandler;
	private SocketThread mSocketThread;
	private PeerConnectionThread mPeerConnectionThread;
	private Context mContext;

	public WebrtcThread(Context context) {
		mContext = context;
		mEventThread = new HandlerThread(this.getClass().getName());
		mEventThread.start();
		mEventHandler = new EventHandler(mEventThread.getLooper());
		mSocketThread = new SocketThread(mEventHandler);
		mPeerConnectionThread = new PeerConnectionThread(mEventHandler);
	}

	@Override
	public void attachSignalingServer(final String url) {
		if(mSocketThread.isConnected()) {
			Toast.makeText(mContext, "Already socket is connected.", Toast.LENGTH_SHORT).show();
			return;
		}

		mSocketThread.connect(url);
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

	private void onError(String message) {
		Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
	}

	private class EventHandler extends Handler {

		public EventHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_SOCKET_ERROR:
				onError((String)msg.obj);
				break;
			default:
				Log.w(TAG, "unknown message");
				break;
			}
		}
	}
}