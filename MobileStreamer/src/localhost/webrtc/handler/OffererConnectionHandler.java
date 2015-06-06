package localhost.webrtc.handler;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.PeerConnection.IceGatheringState;
import org.webrtc.PeerConnection.Observer;
import org.webrtc.PeerConnection.SignalingState;

import android.util.Log;

public class OffererConnectionHandler implements Observer {

	private static final String TAG = OffererConnectionHandler.class.getName();

	@Override
	public void onAddStream(MediaStream arg0) {
		Log.i(TAG, "onAddStream");
	}

	@Override
	public void onDataChannel(DataChannel arg0) {
		Log.i(TAG, "onDataChannel");
	}

	@Override
	public void onError() {
		Log.i(TAG, "onError");
	}

	@Override
	public void onIceCandidate(IceCandidate arg0) {
		Log.i(TAG, "onIceCandidate");
	}

	@Override
	public void onIceConnectionChange(IceConnectionState arg0) {
		Log.i(TAG, "onIceConnectionChange");
	}

	@Override
	public void onIceGatheringChange(IceGatheringState arg0) {
		Log.i(TAG, "onIceGatheringChange");
	}

	@Override
	public void onRemoveStream(MediaStream arg0) {
		Log.i(TAG, "onRemoveStream");
	}

	@Override
	public void onRenegotiationNeeded() {
		Log.i(TAG, "onRenegotiationNeeded");
	}

	@Override
	public void onSignalingChange(SignalingState arg0) {
		Log.i(TAG, "onSignalingChange");
	}
}