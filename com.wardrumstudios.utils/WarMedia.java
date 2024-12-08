package com.wardrumstudios.utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import com.nvidia.devtech.NvUtil;

import java.io.File;

public class WarMedia extends WarGamepad
{
	private String baseDirectory;
	private String baseDirectoryRoot;
	private String apkFileName;
	
	public String GetGameBaseDirectory() {
		if (Environment.getExternalStorageState().equals("mounted"))
		{
			try
			{
				File externalFilesDir = getExternalFilesDir(null);
				String absolutePath = externalFilesDir.getAbsolutePath();
				this.baseDirectoryRoot = absolutePath.substring(0, absolutePath.indexOf("/Android"));
				return getFilesDir().getAbsolutePath() + "/";
			} catch (Exception e)
			{
			}
		}
		return "";
	}
	
	public void onCreate(Bundle bundle)
	{
		apkFileName = getApplicationContext().getPackageName();
		
		this.baseDirectory = GetGameBaseDirectory();
		
		NvUtil.getInstance().setActivity(this);
		NvUtil.getInstance().setAppLocalValue("STORAGE_ROOT", this.baseDirectory);
		NvUtil.getInstance().setAppLocalValue("STORAGE_ROOT_BASE", this.baseDirectoryRoot);
		
		super.onCreate(bundle);
	}
	public void ShowKeyboard(int i)
	{
		//"**** ShowKeyboard");
	}
	
	public boolean IsKeyboardShown()
	{
		//"**** IsKeyboardShown");
		return false;
	}
	
	public void PlayMovie(String str, float f)
	{
		//"**** PlayMovie");
	}
	
	public void PlayMovieInFile(String str, float f, int i, int i2)
	{
		//"**** PlayMovieInFile");
		
	}
	
	public void PlayMovieInWindow(String str, int i, int i2, int i3, int i4, float f, int i5, int i6, int i7) {
		//"**** PlayMovieInWindow");
	}
	
	public void StopMovie() {
		//"**** StopMovie");
	//	this.resumeEvent();
	}
	
	public void MovieSetSkippable(boolean z)
	{
		//"**** MovieSetSkippable");
	}
	
	public int IsMoviePlaying()
	{
		//"**** IsMoviePlaying");
		return 0;
	}
	
	public boolean DeleteFile(String str) {
    File file = new File(baseDirectory + "/" + str.replace('\\', '/'));
    return file.exists() && file.delete();
    }
	
	public boolean FileRename(String str, String str2, int i) {
        new File(String.valueOf(this.baseDirectory) + "/" + str.replace('\\', '/')).renameTo(new File(String.valueOf(this.baseDirectory) + "/" + str2.replace('\\', '/')));
        return true;
    }
	public int GetDeviceLocale() {
		//"**** GetDeviceLocale");
		return 0;
	}
	
	public boolean IsPhone() // TODO: implement this
	{
		//"**** IsPhone");
		return true;
	}
	
	public int GetDeviceType() // TODO: implement this
	{
		
		int i = 0;
		//"Build info version device  " + Build.DEVICE);
		//"Build MANUFACTURER  " + Build.MANUFACTURER);
		//"Build BOARD  " + Build.BOARD);
		//"Build DISPLAY  " + Build.DISPLAY);
		//"Build CPU_ABI  " + Build.CPU_ABI);
		//"Build CPU_ABI2  " + Build.CPU_ABI2);
		//"Build HARDWARE  " + Build.HARDWARE);
		//"Build MODEL  " + Build.MODEL);
		//"Build PRODUCT  " + Build.PRODUCT);
		int i2 = 0;
		int numberOfProcessors = 1 * 4;
		int i3 = 8 * 64;
		if (IsPhone())
		{
			i = 1;
		}
		return i + i2 + numberOfProcessors + i3;
	}
	
	public int GetDeviceInfo(int i) // TODO: implement this
	{
		switch (i) {
			case 0:
			return 1; // num of cpu
			case 1:
			return 1; // touch screen
			default:
			return -1;
		}
	}
	
	public String GetAndroidBuildinfo(int i) {
		//"**** GetAndroidBuildinfo");
		switch (i) {
			case 0:
			return Build.MANUFACTURER;
			case 1:
			return Build.PRODUCT;
			case 2:
			return Build.MODEL;
			default:
			return "UNKNOWN";
		}
	}
	
	public String OBFU_GetDeviceID() {
		//"**** OBFU_GetDeviceID");
		return "no id";
	}
	
