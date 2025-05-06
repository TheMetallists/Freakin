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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import aq.metallists.freundschaft.FSRoomMember;
import aq.metallists.freundschaft.R;
import aq.metallists.freundschaft.overridable.FSClientAdapter;
import aq.metallists.freundschaft.service.FreundschaftService;
import aq.metallists.freundschaft.service.events.PAbortRequestMessage;
import aq.metallists.freundschaft.service.events.PAudioLevelMSG;
import aq.metallists.freundschaft.service.events.PConnectedMessage;
import aq.metallists.freundschaft.service.events.PConnectingMessage;
import aq.metallists.freundschaft.service.events.PFailureMessage;
import aq.metallists.freundschaft.service.events.PRequestDataUpdate;
import aq.metallists.freundschaft.service.events.PTTMessage;
import aq.metallists.freundschaft.service.events.PUserListMessage;

public class HomeFragment extends Fragment {
    private ToggleButton tbt;
    private FSClientAdapter userAdapter;
    private Button btnConn;
    private ProgressBar lvlBar;
    private int ictr;

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
                    EventBus.getDefault().post(new PAbortRequestMessage());
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
                if (b) {
                    tbt.setBackgroundColor(Color.parseColor("#FF0000"));
                } else {
                    tbt.setBackgroundColor(Color.parseColor("#00FF00"));
                }
                synchronized (HomeFragment.this) {
                    if (ictr > 0) {
                        ictr--;
                        return;
                    }
                }

                EventBus.getDefault().post(new PTTMessage(b));
            }
        });



        return root;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPAudioLevelMSG(PAudioLevelMSG event){
        lvlBar.setIndeterminate(false);
        lvlBar.setMax(Short.MAX_VALUE);
        lvlBar.setProgress((int) event.level);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorEvent(PFailureMessage event) {
        lvlBar.setIndeterminate(false);
        lvlBar.setProgress(0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPTTMEvent(PTTMessage event) {
        if (tbt.isChecked() != event.isKeyedDown) {
            synchronized (this) {
                ictr++;
            }
            tbt.setChecked(event.isKeyedDown);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserListEvent(PUserListMessage event) {
        userAdapter.clear();
        for (FSRoomMember fsr : event.items) {
            userAdapter.add(fsr);
        }
        userAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectedEvent(PConnectedMessage event) {
        if (event.isConnected) {
            btnConn.setText(R.string.label_disconnect);
        } else {
            btnConn.setText(R.string.label_connect);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectingEvent(PConnectingMessage event) {
        if (event.isConnecting) {
            lvlBar.setIndeterminate(true);
        } else {
            lvlBar.setIndeterminate(false);
            lvlBar.setProgress(0);
        }
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
        EventBus.getDefault().register(this);
        EventBus.getDefault().post(new PRequestDataUpdate());
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);;
    }
}