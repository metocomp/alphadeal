/**
 * wechatdonal
 */
package com.youthschat.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tools.UIHelper;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.telephony.SmsManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.youthschat.R;
import com.youthschat.bean.StrangerEntity;
import com.youthschat.bean.UserInfo;
import com.youthschat.config.ApiClent;
import com.youthschat.config.AppActivity;
import com.youthschat.config.CommonValue;
import com.youthschat.config.ApiClent.ClientCallback;
import com.youthschat.ui.adapter.StrangerAdapter;
import com.youthschat.utils.Utils;

/**
 * wechat
 *
 * @author donal
 *
 */
public class FindFriend extends AppActivity implements OnScrollListener, OnRefreshListener{
	
	private int lvDataState;
	private int currentPage;
	
	private ListView unregisteredlistView;
//	private ListView registeredListView;
	private List<UserInfo> unregisteredData;
	private StrangerAdapter unregisteredAdapter;
//	private List<UserInfo> registeredData;
//	private StrangerAdapter registeredAdapter;
	private SwipeRefreshLayout swipeLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.findfriend);
		initUI();
		getFriendCardFromCache();
	}
	
	private void initUI() {
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.xrefresh);
		swipeLayout.setOnRefreshListener(this);
	    swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
	            android.R.color.holo_green_light, 
	            android.R.color.holo_orange_light, 
	            android.R.color.holo_red_light);
		unregisteredlistView = (ListView)findViewById(R.id.xlistview);
		unregisteredlistView.setOnScrollListener(this);
        unregisteredlistView.setDividerHeight(0);
        unregisteredData = new ArrayList<UserInfo>();
		unregisteredAdapter = new StrangerAdapter(this, unregisteredData);
		unregisteredlistView.setAdapter(unregisteredAdapter);
		
