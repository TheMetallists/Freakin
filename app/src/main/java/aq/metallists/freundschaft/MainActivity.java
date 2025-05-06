package aq.metallists.freundschaft;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Color;
import android.hardware.lights.LightsManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.navigation.NavDestination;
import androidx.preference.PreferenceManager;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;

import aq.metallists.freundschaft.overridable.RadioFlavorModule;
import aq.metallists.freundschaft.overridable.devices.GenericDevice;
import aq.metallists.freundschaft.overridable.devices.GenericDeviceDetector;
import aq.metallists.freundschaft.overridable.devices.HyteraPNC370Device;
import aq.metallists.freundschaft.overridable.devices.HyteraPNC370SEDevice;
import aq.metallists.freundschaft.overridable.devices.TeloTE590Device;
import aq.metallists.freundschaft.service.events.PRequestDataUpdate;
import aq.metallists.freundschaft.tools.DeviceZooHelper;
import aq.metallists.freundschaft.tools.Logger;
import aq.metallists.freundschaft.vocoder.GSMNativeVocoder;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;


    private boolean pttHyteraDebug = false;
    private static GenericDevice pttDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_channellist, R.id.nav_settings, R.id.nav_about)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView flavorHdg = (TextView) headerView.findViewById(R.id.nav_header_subtitle);
            if (flavorHdg != null) {
                String flavor = "";

                if (RadioFlavorModule.isAudiophile()) {
                    flavor = getString(R.string.flavor_audiophile);
                }

                if (RadioFlavorModule.isHighTerra()) {
                    if (!flavor.isEmpty()) {
                        flavor += ", ";
                    }
                    flavor += getString(R.string.flavor_histeria);
                }

                if (flavor.isEmpty()) {
                    flavor = getString(R.string.flavor_main);
                }

                flavor = String.format(Locale.CANADA, "%s [%s]", flavor, DeviceZooHelper.getDeviceDesignator());

                flavorHdg.setText(flavor);
            }
        }

        pttDevice = GenericDeviceDetector.getDevice(getBaseContext());

        // check the C part, crash if failed to run
        GSMNativeVocoder.checkIfAlive();


        setupExternalPtt();
        setupScreenLock();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "PERMISSION NOT GRANTED!", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
        doCreateNotificationChannels();

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                startActivity(
                        new Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:aq.metallists.freundschaft")
                        )
                );
            }
        }*/

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_mission_abort) {
            System.exit(2);
            return true;
        } else if (item.getItemId() == R.id.action_switch_screenwl) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean newValue = !sp.getBoolean("opt_awakening", false);
            sp.edit().putBoolean(
                    "opt_awakening",
                    newValue
            ).apply();

            if (newValue) {
                Toast.makeText(getApplicationContext(), R.string.msg_screenwl_enabled, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.msg_screenwl_disabled, Toast.LENGTH_LONG).show();
            }
        } else if (item.getItemId() == R.id.action_crash_that_trash) {
            GSMNativeVocoder.crashThisTrash();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onSupportNavigateUp() {

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void setupExternalPtt() {
        // external ptt settings
        boolean pttHyteraMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("opt_hytera_mode", true);
        if (RadioFlavorModule.isHighTerra()) {
            pttHyteraMode = true;
        }

        this.pttHyteraDebug = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("opt_hytera_kbddebug", false);
    }

    public void setupScreenLock() {
        boolean isWLEn = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("opt_awakening", false);
        if (isWLEn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        pttDevice.onKeyDown(keyCode, event);

        if (this.pttHyteraDebug) {
            Toast.makeText(getApplicationContext(), String.format(Locale.CANADA, "KeyCode: %d", keyCode), Toast.LENGTH_SHORT).show();
        }

        if (pttDevice instanceof HyteraPNC370Device /*|| pttDevice instanceof HyteraPNC380Device*/) {
            if (keyCode == KeyEvent.KEYCODE_F1) {
                navController.navigate(R.id.nav_settings);
            } else if (keyCode == KeyEvent.KEYCODE_F2) {
                navController.navigate(R.id.nav_channellist);
            } else if (keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {
                ListView chatroomList = (ListView) findViewById(R.id.networks_list);
                if (chatroomList != null) {
                    chatroomList.performClick();
                }
            }
        }

        if (pttDevice instanceof HyteraPNC370SEDevice) {
            if (keyCode == KeyEvent.KEYCODE_CALL) {
                NavDestination nd = navController.getCurrentDestination();
                if (nd != null && nd.getId() == R.id.nav_channellist) {
                    navController.popBackStack();
                } else {
                    navController.navigate(R.id.nav_channellist);
                }
            } else if (keyCode == KeyEvent.KEYCODE_F2) {
                NavDestination nd = navController.getCurrentDestination();
                if (nd != null && nd.getId() == R.id.nav_settings) {
                    navController.popBackStack();
                    keyCode = KeyEvent.KEYCODE_BUTTON_2;
                } else {
                    navController.navigate(R.id.nav_settings);
                }
            }
        }

        if (pttDevice instanceof TeloTE590Device) {
            // top button above ptt
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN_LEFT) {
                NavDestination nd = navController.getCurrentDestination();
                if (nd != null && nd.getId() == R.id.nav_channellist) {
                    navController.popBackStack();
                } else {
                    navController.navigate(R.id.nav_channellist);
                }
            }
        }


        if (keyCode == KeyEvent.KEYCODE_BACK) {
            navController.navigate(R.id.nav_home);
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        pttDevice.onKeyUp(keyCode, event);

        return super.onKeyDown(keyCode, event);
    }


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().post(new PRequestDataUpdate());

        setupExternalPtt();
        setupScreenLock();

        ((AudioManager) getSystemService(AUDIO_SERVICE))
                .registerMediaButtonEventReceiver(new ComponentName(this, PttReceiver.class));

    }

    TstServiceConnection htc;

    class TstServiceConnection extends ContentObserver implements ServiceConnection {
        public TstServiceConnection() {
            super(new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    Toast.makeText(getApplicationContext(), "HandlerCTX!!", Toast.LENGTH_LONG).show();
                    return false;
                }
            }));
        }

        public final void onChange(boolean z2) {
            int i = 0;
            try {
                i = Settings.System.getInt(getContentResolver(), "ptt_status");
                Toast.makeText(getApplicationContext(), String.format(Locale.CANADA, "HCPC: %d", i), Toast.LENGTH_SHORT).show();
                Logger.getInstance().w("TstServiceConnection.onChange[PTT]: ".concat(Integer.toString(i)));
            } catch (Exception x) {
                Logger.getInstance().w("TstServiceConnection.onChange", x);
            }
            Logger.getInstance().w("TstServiceConnection.onChange (i):".concat(Integer.toString(i)));

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.getInstance().w("TstServiceConnection.onServiceConnected");
            android.os.IInterface iifc = service.queryLocalInterface("com.hytera.call.service.IHytCallManagerService");
            android.net.Uri ptts = android.provider.Settings.System.getUriFor("ptt_status");
            getContentResolver().registerContentObserver(ptts, true, this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.getInstance().w("TstServiceConnection.onServiceDisconnected");
            getContentResolver().unregisterContentObserver(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).unregisterOnSharedPreferenceChangeListener(this);
        ((AudioManager) getSystemService(AUDIO_SERVICE))
                .unregisterMediaButtonEventReceiver(new ComponentName(this, PttReceiver.class));
    }

    protected void doCreateNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(
                    "CONNECTION_ERRORS",
                    this.getString(R.string.notif_chan_failures_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);


            NotificationChannel chan2 = new NotificationChannel("FRN_NOT_CHANNEL", getString(R.string.notif_chan_main_name), NotificationManager.IMPORTANCE_DEFAULT);
            chan2.setLightColor(Color.BLUE);
            chan2.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            manager.createNotificationChannel(chan2);
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setupExternalPtt();
        setupScreenLock();
    }

    public static class PttReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (pttDevice != null) {
                pttDevice.onReceive(context, intent);
            }
        }
    }
}