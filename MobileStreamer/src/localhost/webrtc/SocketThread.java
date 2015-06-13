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
	private static final SocketThread instance = new SocketThread();
	private Handler mHandler;
	private SocketIO mSocket;
	private SocketHandler mSocketHandler;
	private String mEmail;

	public static final SocketThread getInstance() {
		return instance;
	}
	
	public SocketThread() {
		
	}
	
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

	public boolean isConnected() {
		if(mSocket != null) {
			return mSocket.isConnected();
		}

		return false;
	}

	public void attachServer(final String url) {
		try {
			mSocket = new SocketIO(url, mSocketHandler);
		} catch (MalformedURLException e) {
			mHandler.obtainMessage(WebrtcThread.MSG_SOCKET_ERROR, e.getMessage()).sendToTarget();
		}
	}

	public void detachServer() {
		mSocket.disconnect();
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
			mHandler.obtainMessage(WebrtcThread.MSG_FAILURE_SIGN_IN,
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
			mHandler.obtainMessage(WebrtcThread.MSG_FAILURE_SIGN_IN,
					"데이터를 보내는 도중 오류가 발생하였습니다.").sendToTarget();
		}
	}

	public void createChannel() {
		mSocket.emit("createChannel");
	}

	public void deleteChannel() {
		mSocket.emit("deleteChannel");
	}

	public void enterChannel(final String email) {
		try {
			JSONObject data = new JSONObject();
			data.put("email", email);

			mSocket.emit("enterChannel", data);
		} catch(JSONException e) {
			mHandler.obtainMessage(WebrtcThread.MSG_FAILURE_SIGN_IN,
					"데이터를 보내는 도중 오류가 발생하였습니다.").sendToTarget();
		}
	}

	public void leaveChannel() {
		mSocket.emit("leaveChannel");
	}

	public void sendOffer(final String sdp) {
		try {
			JSONObject data = new JSONObject();
			data.put("sdp", sdp);

			mSocket.emit("offer", data);
		} catch(JSONException e) {
			mHandler.obtainMessage(WebrtcThread.MSG_FAILURE_SIGN_IN,
					"데이터를 보내는 도중 오류가 발생하였습니다.").sendToTarget();
		}
	}

	public void sendAnswer(final String socketId, final String sdp) {
		try {
			JSONObject data = new JSONObject();
			data.put("socketId", socketId);
			data.put("sdp", sdp);

			mSocket.emit("answer", data);
		} catch(JSONException e) {
			mHandler.obtainMessage(WebrtcThread.MSG_FAILURE_SIGN_IN,
					"데이터를 보내는 도중 오류가 발생하였습니다.").sendToTarget();
		}
	}

	private void onSignIn(JSONObject data) {
		try {
			int code = data.getInt("code");
			String message = data.getString("message");

			switch(code) {
			case WebrtcThread.RET_SUCCESS:
				mEmail = data.getString("email");
				mHandler.sendEmptyMessage(WebrtcThread.MSG_SUCCESS_SIGN_IN);
				break;
			case WebrtcThread.RET_FAILURE:
				mHandler.obtainMessage(WebrtcThread.MSG_FAILURE_SIGN_IN, message).sendToTarget();
				break;
			default:
				mHandler.sendEmptyMessage(WebrtcThread.MSG_UNKNOWN);
				break;
			}
		} catch(JSONException e) {
			mHandler.obtainMessage(WebrtcThread.MSG_UNKNOWN, e.getMessage()).sendToTarget();
		}
	}

	private void onSignUp(JSONObject data) {
		try {
			int code = data.getInt("code");
			String message = data.getString("message");

			switch(code) {
			case WebrtcThread.RET_SUCCESS:
				mHandler.sendEmptyMessage(WebrtcThread.MSG_SUCCESS_SIGN_UP);
				break;
			case WebrtcThread.RET_FAILURE:
				mHandler.obtainMessage(WebrtcThread.MSG_FAILURE_SIGN_UP, message).sendToTarget();
				break;
			default:
				mHandler.sendEmptyMessage(WebrtcThread.MSG_UNKNOWN);
				break;
			}
		} catch (JSONException e) {
			mHandler.obtainMessage(WebrtcThread.MSG_UNKNOWN, e.getMessage()).sendToTarget();
		}
	}

	private void onSignOut(JSONObject data) {
		// TODO make me!
	}

	private void onCreateChannel(JSONObject data) {
		try {
			int code = data.getInt("code");
			String message = data.getString("message");

			switch(code) {
			case WebrtcThread.RET_SUCCESS:
				mHandler.sendEmptyMessage(WebrtcThread.MSG_SUCCESS_CREATE_CHANNEL);
				break;
			case WebrtcThread.RET_FAILURE:
				mHandler.obtainMessage(WebrtcThread.MSG_FAILURE_CREATE_CHANNEL, message).sendToTarget();
				break;
			default:
				mHandler.sendEmptyMessage(WebrtcThread.MSG_UNKNOWN);
				break;
			}
		} catch(JSONException e) {
			mHandler.obtainMessage(WebrtcThread.MSG_UNKNOWN, e.getMessage()).sendToTarget();
		}
	}

	private void onDeleteChannel(JSONObject data) {
		// TODO make me!
	}

	private void onEnterChannel(JSONObject data) {
		try {
			int code = data.getInt("code");
			String message = data.getString("message");

			switch(code) {
			case WebrtcThread.RET_SUCCESS:
				mHandler.sendEmptyMessage(WebrtcThread.MSG_SUCCESS_ENTER_CHANNEL);
				break;
			case WebrtcThread.RET_FAILURE:
				mHandler.obtainMessage(WebrtcThread.MSG_FAILURE_ENTER_CHANNEL, message).sendToTarget();
				break;
			default:
				mHandler.sendEmptyMessage(WebrtcThread.MSG_UNKNOWN);
				break;
			}
		} catch (Exception e) {
			mHandler.obtainMessage(WebrtcThread.MSG_UNKNOWN, e.getMessage()).sendToTarget();
		}
	}

	private void onLeaveChannel(JSONObject data) {
		// TODO make me!
	}

	private void onReceiveOffer(JSONObject data) {
		try {
			String socketId = data.getString("socketId");
			String sdp = data.getString("sdp");

			WebrtcThread.getInstance().receiveOffer(socketId, sdp);
		} catch (Exception e) {
			mHandler.obtainMessage(WebrtcThread.MSG_UNKNOWN, e.getMessage()).sendToTarget();
		}
	}

	private void onReceiveAnswer(JSONObject data) {
		try {
			String sdp = data.getString("sdp");

			WebrtcThread.getInstance().receiveAnswer(sdp);
		} catch (Exception e) {
			mHandler.obtainMessage(WebrtcThread.MSG_UNKNOWN, e.getMessage()).sendToTarget();
		}
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
			case "createChannel":
				onCreateChannel((JSONObject)args[0]);
				break;
			case "deleteChannel":
				onDeleteChannel((JSONObject)args[0]);
				break;
			case "enterChannel":
				onEnterChannel((JSONObject)args[0]);
				break;
			case "leaveChannel":
				onLeaveChannel((JSONObject)args[0]);
				break;
			case "offer":
				onReceiveOffer((JSONObject)args[0]);
				break;
			case "answer":
				onReceiveAnswer((JSONObject)args[0]);
				break;
			default:
				break;
			}
		}

		@Override
		public void onConnect() {
			Log.i(TAG, "onConnect");
			mHandler.sendEmptyMessage(WebrtcThread.MSG_SUCCESS_ATTACH_SERVER);
		}

		@Override
		public void onDisconnect() {
			Log.i(TAG, "onDisconnect");
		}

		@Override
		public void onError(SocketIOException e) {
			Log.i(TAG, "onError");
			mHandler.obtainMessage(WebrtcThread.MSG_FAILURE_ATTACH_SERVER, e.getMessage()).sendToTarget();
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