package aq.metallists.freundschaft.overridable.devices;

import android.content.Context;

public class HyteraPNC380Device extends HyteraPNC370Device {
    public HyteraPNC380Device(Context c) {
        super(c);
    }

    @Override
    public String getName() {
        return "Hytera PNC 380";
    }
}
