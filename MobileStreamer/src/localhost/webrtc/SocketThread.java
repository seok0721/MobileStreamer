package localhost.webrtc;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class SocketThread {

	public interface EventListener {
		public void onListen(int event, int code, Object data);
	}

	private static final String TAG = SocketThread.class.getName();
	private static SocketThread instance;
	private List<EventListener> listenerList = new LinkedList<EventListener>();
	private HandlerThread eventThread;
	private EventHandler eventHandler;
	private SocketIO socket;

	public static SocketThread getInstance() {
		if(instance == null) {
			instance  = new SocketThread();
		}

		return instance;
	}

	public void addListener(EventListener listener) {
		listenerList.add(listener);
	}

	public void removeListener(EventListener listener) {
		listenerList.remove(listener);
	}

	public void attachServer(final String url) {
		try {
			socket = new SocketIO(url, new SocketHandler());
		} catch (MalformedURLException e) {
			eventHandler.obtainMessage(SocketEvent.MSG_ATTACH_SERVER, SocketEvent.FAILURE, 0, e.getMessage()).sendToTarget();
		}
	}

	public void detachServer() {
		socket.disconnect();
	}

	public void signUp(final String email, final String name, final String password, final String base64Image) {
		try {
			JSONObject data = new JSONObject();
			data.put("email", email);
			data.put("name", name);
			data.put("passwd", password);
			data.put("thumbnail", base64Image);

			socket.emit("signUp", data);
		} catch(JSONException e) {
			eventHandler.obtainMessage(SocketEvent.MSG_SIGN_UP, SocketEvent.FAILURE, 0, e.getMessage()).sendToTarget();
		}
	}

	public void signIn(final String email, final String password) {
		try {
			JSONObject data = new JSONObject();
			data.put("email", email);
			data.put("passwd", password);

			socket.emit("signIn", data);
		} catch(JSONException e) {
			eventHandler.obtainMessage(SocketEvent.MSG_SIGN_IN, SocketEvent.FAILURE, 0, e.getMessage()).sendToTarget();
		}
	}

	public void createChannel() {
		socket.emit("createChannel");
	}

	public void deleteChannel() {
		socket.emit("deleteChannel");
	}

	public void enterChannel(final String email) {
		try {
			JSONObject data = new JSONObject();
			data.put("email", email);

			socket.emit("enterChannel", data);
		} catch(JSONException e) {
			eventHandler.obtainMessage(SocketEvent.MSG_ENTER_CHANNEL, SocketEvent.FAILURE, 0, e.getMessage()).sendToTarget();
		}
	}

	public void leaveChannel() {
		socket.emit("leaveChannel");
	}

	public void sendOffer(final String sdp) {
		try {
			JSONObject data = new JSONObject();
			data.put("sdp", sdp);

			socket.emit("offer", data);
		} catch(JSONException e) {
			eventHandler.obtainMessage(SocketEvent.MSG_SEND_OFFER, SocketEvent.FAILURE, 0, e.getMessage()).sendToTarget();
		}
	}

	public void sendAnswer(final String socketId, final String sdp) {
		try {
			JSONObject data = new JSONObject();
			data.put("socketId", socketId);
			data.put("sdp", sdp);

			socket.emit("answer", data);
		} catch(JSONException e) {
			eventHandler.obtainMessage(SocketEvent.MSG_SEND_ANSWER, SocketEvent.FAILURE, 0, e.getMessage()).sendToTarget();
		}
	}

	private SocketThread() {
		eventThread = new HandlerThread(TAG);
		eventThread.start();
		eventHandler = new EventHandler(eventThread.getLooper());
	}

	private class SocketHandler implements IOCallback {

		@Override
		public void on(String event, IOAcknowledge ack, Object... args) {
			switch(event) {
			case "signUp":
				handleMessage(SocketEvent.MSG_SIGN_UP, (JSONObject)args[0]);
				break;
			case "signIn":
				handleMessage(SocketEvent.MSG_SIGN_IN, (JSONObject)args[0]);
				break;
			case "signOut":
				handleMessage(SocketEvent.MSG_SIGN_OUT, (JSONObject)args[0]);
				break;
			case "createChannel":
				handleMessage(SocketEvent.MSG_CREATE_CHANNEL, (JSONObject)args[0]);
				break;
			case "deleteChannel":
				handleMessage(SocketEvent.MSG_DELETE_CHANNEL, (JSONObject)args[0]);
				break;
			case "enterChannel":
				handleMessage(SocketEvent.MSG_ENTER_CHANNEL, (JSONObject)args[0]);
				break;
			case "leaveChannel":
				handleMessage(SocketEvent.MSG_LEAVE_CHANNEL, (JSONObject)args[0]);
				break;
			case "receiveOffer":
				eventHandler.obtainMessage(SocketEvent.MSG_SEND_OFFER, SocketEvent.SUCCESS, 0, (JSONObject)args[0]).sendToTarget();
				break;
			case "receiveAnswer":
				eventHandler.obtainMessage(SocketEvent.MSG_SEND_OFFER, SocketEvent.SUCCESS, 0, (JSONObject)args[0]).sendToTarget();
				break;
			default:
				eventHandler.sendEmptyMessage(SocketEvent.MSG_UNKNOWN);
				break;
			}
		}

		@Override
		public void onConnect() {
			eventHandler.obtainMessage(SocketEvent.MSG_ATTACH_SERVER, SocketEvent.SUCCESS).sendToTarget();
		}

		@Override
		public void onDisconnect() {
			eventHandler.obtainMessage(SocketEvent.MSG_DETACH_SERVER, SocketEvent.SUCCESS).sendToTarget();
		}

		@Override
		public void onError(SocketIOException e) {
			eventHandler.obtainMessage(SocketEvent.MSG_ATTACH_SERVER, SocketEvent.FAILURE).sendToTarget();
		}

		@Override
		public void onMessage(String event, IOAcknowledge ack) {
			Log.i(TAG, "onMessage");
		}

		@Override
		public void onMessage(JSONObject data, IOAcknowledge ack) {
			Log.i(TAG, "onMessage");
		}
	}

	private void handleMessage(int event, JSONObject data) {
		try {
			int code = data.getInt("code");
			String message = data.getString("message");

			switch(code) {
			case SocketEvent.SUCCESS:
				eventHandler.obtainMessage(event, code).sendToTarget();
				break;
			case SocketEvent.FAILURE:
				eventHandler.obtainMessage(event, code, 0, message).sendToTarget();
				break;
			default:
				eventHandler.sendEmptyMessage(SocketEvent.MSG_UNKNOWN);
				break;
			}
		} catch(JSONException e) {
			eventHandler.obtainMessage(SocketEvent.MSG_UNKNOWN, e.getMessage()).sendToTarget();
		}
	}

	private class EventHandler extends Handler {

		public EventHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			for(EventListener listener : listenerList) {
				listener.onListen(msg.what, msg.arg1, msg.obj);
			}
		}
	}
}