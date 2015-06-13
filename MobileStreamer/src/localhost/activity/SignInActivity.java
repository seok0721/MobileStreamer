package localhost.activity;

import localhost.activity.util.HashUtils;
import localhost.mobilestreamer.R;
import localhost.webrtc.SocketEvent;
import localhost.webrtc.SocketThread;
import localhost.webrtc.SocketThread.EventListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignInActivity extends Activity implements EventListener, OnClickListener {

	private SocketThread socketThread = SocketThread.getInstance();
	private EditText edtEmail;
	private EditText edtPassword;
	private Button btnSignIn;
	private Button btnNewAccount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);

		socketThread.addListener(this);

		edtEmail = (EditText)findViewById(R.id.edtSignInEmail);
		edtPassword = (EditText)findViewById(R.id.edtSignInPassword);
		btnSignIn = (Button)findViewById(R.id.btnSignIn);
		btnSignIn.setOnClickListener(this);
		btnNewAccount = (Button)findViewById(R.id.btnNewAccount);
		btnNewAccount.setOnClickListener(this);
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

	@Override
	public void onListen(int event, int code, Object data) {
		switch(event) {
		case SocketEvent.MSG_SIGN_IN:
			onSignIn(code, data);
			break;
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
		socketThread.signIn(email, password);
	}

	private void onClickBtnNewAccount(View v) {
		startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
	}

	private void onSignIn(int code, Object data) {
		switch(code) {
		case SocketEvent.SUCCESS:
			Toast.makeText(this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(this, ShootingActivity.class));
			finish();
			break;
		case SocketEvent.FAILURE:
			Toast.makeText(this, (String)data, Toast.LENGTH_SHORT).show();
			break;
		}
	}
}