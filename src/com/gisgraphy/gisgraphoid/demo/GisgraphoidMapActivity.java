package com.gisgraphy.gisgraphoid.demo;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.gisgraphy.gisgraphoid.example.R;
import com.gisgraphy.gisgraphoid.map.AddressOverlayItem;

/**
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 *
 */
public class GisgraphoidMapActivity extends Activity {
	protected boolean isRouteDisplayed() {
		return false;
	}

	private org.osmdroid.views.MapView osmMap;
	//private MapView  googleMap;
	private double lat;
	private double lon;
	private String name;
	private Address address;
	private ItemizedOverlayWithFocus<OverlayItem> mMyLocationOverlay;
	private static String LOG_TAG="Gisgraphoid-demo-map";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gisgraphoidmap);
		
		Intent intent = getIntent();
		lat = intent.getDoubleExtra(ExtraInfos.LATITUDE,0);
		lon = intent.getDoubleExtra(ExtraInfos.LONGITUDE,0);
		name=intent.getStringExtra(ExtraInfos.FEATURE_NAME);
		address = intent.getParcelableExtra(ExtraInfos.ADDRESS);
		Log.i(LOG_TAG, "lat="+lat);
		Log.i(LOG_TAG, "lon="+lon);
		Log.i(LOG_TAG, "name="+name);
		
		
		
		osmMap =(org.osmdroid.views.MapView) findViewById(R.id.osm_map);
		osmMap.setBuiltInZoomControls(true);
		osmMap.setMultiTouchControls(true);
		navigateToOsmMAP(lat * 1000000,lon* 1000000 ,name,
			osmMap);
		addOverlay(address);
		
	}
	
	protected void addOverlay(Address address){
	    if (address!=null){
		OverlayItem item = new AddressOverlayItem(address.getFeatureName(),"",new GeoPoint(address.getLatitude(), address.getLongitude()),address);
		item.setMarker(getResources().getDrawable(R.drawable.marker_default));
		List<OverlayItem> list = new ArrayList<OverlayItem>();
		list.add(item);
		this.mMyLocationOverlay = new ItemizedOverlayWithFocus<OverlayItem>(list, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {

		    public boolean onItemLongPress(int index, OverlayItem item) {
			AddressOverlayItem addressOverlayItem = (AddressOverlayItem)item;
			Address addressFromOverlay = addressOverlayItem.getAddress();
			String locality = addressFromOverlay.getLocality();
			String fullname = addressFromOverlay.getFeatureName();
			if (locality!=null){
			    fullname = fullname+" ("+locality+")";
			}
			Toast.makeText(
				GisgraphoidMapActivity.this,
				fullname + "\n" +
				//addressFromOverlay.getLocality()!=null?"("+addressFromOverlay.getLocality()+")":""+
				"lat : " + addressFromOverlay.getLatitude() + "\n" +
				"long : " + addressFromOverlay.getLongitude(),
				Toast.LENGTH_LONG).show();
			return false;
		    }

		    public boolean onItemSingleTapUp(int arg0, OverlayItem arg1) {
			osmMap.postInvalidate();
			return true;
		    }
			
		}, new ResourceProxyImpl(getApplicationContext()));
	    }
	    osmMap.getOverlays().add(mMyLocationOverlay);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
		Log.d(LOG_TAG, keyCode+" button pressed");
		Intent backIntent = new Intent(this, GisgraphoidMain.class);              
	        startActivity(backIntent);          
	        finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}


	/*public void navigateToGoogleMap(double latitude, double longitude,
			MapView mv) {
		GeoPoint p = new GeoPoint((int) latitude, (int) longitude); 
		mv.displayZoomControls(true); 
		MapController mc = mv.getController();
		mc.setCenter(p); 
		int zoomlevel = mv.getMaxZoomLevel(); 
		mc.setZoom(zoomlevel - 1); 
		mv.setSatellite(false); 

	}*/
	
	
	/**
	 * Navigates a given google Map to the specified Longitude and Latitude
	 * 
	 * @param latitude
	 * @param longitude
	 * @param mv
	 */
	public void navigateToOsmMAP(double latitude, double longitude,String name,
			org.osmdroid.views.MapView mv) {
		org.osmdroid.util.GeoPoint p = new org.osmdroid.util.GeoPoint((int) latitude, (int) longitude); 
		org.osmdroid.views.MapController mc = mv.getController();
		mc.setZoom(14); 
		mc.setCenter(p);
	}
	

	public class ResourceProxyImpl extends DefaultResourceProxyImpl {

		private final Context mContext;

		public ResourceProxyImpl(final Context pContext) {
			super(pContext);
			mContext = pContext;
		}

		@Override
		public String getString(final string pResId) {
			try {
				final int res = R.string.class.getDeclaredField(pResId.name()).getInt(null);
				return mContext.getString(res);
			} catch (final Exception e) {
				return super.getString(pResId);
			}
		}

		@Override
		public Bitmap getBitmap(final bitmap pResId) {
			try {
				final int res = R.drawable.class.getDeclaredField(pResId.name()).getInt(null);
				return BitmapFactory.decodeResource(mContext.getResources(), res);
			} catch (final Exception e) {
				return super.getBitmap(pResId);
			}
		}

		@Override
		public Drawable getDrawable(final bitmap pResId) {
			try {
				final int res = R.drawable.class.getDeclaredField(pResId.name()).getInt(null);
				return mContext.getResources().getDrawable(res);
			} catch (final Exception e) {
				return super.getDrawable(pResId);
			}
		}
	}
	
	
	
	
}