package com.hmi.smartphotosharing;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.gson.Gson;
import com.hmi.smartphotosharing.groups.GroupManageActivity;
import com.hmi.smartphotosharing.groups.GroupsActivity;
import com.hmi.smartphotosharing.json.FetchJSON;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.json.PhotoListResponse;
import com.hmi.smartphotosharing.json.PhotoResponse;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.StringResponse;
import com.hmi.smartphotosharing.json.Subscription;
import com.hmi.smartphotosharing.json.SubscriptionListResponse;
import com.hmi.smartphotosharing.json.UserResponse;
import com.hmi.smartphotosharing.util.Util;

public class PhotoDetailActivity extends NavBarActivity implements OnDownloadListener {

	public static final int CODE_PHOTO = 1;
	public static final int CODE_COMMENT_ADD = 2;
	public static final int CODE_LIKE = 3;
	public static final int CODE_GROUP = 4;
	public static final int CODE_COMMENT_REMOVE = 5;
	public static final int CODE_SUB = 6;
	public static final int CODE_FRIENDS = 7;
	public static final int CODE_SPOT = 8;
	public static final int CODE_USER = 9;
	public static final int CODE_DELETE = 10;
	
	public static final String KEY_ID = "id";
	public static final String KEY_GID = "gid";
	public static final String KEY_UID = "uid";
	public static final String KEY_SSID = "ssid";
	public static final String KEY_FID = "fid";
	public static final String KEY_TYPE = "type";

	private static final int TEN_SECONDS = 10 * 1000;
	
	private long id, gid, uid, ssid, fid;
	private int type;
	private ViewPager vp;
	
	private int currentPage;

