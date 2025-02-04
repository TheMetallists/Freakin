package aq.metallists.freundschaft;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;

import aq.metallists.freundschaft.overridable.RadioFlavorModule;
import aq.metallists.freundschaft.vocoder.GSMNativeVocoder;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;

    private boolean pttUseVD = false;
    private boolean pttUseVU = false;
    private boolean pttHyteraMode = false;
    private boolean pttHyteraDebug = false;

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
                R.id.nav_home, R.id.nav_channellist, R.id.nav_settings)
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
                    if (flavor.length() > 0) {
                        flavor += ", ";
                    }
                    flavor += getString(R.string.flavor_histeria);
                }

                if (flavor.length() < 1) {
                    flavor = getString(R.string.flavor_main);
                }

                flavorHdg.setText(flavor);
            }
        }


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
        String pttMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("opt_ptt_mode", getString(R.string.acc_ptt_none));
        if (pttMode.equals(getString(R.string.acc_ptt_kup))) {
            this.pttUseVU = true;
        } else if (pttMode.equals(getString(R.string.acc_ptt_kdn))) {
            this.pttUseVD = true;
        } else {
            this.pttUseVU = false;
            this.pttUseVD = false;
        }

        this.pttHyteraMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("opt_hytera_mode", true);
        if (RadioFlavorModule.isHighTerra()) {
            this.pttHyteraMode = true;
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
        if ((this.pttUseVU && keyCode == KeyEvent.KEYCODE_VOLUME_UP) ||
                (this.pttUseVD && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            Intent intent = new Intent("aq.metallists.ptt_external");
            intent.putExtra("new_ptt", true);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            return true;
        }
        if (this.pttHyteraDebug) {
            Toast.makeText(getApplicationContext(), String.format(Locale.CANADA, "KeyCode: %d", keyCode), Toast.LENGTH_SHORT).show();
        }

        if (this.pttHyteraMode) {
            if (keyCode == KeyEvent.KEYCODE_F12) {
                Intent intent = new Intent("aq.metallists.ptt_external");
                intent.putExtra("new_ptt", true);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_F1) {
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


        if (keyCode == KeyEvent.KEYCODE_BACK) {
            navController.navigate(R.id.nav_home);
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((this.pttUseVU && keyCode == KeyEvent.KEYCODE_VOLUME_UP) ||
                (this.pttUseVD && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            Intent intent = new Intent("aq.metallists.ptt_external");
            intent.putExtra("new_ptt", false);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            return true;
        }

        if (this.pttHyteraMode) {
            if (keyCode == KeyEvent.KEYCODE_F12) {
                Intent intent = new Intent("aq.metallists.ptt_external");
                intent.putExtra("new_ptt", false);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("aq.metallists.request_data_update"));

        setupExternalPtt();
        setupScreenLock();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).unregisterOnSharedPreferenceChangeListener(this);
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
}