//		registeredListView = (ListView)findViewById(R.id.registedlistview);
//		registeredListView.setOnScrollListener(this);
//		registeredListView.setDividerHeight(0);
//        registeredData = new ArrayList<UserInfo>();
//		registeredAdapter = new StrangerAdapter(this, registeredData);
//		registeredListView.setAdapter(registeredAdapter);
		
	}
	
	private void getFriendCardFromCache() {
		currentPage = 1;
		findFriend(currentPage, "", UIHelper.LISTVIEW_ACTION_REFRESH);
	}
	
	private void findFriend(final int page, String nickName, final int action) {
		final String apiKey = appContext.getLoginApiKey();
		ApiClent.findFriend(appContext, apiKey, page+"", UIHelper.LISTVIEW_COUNT+"", nickName, new ClientCallback() {
			@Override
			public void onSuccess(Object data) {
				final StrangerEntity entity = (StrangerEntity)data;
				switch (entity.status) {
				case 1:
					ApiClent.getMyFriend(appContext, apiKey, page+"", UIHelper.LISTVIEW_COUNT+"", new ClientCallback() {
						@Override
						public void onSuccess(Object data) {
							StrangerEntity myFriendsEntity = (StrangerEntity)data;
							switch (myFriendsEntity.status) {
							case 1:
								handleFriends(myFriendsEntity, entity, action);
								break;
							default:
								showToast(myFriendsEntity.msg);
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
	
	private void handleFriends(StrangerEntity myFriendsEntity, final StrangerEntity entity, final int action) {
		final int UNREGISTERED = 0;
		getAllContacts(myFriendsEntity.userList, entity.userList, new Callback() {

			@Override
			public void onResult(List<List<UserInfo>> result) {
				switch (action) {
					case UIHelper.LISTVIEW_ACTION_INIT:
					case UIHelper.LISTVIEW_ACTION_REFRESH:
						unregisteredData.clear();
						unregisteredData.addAll(result.get(UNREGISTERED));
	//					registeredData.clear();
	//					registeredData.addAll(result.get(REGISTERED));
						break;
					case UIHelper.LISTVIEW_ACTION_SCROLL:
						unregisteredData.addAll(result.get(UNREGISTERED));
	//					registeredData.addAll(result.get(REGISTERED));
						break;
				}
				if(entity.userList.size() == UIHelper.LISTVIEW_COUNT){					
					lvDataState = UIHelper.LISTVIEW_DATA_MORE;
//					unregisteredData.addAll(result.get(UNREGISTERED));
//					registeredData.addAll(result.get(REGISTERED));
				}
				else {
					lvDataState = UIHelper.LISTVIEW_DATA_FULL;
					unregisteredAdapter.notifyDataSetChanged();
//					registeredAdapter.notifyDataSetChanged();
//					setListViewHeightBasedOnChildren(registeredListView);
//					setListViewHeightBasedOnChildren(unregisteredlistView);
				}
//				if(unregisteredData.isEmpty() && registeredData.isEmpty()){
//					lvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
//				}
				swipeLayout.setRefreshing(false);
				
			}

			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	private void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter(); 
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
	
	@Override
	public void onBackPressed() {
		isExit();
	}
	
	public void show2OptionsDialog(final String[] operation ,final UserInfo model){
		new AlertDialog.Builder(context).setTitle(null).setItems(operation,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					if (operation[0].equals(CommonValue.Operation.addFriend)) {
						addFriend(model);
					} else if (operation[0].equals(CommonValue.Operation.inviteFriend)) {
						inviteFriend(model);
					}
					break;
				}
			}
		}).show();
	}
	
	private void inviteFriend(final UserInfo user) {
		try {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(user.userName /* phone number */, null, CommonValue.Messages.inviteMsg, null, null);
			Toast.makeText(getApplicationContext(), "SMS Invitation Sent!",
						Toast.LENGTH_LONG).show();
		  } catch (Exception e) {
			Toast.makeText(getApplicationContext(),
				"SMS sent faild, please try again later!",
				Toast.LENGTH_LONG).show();
			e.printStackTrace();
		  }
	}
	
	private void addFriend(final UserInfo user) {
		ApiClent.addFriend(appContext, appContext.getLoginApiKey(), user.userId, new ClientCallback() {
			
			@Override
			public void onSuccess(Object data) {
				showToast((String)data);
				addFriendBroadcast(user);
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

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (lvDataState != UIHelper.LISTVIEW_DATA_MORE) {
            return;
        }
        if (firstVisibleItem + visibleItemCount >= totalItemCount
                && totalItemCount != 0) {
        	lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
        	currentPage++;
        	findFriend(currentPage, "", UIHelper.LISTVIEW_ACTION_SCROLL);
        }
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}

	@Override
	public void onRefresh() {
		if (lvDataState != UIHelper.LISTVIEW_DATA_LOADING) {
			lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
			currentPage = 1;
			findFriend(currentPage, "", UIHelper.LISTVIEW_ACTION_REFRESH);
		}
		else {
			swipeLayout.setRefreshing(false);
		}
	}
	
	private void addFriendBroadcast(UserInfo user) {
		Intent intent = new Intent(CommonValue.ADD_FRIEND_ACTION);
		intent.putExtra("user", user);
		sendBroadcast(intent);
	}
	
	public interface Callback {
		void onResult(List<List<UserInfo>> userInfo);
		void onError(Exception e);
	}
	
    private static class Result {
        private final boolean isSuccess;
        private final List<List<UserInfo>> result;
        private final Exception exception;

        private Result(List<List<UserInfo>> result) {
            this.isSuccess = true;
            this.result = result;
            this.exception = null;
        }
        private Result(Exception e) {
            this.isSuccess = false;
            this.result = null;
            this.exception = e;
        }
    }
	
	/**
	 * Gets all users from phone contacts, and categorize them into registered and un-registered. Async
	 */
	private void getAllContacts(final List<UserInfo> myFriends, final List<UserInfo> registeredUsers, final Callback callback) {
		new AsyncTask<Void, Void, Result>() {

			@Override
			protected Result doInBackground(Void... params) {
				PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
				List<List<UserInfo>> result = new LinkedList<List<UserInfo>>();
				Map<String, UserInfo> registeredPhones = new HashMap<String, UserInfo>();
				Map<String, UserInfo> myFriendsMap = new HashMap<String, UserInfo>();
				for (UserInfo user : myFriends) {
					myFriendsMap.put(user.userName, user);
				}
				for (UserInfo user : registeredUsers) {
					registeredPhones.put(user.userName, user);
				}
				List<UserInfo> myRegisteredUsers = new LinkedList<UserInfo>();
				List<UserInfo> myUnregisteredUsers = new LinkedList<UserInfo>();
		        ContentResolver cr = getContentResolver();
		        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
		                null, null, null, null);
		        if (cur.getCount() > 0) {
				    while (cur.moveToNext()) {
				        String id = cur.getString(
			                        cur.getColumnIndex(ContactsContract.Contacts._ID));
				        String name = cur.getString(
			                        cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				        // TODO(larry): this is not working as it populates the url in Android.
				        String photoUrl = cur.getString(
		                        cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
				        if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
				        	if (Integer.parseInt(cur.getString(
				                    cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
				                Cursor pCur = cr.query(
				                		ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
							  		    null, 
							  		    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
							  		    new String[]{id}, null);
				                boolean foundMatchPhone = false;
					  	        while (pCur.moveToNext() && !foundMatchPhone) {
					  	        	String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					  	        	try {
					  	        	    // phone must begin with '+'
					  	        	    PhoneNumber numberProto = phoneUtil.parse(phone, "US");
					  	        	    phone = phoneUtil.format(numberProto, PhoneNumberFormat.E164);
					  	        	} catch (NumberParseException e) {
					  	        		// TODO(larry): Use user's country as default country code.
					  	        		phone = "+1" + phone;
					  	        	}
					  	        	
					  	        	String formatedPhone = Utils.formatPhone(phone);
					  	        	if (registeredPhones.containsKey(formatedPhone) && !myFriendsMap.containsKey(formatedPhone)) {
					  	        		myRegisteredUsers.add(registeredPhones.get(formatedPhone));
					  	        		foundMatchPhone = true;
					  	        	}
					  	        	if (pCur.isLast() && !foundMatchPhone) {
					  	        		myUnregisteredUsers.add(new UserInfo(formatedPhone, name, photoUrl));
					  	        	}
					  	        }
					  	        pCur.close();
					  	    }
				        }
		            }
			 	}
		        cur.close();
//		        result.add(myRegisteredUsers);
		        Collections.sort(myUnregisteredUsers, new Comparator<UserInfo>() {
					@Override
					public int compare(UserInfo lhs, UserInfo rhs) {
						if (lhs.nickName.compareTo(rhs.nickName) < 0) {
							return -1;
						}
						if (lhs.nickName.compareTo(rhs.nickName) > 0) {
							return 1;
						}
						return 0;
					}
				});
		        myRegisteredUsers.addAll(myUnregisteredUsers);
		        // myRegisteredUsers contains both registered and unregistered for now. TODO(larryl): use separate lists.
		        result.add(myRegisteredUsers);
		        return new Result(result);
			}
			
            @Override
            protected void onPostExecute(Result result) {
                if (result.isSuccess) {
                    callback.onResult(result.result);
                } else {
                    callback.onError(result.exception);
                }
            }
		}.execute();
	}
}
