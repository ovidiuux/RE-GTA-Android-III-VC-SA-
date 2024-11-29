package com.nvidia.devtech;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.rockstargames.gtavc/dex-files/0.dex */
public class NvAPKFileHelper {
    private static NvAPKFileHelper instance = new NvAPKFileHelper();
    private static final boolean logAssetFiles = false;
    String[] apkFiles;
    private Context context = null;
    int apkCount = 0;
    int myApkCount = 0;
    boolean hasAPKFiles = false;

    public void setContext(Context context) {
        this.context = context;
    }

    public static NvAPKFileHelper getInstance() {
        return instance;
    }

    private int findInAPKFiles(String filename) {
        if (this.myApkCount == 0) {
            return -1;
        }
        String mp3Test = filename + ".mp3";
        for (int i = 0; i < this.apkFiles.length; i++) {
            if (filename.compareToIgnoreCase(this.apkFiles[i]) == 0 || mp3Test.compareToIgnoreCase(this.apkFiles[i]) == 0) {
                if (filename.compareTo(this.apkFiles[i]) != 0) {
                }
                return i;
            }
        }
        return -1;
    }

    void AddAssetFile(String filename) {
        String[] strArr = this.apkFiles;
        int i = this.myApkCount;
        this.myApkCount = i + 1;
        strArr[i] = filename;
    }

    int getDirectoryListing(AssetManager assets, String dir, int listCount) {
        try {
            if (this.apkFiles == null && listCount > 0) {
                this.apkFiles = new String[listCount];
            }
            String[] myFiles = assets.list(dir);
            if (myFiles.length == 0) {
                if (listCount > 0) {
                    AddAssetFile(dir);
                } else {
                    this.apkCount++;
                }
            } else if (listCount == 0) {
            }
            for (int i = 0; i < myFiles.length; i++) {
                if (myFiles[i].indexOf(46) == -1) {
                    String newFile = dir.length() > 0 ? dir + "/" + myFiles[i] : myFiles[i];
                    getDirectoryListing(assets, newFile, listCount);
                } else if (listCount > 0) {
                    AddAssetFile(dir.length() > 0 ? dir + "/" + myFiles[i] : myFiles[i]);
                } else {
                    this.apkCount++;
                }
            }
        } catch (Exception ex) {
            System.out.println("ERROR: getDirectoryListing " + ex.getMessage());
        }
        return 0;
    }

    void GetAssetList() {
        try {
            InputStream is = this.context.getAssets().open("assetfile.txt");
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            int listCount = Integer.parseInt(r.readLine());
            this.myApkCount = 0;
            if (listCount > 0) {
                this.apkFiles = new String[listCount];
                while (true) {
                    String line = r.readLine();
                    if (line != null) {
                        String[] strArr = this.apkFiles;
                        int i = this.myApkCount;
                        this.myApkCount = i + 1;
                        strArr[i] = line;
                    } else {
                        return;
                    }
                }
            }
        } catch (Exception e) {
            AssetManager assets = this.context.getAssets();
            getDirectoryListing(assets, "", 0);
            getDirectoryListing(assets, "", this.apkCount);
        }
    }

    public NvAPKFile openFileAndroid(String filename) {
        if (!this.hasAPKFiles) {
            this.apkCount = 0;
            this.apkFiles = null;
            GetAssetList();
            this.hasAPKFiles = true;
        }
        int apkIndex = findInAPKFiles(filename);
        if (apkIndex == -1) {
            return null;
        }
        NvAPKFile ret = new NvAPKFile();
        ret.is = null;
        ret.length = 0;
        ret.position = 0;
        ret.bufferSize = 0;
        try {
            ret.is = this.context.getAssets().open(this.apkFiles[apkIndex]);
            int size = ret.is.available();
            ret.length = size;
            ret.is.mark(268435456);
            ret.bufferSize = 1024;
            ret.data = new byte[ret.bufferSize];
            return ret;
        } catch (Exception e) {
            return null;
        }
    }

    public void readFileAndroid(NvAPKFile strm, int size) {
        if (size > strm.bufferSize) {
            strm.data = new byte[size];
            strm.bufferSize = size;
        }
        try {
            strm.is.read(strm.data, 0, size);
            strm.position += size;
        } catch (IOException e) {
        }
    }

    public long seekFileAndroid(NvAPKFile strm, int offset) {
        long totalSkip = 0;
        long skippedBytes = 0;
        try {
            strm.is.reset();
            for (int count = 128; offset > 0 && count > 0; count--) {
                try {
                    skippedBytes = strm.is.skip(offset);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                totalSkip += skippedBytes;
                offset = (int) (offset - skippedBytes);
            }
        } catch (IOException e2) {
        }
        strm.position = (int) totalSkip;
        return totalSkip;
    }

    public void closeFileAndroid(NvAPKFile strm) {
        try {
            strm.is.close();
        } catch (IOException e) {
        }
        strm.data = new byte[0];
        strm.is = null;
    }
}
