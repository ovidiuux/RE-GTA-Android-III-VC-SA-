package com.wardrumstudios.utils;

public class WarBilling extends WarBase {
    public native void notifyChange(String str, int i);
    public native void changeConnection(boolean z);

    public void AddSKU(String str)
    {
    }

    public boolean InitBilling()
    {
        return true;
    }

    public String LocalizedPrice(String str) {
        return "";
    }

    public boolean RequestPurchase(String str)
    {
        return true;
    }

    public void SetBillingKey(String str)
    {
       
    }
}