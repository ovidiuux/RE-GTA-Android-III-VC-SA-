package com.iro.mamaiavice;

import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MovieActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        MainActivity mainActivity = MainActivity.getInstance();
		
		getWindow().getDecorView().setSystemUiVisibility(5894);
		FrameLayout frameLayout = new FrameLayout(this);
		frameLayout.setLayoutParams(new FrameLayout.LayoutParams(
		FrameLayout.LayoutParams.MATCH_PARENT,
		FrameLayout.LayoutParams.MATCH_PARENT
		));
        
		TextureView textureView = new TextureView(this);
		textureView.setLayoutParams(new FrameLayout.LayoutParams(
		FrameLayout.LayoutParams.MATCH_PARENT,
		FrameLayout.LayoutParams.MATCH_PARENT
		));
		frameLayout.addView(textureView);
		setContentView(frameLayout);
        
		String videoPath = getFilesDir().getAbsolutePath() + "/movies/Logo.mpg.m4v";
		Uri videoUri = Uri.parse(videoPath);
		MediaPlayer mediaPlayer = new MediaPlayer();
		
		try {
			mediaPlayer.setDataSource(this, videoUri);
			
			textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
				@Override
				public void onSurfaceTextureAvailable(android.graphics.SurfaceTexture surface, int width, int height) {
					mediaPlayer.setSurface(new android.view.Surface(surface));
					try {
						mediaPlayer.prepare();
						mediaPlayer.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				@Override
				public void onSurfaceTextureSizeChanged(android.graphics.SurfaceTexture surface, int width, int height) {
				}
				
				@Override
				public boolean onSurfaceTextureDestroyed(android.graphics.SurfaceTexture surface) {
					mediaPlayer.release();
					return true;
				}
				
				@Override
				public void onSurfaceTextureUpdated(android.graphics.SurfaceTexture surface) {
				}
			});
			
			mediaPlayer.setOnCompletionListener(mp -> {
    mainActivity._RandomDisplayAds();
    finish();
}); 
			
			textureView.setOnClickListener(v -> {
    mainActivity._RandomDisplayAds();
    finish();
}); 
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
