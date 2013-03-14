package com.hmi.smartphotosharing.subscriptions;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.PhotoDetailActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.json.Subscription;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Custom ArrayAdapter class that is used to display a list of items with an icon.
 * This class controls how the ListView will display the list of Group items.
 * @author Edwin
 *
 */
public class SubscriptionAdapter extends ArrayAdapter<Subscription> {

    private static final int SWIPE_MAX_OFF_PATH = 250;
    private GestureDetector gestureDetector;
    private static final int GLOBE_WIDTH = 256;
    private static final Double LN2 = 0.6931471805599453;
    
    private static final int CODE_SUB_REMOVE = 3;
    
    OnTouchListener gestureListener;
    
	Context context;		// The parenting Context that the Adapter is embedded in
	int layoutResourceId;	// The xml layout file for each ListView item
	Subscription data[] = null;	// A Group array that contains all list items
	ImageLoader imageLoader;
		
	public SubscriptionAdapter(Context context, int resource, Subscription[] objects, ImageLoader im) {
		super(context, resource, objects);
		
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
        this.imageLoader = im;
        
	}
	
    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Subscription getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }
    
	/**
	 * This method overrides the inherited getView() method.
	 * It is called for every ListView item to create the view with
	 * the properties that we want.
	 */
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Subscription subscription = getItem(position);
        
        View v = convertView;
        SubscriptionHolder holder = null;
       
        if(v == null) {
        	
        	// Inflater used to parse the xml file
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(layoutResourceId, null);
           
            holder = new SubscriptionHolder();
            holder.imgIcon = (ImageView)v.findViewById(R.id.icon);
            holder.txtTitle = (TextView)v.findViewById(R.id.item_text);
            holder.totalNew = (TextView)v.findViewById(R.id.total_new);
            holder.gallery = (LinearLayout) v.findViewById(R.id.gallery);
            holder.delete = (ImageView) v.findViewById(R.id.sub_delete);
            v.setTag(holder);
        } else {
            holder = (SubscriptionHolder)v.getTag();
        }
                        
            
        // Delete button
        holder.delete.setImageResource(R.drawable.ic_delete);
        holder.delete.setOnClickListener(new DeleteClickListener(subscription.getId()));
        
        // Show subscription as a person
        if (subscription.person != null) {
        	holder.txtTitle.setText(subscription.user.rname);
	        String url = Util.USER_DB + subscription.user.picture;
	        imageLoader.displayImage(url, holder.imgIcon);
        } 
        
        // Show subscription as a location
        else {
        	holder.txtTitle.setText(subscription.name);
        	
        	String url = context.getResources().getString(R.string.subscription_static_map);
        	
        	double lat1 = Double.parseDouble(subscription.lat1);
        	double lat2 = Double.parseDouble(subscription.lat2);
        	double lon1 = Double.parseDouble(subscription.lon1);
        	double lon2 = Double.parseDouble(subscription.lon2);
        	
        	// Move to the center of the Group location
            double centerLat = ((lat1 + lat2)/2);
            double centerLong = ((lon1 + lon2)/2);

            // Calculate the zoom level from the GPS bounds
            Double angle = lon2 - lon1;
            if (angle < 0) {
              angle += 360;
            }
            
            int zoom = (int)(Math.log(100 * 360 / angle / GLOBE_WIDTH) / LN2);
        	String mapUrl = String.format(url, centerLat, centerLong, zoom, Util.API_KEY);
        	
        	Log.d("ZOOM", mapUrl);
        	imageLoader.displayImage(mapUrl, holder.imgIcon);
        	
        }
                
        // Show the number of updates
        if (subscription.totalnew == 0) {
        	holder.totalNew.setVisibility(TextView.INVISIBLE);
        } else {
            holder.totalNew.setVisibility(TextView.VISIBLE); // Needed because of the holder pattern
            holder.totalNew.setText(Integer.toString(subscription.totalnew));
        }
        
        // Add photos to the gallery
        holder.gallery.removeAllViews();
        if (subscription.photos != null && subscription.photos.size() > 0) {
			for (Photo p : subscription.photos) {
				View imgView = getImageView(p.thumb);
				imgView.setOnClickListener(new MyOnItemClickListener(context, getItemId(position), p.getId()));
				holder.gallery.addView(imgView);
			}
        }    
        
        // Detect swipes
        holder.gallery.setOnTouchListener(gestureListener);
        
        return v;
    }

    public View getImageView(String path){
                
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new LayoutParams(60, 60));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        imageLoader.displayImage(path, imageView);
        return imageView;
    }
    
	/**
	 * The Groupholder class is used to cache the Views
	 * so they can be reused for every row in the ListView.
	 * Mainly a performance improvement by recycling the Views.
	 * @author Edwin
	 *
	 */
    static class SubscriptionHolder {
        ImageView imgIcon;
        TextView txtTitle;
        TextView totalNew;
        LinearLayout gallery;
        ImageView delete;
    }	
    	
    /**
     * Listener for clicking images on the gallery.
     * @author Edwin
     *
     */
    private class MyOnItemClickListener implements OnClickListener{    
        private Context context;
        private long ssid, iid;
        
        public MyOnItemClickListener(Context context, long ssid, long iid){
            this.context = context;
            this.ssid = ssid;
            this.iid = iid;
        }
        
		@Override
		public void onClick(View arg0) {
        	Intent intent = new Intent(context, PhotoDetailActivity.class);
        	intent.putExtra("ssid", ssid);
		    intent.putExtra("id", iid);
		    context.startActivity(intent);
			
		}

    }
    
    private class DeleteClickListener implements OnClickListener{    

        private long ssid;
        
        public DeleteClickListener(long ssid){
            this.ssid = ssid;
        }
        
        @Override
        public void onClick(View arg0) {
        	confirmDeleteCommentDialog(context, ssid);
        	
        }       
    }
    
    private void confirmDeleteCommentDialog(final Context c, final long ssid) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("Are you sure you want to delete this subscription?")
		     .setCancelable(false)       
		     .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int bid) {

			       		SharedPreferences settings = context.getSharedPreferences(Login.SESSION_PREFS, Context.MODE_PRIVATE);
			       		String hash = settings.getString(Login.SESSION_HASH, null);
			       		
			       		String deleteUrl = Util.getUrl(context,R.string.subscriptions_http_remove);
			       			
			               HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
			               try {
			       			map.put("sid", new StringBody(hash));
			       	        map.put("ssid", new StringBody(Long.toString(ssid)));
			       		} catch (UnsupportedEncodingException e) {
			       			e.printStackTrace();
			       		}
		               
		                PostData pr = new PostData(deleteUrl,map);
		                new PostRequest(context, CODE_SUB_REMOVE).execute(pr);
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
