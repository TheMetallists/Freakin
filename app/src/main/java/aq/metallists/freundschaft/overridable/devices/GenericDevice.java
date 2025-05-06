package aq.metallists.freundschaft.overridable.devices;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public interface GenericDevice {
    String getName();

    void onReceive(Context context, Intent intent);

    void onKeyDown(int keyCode, KeyEvent event);

    void onKeyUp(int keyCode, KeyEvent event);

    boolean isCustomLed();

    void setPttLed(int led);

}
