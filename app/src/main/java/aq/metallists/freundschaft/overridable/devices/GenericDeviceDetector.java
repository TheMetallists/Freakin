package aq.metallists.freundschaft.overridable.devices;

import android.content.Context;
import aq.metallists.freundschaft.tools.DeviceZooHelper;

public class GenericDeviceDetector {
    public static GenericDevice getDevice(Context c) {
        //TODO: HyteraMode returns something else than GenericShovel
        switch (DeviceZooHelper.getDeviceDesignator()) {
            case "alps TELO_TE590":
                return new TeloTE590Device(c);
            case "unknown PNC 370":
            case "Hytera PNC 370":
                return new HyteraPNC370Device(c);
            case "Hytera PNC360S": // 370SE reflashed
            case "Hytera PNC370SE":
                return new HyteraPNC370SEDevice(c);
            case "Hytera PNC380":
                return new HyteraPNC380Device(c);
            case "Hytera PDC760":
                return new HyteraPDC760Device(c);
            default:
                return new GenericShovel(c);
        }
    }
}
