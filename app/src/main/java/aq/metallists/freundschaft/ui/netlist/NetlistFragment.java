package aq.metallists.freundschaft.ui.netlist;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import aq.metallists.freundschaft.FSRoomMember;
import aq.metallists.freundschaft.R;
import aq.metallists.freundschaft.overridable.FSClientAdapter;
import aq.metallists.freundschaft.service.events.PAbortRequestMessage;
import aq.metallists.freundschaft.service.events.PNetListMessage;
import aq.metallists.freundschaft.service.events.PRequestDataUpdate;

public class NetlistFragment extends Fragment {
    private NetlistAdapter nlAdapter;
    private ListView chatroomList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        chatroomList = (ListView) root.findViewById(R.id.networks_list);

        ArrayList<String> allObjects = new ArrayList<>();
        nlAdapter = new NetlistAdapter(this.getActivity(), R.layout.lv_user_entry, allObjects);
        chatroomList.setAdapter(nlAdapter);
        chatroomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chatroomList.getChildAt(position).performClick();
                //Toast.makeText(getActivity().getApplicationContext(), "asd", Toast.LENGTH_LONG).show();
            }
        });


        return root;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetlistEvent(PNetListMessage event) {
        nlAdapter.clear();
        for (String fsr : event.netList) {
            nlAdapter.add(fsr);
        }

        nlAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (chatroomList != null && chatroomList.getChildAt(0) != null) {
            chatroomList.getChildAt(0).requestFocus();
        }

        Activity act = getActivity();
        if (act != null) {
            EventBus.getDefault().register(this);
            EventBus.getDefault().post(new PRequestDataUpdate());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

}