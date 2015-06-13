package localhost.activity;

import localhost.activity.util.HashUtils;
import localhost.mobilestreamer.R;
import localhost.webrtc.WebrtcThread;
import localhost.webrtc.WebrtcThread.OnSignInListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignInActivity extends Activity implements OnSignInListener, OnClickListener {

	private WebrtcThread mWebrtcThread;
	private EditText mEdtEmail;
	private EditText mEdtPassword;
	private Button mBtnSignIn;
	private Button mBtnNewAccount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);

		mWebrtcThread = WebrtcThread.getInstance();
		mWebrtcThread.setOnSignInListener(this);

		mEdtEmail = (EditText)findViewById(R.id.edtSignInEmail);
		mEdtPassword = (EditText)findViewById(R.id.edtSignInPassword);
		mBtnSignIn = (Button)findViewById(R.id.btnSignIn);
		mBtnSignIn.setOnClickListener(this);
		mBtnNewAccount = (Button)findViewById(R.id.btnNewAccount);
		mBtnNewAccount.setOnClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();

		if(!mWebrtcThread.isConnected()) {
			mWebrtcThread.attachSignalingServer();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if(mWebrtcThread.isConnected()) {
			mWebrtcThread.detachSignalingServer();
		}
	}

	@Override
	public void onSuccess(int event) {
		Toast.makeText(this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show();
		startActivity(new Intent(this, ShootingActivity.class));
		finish();
	}

	@Override
	public void onFailure(int event, String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btnSignIn:
			onClickBtnSignIn(v);
			break;
		case R.id.btnNewAccount:
			onClickBtnNewAccount(v);
			break;
		}
	}

	private void onClickBtnSignIn(View v) {
		String email = mEdtEmail.getText().toString().trim();
		String password = mEdtPassword.getText().toString().trim();

		if(email.length() == 0) {
			Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
			mEdtEmail.selectAll();
			return;
		}

		if(password.length() == 0) {
			Toast.makeText(this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
			mEdtPassword.selectAll();
			return;
		}

		password = HashUtils.md5("prefix" + password);
		mWebrtcThread.signIn(email, password);
	}

	private void onClickBtnNewAccount(View v) {
		startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
	}
}