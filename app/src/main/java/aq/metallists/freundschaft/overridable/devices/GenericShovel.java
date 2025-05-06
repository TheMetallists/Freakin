package aq.metallists.freundschaft.overridable.devices;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.widget.Toast;
import androidx.preference.PreferenceManager;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;

import aq.metallists.freundschaft.R;
import aq.metallists.freundschaft.service.events.PTTMessage;

public class GenericShovel implements GenericDevice {
    private boolean pttUseVD = false;
    private boolean pttUseVU = false;

    public GenericShovel(Context c) {
        // external ptt settings
        String pttMode = PreferenceManager.getDefaultSharedPreferences(c).getString("opt_ptt_mode", c.getString(R.string.acc_ptt_none));
        if (pttMode.equals(c.getString(R.string.acc_ptt_kup))) {
            this.pttUseVU = true;
        } else if (pttMode.equals(c.getString(R.string.acc_ptt_kdn))) {
            this.pttUseVD = true;
        } else {
            this.pttUseVU = false;
            this.pttUseVD = false;
        }
    }

    @Override
    public String getName() {
        return "Generic Shovel";
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Todo: this is debug!
        String intentAction = intent.getAction();
        assert intentAction != null;
        if (intentAction.equals("android.intent.action.MEDIA_BUTTON")) {
            KeyEvent keyEvent = intent.getParcelableExtra("android.intent.extra.KEY_EVENT");
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
        if ((this.pttUseVU && keyCode == KeyEvent.KEYCODE_VOLUME_UP) ||
                (this.pttUseVD && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            EventBus.getDefault().post(new PTTMessage(true));
        }
    }

    @Override
    public void onKeyUp(int keyCode, KeyEvent event) {
        if ((this.pttUseVU && keyCode == KeyEvent.KEYCODE_VOLUME_UP) ||
                (this.pttUseVD && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            EventBus.getDefault().post(new PTTMessage(false));
        }
    }

    @Override
    public boolean isCustomLed() {
        return false;
    }

    @Override
    public void setPttLed(int led) {

    }


}
