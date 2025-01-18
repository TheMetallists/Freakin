package aq.metallists.freundschaft.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import aq.metallists.freundschaft.FSClient;
import aq.metallists.freundschaft.MainActivity;
import aq.metallists.freundschaft.R;
import aq.metallists.freundschaft.overridable.FSAndroidUser;
import aq.metallists.freundschaft.overridable.FSUnauthorizedError;
import aq.metallists.freundschaft.overridable.FSVedroidSoundInterface;
import aq.metallists.freundschaft.vocoder.GSMVocoderOptions;

public class FreundschaftService extends Service {
    public FreundschaftService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private final String NOT_CHANNEL = "FRN_NOT_CHANNEL";

    Notification ncc;
    FSAndroidUser usr;
    FSClient fsc;
    LocalBroadcastManager lbm;
    BroadcastReceiver brs;
    SharedPreferences pfm;

    PowerManager.WakeLock wake;
    WifiManager.WifiLock mWifiLock = null;
    private boolean pttInhibit;

    private void setPtt(boolean ptt) {
        if (pttInhibit && ptt)
            return;

        if (fsc != null) {
            fsc.allowCompressor(pfm.getBoolean("snd_effect_compressor", false));
            fsc.allowLPF(pfm.getBoolean("snd_effect_lpf", false));
            fsc.setPtt(ptt);
        }
    }

    @NonNull
    @TargetApi(26)
    private void startMyOwnForeground() {

        NotificationChannel chan = new NotificationChannel(NOT_CHANNEL, NOT_CHANNEL + "_CHAN_NAME", NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOT_CHANNEL);
        this.ncc = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_freakin_tray)
                .setContentTitle(getString(R.string.svc_not_title))
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, this.ncc);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent pit = new Intent(this, MainActivity.class);
        PendingIntent pid = PendingIntent.getActivity(this, 0, pit, PendingIntent.FLAG_MUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startMyOwnForeground();
        } else {
            // create a notification
            this.ncc = new NotificationCompat.Builder(getApplicationContext(), NOT_CHANNEL)
                    .setContentTitle(getString(R.string.svc_not_title))
                    //.setContentText(getString(R.string.svc_not_text))
                    .setSmallIcon(R.drawable.ic_freakin_tray)
                    .setContentIntent(pid)
                    .build();


            startForeground(2, this.ncc);
        }


        this.lbm = LocalBroadcastManager.getInstance(getApplicationContext());

        IntentFilter intFilter = new IntentFilter();
        intFilter.addAction("aq.metallists.ptt");
        intFilter.addAction("aq.metallists.vox");
        intFilter.addAction("aq.metallists.mission_abort");
        intFilter.addAction("aq.metallists.error.unauthorized");
        intFilter.addAction("aq.metallists.error.extra");
        intFilter.addAction("aq.metallists.request_data_update");

        this.brs = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "aq.metallists.ptt": {
                        boolean pttStatus = intent.getBooleanExtra("newptt", false);
                        setPtt(pttStatus);

                        if (pttStatus) {
                            setRxTxStatus(1);
                        } else {
                            setRxTxStatus(0);
                        }

                        break;
                    }
                    case "aq.metallists.vox": {
                        boolean pttStatus = intent.getBooleanExtra("newvox", false);

                        if (pttStatus) {
                            setRxTxStatus(-1);
                        } else {
                            setRxTxStatus(0);
                        }
                        break;
                    }
                    case "aq.metallists.mission_abort": {
                        if (fsc != null) {
                            fsc.abort();
                            fsc = null;
                        }
                        if (wake != null) {
                            try {
                                wake.release();
                            } catch (Exception x) {
                            }
                        }
                        break;
                    }
                    case "aq.metallists.error.unauthorized": {
                        Toast.makeText(getApplicationContext(), R.string.error_unauthorized, Toast.LENGTH_LONG).show();
                        stopSelf();
                        break;
                    }
                    case "aq.metallists.error.extra": {


                        Toast.makeText(getApplicationContext(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                        if (fsc != null) {
                            fsc.abort();
                            fsc = null;
                        }
                        stopSelf();
                        break;
                    }
                    case "aq.metallists.request_data_update": {
                        if (fsc != null)
                            fsc.requestDataUpdate();
                    }
                }

            }
        };
        this.lbm.registerReceiver(this.brs, intFilter);


        pfm = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        try {
            GSMVocoderOptions.setOptionsFromPreferences(pfm, getApplicationContext());
        } catch (Exception x) {
            x.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.opt_sfx_fatality, Toast.LENGTH_LONG).show();
            stopSelf();
            return START_NOT_STICKY;
        }

        this.usr = new FSAndroidUser(getApplicationContext());
        fsc = FSClient.createForUser(usr,
                pfm.getString("acc_server", getString(R.string.acc_def_host)),
                10024,
                pfm.getString("acc_raum", getString(R.string.acc_def_raum))
        );

        fsc.setPttTimeout(Long.parseLong(pfm.getString("opt_ptt_timeout", "0")));
        pttInhibit = pfm.getBoolean("opt_ptt_inhibit", false);

        try {
            FSVedroidSoundInterface fsi = new FSVedroidSoundInterface(getApplicationContext());
            fsc.setSoundInterface(fsi);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //fsc.connectLogin();
                        fsc.enterProtocolLoop();
                    } catch (FSUnauthorizedError c) {
                        lbm.sendBroadcast(new Intent("aq.metallists.error.unauthorized"));
                    } catch (IOException x) {
                        lbm.sendBroadcast(new Intent("aq.metallists.error.extra"));
                        //Toast.makeText(getApplicationContext(), "EXCEPTION WHILE CONNECTING 1!", Toast.LENGTH_LONG).show();
                        x.printStackTrace();
                        fsc.abort();
                        fsc = null;
                        stopSelf();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception x) {
            Toast.makeText(getApplicationContext(), "EXCEPTION WHILE CONNECTING!", Toast.LENGTH_LONG).show();
            x.printStackTrace();
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Freakin::FreundschaftService");
        wake.acquire();
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (mWifiLock == null)
            mWifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "Freakin::FreundschaftServiceWifi");
        mWifiLock.setReferenceCounted(false);
        if (!mWifiLock.isHeld())
            mWifiLock.acquire();


        return START_NOT_STICKY; // ??? maybe use "NOT_STICKY?"
    }

    private void setRxTxStatus(int mode) {
        Intent ni = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, ni, PendingIntent.FLAG_MUTABLE);

        int icn = R.drawable.ic_freakin_tray;
        if (mode == 1) {
            icn = R.drawable.ic_freakin_tray_tx;
        } else if (mode == -1) {
            icn = R.drawable.ic_freakin_tray_rx;
        }

        Notification nt = new NotificationCompat.Builder(this, NOT_CHANNEL)
                .setContentTitle(getText(R.string.svc_not_title))
                //.setContentText(status)
                .setSmallIcon(icn)
                //.setLights(Color.RED, 500, 500)
                .setContentIntent(pi)
                .build();


        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(2, nt);
    }

    @Override
    public boolean stopService(Intent name) {
        if (fsc != null) {
            fsc.abort();
            fsc = null;
        }

        try {
            wake.release();
        } catch (Exception x) {
        }

        try {
            if (mWifiLock != null && mWifiLock.isHeld())
                mWifiLock.release();
        } catch (Exception x) {
        }

        if (this.lbm != null) {
            this.lbm.unregisterReceiver(this.brs);
        }

        return super.stopService(name);
    }
}