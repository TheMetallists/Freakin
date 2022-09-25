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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

import aq.metallists.freundschaft.FSRoomMember;
import aq.metallists.freundschaft.R;
import aq.metallists.freundschaft.overridable.FSClientAdapter;

public class NetlistFragment extends Fragment {
    private NetlistAdapter nlAdapter;
    private BroadcastReceiver bres;
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

        // networks_list
        this.bres = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("aq.metallists.netlist")) {
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        ArrayList<String> dataList = (ArrayList<String>) bundle.getSerializable("newNetList");
                        nlAdapter.clear();
                        for (String fsr : dataList) {
                            nlAdapter.add(fsr);
                        }

                        nlAdapter.notifyDataSetChanged();
                    }

                }

            }
        };


        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (chatroomList != null && chatroomList.getChildAt(0) != null) {
            chatroomList.getChildAt(0).requestFocus();
        }

        Activity act = getActivity();
        if (act != null) {
            IntentFilter iff = new IntentFilter();
            iff.addAction("aq.metallists.netlist");

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