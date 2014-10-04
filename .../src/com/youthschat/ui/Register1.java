package com.youthschat.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import com.youthschat.R;
import com.youthschat.config.AppActivity;
import com.youthschat.config.CommonValue;
import com.youthschat.utils.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import tools.AppManager;
import tools.StringUtils;
import tools.UIHelper;

public class Register1 extends AppActivity{
	private EditText mobileET;
	private TextView mobileTV;
//	private CountDown cd;
	boolean canVertify ;
	int leftSeconds;
	private ProgressDialog loadingPd;
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
		setContentView(R.layout.register1);
		initUI();
	}
	
	private void initUI() {
		TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		Button rightBarButton = (Button) findViewById(R.id.rightBarButton);
		accretionArea(rightBarButton);
		String phoneNumber = tMgr.getLine1Number();
		mobileET = (EditText) findViewById(R.id.editTextPhone);
		mobileTV = (TextView) findViewById(R.id.phoneText);
		if (phoneNumber == null || "".equals(phoneNumber.trim())) {
			mobileTV.setText("It seems that we have problem to get the phone number from your device. Please make sure the SIM card is inserted.");
			rightBarButton.setEnabled(false);
			rightBarButton.setVisibility(View.INVISIBLE);
		} else {
			mobileET.setText(phoneNumber);
			mobileET.setEnabled(false);
			spinner = (Spinner) findViewById(R.id.country_spinner);
			countryCodeMap = Utils.populateCountries(this, spinner);
		}
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.rightBarButton:
			this.getVertifyCode();
			break;
		}
	}

	private void getVertifyCode() {
//		if (StringUtils.isMobileNO(mobileET.getText().toString())) {
//			step2();
//		}
//		else {
//			showToast("请输入正确的手机号码");
//		}
		// TODO(larry): Validate phone number. 
		step2();
	}
	
	private void step2() {
		Intent intent = new Intent(Register1.this, Register2.class);
		intent.putExtra("mobile",
				Utils.formatPhone(
						countryCodeMap.get(spinner.getSelectedItem().toString()) + mobileET.getText().toString()));
		startActivityForResult(intent, CommonValue.REQUEST_REGISTER_INFO);
	}
}
