package com.gisgraphy.gisgraphoid.demo;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

import com.gisgraphy.gisgraphoid.example.R;

public class GisgraphoidMain extends TabActivity {

	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.gisgraphoidmain);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, GisgraphoidGeocodingActivity.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    String tabName = getResources().getString(R.string.geocoding_menu_title);
	    spec = tabHost.newTabSpec(tabName).setIndicator(tabName,
	                      res.getDrawable(R.drawable.ic_launcher))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabName = getResources().getString(R.string.reverse_geocoding_menu_title);
	    intent = new Intent().setClass(this, GisgraphoidReverseGeocodingActivity.class);
	    spec = tabHost.newTabSpec(tabName).setIndicator(tabName,
	                      res.getDrawable(R.drawable.ic_launcher))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabName = getResources().getString(R.string.about_menu_title);
	    intent = new Intent().setClass(this, GisgraphoidAbout.class);
	    spec = tabHost.newTabSpec(tabName).setIndicator(tabName,
	                      res.getDrawable(R.drawable.ic_launcher))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	   // tabHost.setCurrentTab(2);
	}
}
