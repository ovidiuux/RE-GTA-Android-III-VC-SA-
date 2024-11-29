package com.nvidia.devtech;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.Toast;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.fragment.app.FragmentActivity;
import com.iro.mamaiavice.R;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;


public abstract class NvEventQueueActivity extends FragmentActivity implements SensorEventListener {


    protected Handler handler = null;

    private int SwapBufferSkip = 0;

    protected boolean paused = false;

    protected boolean wantsMultitouch = true;

	protected boolean supportPauseResume = true;
    protected boolean ResumeEventDone = false;

    //accelerometer related
    protected boolean wantsAccelerometer = false;
 //   protected SensorManager mSensorManager = null;
    protected ClipboardManager mClipboardManager = null;
    protected int mSensorDelay = SensorManager.SENSOR_DELAY_GAME; //other options: SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_NORMAL and SensorManager.SENSOR_DELAY_UI
	protected Display display = null;

	FrameLayout mAndroidUI = null;
    private static final int EGL_RENDERABLE_TYPE = 0x3040;
    private static final int EGL_OPENGL_ES2_BIT = 0x0004;
    private static final int EGL_OPENGL_ES3_BIT = 64;
    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    EGL10 egl = null;
    GL11 gl = null;

    private boolean ranInit = false;
    protected EGLSurface eglSurface = null;
    protected EGLDisplay eglDisplay = null;
    protected EGLContext eglContext = null;
    protected EGLConfig eglConfig = null;

	protected SurfaceHolder cachedSurfaceHolder = null;
    private int surfaceWidth = 0;
    private int surfaceHeight = 0;

    private int fixedWidth = 0;
    private int fixedHeight = 0;
    private boolean HasGLExtensions = false;
    private String glVendor = null;
    private String glExtensions = null;
    private String glRenderer = null;
    private String glVersion = null;
    //private boolean GameIsFocused = false;
    private boolean viewIsActive = false;
    boolean waitingForResume = false;

    private ConstraintLayout mRootFrame = null;
    private SurfaceView mSurfaceView = null;
    private GestureDetector gestureDetector;
private long mLastTime = 0;
private int fps = 0, ifps = 0;
    //private HeightProvider mHeightProvider = null;
  //  private DialogClientSettings mDialogClientSettings = null;

    /* *
     * Helper function to select fixed window size.
     * */ 
    public void setFixedSize(int fw, int fh)
    {
    	fixedWidth = fw;
    	fixedHeight = fh;
    }

    private int mUseFullscreen = 0;

    public void showClientSettings()
    {
        
    }

    public static int dpToPx(float f, Context context) {
        return (int) TypedValue.applyDimension(1, f, context.getResources().getDisplayMetrics());
    }

    public void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY



        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * Helper class used to pass raw data around.  
     */
    public class RawData
    {
        /** The actual data bytes. */
        public byte[] data;
        /** The length of the data. */
        public int length;
    }
    /**
     * Helper class used to pass a raw texture around. 
     */
    public class RawTexture extends RawData
    {
        /** The width of the texture. */
        public int width;
        /** The height of the texture. */
        public int height;
    }

