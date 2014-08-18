package com.youthschat.service;

import java.util.Calendar;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import com.youthschat.R;
import com.google.gson.Gson;
import com.youthschat.bean.JsonMessage;
import com.youthschat.config.CommonValue;
import com.youthschat.config.MessageManager;
import com.youthschat.config.NoticeManager;
import com.youthschat.config.XmppConnectionManager;
import com.youthschat.im.Chating;
import com.youthschat.im.model.IMMessage;
import com.youthschat.im.model.Notice;
import com.youthschat.ui.Tabbar;

import tools.Logger;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * 
 * 聊天服务.
 * 
 */
public class IMChatService extends Service {
	private Context context;
	private NotificationManager notificationManager;
	private ChatListener cListener;

	@Override
	public void onCreate() {
		context = this;
		Logger.i("c");
		super.onCreate();
		
		initChatManager();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logger.i("s");
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
//		XMPPConnection conn = XmppConnectionManager.getInstance()
//				.getConnection();
//		if (cListener != null) {
//			conn.removePacketListener(cListener);
//		}
		super.onDestroy();
	}

	private void initChatManager() {
		XMPPConnection conn = XmppConnectionManager.getInstance()
				.getConnection();
		//在登陆以后应该建立一个监听消息的监听器，用来监听收到的消息：  
        ChatManager chatManager = conn.getChatManager();  
        chatManager.addChatListener(new ChatListener()); 
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//		cListener = new ChatListener();
//		conn.addPacketListener(cListener, new MessageTypeFilter(
//				Message.Type.chat));
	}

	class ChatListener implements ChatManagerListener {

		@Override
		public void chatCreated(Chat chat, boolean arg0) {
			 chat.addMessageListener(new MessageListener(){    
	                public void processMessage(Chat arg0, Message arg) {    
	            		Message message = arg;
	        			if (message != null && message.getBody() != null
	        					&& !message.getBody().equals("null")) {
	        				IMMessage msg = new IMMessage();
	        				// String time = (String)
	        				// message.getProperty(IMMessage.KEY_TIME);
	        				String time = (System.currentTimeMillis()/1000)+"";//DateUtil.date2Str(Calendar.getInstance(), Constant.MS_FORMART);
	        				msg.setTime(time);
	        				msg.setContent(message.getBody());
	        				if (Message.Type.error == message.getType()) {
	        					msg.setType(IMMessage.ERROR);
	        				} else {
	        					msg.setType(IMMessage.SUCCESS);
	        				}
	        				String from = message.getFrom().split("/")[0];
	        				msg.setFromSubJid(from);
	        				NoticeManager noticeManager = NoticeManager
	        						.getInstance(context);
	        				Notice notice = new Notice();
	        				notice.setTitle("Chats");
	        				notice.setNoticeType(Notice.CHAT_MSG);
	        				notice.setContent(message.getBody());
	        				notice.setFrom(from);
	        				notice.setStatus(Notice.UNREAD);
	        				notice.setNoticeTime(time);

	        				IMMessage newMessage = new IMMessage();
	        				newMessage.setMsgType(0);
	        				newMessage.setFromSubJid(from);
	        				newMessage.setContent(message.getBody());
	        				newMessage.setTime(time);
	        				newMessage.setType(0); 
	        				MessageManager.getInstance(context).saveIMMessage(newMessage);
	        				long noticeId = -1;

	        				noticeId = noticeManager.saveNotice(notice);
	        				if (noticeId != -1) {
	        					Intent intent = new Intent(CommonValue.NEW_MESSAGE_ACTION);
	        					intent.putExtra(IMMessage.IMMESSAGE_KEY, msg);
	        					intent.putExtra("notice", notice);
	        					sendBroadcast(intent);
	        					setNotiType(R.drawable.chat, "New Message", notice.getContent(), Tabbar.class, from, noticeId);

	        				}
	        			}
	                }    
	            });
		}
		
	}

	private void setNotiType(int iconId, String contentTitle,
			String contentText, Class activity, String from, long notificationId) {
		JsonMessage msg = new JsonMessage();
		Gson gson = new Gson();
		msg = gson.fromJson(contentText, JsonMessage.class);
		Intent notifyIntent = new Intent(this, activity);
		notifyIntent.putExtra("to", from);
		PendingIntent appIntent = PendingIntent.getActivity(this, 0,
				notifyIntent, 0);
		Notification myNoti = new Notification();
		myNoti.flags = Notification.FLAG_AUTO_CANCEL;
		myNoti.icon = iconId;
		myNoti.tickerText = contentTitle;
		myNoti.defaults |= Notification.DEFAULT_SOUND;
		myNoti.defaults |= Notification.DEFAULT_VIBRATE;
		myNoti.setLatestEventInfo(this, contentTitle, msg.text, appIntent);
		try {
			notificationManager.notify(0, myNoti);
		} catch (Exception e) {
			// noop;
		}
	}
}
