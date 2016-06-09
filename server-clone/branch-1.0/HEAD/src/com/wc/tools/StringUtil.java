package com.wc.tools;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

public class StringUtil {
	
	private static final String AB = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static Random RANDOM = new Random();
	
	public static boolean isNullOrEmpty(String str)
	{
		if (str==null || str.length()==0)
		return true;
		else return false;
	}
	public static String addZeroForNum(String str, int strLength) {
		int strLen = str.length();
		if (strLen < strLength) {
		while (strLen < strLength) {
		StringBuffer sb = new StringBuffer();
		sb.append("0").append(str);//��0
		// sb.append(str).append("0");//�Ҳ�0
		str = sb.toString();
		strLen = str.length();
		}
		}
		return str;
	}
	
	
	public static String getIP(HttpServletRequest request)
	{
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0
				|| "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("PRoxy-Client-IP");
		}
		if (ip == null || ip.length() == 0
				|| "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0
				|| "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
	
	
	public static String nextRandomCode(int length) {
		   StringBuilder sb = new StringBuilder( length );
		   for( int i = 0; i < length; i++ ) 
		      sb.append( AB.charAt( RANDOM.nextInt(AB.length()) ) );
		   return sb.toString();
	}
}
