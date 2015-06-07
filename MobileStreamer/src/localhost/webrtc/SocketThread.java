package localhost.webrtc;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;

import localhost.Global;

import org.json.JSONException;
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

	public void signUp(final String email, final String name,
			final String password, final String base64Image) {
		try {
			JSONObject data = new JSONObject();
			data.put("email", email);
			data.put("name", name);
			data.put("passwd", password);
			data.put("thumbnail", base64Image);

			mSocket.emit("signUp", data);
		} catch(JSONException e) {
			mHandler.obtainMessage(IWebrtcThread.MSG_FAILURE_SIGN_IN,
					"데이터를 보내는 도중 오류가 발생하였습니다.").sendToTarget();
		}
	}

	public void signIn(final String email, final String password) {
		try {
			JSONObject data = new JSONObject();
			data.put("email", email);
			data.put("passwd", password);

			mSocket.emit("signIn", data);
		} catch(JSONException e) {
			mHandler.obtainMessage(IWebrtcThread.MSG_FAILURE_SIGN_IN,
					"데이터를 보내는 도중 오류가 발생하였습니다.").sendToTarget();
		}
	}

	private void onSignIn(JSONObject data) {
		try {
			int code = data.getInt("code");
			String message = data.getString("message");

			switch(code) {
			case IWebrtcThread.RET_SUCCESS:
				mHandler.sendEmptyMessage(IWebrtcThread.MSG_SUCCESS_SIGN_IN);
				break;
			case IWebrtcThread.RET_FAILURE:
				mHandler.obtainMessage(IWebrtcThread.MSG_FAILURE_SIGN_IN, message).sendToTarget();
				break;
			default:
				mHandler.sendEmptyMessage(IWebrtcThread.MSG_UNKNOWN);
				break;
			}
		} catch(JSONException e) {
			mHandler.obtainMessage(IWebrtcThread.MSG_UNKNOWN, e.getMessage()).sendToTarget();
		}
	}

	private void onSignUp(JSONObject data) {
		try {
			int code = data.getInt("code");
			String message = data.getString("message");

			switch(code) {
			case IWebrtcThread.RET_SUCCESS:
				mHandler.sendEmptyMessage(IWebrtcThread.MSG_SUCCESS_SIGN_UP);
				break;
			case IWebrtcThread.RET_FAILURE:
				mHandler.obtainMessage(IWebrtcThread.MSG_FAILURE_SIGN_UP, message).sendToTarget();
				break;
			default:
				mHandler.sendEmptyMessage(IWebrtcThread.MSG_UNKNOWN);
				break;
			}
		} catch (JSONException e) {
			mHandler.obtainMessage(IWebrtcThread.MSG_UNKNOWN, e.getMessage()).sendToTarget();
		}
	}

	private void onSignOut(JSONObject data) {
		// TODO make me!
	}

	private class SocketHandler implements IOCallback {

		@Override
		public void on(String event, IOAcknowledge ack, Object... args) {
			switch(event) {
			case "signUp":
				onSignUp((JSONObject)args[0]);
				break;
			case "signIn":
				onSignIn((JSONObject)args[0]);
				break;
			case "signOut":
				onSignOut((JSONObject)args[0]);
				break;
			default:
				break;
			}
		}

		@Override
		public void onConnect() {
			mHandler.sendEmptyMessage(IWebrtcThread.MSG_SUCCESS_ATTACH_SERVER);
		}

		@Override
		public void onDisconnect() {
			Log.i(TAG, "onDisconnect");
		}

		@Override
		public void onError(SocketIOException e) {
			mHandler.obtainMessage(IWebrtcThread.MSG_FAILURE_ATTACH_SERVER, e.getMessage()).sendToTarget();
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