    /**
     * Helper function to load a file into a {@link NvEventQueueActivity.RawData} object.
     * It'll first try loading the file from "/data/" and if the file doesn't
     * exist there, it'll try loading it from the assets directory inside the
     * .APK file. This is to allow the files inside the apk to be overridden
     * or not be part of the .APK at all during the development phase of the
     * application, decreasing the size needed to be transmitted to the device
     * between changes to the code.
     * 
     * @param filename The file to load.
     * @return The RawData object representing the file's fully loaded data,
     * or null if loading failed. 
     */
    public RawData loadFile(String filename)
    {
        InputStream is = null;
        RawData ret = new RawData();
        try {
            try
            {
                is = new FileInputStream(filename);
            }
            catch (Exception e)
            {
                try
                {
                    is = getAssets().open(filename); 
                }
                catch (Exception e2)
                {
                }
            }
            int size = is.available();
            ret.length = size;
            ret.data = new byte[size];
            is.read(ret.data);
        }
        catch (IOException ioe)
        {
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (Exception e) {}
            }
        }
        return ret;
    }

    /**
     * Helper function to load a texture file into a {@link NvEventQueueActivity.RawTexture} object.
     * It'll first try loading the texture from "/data/" and if the file doesn't
     * exist there, it'll try loading it from the assets directory inside the
     * .APK file. This is to allow the files inside the apk to be overridden
     * or not be part of the .APK at all during the development phase of the
     * application, decreasing the size needed to be transmitted to the device
     * between changes to the code.
     * 
     * The texture data will be flipped and bit-twiddled to fit being loaded directly
     * into OpenGL ES via the glTexImage2D call.
     * 
     * @param filename The file to load.
     * @return The RawTexture object representing the texture's fully loaded data,
     * or null if loading failed. 
     */
    public RawTexture loadTexture(String filename)
    {
        RawTexture ret = new RawTexture();
        try {
            InputStream is = null;
            try
            {
                is = new FileInputStream(filename);
            }
            catch (Exception e)
            {
                try
                {
                    is = getAssets().open(filename); 
                }
                catch (Exception e2)
                {
                }
            }
            
            Bitmap bmp = BitmapFactory.decodeStream(is);
            ret.width = bmp.getWidth();
            ret.height = bmp.getHeight();
            int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
            bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
    
            // Flip texture
            int[] tmp = new int[bmp.getWidth()];
            final int w = bmp.getWidth(); 
            final int h = bmp.getHeight();
            for (int i = 0; i < h>>1; i++)
            {
                System.arraycopy(pixels, i*w, tmp, 0, w);
                System.arraycopy(pixels, (h-1-i)*w, pixels, i*w, w);
                System.arraycopy(tmp, 0, pixels, (h-1-i)*w, w);
            }
    
            // Convert from ARGB -> RGBA and put into the byte array
            ret.length = pixels.length * 4;
            ret.data = new byte[ret.length];
            int pos = 0;
            int bpos = 0;
            for (int y = 0; y < h; y++)
            {
                for (int x = 0; x < w; x++, pos++)
                {
                    int p = pixels[pos];
                    ret.data[bpos++] = (byte) ((p>>16)&0xff);
                    ret.data[bpos++] = (byte) ((p>> 8)&0xff);
                    ret.data[bpos++] = (byte) ((p>> 0)&0xff);
                    ret.data[bpos++] = (byte) ((p>>24)&0xff);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ret;
    }
    
    /**
     * Function called when app requests accelerometer events.
     * Applications need/should NOT overide this function - it will provide
     * accelerometer events into the event queue that is accessible
     * via the calls in nv_event.h
     * 
     * @param values0: values[0] passed to onSensorChanged(). For accelerometer: Acceleration minus Gx on the x-axis.
     * @param values1: values[1] passed to onSensorChanged(). For accelerometer: Acceleration minus Gy on the y-axis.
     * @param values2: values[2] passed to onSensorChanged(). For accelerometer: Acceleration minus Gz on the z-axis.
     * @return True if the event was handled.
     */
    public native boolean accelerometerEvent(float values0, float values1, float values2);
    
    /**
     * The following indented function implementations are defined in libnvevent.a
     * The application does not and should not overide this; nv_event handles this internally
     * And remaps as needed into the native calls exposed by nv_event.h
     */
		public native void cleanup();
		public native boolean init(boolean z);
		public native void setWindowSize(int w, int h);
		public native void quitAndWait();
		public native void postCleanup();

        public native void imeClosed();
        

        public native void lowMemoryEvent(); // TODO: implement this
        public native boolean processTouchpadAsPointer(ViewParent viewParent, boolean z);
        public native void notifyChange(String str, int i);
        public native void changeConnection(boolean z);

		public native void pauseEvent();
		public native void resumeEvent();
		public native boolean touchEvent(int action, int x, int y, MotionEvent event);
		public native void jniNvAPKInit(Object obj);
		public native boolean multiTouchEvent(int action, int count, 
			int x0, int y0, int x1, int y1, MotionEvent event);
		public native boolean keyEvent(int action, int keycode, int unicodeChar, int metaState, KeyEvent event);
		private float initialY = -1;
		private boolean swipeDownShow = false;
		    public native boolean multiTouchEvent4(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, MotionEvent motionEvent);

	/**
	 * END indented block, see in comment at top of block
	 */

    /**
     * Declaration for function defined in nv_time/nv_time.cpp
     * It initializes and returns time through Nvidia's egl extension for time.
     * It is useful while debugging the demo using PerfHUD.
     * 
     * @see: nv_time/nv_time.cpp for implementation details.
     */
    public native void nvAcquireTimeExtension();
    public native long nvGetSystemTime();

    @SuppressLint("SuspiciousIndentation")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        System.out.println("**** onCreate");
        super.onCreate(savedInstanceState);
        
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
            initialY = e.getY();
            return true;
            }
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                  if (initialY != -1 && initialY < getHeightLimit() && !swipeDownShow) {
                    float deltaY = e2.getY() - initialY;
                    if (deltaY > 650) {
                        // Detectăm un swipe în jos (mai mult de 100 pixeli)
                     //   Toast.makeText(getApplicationContext(), "Swipe în jos detectat!", Toast.LENGTH_SHORT).show();
                     pauseEvent();
                     swipeDownShow = true;
                     String[] menu = {"Viață", "Fără poliție", "Arme", "Conduceți pe apă", "Explodează tot", "Armură", "Viteză"};

                     String[] cheat = {"aspirine", "leavemealone", "nUTTERTOOLS", "seaways", "bigbang", "preciousprOTeCTion", "Onspeed"};

AlertDialog.Builder builder = new AlertDialog.Builder(NvEventQueueActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
builder.setItems(menu, new DialogInterface.OnClickListener() {
    @Override
    public void onClick(DialogInterface dialog, int which) {
            simulateUserInput(cheat[which]);
            swipeDownShow =false;
        DoResumeEvent();
    }
});
builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
DoResumeEvent();
swipeDownShow =false;
        }
    });
builder.show();
                                                
                }
                }
                return true;
            }
        });
        
        
        
     //   this.assetMgr = getAssets();
        jniNvAPKInit(getAssets());

		if(supportPauseResume)
		{
		    System.out.println("Calling init(false)");
            init(false);
        }
        handler = new Handler();
