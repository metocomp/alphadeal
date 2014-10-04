package com.youthschat.bean;

import java.util.ArrayList;
import java.util.List;

import bean.Entity;

public class KeyValue extends Entity{
	public String key;
	public String value;
	public KeyValue(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}
	
}
