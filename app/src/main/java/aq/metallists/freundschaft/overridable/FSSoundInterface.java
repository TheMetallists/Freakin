package aq.metallists.freundschaft.overridable;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;

import aq.metallists.freundschaft.FSRoomMember;
import aq.metallists.freundschaft.MainActivity;
import aq.metallists.freundschaft.R;
import aq.metallists.freundschaft.service.events.PAudioLevelMSG;
import aq.metallists.freundschaft.service.events.PConnectedMessage;
import aq.metallists.freundschaft.service.events.PConnectingMessage;
import aq.metallists.freundschaft.service.events.PNetListMessage;
import aq.metallists.freundschaft.service.events.PUserListMessage;
import aq.metallists.freundschaft.service.events.PInboundXmissionMSG;
import aq.metallists.freundschaft.tools.Logger;


public class FSSoundInterface {

    AudioTrack line;
    AudioRecord micline;
    boolean outStarted = false;
    Context ctx;

    @SuppressLint("MissingPermission")
    public FSSoundInterface(Context ctxx) throws Exception {
        line = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                8000 * 2, AudioTrack.MODE_STREAM);


        micline = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, 3200 * 2);
        micline.startRecording();

        this.ctx = ctxx;
    }

    public void abort() {
        this.micline.stop();
        this.line.stop();
        this.micline = null;
        this.line = null;
    }


    public void playVoicePacket(byte[] packet) {
        line.write(packet, 0, packet.length);
        if (!this.outStarted) {
            line.play();
            this.outStarted = true;
        }
    }

    public int recordVoicePacket(byte[] packet) {
        if (this.micline != null) {
            return this.micline.read(packet, 0, packet.length);
        }

        return 0;
    }


    public void setInboundXmission(boolean newPtt) {
        EventBus.getDefault().post(new PInboundXmissionMSG(newPtt));
        if (newPtt) {
            this.line.play();
        } else {
            this.line.flush();
            /*try {
                this.line.wait();
            } catch (Exception x) {
                x.printStackTrace();
            }*/
            this.line.stop();
        }
    }


    public void setPtt(boolean newPtt) {

        /*if (newPtt) {
            if (micline == null) {
                micline = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, 3200 * 2);
                micline.startRecording();
            }
        } else {
            if (micline != null) {
                micline = null;
            }
        }*/
    }


    public void setClientList(FSRoomMember[] clients) {
        ArrayList<FSRoomMember> uList = new ArrayList<>(Arrays.asList(clients));
        EventBus.getDefault().post(new PUserListMessage(uList));
    }

    public void setNetworksList(String[] networks) {
        ArrayList<String> netlist = new ArrayList<>(Arrays.asList(networks));
        EventBus.getDefault().post(new PNetListMessage(netlist));
    }


    public void setConnected(boolean connected) {
        EventBus.getDefault().post(new PConnectedMessage(connected));
    }

    public void onDisconnected(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(
                    "CONNECTION_ERRORS",
                    this.ctx.getString(R.string.notif_chan_failures_name),
                    NotificationManager.IMPORTANCE_NONE
            );
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) this.ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
        }


        Notification nt = new NotificationCompat.Builder(this.ctx, "CONNECTION_ERRORS")
                .setContentTitle(this.ctx.getText(R.string.svc_not_title))
                .setContentText("Disconnected: ".concat(message).concat(", reconnecting in 5 sec..."))
                .setSmallIcon(R.drawable.ic_freakin_tray)
                //.setLights(Color.RED, 500, 500)
                .setContentIntent(PendingIntent.getActivity(
                        this.ctx, 0, new Intent(this.ctx, MainActivity.class), PendingIntent.FLAG_MUTABLE)
                )
                .setTimeoutAfter(8000)
                .build();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager nm = (NotificationManager) this.ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(4, nt);
        } else {
            Logger.getInstance().e("Unable to notify about a crash, because of Build.VERSION.SDK_INT >= Build.VERSION_CODES.M");
        }

    }


    public void setConnecting(boolean isConn) {
        EventBus.getDefault().post(new PConnectingMessage(isConn));
    }


    public void setAudioLevel(short tgtLevel) {
        EventBus.getDefault().post(new PAudioLevelMSG(tgtLevel));
    }

    MediaPlayer mMediaPlayer;


    public void pttTimedOut() {
        try {
            AssetFileDescriptor afd = ctx.getAssets().openFd("ptt_timeout.ogg");
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception ex) {
            Logger.getInstance().e("Error playing PTT Timeout: ", ex);
        }
    }

}