//        if(wantsAccelerometer && (mSensorManager == null)) {
//            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        }

        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        NvUtil.getInstance().setActivity(this);
        NvAPKFileHelper.getInstance().setContext(this);

        display = ((WindowManager)this.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        systemInit();

        hideSystemUI();



    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

private float getHeightLimit() {
        return getResources().getDisplayMetrics().heightPixels / 6.0f;  // 1/3 din înălțimea ecranului
    }

    public void onWindowFocusChanged(boolean hasFocus) {

        super.onWindowFocusChanged(hasFocus);

        hideSystemUI();
    }

    /**
     * Implementation function: defined in libnvevent.a
     * The application does not and should not overide this; nv_event handles this internally
     * And remaps as needed into the native calls exposed by nv_event.h
     */
    @Override
    public void onResume() {
        Log.d("NvEvent", "NvEvent onResume");
//        if (this.mSensorManager != null) {
//            this.mSensorManager.registerListener(this, this.mSensorManager.getDefaultSensor(1), this.mSensorDelay);
//        }

        super.onResume();
        this.paused = false;

    }

    /**
     * Implementation function: defined in libnvevent.a
     * The application does not and should not overide this; nv_event handles this internally
     * And remaps as needed into the native calls exposed by nv_event.h
     */
    @Override
    public void onRestart() {
        Log.d("NvEvent", "NvEvent onRestart");

        super.onRestart();
    }
    
    /**
     * Implementation function: defined in libnvevent.a
     * The application does not and should not overide this; nv_event handles this internally
     * And remaps as needed into the native calls exposed by nv_event.h
     */
    public void onPause() {
        Log.d("NvEvent", "**** onPause");
        super.onPause();
        this.paused = true;
        if (this.ResumeEventDone) {
            Log.d("NvEvent", "java is invoking pauseEvent(), this will block until\nthe client calls NVEventPauseProcessed");
            pauseEvent();
            Log.d("NvEvent", "pauseEvent() returned");
        }
    }
    
    /**
     * Implementation function: defined in libnvevent.a
     * The application does not and should not overide this; nv_event handles this internally
     * And remaps as needed into the native calls exposed by nv_event.h
     */
	@Override
    public void onStop() {
        Log.d("NvEvent", "NvEvent onStop");
//        if (this.mSensorManager != null) {
//            this.mSensorManager.unregisterListener(this);
//        }

        super.onStop();
    }

    /**
     * Implementation function: defined in libnvevent.a
     * The application should *probably* not overide this; nv_event handles this internally
     * And remaps as needed into the native calls exposed by nv_event.h
	 *
	 * NOTE: An application may need to override this if the app has an
	 *       in-process instance of the Service class and the native side wants to
	 *       keep running. The app would want to execute the content of the
	 *       if(supportPauseResume) clause when it is time to exit.
     */
    @Override
    public void onDestroy() {
        Log.d("NvEvent", "NvEvent onDestroy");
//        if (this.supportPauseResume) {
//            quitAndWait();
//            finish();
//        }
        finishAndRemoveTask();
        super.onDestroy();
        systemCleanup();
        //systemCleanup();
    }

    /**
     * Implementation function: defined in libnvevent.a
     * The application does not and should not overide this; nv_event handles this internally
     * And remaps as needed into the native calls exposed by nv_event.h
     */

    public void mSleep(long milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
        }
    }

    public void DoResumeEvent() {
        if (!this.waitingForResume) {
            new Thread(new Runnable() {
                public void run() {
                    NvEventQueueActivity.this.waitingForResume = true;
                    while (NvEventQueueActivity.this.cachedSurfaceHolder == null) {
                        NvEventQueueActivity.this.mSleep(1000);
                    }
                    NvEventQueueActivity.this.waitingForResume = false;
                    NvEventQueueActivity.this.resumeEvent();
                    NvEventQueueActivity.this.ResumeEventDone = true;
                }
            }).start();
        }
    }

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Auto-generated method stub
	}

    /**
     * Implementation function: defined in libnvevent.a
     * The application does not and should not overide this; nv_event handles this internally
     * And remaps as needed into the native calls exposed by nv_event.h
     */
	public void onSensorChanged(SensorEvent event) {
		// Auto-generated method stub
//		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
//        {
//            float roll = 0.0f;
//            float pitch = 0.0f;
//            switch (this.display.getRotation()) {
//                case 0:
//                    roll = -event.values[0];
//                    pitch = event.values[1];
//                    break;
//                case 1:
//                    roll = event.values[1];
//                    pitch = event.values[0];
//                    break;
//                case 2:
//                    roll = event.values[0];
//                    pitch = event.values[1];
//                    break;
//                case 3:
//                    roll = -event.values[1];
//                    pitch = event.values[0];
//                    break;
//            }
//            accelerometerEvent(roll, pitch, event.values[2]);
//        }
	}
    
    /**
     * Implementation function: defined in libnvevent.a
     * The application does not and should not overide this; nv_event handles this internally
     * And remaps as needed into the native calls exposed by nv_event.h
     */
