package aq.metallists.freundschaft.tools;

import android.content.Context;
import android.os.Build;

import java.util.Locale;

import aq.metallists.freundschaft.overridable.devices.GenericDeviceDetector;

public class DeviceZooHelper {
    public static String getDeviceDesignator() {
        return String.format(Locale.CANADA, "%s %s", Build.MANUFACTURER, Build.MODEL);
    }

    public static String getDeviceDriverName(Context c) {
        return GenericDeviceDetector.getDevice(c).getName();
    }
}
