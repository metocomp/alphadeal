/**
 * wechatdonal
 */
package com.youthschat.bean;

import java.io.Serializable;

import tools.AppException;
import tools.Logger;

import com.google.gson.Gson;

/**
 * wechat
 *
 * @author donal
 *
 */
public class UserInfo implements Serializable {
	
	private static final long serialVersionUID = 42L;

	public String userId;
	public String nickName;
	public String description;
	/** null if unregistered */
	public String registerDate;
	public String userHead;
	/** Registered phone number */
	public String userName;
	/** Email */
	public String userEmail;
	
	public UserInfo() {}
	
	/**
	 * Constructs a user model.
	 * 
	 * @param userName The phone number of the user
	 * @param nickName The nick name
	 * @param photoUrl The URL of the user head photo
	 */
	public UserInfo(String userName, String nickName, String photoUrl) {
		this.userName = userName;
		this.nickName = nickName;
		this.userHead = photoUrl;
	}
	
	/**
	 * @param string
	 * @return
	 * @throws AppException 
	 */
	public static UserInfo parse(String string) throws AppException {
		UserInfo data = new UserInfo();
		try {
			Gson gson = new Gson();
			data = gson.fromJson(string, UserInfo.class);
		} catch (Exception e) {
			Logger.i(e);
			throw AppException.json(e);
		}
		return data;
	}
}
