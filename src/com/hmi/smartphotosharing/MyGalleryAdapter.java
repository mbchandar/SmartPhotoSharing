package com.hmi.smartphotosharing;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

/**
 * Base Adapter subclass creates Gallery view
 * - provides method for adding new images from user selection
 * - provides method to return bitmaps from array
 *
 */
public class MyGalleryAdapter extends BaseAdapter {


	private Context context;
	private DrawableManager dm;
	private List<String> data;
	
	//use the default gallery background image
    int defaultItemBackground;
    
    //placeholder bitmap for empty spaces in gallery
    Bitmap placeholder;

    //constructor
    public MyGalleryAdapter(Context c, List<String> list, DrawableManager dm) {
        context = c;
        this.dm = dm;
        this.data = list;
    }

    //BaseAdapter methods
    
    @Override
    public int getCount() {
    	if (data == null) {
    		return 0;
    	} else {
    		return data.size();
    	}
    }

    @Override
    public String getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    //get view specifies layout and display options for each thumbnail in the gallery
    public View getView(int position, View convertView, ViewGroup parent) {

    	//create the view
        ImageView imageView = new ImageView(context);
        //specify the bitmap at this position in the array

        String photo = data.get(position);
        dm.fetchDrawableOnThread(photo, imageView);
        
        //set layout options
        imageView.setLayoutParams(new Gallery.LayoutParams(150, 100));
        //scale type within view area
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        //set default gallery item background
        imageView.setBackgroundResource(defaultItemBackground);
        //return the view
        
        //imageView.setOnLongClickListener(new OnItemClickListener(context, position));
        
        return imageView;
    }
    



}