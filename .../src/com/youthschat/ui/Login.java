/**
 * wechatdonal
 */
package com.youthschat.ui;

import java.util.ArrayList;
import java.util.Map;

import tools.AppManager;
import tools.ImageUtils;
import tools.UIHelper;

import com.youthschat.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.youthschat.bean.UserEntity;
import com.youthschat.config.ApiClent;
import com.youthschat.config.AppActivity;
import com.youthschat.config.ApiClent.ClientCallback;
import com.youthschat.ui.adapter.TextAdapter;
import com.youthschat.utils.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Spinner;

/**
 * wechat
 *
 * @author donal
 *
 */
public class Login extends AppActivity{
	
	private ProgressDialog loadingPd;
	private InputMethodManager imm;
	private EditText accountET;
	private EditText passwordET;
	private Spinner spinner;
	private Map<String, String> countryCodeMap;
	
	@Override
	public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);
	}

	  @Override
	public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  
	}
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		initUI();
	}
	
	private void initUI() {
		final ListView xlistView = (ListView) findViewById(R.id.xlistview);
		View mHeaderView = getLayoutInflater().inflate(R.layout.login_header, null);
		RelativeLayout layout = (RelativeLayout) mHeaderView.findViewById(R.id.layout);
		xlistView.addHeaderView(mHeaderView);
		LayoutParams p = (LayoutParams) layout.getLayoutParams();
		p.height = ImageUtils.getDisplayHeighth(this)-40;
		layout.setLayoutParams(p);
		xlistView.setAdapter(new TextAdapter(this, new ArrayList<String>()));
		xlistView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scollState) {
				if (scollState == SCROLL_STATE_TOUCH_SCROLL) {
					imm.hideSoftInputFromWindow(xlistView.getWindowToken(), 0);
				}
			}
			
			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
				
			}
		});
		TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		accountET = (EditText) mHeaderView.findViewById(R.id.editTextAccount);
		accountET.setText(tMgr.getLine1Number());
		passwordET = (EditText) mHeaderView.findViewById(R.id.editTextPassword);
		spinner = (Spinner) findViewById(R.id.country_spinner);
		countryCodeMap = Utils.populateCountries(this, spinner);
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.registerButton:
			register();
			break;

		case R.id.loginButton:
			imm.hideSoftInputFromWindow(passwordET.getWindowToken(), 0);
			login();
			break;
			
		case R.id.forgotPasswordButton:
			forgotPassword();
			break;
		}
			
	}
	
	private void register() {
		Intent intent = new Intent(Login.this, Register1.class);
		startActivity(intent);
	}
	
	private void forgotPassword() {
		Intent intent = new Intent(Login.this, ForgotPassword.class);
		startActivity(intent);
	}
	
	private void login() {
		// Account = country + phone number
		String account = Utils.formatPhone(countryCodeMap.get(spinner.getSelectedItem().toString()) + accountET.getText().toString());
		final String password = passwordET.getText().toString();
		if (account.length() == 0 ||  password.length() ==0) {
			showToast("Please enter the phone number and password");
		}
		else {
			loadingPd = UIHelper.showProgress(this, null, null, true);
			ApiClent.login(appContext, account, password, new ClientCallback() {
				@Override
				public void onSuccess(Object data) {
					UserEntity user = (UserEntity) data;
					if (user.status == 1) {
						appContext.saveLoginInfo(user);
						appContext.saveLoginPassword(password);
						saveLoginConfig(appContext.getLoginInfo());
						Intent intent = new Intent(Login.this, Tabbar.class);
						startActivity(intent);
						AppManager.getAppManager().finishActivity(Login.this);
						UIHelper.dismissProgress(loadingPd);
					}
				}
				
				@Override
				public void onFailure(String message) {
					UIHelper.dismissProgress(loadingPd);
				}
				
				@Override
				public void onError(Exception e) {
					UIHelper.dismissProgress(loadingPd);
				}
			});
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
//		case CommonValue.LoginAndRegister.RegisterSuccess:
//			setResult(RESULT_OK);
//			AppManager.getAppManager().finishActivity(Login.this);
//			break;
		default:
			break;
		}
	}
}
