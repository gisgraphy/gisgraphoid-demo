package com.gisgraphy.gisgraphoid.map;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import android.location.Address;

public class AddressOverlayItem extends OverlayItem{

	    public Address address;
	    
	    public AddressOverlayItem(String aTitle, String aDescription, GeoPoint aGeoPoint,Address address) {
		super(aTitle, aDescription, aGeoPoint);
		this.address=address;
	    }

	    public Address getAddress() {
	        return address;
	    }
	    
}