package com.youthschat.ui;

import com.youthschat.R;
import com.youthschat.bean.StrangerEntity;
import com.youthschat.config.ApiClent;
import com.youthschat.config.AppActivity;
import com.youthschat.config.CommonValue;
import com.youthschat.config.ApiClent.ClientCallback;
import com.youthschat.utils.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import tools.AppManager;

public class RetrievePassword extends AppActivity{
	private EditText codeET;
	private EditText newPassET;
	private EditText repeatPassET;
	private Button verifyCodeButton;
	private Button sendCodeAgainButton;
	private String phone;
	private String email;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case CommonValue.REQUEST_REGISTER_INFO:
			AppManager.getAppManager().finishActivity(this);
			break;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.retrieve_password2);
		initUI();
	}
	
	private void initUI() {
		phone = getIntent().getStringExtra("phone");
		email = getIntent().getStringExtra("email");
		codeET = (EditText) findViewById(R.id.editTextCode);
		newPassET = (EditText) findViewById(R.id.editTextNewPassword);
		repeatPassET = (EditText) findViewById(R.id.editTextRepeatNewPassword);
		verifyCodeButton = (Button) findViewById(R.id.verifyCodeButton);
		sendCodeAgainButton = (Button) findViewById(R.id.sendCodeAgainButton);
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.verifyCodeButton:
			if (codeET.getText().toString().isEmpty()) {
				showToast("Please enter the verification code");
			}
			if (newPassET.getText().toString().isEmpty()) {
				showToast("Please enter the new password");
			}
			if (repeatPassET.getText().toString().isEmpty()) {
				showToast("Please repeat password");
			}
			if (!newPassET.getText().toString().equals(repeatPassET.getText().toString())) {
				showToast("Passwords doesn't match each other");
			}
			resetPass(phone, email, newPassET.getText().toString());
			break;
		case R.id.sendCodeAgainButton:
			sendCode(phone, email);
			break;
		}
	}
	
	private void resetPass(final String phone, final String email, final String password) {
		if (phone == null || email == null) {
			Intent intent = new Intent(RetrievePassword.this, ForgotPassword.class);
			intent.putExtra("phone", phone);
			intent.putExtra("email", email);
			startActivity(intent);
		}
		final String apiKey = appContext.getLoginApiKey();
		ApiClent.resetPassword(appContext, apiKey, phone, email, newPassET.getText().toString(), codeET.getText().toString(),
				new ClientCallback() {
			@Override
			public void onSuccess(Object data) {
				final StrangerEntity entity = (StrangerEntity)data;
				switch (entity.status) {
				case 1:
					Intent intent = new Intent(RetrievePassword.this, Login.class);
					startActivity(intent);
					showToast("New password has been set");
					break;
				default:
					showToast(entity.msg);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
				showToast(message);
			}
			
			@Override
			public void onError(Exception e) {
			}
		});
	}
	
	private void sendCode(final String phone, final String email) {
		if (phone == null || email == null) {
			Intent intent = new Intent(RetrievePassword.this, ForgotPassword.class);
			intent.putExtra("phone", phone);
			intent.putExtra("email", email);
			startActivity(intent);
		}
		final String apiKey = appContext.getLoginApiKey();
		ApiClent.sendCode(appContext, apiKey, phone, email, new ClientCallback() {
			@Override
			public void onSuccess(Object data) {
				final StrangerEntity entity = (StrangerEntity)data;
				switch (entity.status) {
				case 1:
					Button button = (Button) findViewById(R.id.sendCodeAgainButton);
					button.setClickable(false);
					showToast("Please check your email for the code");
					break;
				default:
					showToast(entity.msg);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
				showToast(message);
			}
			
			@Override
			public void onError(Exception e) {
			}
		});
	}
}
