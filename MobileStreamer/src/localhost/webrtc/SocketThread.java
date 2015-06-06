package localhost.webrtc;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;

import localhost.Global;

import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

public class SocketThread {

	private static final String TAG = Global.TAG;
	private Handler mHandler;
	private SocketIO mSocket;
	private SocketHandler mSocketHandler;

	public SocketThread(final Handler handler) {
		mHandler = handler;
		mSocketHandler = new SocketHandler();
	}

	public void reset() {
		if(mSocket != null && mSocket.isConnected()) {
			mSocket.disconnect();
		}

		mSocket = null;
	}

	public void connect(final String url) {
		try {
			mSocket = new SocketIO(url, mSocketHandler);
		} catch (MalformedURLException e) {
			mHandler.obtainMessage(IWebrtcThread.MSG_SOCKET_ERROR, e.getMessage()).sendToTarget();
		}
	}

	public void disconnect() {
		mSocket.disconnect();
	}

	public boolean isConnected() {
		if(mSocket != null) {
			return mSocket.isConnected();
		}

		return false;
	}

	public void enterChannel(final String channel) {
		mSocket.emit("enterChannel", channel);
	}

	private class SocketHandler implements IOCallback {

		@Override
		public void on(String event, IOAcknowledge ack, Object... args) {
			Log.i(TAG, "on");
		}

		@Override
		public void onConnect() {
			Log.i(TAG, "onConnect");
		}

		@Override
		public void onDisconnect() {
			Log.i(TAG, "onDisconnect");
		}

		@Override
		public void onError(SocketIOException e) {
			mHandler.obtainMessage(IWebrtcThread.MSG_SOCKET_ERROR, e.getMessage()).sendToTarget();
		}

		@Override
		public void onMessage(String event, IOAcknowledge ack) {
			Log.i(TAG, "onMessage");
		}

		@Override
		public void onMessage(JSONObject event, IOAcknowledge ack) {
			Log.i(TAG, "onMessage");
		}
	}
}