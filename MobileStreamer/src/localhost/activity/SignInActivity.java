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
	private EditText edtEmail;
	private EditText edtPassword;
	private Button btnSignIn;
	private Button btnNewAccount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);

		mWebrtcThread = WebrtcThread.getInstance();
		mWebrtcThread.setOnSignInListener(this);

		edtEmail = (EditText)findViewById(R.id.edtSignInEmail);
		edtPassword = (EditText)findViewById(R.id.edtSignInPassword);
		btnSignIn = (Button)findViewById(R.id.btnSignIn);
		btnSignIn.setOnClickListener(this);
		btnNewAccount = (Button)findViewById(R.id.btnNewAccount);
		btnNewAccount.setOnClickListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		//		if(!mWebrtcThread.isConnected()) {
		//			mWebrtcThread.attachSignalingServer();x
		//		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		//		if(mWebrtcThread.isConnected()) {
		//			mWebrtcThread.detachSignalingServer();
		//		}
	}

	@Override
	public void onSuccess() {
		Toast.makeText(this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show();
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}

	@Override
	public void onFailure(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		finish();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btnSignIn:
			onClickBtnSignIn(v);
			break;
		case R.id.btnNewAccount:
			onClickBtnNewAccount(v);
		}
	}

	private void onClickBtnSignIn(View v) {
		String email = edtEmail.getText().toString().trim();
		String password = edtPassword.getText().toString().trim();

		if(email.length() == 0) {
			Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
			edtEmail.selectAll();
			return;
		}

		if(password.length() == 0) {
			Toast.makeText(this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
			edtPassword.selectAll();
			return;
		}

		password = HashUtils.md5("prefix" + password);
		mWebrtcThread.signIn(email, password);
	}

	private void onClickBtnNewAccount(View v) {
		startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
	}
}