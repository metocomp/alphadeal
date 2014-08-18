/**
 * wechatdonal
 */
package com.youthschat.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.XMPPConnection;

import tools.AppManager;
import tools.ImageUtils;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.youthschat.R;
import com.youthschat.bean.KeyValue;
import com.youthschat.bean.UserEntity;
import com.youthschat.bean.UserInfo;
import com.youthschat.config.ApiClent;
import com.youthschat.config.AppActivity;
import com.youthschat.config.CommonValue;
import com.youthschat.config.FriendManager;
import com.youthschat.config.MessageManager;
import com.youthschat.config.NoticeManager;
import com.youthschat.config.XmppConnectionManager;
import com.youthschat.config.ApiClent.ClientCallback;
import com.youthschat.im.Chating;
import com.youthschat.ui.adapter.FieldAdapter;
import com.youthschat.utils.Utils;

/**
 * wechat
 *
 * @author donal
 *
 */
public class Me extends AppActivity{
	
	private static final String NICK = "Nick Name";
	private static final String DESCRIPTION = "Signature";
	private static final String EMAIL = "Email";
	
	private ImageView avatarView;
	private TextView nameTV;
	private ListView iphoneTreeView;
	private List<KeyValue> datas = new ArrayList<KeyValue>();
	private FieldAdapter fieldAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wechat);
		initUI();
	}
	
	private void initUI() {
		LayoutInflater inflater = LayoutInflater.from(this);
		iphoneTreeView = (ListView) findViewById(R.id.xlistview);
		View header = inflater.inflate(R.layout.more_headerview, null);
		avatarView = (ImageView) header.findViewById(R.id.avatar);
		nameTV = (TextView) header.findViewById(R.id.title);
		imageLoader.displayImage(CommonValue.BASE_URL+appContext.getLoginUserHead(), avatarView, CommonValue.DisplayOptions.avatar_options);
		iphoneTreeView.addHeaderView(header);
		View footer = inflater.inflate(R.layout.me_footer, null);
		iphoneTreeView.addFooterView(footer);
		fieldAdapter = new FieldAdapter(datas, this);
		iphoneTreeView.setAdapter(fieldAdapter);
		setInfo();
		initNickDialog();
		initDescDialog();
		initEmailDialog();
		iphoneTreeView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				if (position == 1) {
					textDialogNickName.show();
				}
				else if (position == 2) {
					textDialogDescription.show();
				}
				else if (position == 3) {
					textDialogEmail.show();
				}
			}
		});
	}
	
	private void setInfo() {
		UserEntity user = appContext.getLoginInfo();
		datas.add(new KeyValue(NICK, user.userInfo.nickName));
		datas.add(new KeyValue(DESCRIPTION, user.userInfo.description));
		datas.add(new KeyValue(EMAIL, user.userInfo.userEmail));
		fieldAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onBackPressed() {
		isExit();
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.exit:
			XMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
			if (connection.isConnected()) {
				connection.disconnect();
			}
			stopService();
			MessageManager.destroy();
			NoticeManager.destroy();
			FriendManager.destroy();
			appContext.setUserLogout();
			AppManager.getAppManager().finishAllActivity();
			startActivity(new Intent(this, Login.class));
			break;

		default:
			PhotoChooseOption();
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		String newPhotoPath;
		switch (requestCode) {
		case ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA:
			if (StringUtils.notEmpty(theLarge)) {
				File file = new File(theLarge);
				File dir = new File( ImageUtils.CACHE_IMAGE_FILE_PATH);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				String imagePathAfterCompass = ImageUtils.CACHE_IMAGE_FILE_PATH + file.getName();
				try {
					ExifInterface sourceExif = new ExifInterface(theLarge);
					String orientation = sourceExif.getAttribute(ExifInterface.TAG_ORIENTATION);
					ImageUtils.saveImageToSD(imagePathAfterCompass, ImageUtils.getSmallBitmap(theLarge, 200), 80);
					ExifInterface exif = new ExifInterface(imagePathAfterCompass);
					exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
				    exif.saveAttributes();
					newPhotoPath = imagePathAfterCompass;
					uploadPhotoService(newPhotoPath);
				} catch (IOException e) {
					Crashlytics.logException(e);
				}
			}
			break;
		case ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD:
			if(data == null)  return;
			Uri thisUri = data.getData();
        	String thePath = ImageUtils.getAbsolutePathFromNoStandardUri(thisUri);
        	if(StringUtils.empty(thePath)) {
        		newPhotoPath = ImageUtils.getAbsoluteImagePath(this,thisUri);
        	}
        	else {
        		newPhotoPath = thePath;
        	}
        	File file = new File(newPhotoPath);
			File dir = new File( ImageUtils.CACHE_IMAGE_FILE_PATH);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String imagePathAfterCompass = ImageUtils.CACHE_IMAGE_FILE_PATH + file.getName();
			try {
				ExifInterface sourceExif = new ExifInterface(newPhotoPath);
				String orientation = sourceExif.getAttribute(ExifInterface.TAG_ORIENTATION);
				ImageUtils.saveImageToSD(imagePathAfterCompass, ImageUtils.getSmallBitmap(newPhotoPath, 200), 80);
				ExifInterface exif = new ExifInterface(imagePathAfterCompass);
				exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
			    exif.saveAttributes();
				newPhotoPath = imagePathAfterCompass;
				uploadPhotoService(newPhotoPath);
			} catch (IOException e) {
//				Crashlytics.logException(e);
			}
			break;
		}
	}
	
	private String theLarge;
	private void PhotoChooseOption() {
		closeInput();
		CharSequence[] item = {"From Gallery", "Take Photo"};
		AlertDialog imageDialog = new AlertDialog.Builder(this).setTitle(null).setIcon(android.R.drawable.btn_star).setItems(item,
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int item)
					{
						//手机选图
						if( item == 0 )
						{
							Intent intent = new Intent(Intent.ACTION_PICK,
									Media.EXTERNAL_CONTENT_URI);
							startActivityForResult(Intent.createChooser(intent, "Select Photo"),ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD); 
						}
						//拍照
						else if( item == 1 )
						{	
							String savePath = "";
							//判断是否挂载了SD卡
							String storageState = Environment.getExternalStorageState();		
							if(storageState.equals(Environment.MEDIA_MOUNTED)){
								savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + ImageUtils.DCIM;//存放照片的文件夹
								File savedir = new File(savePath);
								if (!savedir.exists()) {
									savedir.mkdirs();
								}
							}
							//没有挂载SD卡，无法保存文件
							if(StringUtils.empty(savePath)){
								UIHelper.ToastMessage(Me.this, "Please check SD card", Toast.LENGTH_SHORT);
								return;
							}
							String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
							String fileName = "c_" + timeStamp + ".jpg";//照片命名
							File out = new File(savePath, fileName);
							Uri uri = Uri.fromFile(out);
							theLarge = savePath + fileName;//该照片的绝对路径
							Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
							startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
						}   
					}}).create();
			 imageDialog.show();
	}
	
	private void uploadPhotoService(String file) {
		pg = UIHelper.showProgress(this, "", "Upload Image", true);
		imageLoader.displayImage("file:///"+file, avatarView, CommonValue.DisplayOptions.avatar_options);
		ApiClent.uploadFile(appContext.getLoginApiKey(), file, new ClientCallback() {
			
			@Override
			public void onSuccess(Object data) {
				UIHelper.dismissProgress(pg);
				String head = (String) data;
				modify("", head, "", "");
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(pg); 
			}
			
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(pg);
			}
		});
	}
	
	private void modify(String nickname, String head, String des, String email) {
		pg = UIHelper.showProgress(this, "", "Saving...", true);
		ApiClent.modifiedUser(appContext, appContext.getLoginApiKey(), nickname, head, des, email, new ClientCallback() {
			
			@Override
			public void onSuccess(Object data) {
				UIHelper.dismissProgress(pg);
				showToast((String)data);
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(pg);
			}
			
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(pg);
			}
		});
	}
	
	private AlertDialog textDialogNickName;
	private void initNickDialog() {
		LayoutInflater inFlater = LayoutInflater.from(this);  
		View textDialogView = inFlater.inflate(R.layout.lovecode_edit_edittext_dialog, null);
		final EditText ed = (EditText) textDialogView.findViewById(R.id.text);
		ed.setText(datas.get(0).value);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle("Edit Nick Name");
		builder.setView(textDialogView);
		builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (StringUtils.empty(ed.getText().toString())) {
					return;
				}
				datas.set(0, new KeyValue(NICK, ed.getText().toString()));
				fieldAdapter.notifyDataSetChanged();
				modify(ed.getText().toString(), "", "", "");
			}
		});
		builder.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		textDialogNickName = builder.create();
	}
	
	private AlertDialog textDialogDescription;
	private void initDescDialog() {
		LayoutInflater inFlater = LayoutInflater.from(this);  
		View textDialogView = inFlater.inflate(R.layout.lovecode_edit_edittext_dialog, null);
		final EditText ed = (EditText) textDialogView.findViewById(R.id.text);
		ed.setText(datas.get(1).value);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle("Edit Signature");
		builder.setView(textDialogView);
		builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (StringUtils.empty(ed.getText().toString())) {
					return;
				}
				datas.set(1, new KeyValue(DESCRIPTION, ed.getText().toString()));
				fieldAdapter.notifyDataSetChanged();
				modify("", "", ed.getText().toString(), "");
			}
		});
		builder.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		textDialogDescription = builder.create();
	}
	
	private AlertDialog textDialogEmail;
	private void initEmailDialog() {
		LayoutInflater inFlater = LayoutInflater.from(this);  
		View textDialogView = inFlater.inflate(R.layout.lovecode_edit_edittext_dialog, null);
		final EditText ed = (EditText) textDialogView.findViewById(R.id.text);
		ed.setText(datas.get(2).value);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle("Edit Email");
		builder.setView(textDialogView);
		builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (StringUtils.empty(ed.getText().toString())) {
					return;
				}
				if (!Utils.isValidEmail(ed.getText().toString())) {
					showToast("Please enter a valid email address");
					return;
				}
				datas.set(2, new KeyValue(EMAIL, ed.getText().toString()));
				fieldAdapter.notifyDataSetChanged();
				modify("", "", "", ed.getText().toString());
			}
		});
		builder.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		textDialogEmail = builder.create();
	}
}
