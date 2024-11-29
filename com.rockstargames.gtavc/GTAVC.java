package com.rockstargames.gtavc;

import android.content.Intent;
import android.os.Bundle;
import com.wardrumstudios.utils.WarMedia;

public class GTAVC extends WarMedia {
public native void WeaponCheat1();
    static {
        System.loadLibrary("MV");
    }

    public boolean ServiceAppCommand(String str, String str2) {
        return false;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }
}