@Override
    public boolean onTouchEvent(MotionEvent event)
    {
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
        swipeDownShow = false;  // Resetarea flag-ului pentru swipeDown
    }
    gestureDetector.onTouchEvent(event);
        boolean ret = super.onTouchEvent(event);
      
        if (!ret)
        {
        	if (wantsMultitouch)
        	{
	        	int count = 0;
        		int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
	        	// marshal up the data.
	        	int numEvents = event.getPointerCount();
	        	for (int i=0; i<numEvents; i++)
	        	{
	        		// only use pointers 0 and 1...
	        		int index = event.getPointerId(i);
	        		if (index < 2)
	        		{
	        			if (count == 0)
	        			{
	        				x1 = (int)event.getX(i);
	        				y1 = (int)event.getY(i);
	        				count++;
	        			}
	        			else if (count == 1)
	        			{
	        				x2 = (int)event.getX(i);
	        				y2 = (int)event.getY(i);
		        			count++;
	        			}
	        		}
	        	}
	            ret = multiTouchEvent(event.getAction(), count, x1, y1, x2, y2, event);
        	}
        	else // old style input.*/
        	{
                ret = touchEvent(event.getAction(), (int)event.getX(), (int)event.getY(), event);
        	}
        }
        if (swipeDownShow) {
        ret = super.onTouchEvent(event);
        }
        return ret;
    }
    /**
     * Implementation function: defined in libnvevent.a
     * The application does not and should not overide this; nv_event handles this internally
     * And remaps as needed into the native calls exposed by nv_event.h
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        boolean ret = false;

        if (keyCode == 24 || keyCode == 25) {
            return super.onKeyDown(keyCode, event);
        }
        if (keyCode == 89 || keyCode == 85 || keyCode == 90) {
            return false;
        }
        if (!(keyCode == 82 || keyCode == 4)) {
            ret = super.onKeyDown(keyCode, event);
        }
        if (!ret) {
            ret = keyEvent(event.getAction(), keyCode, event.getUnicodeChar(), event.getMetaState(), event);
        }
        return ret;
    }
 
    /**
     * Implementation function: defined in libnvevent.a
     * The application does not and should not overide this; nv_event handles this internally
     * And remaps as needed into the native calls exposed by nv_event.h
     */
     
     private void simulateUserInput(String input) {
    for (char c : input.toCharArray()) {
        // Convert character to keycode
        int keyCode = KeyEvent.keyCodeFromString("KEYCODE_" + Character.toUpperCase(c));

        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
            continue; // Skip unknown characters
        }

        // Generate KeyEvent.ACTION_DOWN
        if (Character.isUpperCase(c)) {
        KeyEvent keyDownEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        dispatchKeyEvent(keyDownEvent);
        simulateMultiTouch();
        KeyEvent keyDownEvent2 = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
        dispatchKeyEvent(keyDownEvent2);
        } else {
        KeyEvent keyDownEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        dispatchKeyEvent(keyDownEvent);
        }
        simulateMultiTouch();

       
        
     //   simulateMultiTouch();

}
simulateMultiTouch();
        
    }
    


    
    // Simulate two taps
    

private void simulateMultiTouch() {
    long downTime = SystemClock.uptimeMillis();

    // Pointer coordinates
    int x1 = getScreenWidth()/2; // Left-center
    int y1 = getScreenHeight() - 15;
    int x2 = getScreenWidth()/2; // Right-center
    int y2 = getScreenHeight() - 20;

    // Simulate ACTION_DOWN for the first pointer
    multiTouchEvent(MotionEvent.ACTION_DOWN, 1, x1, y1, 0, 0, null);

    // Simulate ACTION_POINTER_DOWN for the second pointer
    multiTouchEvent(MotionEvent.ACTION_POINTER_DOWN, 2, x1, y1, x2, y2, null);

    // Simulate ACTION_MOVE (both fingers still touching)
    multiTouchEvent(MotionEvent.ACTION_MOVE, 2, x1, y1, x2, y2, null);

    // Simulate ACTION_POINTER_UP for the second pointer
    multiTouchEvent(MotionEvent.ACTION_POINTER_UP, 1, x1, y1, 0, 0, null);

    // Simulate ACTION_UP for the first pointer
    multiTouchEvent(MotionEvent.ACTION_UP, 0, 0, 0, 0, 0, null);
}

