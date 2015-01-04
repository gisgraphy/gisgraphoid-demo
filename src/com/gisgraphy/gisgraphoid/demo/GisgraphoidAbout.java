package com.gisgraphy.gisgraphoid.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.gisgraphy.gisgraphoid.example.R;

public class GisgraphoidAbout extends Activity {

    TextView textview ;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.gisgraphoidabout);
	    String aboutGisgraphoid = getResources().getString(R.string.about_gisgraphoid_text);
	    String aboutGisgraphy = getResources().getString(R.string.about_gisgraphy_text);
	    textview = (TextView) findViewById(R.id.gisgraphoid_about_content);
	    textview.setText(aboutGisgraphoid+"\n\n"+aboutGisgraphy);
	}
}
