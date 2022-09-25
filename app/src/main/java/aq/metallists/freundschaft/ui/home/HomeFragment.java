package aq.metallists.freundschaft.ui.home;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

import aq.metallists.freundschaft.FSRoomMember;
import aq.metallists.freundschaft.R;
import aq.metallists.freundschaft.overridable.FSClientAdapter;
import aq.metallists.freundschaft.service.FreundschaftService;

public class HomeFragment extends Fragment {
    private ToggleButton tbt;
    private FSClientAdapter userAdapter;
    private BroadcastReceiver bres;
    private Button btnConn;
    private ProgressBar lvlBar;

    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState
    ) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        lvlBar = (ProgressBar) root.findViewById(R.id.soundLevel);
        lvlBar.setIndeterminate(false);

        btnConn = (Button) root.findViewById(R.id.btn_connect);
        btnConn.setText(R.string.label_connect);
        btnConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity act = getActivity();
                if (act == null) {
                    System.err.println("ACTIVITY NOT OK!");
                    return;
                }
                if (isMyServiceRunning(FreundschaftService.class)) {
                    LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).sendBroadcast(new Intent("aq.metallists.mission_abort"));
                    act.stopService(new Intent(act, FreundschaftService.class));
                } else {
                    act.startService(new Intent(act, FreundschaftService.class));
                }
            }
        });

        ListView chatroomList = (ListView) root.findViewById(R.id.clients_list);
        ArrayList<FSRoomMember> allObjects = new ArrayList<>();
        userAdapter = new FSClientAdapter(this.getActivity(), R.layout.lv_user_entry, allObjects);
        chatroomList.setAdapter(userAdapter);


        tbt = (ToggleButton) root.findViewById(R.id.tbt_prr);

        tbt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Intent intent = new Intent("aq.metallists.ptt");

                if (b) {
                    tbt.setBackgroundColor(Color.parseColor("#FF0000"));
                } else {
                    tbt.setBackgroundColor(Color.parseColor("#00FF00"));
                }


                // You can also include some extra data.
                intent.putExtra("newptt", b);
                Activity pac = getActivity();
                if (pac == null) {
                    return;
                }
                LocalBroadcastManager.getInstance(pac.getApplicationContext()).sendBroadcast(intent);
            }
        });

        this.bres = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("aq.metallists.ptt_external")) {
                    boolean newPttx = intent.getBooleanExtra("new_ptt", false);
                    if (tbt.isChecked() != newPttx) {
                        tbt.setChecked(newPttx);
                    }
                } else if (intent.getAction().equals("aq.metallists.userlist")) {
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        ArrayList<FSRoomMember> dataList = (ArrayList<FSRoomMember>) bundle.getSerializable("newUserList");
                        userAdapter.clear();
                        for (FSRoomMember fsr : dataList) {
                            userAdapter.add(fsr);
                        }

                        userAdapter.notifyDataSetChanged();
                    }

                } else if (intent.getAction().equals("aq.metallists.connected")) {
                    boolean newConnected = intent.getBooleanExtra("newconnected", false);
                    if (newConnected) {
                        btnConn.setText(R.string.label_disconnect);
                    } else {
                        btnConn.setText(R.string.label_connect);
                    }
                } else if (intent.getAction().equals("aq.metallists.connecting")) {
                    boolean newConnecting = intent.getBooleanExtra("newconnecting", false);
                    if (newConnecting) {
                        lvlBar.setIndeterminate(true);
                    } else {
                        lvlBar.setIndeterminate(false);
                        lvlBar.setProgress(0);
                    }
                } else if (intent.getAction().equals("aq.metallists.newAudioLevel")) {
                    short level = intent.getShortExtra("level", (short) 0);
                    lvlBar.setIndeterminate(false);
                    lvlBar.setMax(Short.MAX_VALUE);
                    lvlBar.setProgress((int) level);
                }

            }
        };


        return root;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        Activity act = getActivity();
        if (act == null)
            return true;

        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        btnConn.requestFocus();

        Activity act = getActivity();
        if (act != null) {
            IntentFilter iff = new IntentFilter();
            iff.addAction("aq.metallists.ptt_external");
            iff.addAction("aq.metallists.userlist");
            iff.addAction("aq.metallists.connected");
            iff.addAction("aq.metallists.connecting");
            iff.addAction("aq.metallists.newAudioLevel");

            LocalBroadcastManager
                    .getInstance(getActivity().getApplicationContext())
                    .registerReceiver(this.bres, iff);

            LocalBroadcastManager
                    .getInstance(act.getApplicationContext())
                    .sendBroadcast(new Intent("aq.metallists.request_data_update"));

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager
                .getInstance(getActivity().getApplicationContext())
                .unregisterReceiver(this.bres);
    }
}