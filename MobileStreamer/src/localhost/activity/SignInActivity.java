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

/**
 * 사인-인 액티비티는 사용자 인증을 처리합니다.
 * 
 * 스플래시 액티비티가 서버에 접속 성공 했을 때 시작합니다.
 */
public class SignInActivity extends Activity implements EventListener, OnClickListener {
	/**
	 * 소켓 스레드
	 */
	private SocketThread socketThread = SocketThread.getInstance();
	/**
	 * 이메일 입력 뷰
	 */
	private EditText edtEmail;
	/**
	 * 비밀번호 입력 뷰
	 */
	private EditText edtPassword;
	/**
	 * 사인-인 버튼 뷰
	 */
	private Button btnSignIn;
	/**
	 * 새로운 계정 생성 뷰
	 */
	private Button btnNewAccount;

	/**
	 * 액티비티를 이벤트 리스너로 등록하고 각종 뷰를 초기화 합니다.
	 * 
	 * 사인-인 버튼, 새 계정 버튼은 액티비티에 있는 버튼 클릭 함수를 리스너로 등록합니다.
	 */
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

	/**
	 * 사인-인 버튼, 새 계정 버튼을 눌렀을 때 호출합니다.
	 */
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

	/**
	 * 사인-인 버튼을 눌렀을 때 밸리데이션 체크를 실시하며 서버로 로그인 이벤트를 전송합니다.
	 */
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

		// 패스워드를 md5로 암호화합니다. 실제 인터넷에 md5 암호화 결과 테이블이 많이 존재하기 때문에
		// 접두사를 추가하여 예상 패스워드 공격을 방지합니다.
		password = HashUtils.md5("prefix" + password);

		// 사인-인 이벤트 발생
		socketThread.signIn(email, password);
	}

	/**
	 * 새 계정 버튼을 눌렀을 때 사인-온 액티비티로 이동합니다.
	 */
	private void onClickBtnNewAccount(View v) {
		startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
	}

	/**
	 * 서버에서 이벤트를 받았을 때 호출합니다.
	 * 
	 * 사인-인 이벤트를 처리합니다.
	 */
	@Override
	public void onListen(int event, int code, Object data) {
		switch(event) {
		case SocketEvent.MSG_SIGN_IN:
			onSignIn(code, data);
			break;
		}
	}

	/**
	 * 사인-인 이벤트를 처리합니다. 
	 * 
	 * @param code 이벤트 결과 코드
	 * @param data 이벤트 결과 데이터, 에러 발생 시 String값으로 에러 메시지를 받습니다.
	 */
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