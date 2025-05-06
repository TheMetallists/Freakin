package aq.metallists.freundschaft.ui.netlist;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import aq.metallists.freundschaft.FSClient;
import aq.metallists.freundschaft.FSRoomMember;
import aq.metallists.freundschaft.R;
import aq.metallists.freundschaft.service.FreundschaftService;
import aq.metallists.freundschaft.service.events.PAbortRequestMessage;

public class NetlistAdapter extends ArrayAdapter<String> {
    final int INVALID_ID = -1;
    LayoutInflater lInflater;
    Context ctx;

    public NetlistAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
        super(context, resource, objects);

        lInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ctx = context;
    }


    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.lv_user_entry, parent, false);
        }

        String netName = this.getItem(position);

        ImageView uicon = (ImageView) view.findViewById(R.id.lv_user_symbol);
        TextView ucall = (TextView) view.findViewById(R.id.lv_user_callsign);
        TextView ctype = (TextView) view.findViewById(R.id.lv_user_station_kind);

        ucall.setText(netName);
        ctype.setText("FRN Network");
        uicon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_freakin_netzwerk));


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMyServiceRunning(FreundschaftService.class)) {
                    EventBus.getDefault().post(new PAbortRequestMessage());
                    ctx.stopService(new Intent(ctx, FreundschaftService.class));
                }

                PreferenceManager
                        .getDefaultSharedPreferences(ctx)
                        .edit().putString("acc_raum", netName).apply();

                Toast.makeText(ctx, R.string.msg_raum_set, Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= getCount()) {
            return INVALID_ID;
        }

        return position + 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}

