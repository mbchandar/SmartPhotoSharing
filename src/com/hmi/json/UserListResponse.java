package com.hmi.json;

import java.util.List;


public class UserListResponse extends Response {

	public List<User> obj;
	
	public List<User> getObject() {
		return obj;
	}
}
