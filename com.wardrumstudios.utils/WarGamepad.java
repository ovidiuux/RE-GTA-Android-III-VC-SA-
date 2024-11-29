package com.wardrumstudios.utils;

import android.view.ViewParent;

public class WarGamepad extends WarBilling {
    public int GetGamepadType()
    {
        return -1;
    }
    public int GetGamepadButtons()
    {
        return 0;
    }
    public int GetGamepadType(int index) {
      //  System.out.println("**** GetGamepadType()");
        return 0;
    }

    public int GetGamepadButtons(int index) {
       // System.out.println("**** GetGamepadButtons()");
        return 0;
    }

    public float GetGamepadAxis(int index, int axisId) {
      //  System.out.println("**** GetGamepadAxis()");
        return 0.0f;
    }

    public int GetGamepadTrack(int index, int trackId, int coord) {
        return 0;
    }

    public int GetGamepadTrack(int i, int i2)
    {
        return 0;
    }
    public float GetGamepadAxis(int i)
    {
        return 0.0f;
    }

    public native boolean processTouchpadAsPointer(ViewParent viewParent, boolean z);
}