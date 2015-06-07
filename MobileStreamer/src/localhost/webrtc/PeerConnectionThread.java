package localhost.webrtc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import localhost.Global;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaConstraints.KeyValuePair;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnection.IceServer;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class PeerConnectionThread {

	private static final String TAG = Global.TAG;
	private static final String CAMERA = "Camera 0, Facing back, Orientation 90";
	private PeerConnectionFactory mPeerConnectionFactory;
	private List<IceServer> mIceServerList;
	private MediaConstraints mMediaConstraints;
	private PeerConnectionHandler mPeerConnectionHandler;
	private MediaStream mMediaStream;
	private VideoCapturer mVideoCapturer;
	private AudioSource mAudioSource;
	private AudioTrack mAudioTrack;
	private VideoRenderer mVideoRenderer;
	private VideoRenderer.Callbacks mVideoRendererHandler;
	private VideoSource mVideoSource;
	private VideoTrack mVideoTrack;
	private Handler mHandler;
	private Context mContext;

	public enum PeerConnectionType {
		Offerer,
		Answerer
	}

	public PeerConnectionThread(Handler handler) {
		mHandler = handler;

		mIceServerList = new ArrayList<IceServer>();
		mIceServerList.add(new IceServer("stun:stun.l.google.com:19302"));

		mMediaConstraints = new MediaConstraints();
		mMediaConstraints.optional.add(new KeyValuePair("DtlsSrtpKeyAgreement", "true"));

		mPeerConnectionHandler = new PeerConnectionHandler();

		// TODO remove this, mPeerConnectionFactory = new PeerConnectionFactory();
		// TODO remove this, mPeerConnectionFactory.createPeerConnection(mIceServerList, mMediaConstraints, mPeerConnectionHandler);
	}

	// FIXME 여기는 여러 비디오가 들어갈 수 있도록 수정 요청
	public void initMediaStream() {
		long time = Calendar.getInstance().getTimeInMillis();

		mVideoCapturer = VideoCapturer.create(CAMERA);
		mMediaStream = mPeerConnectionFactory.createLocalMediaStream(String.format("%s%d", TAG, time));

		mAudioSource = mPeerConnectionFactory.createAudioSource(new MediaConstraints());
		mAudioTrack = mPeerConnectionFactory.createAudioTrack(String.format("%sa%d", TAG, time), mAudioSource);
		mMediaStream.addTrack(mAudioTrack);

		mVideoRendererHandler = VideoRendererGui.create(0, 0, 100, 100);
		mVideoRenderer = new VideoRenderer(mVideoRendererHandler);
		mVideoSource = mPeerConnectionFactory.createVideoSource(mVideoCapturer, new MediaConstraints());

		mVideoTrack = mPeerConnectionFactory.createVideoTrack(String.format("%sv%d", TAG, time), mVideoSource);
		mVideoTrack.addRenderer(mVideoRenderer);
		mMediaStream.addTrack(mVideoTrack);
	}

	public PeerConnection offer(PeerConnectionType type) {
		final PeerConnection conn = mPeerConnectionFactory.createPeerConnection(mIceServerList, mMediaConstraints, mPeerConnectionHandler);
		conn.addStream(mMediaStream, new MediaConstraints());
		conn.createOffer(new SdpObserver() {

			private SessionDescription mSessionDescription;

			@Override
			public void onCreateSuccess(SessionDescription sdp) {
				mSessionDescription = sdp;
				conn.setLocalDescription(this, sdp);
			}

			@Override
			public void onCreateFailure(String error) {
				Log.e(TAG, "why create sdp fail? cause: " + error);
			}

			@Override
			public void onSetSuccess() {
				Log.i(TAG, "onSetSuccess");
			}

			@Override
			public void onSetFailure(String error) {
				Log.e(TAG, "why set sdp fail? cause: " + error);
			}

		}, new MediaConstraints());

		switch(type) {
		case Offerer:
			break;
		case Answerer:
			break;
		}

		// TODO make handler
		//		OfferHandler handler = new OfferHandler();
		//		PeerConnectionObserver observer = new PeerConnectionObserver();
		//		PeerConnection connection = factory.createPeerConnection(new IceServers(), new SrtpMediaConstraints(), observer);
		//		connection.addStream(deviceCapturer.getMediaStream(), new MediaConstraints());
		//		observer.setPeerConnection(connection, handler);

		return conn;
	}

	private class EventHandler extends Handler {

		public EventHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {

			default:
				Log.e(TAG, "unknown message");
				break;
			}
		}
	}
}