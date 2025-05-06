package aq.metallists.freundschaft.overridable.devices;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.KeyEvent;
import android.widget.Toast;


import org.greenrobot.eventbus.EventBus;

import java.util.Locale;

import aq.metallists.freundschaft.service.events.PTTMessage;
import aq.metallists.freundschaft.tools.LedConsts;
import aq.metallists.freundschaft.tools.Logger;

public class HyteraPNC370Device implements GenericDevice {
    private final Context ctx;

    public HyteraPNC370Device(Context c) {
        ctx = c;
    }

    @Override
    public String getName() {
        return "Hytera PNC 370";
    }

    private static int PNC_KEYDOWN = 0;
    private static int PNC_KEYUP = 1;
    private static int PNC_PTT = KeyEvent.KEYCODE_F12;
    private static int PNC_GREEN = KeyEvent.KEYCODE_F3;

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.meigsmart.meigkeyaccessibility.onkeyevent".equals(intent.getAction())) {
            int action = intent.getIntExtra("action", -1);
            int kCode = intent.getIntExtra("keycode", -1);

            if (kCode == PNC_PTT) {
                if (action == PNC_KEYDOWN) {
                    EventBus.getDefault().post(new PTTMessage(true));
                } else {
                    EventBus.getDefault().post(new PTTMessage(false));
                }
            }

            Logger.getInstance().w(String.format(Locale.CANADA, "A: %d; Kc: %d", action, kCode));
            return;
        }

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

    @Override
    public void onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F12) {
            //EventBus.getDefault().post(new PTTMessage(true));
        }
    }

    @Override
    public void onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F12) {
            //EventBus.getDefault().post(new PTTMessage(false));
        }
    }

    @Override
    public boolean isCustomLed() {
        return true;
    }

    @Override
    public void setPttLed(int mode) {
        int color = 16776960;
        if (mode == LedConsts.LED_RX) {
            color = Color.GREEN;
        } else if (mode == LedConsts.LED_TX) {
            color = Color.RED;
        }
        Notification notification = new Notification();
        notification.ledARGB = color;
        notification.flags = 1024;
        NotificationManager nm = (NotificationManager) this.ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(19871103);
        nm.notify(19871103, notification);
    }

}
