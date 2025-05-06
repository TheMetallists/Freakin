package aq.metallists.freundschaft.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

import androidx.preference.PreferenceManager;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import aq.metallists.freundschaft.FSClient;
import aq.metallists.freundschaft.MainActivity;
import aq.metallists.freundschaft.R;
import aq.metallists.freundschaft.overridable.FSAndroidUser;
import aq.metallists.freundschaft.overridable.FSUnauthorizedError;
import aq.metallists.freundschaft.overridable.FSSoundInterface;
import aq.metallists.freundschaft.overridable.devices.GenericDevice;
import aq.metallists.freundschaft.overridable.devices.GenericDeviceDetector;
import aq.metallists.freundschaft.service.events.PAbortRequestMessage;
import aq.metallists.freundschaft.service.events.PConnectingMessage;
import aq.metallists.freundschaft.service.events.PFailureMessage;
import aq.metallists.freundschaft.service.events.PRequestDataUpdate;
import aq.metallists.freundschaft.service.events.PTTMessage;
import aq.metallists.freundschaft.service.events.PInboundXmissionMSG;
import aq.metallists.freundschaft.tools.LedConsts;
import aq.metallists.freundschaft.tools.Logger;
import aq.metallists.freundschaft.tools.NetworkTool;
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
    SharedPreferences pfm;

    PowerManager.WakeLock wake;
    WifiManager.WifiLock mWifiLock = null;
    private boolean pttInhibit;
    private boolean bPttStatus;

    private GenericDevice myDev;

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void shutUpLogging(PConnectingMessage event) {
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPTTMEvent(PTTMessage event) {
        // TODO: add "is connected" check
        if (event.isKeyedDown == bPttStatus) {
            return;
        }
        bPttStatus = event.isKeyedDown;
        setPtt(event.isKeyedDown);

        if (event.isKeyedDown) {
            setRxTxStatus(LedConsts.LED_TX);
        } else {
            setRxTxStatus(LedConsts.LED_OFF);
        }
    }

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

        NotificationChannel chan = new NotificationChannel(NOT_CHANNEL, getString(R.string.notif_chan_main_name), NotificationManager.IMPORTANCE_NONE);
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

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onErrorEvent(PFailureMessage event) {
        if (event.bIsAuthorizationError) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.error_unauthorized, Toast.LENGTH_LONG).show();
                }
            });

            stopSelf();
        }
        if (event.bIsExtraError) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                }
            });

            if (fsc != null) {
                fsc.abort();
                fsc = null;
            }
            stopSelf();
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onRDUEvent(PRequestDataUpdate event) {
        if (fsc != null)
            fsc.requestDataUpdate();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onInboundXMission(PInboundXmissionMSG event) {
        if (event.isVoxActive) {
            setRxTxStatus(LedConsts.LED_RX);
        } else {
            setRxTxStatus(LedConsts.LED_OFF);
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onAbortRequest(PAbortRequestMessage event) {
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent pit = new Intent(this, MainActivity.class);
        PendingIntent pid = PendingIntent.getActivity(this, 0, pit, PendingIntent.FLAG_MUTABLE);

        myDev = GenericDeviceDetector.getDevice(getApplicationContext());

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

        EventBus.getDefault().register(this);

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
        int port = 10024;
        try {
            port = Integer.parseInt(pfm.getString("acc_servport", "10024"));
        } catch (Exception x) {
            Logger.getInstance().w("Cannot parse port number!", x);
            Toast.makeText(getApplicationContext(), R.string.msg_port_invalid, Toast.LENGTH_LONG).show();
            stopSelf();
        }


        fsc = FSClient.createForUser(usr,
                NetworkTool.getBestServer(pfm.getString("acc_server", getString(R.string.acc_def_host)), port),
                port,
                pfm.getString("acc_raum", getString(R.string.acc_def_raum))
        );

        fsc.setPttTimeout(Long.parseLong(pfm.getString("opt_ptt_timeout", "0")));
        pttInhibit = pfm.getBoolean("opt_ptt_inhibit", false);

        try {
            FSSoundInterface fsi = new FSSoundInterface(getApplicationContext());
            fsc.setSoundInterface(fsi);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //fsc.connectLogin();
                        Logger.getInstance().e("EPL!!!");
                        fsc.enterProtocolLoop();
                    } catch (FSUnauthorizedError c) {
                        onErrorEvent(new PFailureMessage(false, true));

                    } catch (IOException x) {
                        onErrorEvent(new PFailureMessage(true, false));
                        //Toast.makeText(getApplicationContext(), "EXCEPTION WHILE CONNECTING 1!", Toast.LENGTH_LONG).show();
                        x.printStackTrace();
                        if (fsc != null) {
                            fsc.abort();
                            fsc = null;
                            stopSelf();
                        }
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                        onErrorEvent(new PFailureMessage(false, false));
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

        int color = Color.BLUE;
        int onMS = 200;
        int offMS = 10000;
        int icn = R.drawable.ic_freakin_tray;
        if (mode == LedConsts.LED_TX) {
            icn = R.drawable.ic_freakin_tray_tx;
            color = Color.RED;
            onMS = 15000;
            offMS = 10;
        } else if (mode == LedConsts.LED_RX) {
            icn = R.drawable.ic_freakin_tray_rx;
            color = Color.GREEN;
            onMS = 15000;
            offMS = 10;
        }

        if (myDev != null && myDev.isCustomLed()) {
            myDev.setPttLed(mode);
        } else {
            Notification nt = new NotificationCompat.Builder(this, NOT_CHANNEL)
                    .setContentTitle(getText(R.string.svc_not_title))
                    //.setContentText(status)
                    .setSmallIcon(icn)
                    .setLights(color, onMS, offMS)
                    .setContentIntent(pi)
                    .build();

            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(2, nt);
        }


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

        EventBus.getDefault().unregister(this);

        return super.stopService(name);
    }
}