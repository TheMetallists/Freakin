package aq.metallists.freundschaft.overridable.devices;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;

import aq.metallists.freundschaft.service.events.PTTMessage;

public class HyteraPDC760Device implements GenericDevice {
    public final Context ctx;

    public HyteraPDC760Device(Context c) {
        ctx = c;
    }

    @Override
    public String getName() {
        return "Hytera PDC760";
    }

    @Override
    public void onReceive(Context context, Intent intent) {
// Todo: this is debug!
        String intentAction = intent.getAction();
        assert intentAction != null;
        if (intentAction.equals("android.intent.action.MEDIA_BUTTON")) {
            KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra("android.intent.extra.KEY_EVENT");
            assert keyEvent != null;
            int action = keyEvent.getAction();
            int keyCode = keyEvent.getKeyCode();

            Toast.makeText(context, String.format(Locale.CANADA, "MB-KC: %d -> %d", action, keyCode), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "IA: ".concat(intentAction), Toast.LENGTH_LONG).show();
        }
    }

    private boolean lastPTT = false;

    @Override
    public void onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEMO_APP_1) {
            EventBus.getDefault().post(new PTTMessage(!lastPTT));
            lastPTT = !lastPTT;
        }
    }

    @Override
    public void onKeyUp(int keyCode, KeyEvent event) {
    }

    @Override
    public boolean isCustomLed() {
        return false;
    }

    @Override
    public void setPttLed(int led) {

    }

}
