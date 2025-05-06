package aq.metallists.freundschaft.overridable.devices;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.view.KeyEvent;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import aq.metallists.freundschaft.service.events.PTTMessage;
import aq.metallists.freundschaft.tools.Logger;

public class TeloTE590Device implements GenericDevice {
    private final Context ctx;

    public TeloTE590Device(Context c) {
        ctx = c;
    }

    @Override
    public String getName() {
        return "ALPS/Telo TE-590";
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (intentAction == null) {
            return;
        }
        switch (intentAction) {
            case "android.intent.action.PTT.down":
                EventBus.getDefault().post(new PTTMessage(true));
                break;
            case "android.intent.action.PTT.up":
                EventBus.getDefault().post(new PTTMessage(false));
                break;
            default:
                Toast.makeText(ctx, "UNK Intent: ".concat(intentAction), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_STEM_3) {
            try {
                AssetFileDescriptor afd = ctx.getAssets().openFd("warn.ogg");
                MediaPlayer mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                mMediaPlayer.prepare();
                mMediaPlayer.start();
                mMediaPlayer.wait();
            } catch (Exception ex) {
                Logger.getInstance().e("Error playing PTT Timeout: ", ex);
            }
        }
        // topb btn 269 -> top button above ptt
        // knob cw 271
        // knob ccw 270
        // emg btn 267 //KEYCODE_STEM_3
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