	public String FileGetArchiveName(int type) {
		switch (type) {
			case 0:
			return this.apkFileName;
			case 1:
			return "";
			case 2:
			return "";
			default:
			return "";
		}
	}
	
	public boolean IsAppInstalled(String str) {
		//"**** IsAppInstalled");
		return false;
	}
	
	public void OpenLink(String str)
	{
		startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str)));
		//"**** OpenLink");
	}
	
	public void LoadAllGamesFromCloud() {
		//"**** LoadAllGamesFromCloud");
	}
	
	public String LoadGameFromCloud(int i, byte[] bArr) {
		//"**** LoadGameFromCloud");
		return "";
	}
	
	public void SaveGameToCloud(int i, byte[] bArr, int i2) {
		//"**** SaveGameToCloud");
	}
	
	public boolean IsCloudAvailable() {
		//"**** IsCloudAvailable");
		return false;
	}
	
	public boolean NewCloudSaveAvailable(int i) {
		//"**** NewCloudSaveAvailable");
		return false;
	}
	
	public void MovieKeepAspectRatio(boolean z) {
		//"**** MovieKeepAspectRatio");
	}
	
	public void MovieSetText(String str, boolean z, boolean z2) {
		//"**** MovieSetText");
	}
	
	public void MovieDisplayText(boolean z) {
		//"**** MovieDisplayText");
	}
	
	public void MovieClearText(boolean z) {
		//"**** MovieClearText");
	}
	
	public void MovieSetTextScale(int i) {
		//"**** MovieSetTextScale");
	}
	
	public int GetSpecialBuildType() {
		//"**** GetSpecialBuildType");
		return 0;
	}
	
	public void SendStatEvent(String str) {
		//"**** SendStatEvent");
	}
	
	public void SendStatEvent(String eventId, boolean timedEvent) {
		//"**** SendStatEvent");
	}
	
	public void SendStatEvent(String eventId, String paramName, String paramValue, boolean timedEvent) {
		//"**** SendStatEvent");
	}
	
	public void SendTimedStatEventEnd(String eventId) {
		//"**** SendTimedStatEventEnd");
	}
	
	public void SendStatEvent(String str, String str2, String str3) {
		//"**** SendStatEvent1");
	}
	
	public int GetTotalMemory() {
		//"**** GetTotalMemory");
		return 0;
	}
	
	public int GetLowThreshhold() {
		//"**** GetLowThreshhold");
		return 0;
	}
	
	public int GetAvailableMemory() {
		//"**** GetAvailableMemory");
		return 0;
	}
	
	public float GetScreenWidthInches() {
		//"**** GetScreenWidthInches");
		return 0.0f;
	}
	
	public String GetAppId()
	{
		//"**** GetAppId");
		return "";
	}
	
	public void ScreenSetWakeLock(boolean z) {
		//"**** ScreenSetWakeLock");
	}
	
	public boolean ServiceAppCommand(String cmd, String str2) {
		//"**** ServiceAppCommand " + cmd + " " + str2);
		
		if (cmd.equalsIgnoreCase("ForceGermanBuild")) {
			return true;
		}
		return false;
	}
	
	public int ServiceAppCommandValue(String str, String str2) {
		//"**** ServiceAppCommandValue " + str + " " + str2);
		return 0;
	}
	
	public boolean ServiceAppCommandInt(String cmd, int args) {
		//"**** ServiceAppCommandInt " + cmd + " " + args);
		return false;
	}
	
	public boolean isWiFiAvailable() {
		//"**** isWiFiAvailable ");
		return false;
	}
	
	public boolean isNetworkAvailable() {
		//"**** isNetworkAvailable ");
		return false;
	}
	
	public boolean isTV() {
		//"**** isTV ");
		return false;
	}
	
	public void CreateTextBox(int id, int x, int y, int x2, int y2) {
		//"**** CreateTextBox ");
	}
	
	public boolean ConvertToBitmap(byte[] data, int length) {
		//"**** ConvertToBitmap ");
		return false;
	}
	
	public void VibratePhone(int numMilliseconds) {
		//"**** VibratePhone ");
	}
	
	public void VibratePhoneEffect(int effect) {
		//"**** VibratePhoneEffect ");
	}
	
	public void PlayMovieInWindow(String inFilename, int x, int y, int width, int height, float inVolume, int inOffset, int inLength, int looping, boolean forceSize) {
	}
}
