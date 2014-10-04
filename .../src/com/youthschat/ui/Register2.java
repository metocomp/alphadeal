package com.youthschat.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.CookieStore;

import bean.Entity;
import bean.Result;

import com.youthschat.R;
import com.google.gson.Gson;
import com.loopj.android.http.PersistentCookieStore;
import com.youthschat.bean.UserEntity;
import com.youthschat.config.ApiClent;
import com.youthschat.config.AppActivity;
import com.youthschat.config.ApiClent.ClientCallback;
import com.youthschat.utils.Utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import tools.AppException;
import tools.AppManager;
import tools.BaseActivity;
import tools.DecodeUtil;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;

public class Register2 extends AppActivity{
	private EditText passET;
	private EditText nicknameET;
	private EditText repeatPassET;
	private EditText emailET;
	private EditText emailRepeatET;
	private ProgressDialog loadingPd;
	private String mobile;
	private InputMethodManager imm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register2);
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		initUI();
		initData();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void initUI() {
		Button leftBarButton = (Button) findViewById(R.id.leftBarButton);
		accretionArea(leftBarButton);
		Button rightBarButton = (Button) findViewById(R.id.rightBarButton);
		accretionArea(rightBarButton);
		
		nicknameET = (EditText) findViewById(R.id.editTextNickName); 
		repeatPassET = (EditText) findViewById(R.id.editTextRepeatPass);
		passET = (EditText) findViewById(R.id.editTextPass);
		emailET = (EditText) findViewById(R.id.editTextEmail);
		emailRepeatET = (EditText) findViewById(R.id.editTextEmailRepeat);
//		introET = (EditText) findViewById(R.id.editTextIntro); 
	}
	
	private void initData() {
		mobile = getIntent().getStringExtra("mobile");
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.rightBarButton:
			imm.hideSoftInputFromWindow(nicknameET.getWindowToken(), 0);
			vertifiedInfo();
			break;
		}
	}
	
	private void vertifiedInfo() {
		String pass = passET.getText().toString();
		String nickname = nicknameET.getText().toString();
		String repeatPass = repeatPassET.getText().toString();
		String email = emailET.getText().toString();
		String emailRepeat = emailRepeatET.getText().toString();
		if (StringUtils.empty(pass)) {
			showToast("Please enter password");
			return;
		}
		if (!pass.equals(repeatPass)) {
			showToast("Passwords didn't match");
			return;
		}
		if (StringUtils.empty(nickname)) {
			showToast("Please enter a nick name");
			return;
		}
		if (StringUtils.empty(email)) {
			showToast("Please enter an email, this used for retrieving password if needed");
			return;
		}
		if (!Utils.isValidEmail(email)) {
			showToast("Please enter a valid email address");
			return;
		}
		if (StringUtils.empty(emailRepeat)) {
			showToast("Please repeat the email");
			return;
		}
		if (!email.equals(emailRepeat)) {
			showToast("Emails didn't match");
			return;
		}
		
//		if (StringUtils.empty(nickname)) {
//			showToast("请输入介绍");
//			return;
//		}
		commitInfo(pass, nickname, "" /* referrer */, email);
	}
	
	private void commitInfo(final String pass, String nickname, String intro, String email) {
		loadingPd = UIHelper.showProgress(this, null, null, true);
		ApiClent.register(appContext, mobile, pass, nickname, intro, "", email, new ClientCallback() {
			
			@Override
			public void onSuccess(Object data) {
				Entity entity = (Entity) data;
				switch (entity.getError_code()) {
				case 1:
					showToast(entity.getMessage());
					enterIndex();
					break;
				default:
					showToast(entity.getMessage());
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
	
	private void enterIndex() {
		setResult(RESULT_OK);
		AppManager.getAppManager().finishActivity(this);
	}
	
	
}
