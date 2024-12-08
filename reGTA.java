package com.iro.regta;

import android.content.Intent;
import android.os.Bundle;
import com.wardrumstudios.utils.WarMedia;

public class reGTA extends WarMedia {
    static {
        System.loadLibrary("reGTA");
    }

    public boolean ServiceAppCommand(String str, String str2) {
        return false;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }
}
