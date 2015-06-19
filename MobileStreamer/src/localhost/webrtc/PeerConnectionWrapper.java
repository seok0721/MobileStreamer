package localhost.webrtc;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoRenderer;
import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.PeerConnection.IceGatheringState;
import org.webrtc.PeerConnection.Observer;
import org.webrtc.PeerConnection.SignalingState;
import org.webrtc.SessionDescription.Type;

import android.util.Log;

public class PeerConnectionWrapper implements SdpObserver, Observer {

	private static final String TAG = PeerConnectionWrapper.class.getName();
	private SocketThread socketThread = SocketThread.getInstance();
	private PeerConnection connection;
	private PeerConnectionType type;
	private SessionDescription session;
	private String socketId;
	private VideoRenderer.Callbacks renderer;

	public void setConnection(PeerConnection connection) {
		this.connection = connection;
	}

	public PeerConnection getConnection() {
		return connection;
	}

	public PeerConnectionType getType() {
		return type;
	}

	public void setType(PeerConnectionType type) {
		this.type = type;
	}

	public VideoRenderer.Callbacks getRenderer() {
		return renderer;
	}

	public void setRenderer(VideoRenderer.Callbacks renderer) {
		this.renderer = renderer;
	}

	public SessionDescription getSession() {
		return session;
	}

	public void setSession(SessionDescription session) {
		Log.i("", session+"");
		Log.i("", session.type.canonicalForm());
		this.session = session;
	}

	public String getSocketId() {
		return socketId;
	}

	public void setSocketId(String socketId) {
		this.socketId = socketId;
	}

	@Override
	public void onCreateFailure(String error) {
		Log.i(TAG, "onCreateFailure");
	}

	@Override
	public void onCreateSuccess(SessionDescription session) {
		Log.i(TAG, "onCreateSuccess");
		this.session = session;
		switch(type) {
		case Offerer: // same same
			connection.setLocalDescription(this, session);
			break;
		case Answerer: // same same
			connection.setLocalDescription(this, session);
			break;
		}
	}

	@Override
	public void onSetFailure(String error) {
		Log.i(TAG, "onSetFailure");
	}

	@Override
	public void onSetSuccess() {
		Log.i(TAG, "onSetSuccess");
		Log.i(TAG, type.name());

		switch(type) {
		case Offerer:
			if(session.type == Type.OFFER) { // set local offer
				Log.i(TAG, "Offerer");
				Log.i(TAG, connection.getLocalDescription() + "");
				Log.i(TAG, connection.getRemoteDescription() + "");
				// TODO Error!!
				// socketThread.sendOffer(session.description);
			}
			// nothing after set remote offer
			break;
		case Answerer:
			if(session.type == Type.OFFER) {
				connection.createAnswer(this, new MediaConstraints());
			}
			break;
		}
	}

	@Override
	public void onAddStream(MediaStream media) {
		Log.i(TAG, "onAddStream, " + media.videoTracks.size());
		media.videoTracks.get(0).addRenderer(new VideoRenderer(renderer));
	}

	@Override
	public void onDataChannel(DataChannel arg0) {
		Log.i(TAG, "onDataChannel");
	}

	@Override
	public void onIceCandidate(IceCandidate iceCadidate) {
		Log.i(TAG, "onIceCandidate, " + iceCadidate.sdp);
		connection.addIceCandidate(iceCadidate);
	}

	@Override
	public void onIceConnectionChange(IceConnectionState state) {
		Log.i(TAG, "onIceConnectionChange, " + state.name());
	}

	@Override
	public void onIceGatheringChange(IceGatheringState state) {
		Log.i(TAG, "onIceGatheringChange, " + state.name());
		switch(type) {
		case Offerer:
			if(state == IceGatheringState.COMPLETE && connection.signalingState() == SignalingState.HAVE_LOCAL_OFFER) {
				socketThread.sendOffer(connection.getLocalDescription().description);
			}
			break;
		case Answerer:
			if(state == IceGatheringState.COMPLETE && connection.signalingState() == SignalingState.STABLE) {
				socketThread.sendAnswer(socketId, connection.getLocalDescription().description);
			}
			break;
		}
	}

	@Override
	public void onRemoveStream(MediaStream media) {
		Log.i(TAG, "onRemoveStream");
		connection.removeStream(media);
	}

	@Override
	public void onRenegotiationNeeded() {
		Log.i(TAG, "onRenegotiationNeeded");
	}

	@Override
	public void onSignalingChange(SignalingState state) {
		Log.i(TAG, "onSignalingChange, " + state.name());
		/*
		switch(type) {
		case Answerer:
			if(state == SignalingState.STABLE) {
				socketThread.sendAnswer(socketId, connection.getLocalDescription().description);
			}
			break;
		case Offerer:
			break;
		}
		 */
	}
}