private int getScreenWidth() {
    return getResources().getDisplayMetrics().widthPixels;
}

private int getScreenHeight() {
    return getResources().getDisplayMetrics().heightPixels;
}


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if (keyCode == 115 && Build.VERSION.SDK_INT >= 11)
        {
            boolean capsLockOn = event.isCapsLockOn();
            keyEvent(capsLockOn ? 3 : 4, 115, 0, 0, event);
        }
        if (keyCode == 89 || keyCode == 85 || keyCode == 90)
        {
            return false;
        }
        boolean onKeyUp = super.onKeyUp(keyCode, event);
        if (onKeyUp)
        {
            return onKeyUp;
        }
        return keyEvent(event.getAction(), keyCode, event.getUnicodeChar(), event.getMetaState(), event);
    }

    public boolean InitEGLAndGLES2(int EGLVersion) {
        System.out.println("InitEGLAndGLES2");
        if (this.cachedSurfaceHolder == null) {
            System.out.println("InitEGLAndGLES2 failed, cachedSurfaceHolder is null");
            return false;
        }
        boolean eglInitialized = true;
        if (this.eglContext == null) {
            eglInitialized = false;
            if (EGLVersion >= 3) {
                try {
                    eglInitialized = initEGL(3, 24);
                } catch (Exception e) {
                }
                System.out.println("initEGL 3 " + eglInitialized);
            }
            if (!eglInitialized) {
                this.configAttrs = null;
                try {
                    eglInitialized = initEGL(2, 16);
                } catch (Exception e2) {
                }
                System.out.println("initEGL 2 " + eglInitialized);
                if (!eglInitialized) {
                    eglInitialized = initEGL(2, 16);
                    System.out.println("initEGL 2 " + eglInitialized);
                }
            }
        }
        if (eglInitialized) {
            System.out.println("Should we create a surface?");
            if (!this.viewIsActive) {
                System.out.println("Yes! Calling create surface");
                createEGLSurface(this.cachedSurfaceHolder);
                System.out.println("Done creating surface");
            }
            this.viewIsActive = true;
            this.SwapBufferSkip = 1;
            return true;
        }
        System.out.println("initEGLAndGLES2 failed, core EGL init failure");
        return false;
    }
		
    /**
     * Implementation function: defined in libnvevent.a
     * The application does not and should not overide this; nv_event handles this internally
     * And remaps as needed into the native calls exposed by nv_event.h
     */

    public boolean systemInit()
    {
        final NvEventQueueActivity act = this;

        Log.d("NvEvent", "ln systemInit");
        setContentView(R.layout.iro_render);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.main_sv);
        this.mSurfaceView = surfaceView;
        this.mRootFrame = findViewById(R.id.main_fl_root);
        SurfaceHolder holder = surfaceView.getHolder();
        holder.setType(2);
        holder.setKeepScreenOn(true);
        surfaceView.setFocusable(true);
        surfaceView.setFocusableInTouchMode(true);

        getWindow().setSustainedPerformanceMode(true);

        DoResumeEvent();
        holder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder holder) {
                boolean firstRun = NvEventQueueActivity.this.cachedSurfaceHolder == null;
                NvEventQueueActivity.this.cachedSurfaceHolder = holder;
                if (!firstRun && NvEventQueueActivity.this.ResumeEventDone) {
                    NvEventQueueActivity.this.resumeEvent();
                }
                boolean unused = NvEventQueueActivity.this.ranInit = true;
                if (NvEventQueueActivity.this.supportPauseResume || !NvEventQueueActivity.this.init(false)) {
                }
                System.out.println("surfaceCreated: w:" + NvEventQueueActivity.this.surfaceWidth + ", h:" + NvEventQueueActivity.this.surfaceHeight);
                NvEventQueueActivity.this.setWindowSize(NvEventQueueActivity.this.surfaceWidth, NvEventQueueActivity.this.surfaceHeight);

                if (firstRun) {
                   // NvEventQueueActivity.this.GamepadReportSurfaceCreated(holder);
                }
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                System.out.println("Surface changed: " + width + ", " + height);
                NvEventQueueActivity.this.surfaceWidth = width;
                NvEventQueueActivity.this.surfaceHeight = height;
                NvEventQueueActivity.this.setWindowSize(NvEventQueueActivity.this.surfaceWidth, NvEventQueueActivity.this.surfaceHeight);
                NvEventQueueActivity.this.hideSystemUI();
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                NvEventQueueActivity.this.pauseEvent();
                NvEventQueueActivity.this.destroyEGLSurface();
                NvEventQueueActivity.this.viewIsActive = false;
            }
        });
        return true;
    }


    /** The number of bits requested for the red component */
    protected int redSize     = 5;
    /** The number of bits requested for the green component */
    protected int greenSize   = 6;
    /** The number of bits requested for the blue component */
    protected int blueSize    = 5;
    /** The number of bits requested for the alpha component */
    protected int alphaSize   = 0;
    /** The number of bits requested for the stencil component */
    protected int stencilSize = 0;
    /** The number of bits requested for the depth component */
    protected int depthSize   = 16;

    /** Attributes used when selecting the EGLConfig */
    protected int[] configAttrs = null;
    /** Attributes used when creating the context */
    protected int[] contextAttrs = null;

    /**
     * Called to initialize EGL. This function should not be called by the inheriting
     * activity, but can be overridden if needed.
     * 
     * @return True if successful
     */
    public boolean initEGL(int esVersion, int depthBits) {
        int i;
        int eglErr;
        if (esVersion > 2 && Build.VERSION.SDK_INT < 21) {
            return false;
        }
        if (this.configAttrs == null) {
            this.configAttrs = new int[]{12344};
        }
        int[] oldConf = this.configAttrs;
        this.configAttrs = new int[((oldConf.length + 3) - 1)];
        int i2 = 0;
        while (i2 < oldConf.length - 1) {
            this.configAttrs[i2] = oldConf[i2];
            i2++;
        }
        int i3 = i2 + 1;
        this.configAttrs[i2] = EGL_RENDERABLE_TYPE;
        if (esVersion == 3) {
            i = i3 + 1;
            this.configAttrs[i3] = EGL_OPENGL_ES3_BIT;
        } else {
            i = i3 + 1;
            this.configAttrs[i3] = 4;
        }
        int i4 = i + 1;
        this.configAttrs[i] = 12344;
        this.contextAttrs = new int[]{EGL_CONTEXT_CLIENT_VERSION, esVersion, 12344};
        if (this.configAttrs == null) {
            this.configAttrs = new int[]{12344};
        }
        int[] oldConfES2 = this.configAttrs;
        this.configAttrs = new int[((oldConfES2.length + 13) - 1)];
        int i5 = 0;
        while (i5 < oldConfES2.length - 1) {
            this.configAttrs[i5] = oldConfES2[i5];
            i5++;
        }
        int i6 = i5 + 1;
        this.configAttrs[i5] = 12324;
        int i7 = i6 + 1;
        this.configAttrs[i6] = this.redSize;
        int i8 = i7 + 1;
        this.configAttrs[i7] = 12323;
        int i9 = i8 + 1;
        this.configAttrs[i8] = this.greenSize;
        int i10 = i9 + 1;
        this.configAttrs[i9] = 12322;
        int i11 = i10 + 1;
        this.configAttrs[i10] = this.blueSize;
        int i12 = i11 + 1;
        this.configAttrs[i11] = 12321;
        int i13 = i12 + 1;
        this.configAttrs[i12] = this.alphaSize;
        int i14 = i13 + 1;
        this.configAttrs[i13] = 12326;
        int i15 = i14 + 1;
        this.configAttrs[i14] = this.stencilSize;
        int i16 = i15 + 1;
        this.configAttrs[i15] = 12325;
        int i17 = i16 + 1;
        this.configAttrs[i16] = depthBits;
        int i18 = i17 + 1;
        this.configAttrs[i17] = 12344;
        this.egl = (EGL10) EGLContext.getEGL();
        this.egl.eglGetError();
        this.eglDisplay = this.egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        System.out.println("eglDisplay: " + this.eglDisplay + ", err: " + this.egl.eglGetError());
        boolean ret = this.egl.eglInitialize(this.eglDisplay, new int[2]);
        System.out.println("EglInitialize returned: " + ret);
        if (!ret || (eglErr = this.egl.eglGetError()) != 12288) {
            return false;
        }
        System.out.println("eglInitialize err: " + eglErr);
        EGLConfig[] config = new EGLConfig[20];
        int[] num_configs = new int[1];
        this.egl.eglChooseConfig(this.eglDisplay, this.configAttrs, config, config.length, num_configs);
        System.out.println("eglChooseConfig err: " + this.egl.eglGetError());
        System.out.println("num_configs " + num_configs[0]);
        int score = 16777216;
        int[] val = new int[1];
        for (int i19 = 0; i19 < num_configs[0]; i19++) {
            boolean cont = true;
            int j = 0;
            while (true) {
                if (j >= ((oldConfES2.length - 1) >> 1)) {
                    break;
                }
                this.egl.eglGetConfigAttrib(this.eglDisplay, config[i19], this.configAttrs[j * 2], val);
                if ((val[0] & this.configAttrs[(j * 2) + 1]) != this.configAttrs[(j * 2) + 1]) {
                    cont = false;
                    break;
                }
                j++;
            }
            if (cont) {
                this.egl.eglGetConfigAttrib(this.eglDisplay, config[i19], 12324, val);
                int r = val[0];
                this.egl.eglGetConfigAttrib(this.eglDisplay, config[i19], 12323, val);
                int g = val[0];
                this.egl.eglGetConfigAttrib(this.eglDisplay, config[i19], 12322, val);
                int b = val[0];
                this.egl.eglGetConfigAttrib(this.eglDisplay, config[i19], 12321, val);
                int a = val[0];
                this.egl.eglGetConfigAttrib(this.eglDisplay, config[i19], 12325, val);
                int d = val[0];
                this.egl.eglGetConfigAttrib(this.eglDisplay, config[i19], 12326, val);
                int currScore = ((((Math.abs(r - this.redSize) + Math.abs(g - this.greenSize)) + Math.abs(b - this.blueSize)) + Math.abs(a - this.alphaSize)) << 16) + (Math.abs(d - depthBits) << 8) + Math.abs(val[0] - this.stencilSize);
                if (currScore < score) {
                    for (int j2 = 0; j2 < ((this.configAttrs.length - 1) >> 1); j2++) {
                        this.egl.eglGetConfigAttrib(this.eglDisplay, config[i19], this.configAttrs[j2 * 2], val);
                    }
                    score = currScore;
                    this.eglConfig = config[i19];
                }
            }
        }
        if (this.eglConfig == null) {
            this.configAttrs = null;
            return false;
        }
        this.eglContext = this.egl.eglCreateContext(this.eglDisplay, this.eglConfig, EGL10.EGL_NO_CONTEXT, this.contextAttrs);
        System.out.println("eglCreateContext: " + this.egl.eglGetError());
        this.gl = (GL11) this.eglContext.getGL();
        return true;
    }

    /**
     * Called to create the EGLSurface to be used for rendering. This function should not be called by the inheriting
     * activity, but can be overridden if needed.
     * 
     * @param surface The SurfaceHolder that holds the surface that we are going to render to.
     * @return True if successful
     */
    public boolean createEGLSurface(SurfaceHolder surfaceHolder) {
        this.eglSurface = this.egl.eglCreateWindowSurface(this.eglDisplay, this.eglConfig, surfaceHolder, (int[]) null);
        Log.d("dfs","eglCreateWindowSurface err: " + this.egl.eglGetError());
        int[] iArr = new int[1];
        this.egl.eglQuerySurface(this.eglDisplay, this.eglSurface, 12375, iArr);
        this.surfaceWidth = iArr[0];
        this.egl.eglQuerySurface(this.eglDisplay, this.eglSurface, 12374, iArr);
        this.surfaceHeight = iArr[0];
        Log.d("dfs","checking glVendor == null?");
        if (this.glVendor == null) {
            Log.d("dfs","Making current and back");
            makeCurrent();
            unMakeCurrent();
        }
        Log.d("dfs","Done create EGL surface");
        return true;
    }

    /**
     * Destroys the EGLSurface used for rendering. This function should not be called by the inheriting
     * activity, but can be overridden if needed.
     */
    protected void destroyEGLSurface()
    {
        System.out.println("*** destroyEGLSurface");
        if (eglDisplay != null && eglSurface != null)
            egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        if (eglSurface != null)
            egl.eglDestroySurface(eglDisplay, eglSurface);
        eglSurface = null;
    }

    /**
     * Called to clean up egl. This function should not be called by the inheriting
     * activity, but can be overridden if needed.
     */
    protected void cleanupEGL()
    {
		System.out.println("cleanupEGL");
        destroyEGLSurface();
        if (eglDisplay != null)
            egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        if (eglContext != null)
            egl.eglDestroyContext(eglDisplay, eglContext);
        if (eglDisplay != null)
            egl.eglTerminate(eglDisplay);

        eglDisplay = null;
        eglContext = null;
        eglSurface = null;

		ranInit = false;
		eglConfig = null;

		cachedSurfaceHolder = null;
		surfaceWidth = 0;
		surfaceHeight = 0;
    }

    /**
     * Implementation function: 
     * The application does not and should not overide or call this directly
     * Instead, the application should call NVEventEGLSwapBuffers(),
     * which is declared in nv_event.h
     */

    public boolean swapBuffers()
    {
		//long stopTime;
		//long startTime = nvGetSystemTime();

        if (SwapBufferSkip > 0) {
            SwapBufferSkip--;
            System.out.println("swapBuffer wait");
            return true;
        }
        if (eglSurface == null)
        {
	        System.out.println("eglSurface is NULL");
	        return false;
	    }
        else if (!egl.eglSwapBuffers(eglDisplay, eglSurface))
        {
	        System.out.println("eglSwapBufferrr: " + egl.eglGetError());
	        return false;
	    }
		//stopTime = nvGetSystemTime();
		//String s = String.format("%d ms in eglSwapBuffers", (int)(stopTime - startTime));
		//Log.v("EventAccelerometer", s);
	    
	    return true;
    }    

	public boolean getSupportPauseResume()
	{
		return supportPauseResume;
	}
    
    public int getSurfaceWidth()
    {
    	return surfaceWidth;        
    }
    
    public int getSurfaceHeight()
    {
    	return surfaceHeight;           
    }
   
    /**
     * Implementation function: 
     * The application does not and should not overide or call this directly
     * Instead, the application should call NVEventEGLMakeCurrent(),
     * which is declared in nv_event.h
     */

    public void GetGLExtensions()
    {
        if (!HasGLExtensions && gl != null && this.cachedSurfaceHolder != null)
        {
           // gl.glEnable(GL10.GL_CULL_FACE); // ? сглаживание
            glVendor = gl.glGetString(GL10.GL_VENDOR);
            glExtensions = gl.glGetString(GL10.GL_EXTENSIONS);
            glRenderer = gl.glGetString(GL10.GL_RENDERER);
            glVersion = gl.glGetString(GL10.GL_VERSION);
            System.out.println("Vendor: " + glVendor);
            System.out.println("Extensions " + glExtensions);
            System.out.println("Renderer: " + glRenderer);
            System.out.println("GIVersion: " + glVersion);
            if (this.glVendor != null)
            {
                this.HasGLExtensions = true;
            }
        }
    }

    public boolean makeCurrent() {
        EGLContext eGLContext = this.eglContext;
        if (eGLContext == null) {
            Log.d("NvEvent","eglContext is NULL");
            return false;
        }
        EGLSurface eGLSurface = this.eglSurface;
        if (eGLSurface == null) {
            Log.d("NvEvent","eglSurface is NULL");
            return false;
        }
        if (!this.egl.eglMakeCurrent(this.eglDisplay, eGLSurface, eGLSurface, eGLContext)) {
            EGL10 egl10 = this.egl;
            EGLDisplay eGLDisplay = this.eglDisplay;
            EGLSurface eGLSurface2 = this.eglSurface;
            if (!egl10.eglMakeCurrent(eGLDisplay, eGLSurface2, eGLSurface2, this.eglContext)) {
                Log.d("NvEvent","eglMakeCurrent err: " + this.egl.eglGetError());
                return false;
            }
        }
        GetGLExtensions();
        return true;
    }

	public int getOrientation()
	{
        return display.getOrientation();
	}

    /**
     * Implementation function: 
     * The application does not and should not overide or call this directly
     * Instead, the application should call NVEventEGLUnmakeCurrent(),
     * which is declared in nv_event.h
     */
    public boolean unMakeCurrent()
    {
        if (!egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT))
		{
	        System.out.println("egl(Un)MakeCurrent err: " + egl.eglGetError());
	        return false;
	    }
	    
	    return true;
    }

    /**
     * Called when the Activity is exiting and it is time to cleanup.
     * Kept separate from the {@link #cleanup()} function so that subclasses
     * in their simplest form do not need to call any of the parent class' functions. This to make
     * it easier for pure C/C++ application so that these do not need to call java functions from C/C++
     * code.
     * 
     * @see #cleanup()
     */
    protected void systemCleanup()
    {
        if (ranInit)
            cleanup();
        cleanupEGL();

        //postCleanup();
    }

    public byte[] getClipboardText()
    {
        String retn = " ";

        if(mClipboardManager.getPrimaryClip() != null)
        {
            ClipData.Item item = mClipboardManager.getPrimaryClip().getItemAt(0);
            if(item != null)
            {
                CharSequence sequence = item.getText();
                if(sequence != null)
                {
                    retn = sequence.toString();
                }
            }
        }

        byte[] toReturn = null;
        try
        {
            toReturn = retn.getBytes("windows-1251");
        }
        catch(UnsupportedEncodingException e)
        {

        }
        return toReturn;
    }

    public void setPauseState(boolean z2) {
        if (mAndroidUI == null) {
            mAndroidUI = (FrameLayout) findViewById(R.id.ui_layout);
        }
        runOnUiThread(() -> mAndroidUI.setVisibility(z2 ? View.GONE:View.VISIBLE));
    }

    public void CopyTextToBuffer(String string){
       // String text = edtCopy.getText().toString();
        
       // Toast.makeText(this,"Скопированно в буфер обмена ",Toast.LENGTH_SHORT).show();
    }



}
