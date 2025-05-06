package aq.metallists.freundschaft.overridable.devices;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.view.KeyEvent;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import aq.metallists.freundschaft.service.events.PTTMessage;
import aq.metallists.freundschaft.tools.LedConsts;
import aq.metallists.freundschaft.tools.Logger;

public class HyteraPNC370SEDevice implements GenericDevice {
    private final Context ctx;

    public HyteraPNC370SEDevice(Context c) {
        ctx = c;
    }

    boolean playing = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (intentAction == null) {
            return;
        }
        switch (intentAction) {
            case "com.slacorp.eptt.android.PTT_PRESS":
                EventBus.getDefault().post(new PTTMessage(true));
                break;
            case "com.slacorp.eptt.android.PTT_RELEASE":
                EventBus.getDefault().post(new PTTMessage(false));
                break;
            case "android.intent.action.CALL_BUTTON":

                break;
            // top button press
            case "com.slacorp.eptt.android.START_EMERGENCY_CALL":
                try {
                    if (!playing) {
                        AssetFileDescriptor afd = ctx.getAssets().openFd("warn.ogg");
                        MediaPlayer mMediaPlayer = new MediaPlayer();
                        mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mMediaPlayer.stop();
                                playing = false;
                            }
                        });
                        afd.close();
                        mMediaPlayer.prepare();
                        mMediaPlayer.start();
                        playing = true;
                    }

                } catch (Exception ex) {
                    Logger.getInstance().e("Error playing PTT Timeout: ", ex);
                }
                break;
            //top button release
            case "com.slacorp.eptt.android.CANCEL_EMERGENCY_CALL":
                break;
            default:
                //Toast.makeText(ctx, "UNK Intent: ".concat(intentAction), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onKeyDown(int keyCode, KeyEvent event) {
        //System.err.println(String.format("KC: %d", keyCode));
    }

    @Override
    public void onKeyUp(int keyCode, KeyEvent event) {
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


    @Override
    public String getName() {
        return "Hytera PNC 370SE";
    }
}
