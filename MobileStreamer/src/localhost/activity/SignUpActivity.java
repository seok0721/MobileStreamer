package localhost.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import localhost.activity.util.HashUtils;
import localhost.mobilestreamer.R;
import localhost.webrtc.WebrtcThread;
import localhost.webrtc.WebrtcThread.OnSignUpListener;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignUpActivity extends Activity implements OnClickListener, OnSignUpListener {

	private WebrtcThread mWebrtcThread;
	private EditText edtEmail;
	private EditText edtName;
	private EditText edtPassword;
	private EditText edtPasswordRepeat;
	private Button btnSignUp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);

		mWebrtcThread = WebrtcThread.getInstance();
		mWebrtcThread.setOnSignUpListener(this);

		edtEmail = (EditText)findViewById(R.id.edtSignUpEmail);
		edtName = (EditText)findViewById(R.id.edtSignUpName);
		edtPassword = (EditText)findViewById(R.id.edtSignUpPassword);
		edtPasswordRepeat = (EditText)findViewById(R.id.edtSignUpPasswordRepeat);
		btnSignUp = (Button)findViewById(R.id.btnSignUp);
		btnSignUp.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btnSignUp:
			onClickBtnSignUp(v);
			break;
		}
	}

	private void onClickBtnSignUp(View v) {
		String email = edtEmail.getText().toString().trim();
		String name = edtName.getText().toString().trim();
		String password = edtPassword.getText().toString().trim();
		String passwordRepeat = edtPasswordRepeat.getText().toString().trim();

		if(email.length() == 0) {
			Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
			edtEmail.selectAll();
			return;
		}

		if(name.length() == 0) {
			Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show();
			edtName.selectAll();
			return;
		}

		if(password.length() == 0) {
			Toast.makeText(this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
			edtPassword.selectAll();
			return;
		}

		if(passwordRepeat.length() == 0) {
			Toast.makeText(this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
			edtPasswordRepeat.selectAll();
			return;
		}

		if(!password.equals(passwordRepeat)) {
			Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
			edtPassword.selectAll();
			return;
		}

		try {
			name = URLEncoder.encode(name, "utf-8");
			password = HashUtils.md5("prefix" + password);
			mWebrtcThread.signUp(email, name, password, null); // TODO add thumbnail image
		} catch (UnsupportedEncodingException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onSuccess(int event) {
		Toast.makeText(this, "회원 가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
		finish();
	}

	@Override
	public void onFailure(int event, String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}


}