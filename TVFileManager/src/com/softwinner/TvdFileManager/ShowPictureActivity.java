package com.softwinner.TvdFileManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.truba.touchgallery.GalleryWidget.GalleryViewPager;
import ru.truba.touchgallery.GalleryWidget.UrlPagerAdapter;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class ShowPictureActivity extends Activity {
	
	public final static String TAG = "ShowPictureActivity";
	
	private GalleryViewPager mGalleryViewPager;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_picture);
		Uri uri = getIntent().getData();
		if(uri != null) {
			String[] urls = {uri.toString(),};
			List<String> items = new ArrayList<String>();
			Collections.addAll(items, urls);
			UrlPagerAdapter pagerAdapter = new UrlPagerAdapter(this, items);
			mGalleryViewPager = (GalleryViewPager)findViewById(R.id.viewer);
			mGalleryViewPager.setOffscreenPageLimit(3);
			mGalleryViewPager.setAdapter(pagerAdapter);
		} else {
			Log.e(TAG, "uri is null");
		}
	}
}
