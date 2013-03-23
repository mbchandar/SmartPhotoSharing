package com.hmi.smartphotosharing.json;

import java.util.List;

public class Photo {

	public String name;
	public String uid;
	public String iid;
	public String gid;
	
	public String longitude;
	public String latitude;
	
	public String description;
	
	public String time;
	public String active;

	public String age;
	public String thumb;
		
	public String likes;
	public boolean me;
	public boolean isNew;
	
	// Group attributes
	public String groupname;
	
	// User attributes
	public String uname;
	public String rname;
	public String pword;
	public String email;
	public String fb_id;
	public String fb_oauth;
	public String picture;
	
	public boolean dummy;
	
	public List<Comment> comments;
		
	public long getId() {
		return Long.parseLong(iid);		
	}
	
	public int getLikes() {
		return Integer.parseInt(likes);
	}
}