    private LocationManager mLocationManager;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.photo_detail);
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        id = intent.getLongExtra(KEY_ID, 0);
        gid = intent.getLongExtra(KEY_GID, 0);    
        uid = intent.getLongExtra(KEY_UID, 0);    
        ssid = intent.getLongExtra(KEY_SSID, 0); 
        fid = intent.getLongExtra(KEY_FID, 0); 
        type = intent.getIntExtra(KEY_TYPE, 0);
        
        vp = (ViewPager) findViewById(R.id.viewpager);    
        
        if (id != 0) {
			loadData();
        } else {
        	Log.e("SmartPhotoSharing", "Photo id was 0, url was probably incorrect");
        }
        
        // GPS
        mLocationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        
    }

    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
    }
    
    @Override
    public void onPause() {
      super.onPause();

      MyPagerAdapter adapter = (MyPagerAdapter)vp.getAdapter();
      if (mLocationManager != null && adapter != null)
    	  mLocationManager.removeUpdates(adapter);
    }
    
	@Override
    protected void onStop() {
        super.onStop();
        
        MyPagerAdapter adapter = (MyPagerAdapter)vp.getAdapter();
        if (mLocationManager != null && adapter != null)
        	mLocationManager.removeUpdates(adapter);

        EasyTracker.getInstance().activityStop(this); 
    } 
	
    @Override
    protected void onNewIntent(Intent intent) {
        id = intent.getLongExtra(KEY_ID, 0);
    }
            
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.delete:

	       		confirmDeleteCommentDialog();
				return true;
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }

	
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.photo_menu, menu);
		super.onCreateOptionsMenu(menu);
	    return true;
	}

	public void onClickHelp(View view) {
		Util.createSimpleDialog(this, getResources().getString(R.string.photo_detail_spot_help));
	}
	
	public void loadData() {
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
        
		// Group
		if (gid != 0) {
			// Get list of photos
			String photosUrl = String.format(Util.getUrl(this,R.string.group_http_detail_photos),hash,gid);
			Log.d("JSON", photosUrl);
			new FetchJSON(this,CODE_GROUP).execute(photosUrl);
		}
		
		// Subscription
		else if (ssid != 0) {
			String photosUrl = String.format(Util.getUrl(this,R.string.subscriptions_http_photos),hash,ssid);
			Log.d("JSON", photosUrl);
			new FetchJSON(this,CODE_SUB).execute(photosUrl);
		}
		
		else if (fid != 0) {
			//TODO friend stuff here
		}
		
		else if(uid != 0) {

	        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
	        try {
				map.put("sid", new StringBody(hash));
				map.put("uid", new StringBody(Long.toString(uid)));
				map.put("type", new StringBody(Integer.toString(type)));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
	        
	        String usersUrl = Util.getUrl(this,R.string.profile_http);	        

	        PostData pr = new PostData(usersUrl,map);
			new PostRequest(this,CODE_USER).execute(pr);			
		}
		
		// Single photo
		else {
			String photosUrl = String.format(Util.getUrl(this,R.string.photo_detail_http),hash,id);
			Log.d("JSON", photosUrl);
			new FetchJSON(this,CODE_PHOTO).execute(photosUrl);
		}
		
	}
	
	@Override
	public void parseJson(String json, int code) {
		
		Log.i("JSON parse", json);
		
		switch(code){
		case(CODE_PHOTO):
			parsePhoto(json);
			break;
		case(CODE_GROUP):
			parseGroup(json);
			break;
		case(CODE_SUB):
			parseSub(json);
			break;
		case(CODE_COMMENT_ADD):
			parseCommentAdd(json);
			break;
		case(CODE_COMMENT_REMOVE):
			parseCommentRemove(json);
			break;
		case(CODE_LIKE):
			parseLike(json);
			break;
		case(CODE_SPOT):
			parseSpot(json);
			break;
		case(CODE_USER):
			parseUser(json);
			break;
		case(CODE_DELETE):
			parseDelete(json);
			break;
		default:
		}
        
	}

	private void parseDelete(String json) {
		Gson gson = new Gson();
		
		StringResponse response = gson.fromJson(json, StringResponse.class);
		
		Toast.makeText(this, response.getMessage(), Toast.LENGTH_LONG).show();
		if (response.getStatus() == Util.STATUS_OK) {
			if (vp.getChildCount() == 1) {
				finish();
			} else {
				loadData();
			}
		} 
		
	}

	private void parseUser(String json) {
		Gson gson = new Gson();
		UserResponse response = gson.fromJson(json, UserResponse.class);
		
		if (response != null && response.getStatus() == Util.STATUS_OK) {
			List<Photo> photo_list = response.getObject().newest_photos;
			setPhotos(photo_list);
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}

	private void parseSpot(String json) {
		Gson gson = new Gson();
		StringResponse pr = gson.fromJson(json, StringResponse.class);
		
		if (pr.getStatus() == Util.STATUS_OK) {
			loadData();
			Toast.makeText(this, pr.getMessage(), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, pr.getMessage(), Toast.LENGTH_LONG).show();			
			
		}
		
	}
	
	private void parsePhoto(String json) {
		Gson gson = new Gson();
		PhotoResponse response = gson.fromJson(json, PhotoResponse.class);
		
		if (response != null && response.getStatus() == Util.STATUS_OK) {
			List<Photo> photo_list = new ArrayList<Photo>();
			photo_list.add(response.getObject());
			setPhotos(photo_list);
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}

	private void parseCommentAdd(String json) {
		Gson gson = new Gson();
		PhotoResponse pr = gson.fromJson(json, PhotoResponse.class);
		
		if (pr.getStatus() == Util.STATUS_OK)
			loadData();
		
		Toast.makeText(this, pr.getMessage(), Toast.LENGTH_SHORT).show();
		
	}
	
	private void parseCommentRemove(String json) {
		Gson gson = new Gson();
		PhotoResponse response = gson.fromJson(json, PhotoResponse.class);
		
		if (response.getStatus() == Util.STATUS_OK)
			loadData();

		Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		
	}

	private void parseLike(String json) {
		Gson gson = new Gson();
		PhotoResponse response = gson.fromJson(json, PhotoResponse.class);
		
		if (response.getStatus() == Util.STATUS_OK)
			loadData();
		Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		
	}

	private void parseGroup(String result) {
		Gson gson = new Gson();
		PhotoListResponse response = gson.fromJson(result, PhotoListResponse.class);
			
		if (response.getStatus() == Util.STATUS_OK) {						
			setPhotos(response.getObject());			
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	private void parseSub(String result) {
		Gson gson = new Gson();
		SubscriptionListResponse response = gson.fromJson(result, SubscriptionListResponse.class);
			
		if (response.getStatus() == Util.STATUS_OK) {
			List<Photo> photo_list = new ArrayList<Photo>();
			
			for (Subscription s : response.getObject())
				photo_list.addAll(s.photos);
			
			setPhotos(photo_list);
			
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Adds all photos to the ViewPager and selects the current photo from the list.
	 * @param photo_list List of photos
	 */
	private void setPhotos(List<Photo> photo_list) {

		if (photo_list.size() > 0) {
			boolean found = false;
			for (int i = 0; i < photo_list.size() & !found; i++) {
				if (photo_list.get(i).getId() == id) {
					currentPage = i;
					found = true;
				}
			}

			if (!found) currentPage = 0;
			
        	Photo p = photo_list.get(currentPage);

			MyPagerAdapter adapter = new MyPagerAdapter(this,photo_list);
			PageListener pageListener = new PageListener();
			vp.setOnPageChangeListener(pageListener);
	
			vp.setAdapter(adapter);
			
			vp.setCurrentItem(currentPage);

	        setupGps();
		}
	}
	
	
    private class PageListener extends SimpleOnPageChangeListener{
        public void onPageSelected(int position) {
            Log.i("PAGER", "page selected " + position);
            currentPage = position;
        }
    }

	
    private void setupGps() { 
        requestUpdatesFromProvider();
    	 
    }
    
    private void requestUpdatesFromProvider() {
        
        // Network
        String networkProvider = LocationManager.NETWORK_PROVIDER;

        if (mLocationManager.isProviderEnabled(networkProvider)) {
        	mLocationManager.requestLocationUpdates(networkProvider, TEN_SECONDS, 0, (MyPagerAdapter)vp.getAdapter());
        }
        
        // GPS
        String gpsProvider = LocationManager.GPS_PROVIDER;
        
        if (mLocationManager.isProviderEnabled(gpsProvider)) {
            mLocationManager.requestLocationUpdates(gpsProvider, TEN_SECONDS, 0, (MyPagerAdapter)vp.getAdapter());
        }
        
    }

    private void confirmDeleteCommentDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to delete this photo?")
		     .setCancelable(false)       
		     .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int bid) {
		        	   	MyPagerAdapter pager = (MyPagerAdapter) vp.getAdapter();
			       		pager.deleteCurrent(vp.getCurrentItem());
		           }
		       })
		     .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
		
	}
}
