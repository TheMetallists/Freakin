package aq.metallists.freundschaft.overridable;

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
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Arrays;

import aq.metallists.freundschaft.FSRoomMember;
import aq.metallists.freundschaft.MainActivity;
import aq.metallists.freundschaft.R;
import aq.metallists.freundschaft.tools.Logger;


public class FSVedroidSoundInterface implements FSSoundInterface, SoundLevelReceiver {

    AudioTrack line;
    AudioRecord micline;
    boolean outStarted = false;
    Context ctx;

    public FSVedroidSoundInterface(Context ctxx) throws Exception {
        line = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                8000 * 2, AudioTrack.MODE_STREAM);


        micline = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, 3200 * 2);
        micline.startRecording();

        this.ctx = ctxx;
    }

    @Override
    public void playVoicePacket(byte[] packet) {
        line.write(packet, 0, packet.length);
        if (!this.outStarted) {
            line.play();
            this.outStarted = true;
        }
    }

    @Override
    public int recordVoicePacket(byte[] packet) {
        if (this.micline != null) {
            return this.micline.read(packet, 0, packet.length);
        }

        return 0;
    }

    @Override
    public void setVox(boolean newPtt) {
        Intent itt = new Intent("aq.metallists.vox");
        itt.putExtra("newvox", newPtt);
        LocalBroadcastManager.getInstance(this.ctx).sendBroadcast(itt);
    }

    @Override
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

    @Override
    public void setClientList(FSRoomMember[] clients) {
        // newUserList

        ArrayList<FSRoomMember> uList = new ArrayList<>(Arrays.asList(clients));

        Bundle bundle = new Bundle();
        bundle.putSerializable("newUserList", uList);
        Intent itt = new Intent("aq.metallists.userlist");
        itt.putExtras(bundle);

        LocalBroadcastManager.getInstance(this.ctx).sendBroadcast(itt);

    }

    public void setNetworksList(String[] networks) {
        ArrayList<String> netlist = new ArrayList<>(Arrays.asList(networks));

        Bundle bundle = new Bundle();
        bundle.putSerializable("newNetList", netlist);
        Intent itt = new Intent("aq.metallists.netlist");
        itt.putExtras(bundle);

        LocalBroadcastManager.getInstance(this.ctx).sendBroadcast(itt);
    }

    @Override
    public void setConnected(boolean connected) {
        Intent itt = new Intent("aq.metallists.connected");
        itt.putExtra("newconnected", connected);
        LocalBroadcastManager.getInstance(this.ctx).sendBroadcast(itt);
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

    @Override
    public void setConnecting(boolean isConn) {
        Intent itt = new Intent("aq.metallists.connecting");
        itt.putExtra("newconnecting", isConn);
        LocalBroadcastManager.getInstance(this.ctx).sendBroadcast(itt);
    }

    @Override
    public void setAudioLevel(short tgtLevel) {
        Intent itt = new Intent("aq.metallists.newAudioLevel");
        itt.putExtra("level", tgtLevel);
        LocalBroadcastManager.getInstance(this.ctx).sendBroadcast(itt);
    }

    MediaPlayer mMediaPlayer;

    @Override
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
