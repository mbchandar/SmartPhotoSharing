package com.hmi.smartphotosharing;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.hmi.smartphotosharing.groups.GroupCreateActivity;

public class SharePhotoActivity extends Activity  {
	
	public static final int CREATE_GROUP = 4;
	
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_photo);
		
		// Populate the Spinner
		Spinner spinner = (Spinner) findViewById(R.id.groups_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.demo_groups, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		
	    // Get intent, action and MIME type
	    Intent intent = getIntent();
	    String action = intent.getAction();
	    String type = intent.getType();

	    // Handle the Intent form another app
	    if (Intent.ACTION_SEND.equals(action) && type != null) {
	        if ("text/plain".equals(type)) {
	            handleSendText(intent); // Handle text being sent
	        } else if (type.startsWith("image/")) {
	            handleSendImage(intent); // Handle single image being sent
	        }
	    } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
	        if (type.startsWith("image/")) {
	            handleSendMultipleImages(intent); // Handle multiple images being sent
	        }
	    } 
	    
	    // Handle the Intent that was sent internally, from another Activity
	    else {
	    	if (type.startsWith("image/")) {
	            handleSendImage(intent); // Handle single image being sent
	        }
	    }
	}
	
	public void onClickCreateGroup(View v) {
		Intent intent = new Intent(this, GroupCreateActivity.class);
		startActivityForResult(intent, CREATE_GROUP);
	}
	
	public void onClickShare(View v) {
		setResult(RESULT_OK);
		finish();
	}	
	
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == CREATE_GROUP) {
            if (resultCode == RESULT_OK) {
                // A contact was picked.  Here we will just display it
                // to the user.
            	// TODO : refresh group list
            	Toast.makeText(this, "Group Created", Toast.LENGTH_SHORT).show();
            }
        }
    }	
	
	private void handleSendText(Intent intent) {
	    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
	    if (sharedText != null) {
	        // Update UI to reflect text being shared
	    }
	}

	private void handleSendImage(Intent intent) {
	    Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
	    if (imageUri != null) {
	        ImageView view = (ImageView) findViewById(R.id.image1);
	        
	        view.setImageURI(imageUri);
	    }
	}

	private void handleSendMultipleImages(Intent intent) {
	    ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
	    if (imageUris != null) {
	        // Update UI to reflect multiple images being shared
	    }
	}
}