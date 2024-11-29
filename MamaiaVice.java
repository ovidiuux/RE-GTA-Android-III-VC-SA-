package com.iro.mamaiavice;

import android.app.Application;
import android.content.Context;

public class MamaiaVice extends Application {
	
	private static Context context;
	
	public void onCreate() {
		super.onCreate();
		MamaiaVice.context = getApplicationContext();
	}
	
	public static Context getContext() {
		return MamaiaVice.context;
	}
}
