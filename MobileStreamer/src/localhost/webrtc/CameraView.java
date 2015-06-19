package localhost.webrtc;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class CameraView extends GLSurfaceView {

	private Point screenSize = new Point();

	public CameraView(Context context, AttributeSet attr) {
		super(context, attr);
	}

	public void initialize(Activity activity) {
		activity.getWindowManager().getDefaultDisplay().getRealSize(screenSize);
	}

	@Override
	public void onMeasure(int unusedX, int unusedY) {
		// setMeasuredDimension(screenSize.x, screenSize.y - 500);
		setMeasuredDimension(screenSize.x, screenSize.y);
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
	}
}