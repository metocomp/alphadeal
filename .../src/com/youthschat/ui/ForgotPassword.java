package com.youthschat.ui;

import java.util.Map;

import com.youthschat.R;
import com.youthschat.bean.StrangerEntity;
import com.youthschat.config.ApiClent;
import com.youthschat.config.AppActivity;
import com.youthschat.config.CommonValue;
import com.youthschat.config.ApiClent.ClientCallback;
import com.youthschat.utils.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils.StringSplitter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import tools.AppManager;
import tools.StringUtils;
import tools.UIHelper;

public class ForgotPassword extends AppActivity{
	private EditText mobileET;
	private EditText emailET;
	private Button sendButton;
	private Button alreadyHaveCodeButton;
	private Spinner spinner;
	private Map<String, String> countryCodeMap;
	
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
		setContentView(R.layout.retrieve_password);
		initUI();
	}
	
	private void initUI() {
		mobileET = (EditText) findViewById(R.id.editTextPhone);
		emailET = (EditText) findViewById(R.id.editTextEmail);
		sendButton = (Button) findViewById(R.id.sendCodeButton);
		alreadyHaveCodeButton = (Button) findViewById(R.id.alreadyHaveCodeButton);
		spinner = (Spinner) findViewById(R.id.country_spinner);
		countryCodeMap = Utils.populateCountries(this, spinner);
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.sendCodeButton:
			sendCode(mobileET.getText().toString(), emailET.getText().toString());
			break;
		case R.id.alreadyHaveCodeButton:
			if (mobileET.getText().toString().isEmpty()) {
				showToast("Please enter the phone number");
				break;
			}
			if (!Utils.isValidEmail(emailET.getText().toString())) {
				showToast("Please enter a valid email");
				break;
			}
			// Account = country + phone number.
			retrievePassword(Utils.formatPhone(
					countryCodeMap.get(spinner.getSelectedItem().toString()) + mobileET.getText().toString()),
							emailET.getText().toString());
			break;
		}
	}
	
	private void retrievePassword(String phone, String email) {
		Intent intent = new Intent(ForgotPassword.this, RetrievePassword.class);
		intent.putExtra("phone", phone);
		intent.putExtra("email", email);
		startActivity(intent);
	}
	
	private void sendCode(final String phone, final String email) {
		if (!Utils.isValidEmail(email)) {
			showToast("Please enter a valid email");
		}
		if (phone.isEmpty()) {
			showToast("Please enter a phone number");
		}
		final String apiKey = appContext.getLoginApiKey();
		ApiClent.sendCode(appContext, apiKey, phone, email, new ClientCallback() {
			@Override
			public void onSuccess(Object data) {
				final StrangerEntity entity = (StrangerEntity)data;
				switch (entity.status) {
				case 1:
					retrievePassword(phone, email);
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
