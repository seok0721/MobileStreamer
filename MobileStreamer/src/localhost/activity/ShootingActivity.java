package localhost.activity;

import java.util.ArrayList;
import java.util.List;

import localhost.mobilestreamer.R;
import localhost.webrtc.CameraView;
import localhost.webrtc.SocketEvent;
import localhost.webrtc.SocketThread;
import localhost.webrtc.SocketThread.EventListener;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaConstraints.KeyValuePair;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection.IceServer;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoRendererGui.ScalingType;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ShootingActivity extends Activity implements OnClickListener, EventListener{

	private static final Object TAG = null;
	private SocketThread socketThread = SocketThread.getInstance();
	private EditText edtChannel;
	private CameraView mCameraView;
	private Button btnEnterChannel;
	private Button btnCreateChannel;
	private PeerConnectionFactory factory;
	private MediaStream mLocalStream;
	private AudioSource mAudioSource;
	private AudioTrack mAudioTrack;
	private VideoRenderer mVideoRenderer;
	private VideoCapturer mVideoCapturer;
	private VideoSource mVideoSource;
	private VideoTrack mVideoTrack;
	private VideoRenderer.Callbacks[] renderers = new VideoRenderer.Callbacks[4];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shooting);

		edtChannel = (EditText) findViewById(R.id.edtChannel);
		mCameraView = (CameraView)findViewById(R.id.surfaceView);
		btnEnterChannel = (Button) findViewById(R.id.btnEnterChannel);
		btnEnterChannel.setOnClickListener(this);
		btnCreateChannel = (Button) findViewById(R.id.btnCreateChannel);
		btnCreateChannel.setOnClickListener(this);

		PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true, VideoRendererGui.getEGLContext());
		factory = new PeerConnectionFactory();
		mLocalStream = factory.createLocalMediaStream(String.format("%s", TAG));

		// make audio track
		mAudioSource = factory.createAudioSource(new MediaConstraints());
		mAudioTrack = factory.createAudioTrack(String.format("%sa", TAG), mAudioSource);

		// make video track
		mVideoCapturer = VideoCapturer.create("Camera 1, Facing front, Orientation 270");
		mVideoSource = factory.createVideoSource(mVideoCapturer, new MediaConstraints());
		mVideoTrack = factory.createVideoTrack(String.format("%sv", TAG), mVideoSource);

		// attach video renderer
		VideoRendererGui.setView(mCameraView, new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			}
		});
		mCameraView.initialize(this);

		renderers[0] = VideoRendererGui.create(2, 2, 47, 40, ScalingType.SCALE_FILL, true);
		renderers[1] = VideoRendererGui.create(2, 44, 47, 40, ScalingType.SCALE_FILL, true);
		renderers[2] = VideoRendererGui.create(51, 2, 47, 40, ScalingType.SCALE_FILL, true);
		renderers[3] = VideoRendererGui.create(51, 44, 47, 40, ScalingType.SCALE_FILL, true);

		mVideoTrack.addRenderer(new VideoRenderer(renderers[0]));

		// add track to local stream
		mLocalStream.addTrack(mAudioTrack);
		mLocalStream.addTrack(mVideoTrack);

		List<IceServer> iceServerList = new ArrayList<IceServer>();
		iceServerList.add(new IceServer("stun:stun.l.google.com:19302"));

		MediaConstraints mediaConstraints = new MediaConstraints();
		mediaConstraints.optional.add(new KeyValuePair("DtlsSrtpKeyAgreement", "true"));	

		//		mWrappers = new PeerConnectionWrapper[4];
		//		PeerConnectionFactory.initializeAndroidGlobals(this, true, true);
		//		PeerConnectionFactory factory = new PeerConnectionFactory();
		//		PeerConnectionBuilder builder = new PeerConnectionBuilder()
		//		.setFactory(factory)
		//		.setIceServers(iceServerList)
		//		.setMediaConstraints(mediaConstraints)
		//		.setType(PeerConnectionType.OFFERER);

		socketThread.addListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btnEnterChannel:
			onClickBtnEnterChannel(v);
			break;
		case R.id.btnCreateChannel:
			onClickBtnCreateChannel(v);
			break;
		}
	}

	private void onClickBtnEnterChannel(View v) {
		String channel = edtChannel.getText().toString().trim();

		if(channel.length() == 0) {
			Toast.makeText(this, "채널을 입력하세요.", Toast.LENGTH_SHORT).show();
			return;
		}

		socketThread.enterChannel(channel);
	}

	private void onClickBtnCreateChannel(View v) {
		socketThread.createChannel();
	}

	@Override
	public void onListen(int event, int code, Object data) {
		switch(event) {
		case SocketEvent.MSG_CREATE_CHANNEL:
			onCreateChannel(code, data);
			break;
		case SocketEvent.MSG_ENTER_CHANNEL:
			onEnterChannel(code, data);
			break;
		}
	}

	private void onCreateChannel(int code, Object data) {
		switch(code) {
		case SocketEvent.SUCCESS:
			Toast.makeText(this, "채널을 생성하였습니다.", Toast.LENGTH_SHORT).show();
			break;
		case SocketEvent.FAILURE:
			Toast.makeText(this, (String)data, Toast.LENGTH_SHORT).show();
			break;
		}
	}

	private void onEnterChannel(int code, Object data) {
		switch(code) {
		case SocketEvent.SUCCESS:
			Toast.makeText(this, "방에 접속하였습니다.", Toast.LENGTH_SHORT).show();
			// TODO send offer
		case SocketEvent.FAILURE:
			Toast.makeText(this, (String)data, Toast.LENGTH_SHORT).show();
			break;
		}
	}
}