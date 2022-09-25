package aq.metallists.freundschaft.overridable;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import aq.metallists.freundschaft.FSClient;
import aq.metallists.freundschaft.FSRoomMember;
import aq.metallists.freundschaft.R;

public class FSClientAdapter extends ArrayAdapter<FSRoomMember> {
    final int INVALID_ID = -1;
    LayoutInflater lInflater;
    Context ctx;

    public FSClientAdapter(@NonNull Context context, int resource, @NonNull ArrayList<FSRoomMember> objects) {
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

        FSRoomMember fm = this.getItem(position);

        ImageView uicon = (ImageView) view.findViewById(R.id.lv_user_symbol);
        TextView ucall = (TextView) view.findViewById(R.id.lv_user_callsign);
        TextView ctype = (TextView) view.findViewById(R.id.lv_user_station_kind);

        ucall.setText(fm.getCallsignSupplement());

        ctype.setText(fm.getComment());

        switch (fm.getClientType()) {
            case FSRoomMember.TYPE_PC:
                if (fm.getXmitting()) {
                    uicon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_user_pc));
                } else {
                    uicon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_user_pc_green));
                }
                break;
            case FSRoomMember.TYPE_PARROT:
                if (fm.getXmitting()) {
                    uicon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_user_bird));
                } else {
                    uicon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_user_bird_green));
                }
                break;
            default:
                //radio
                if (fm.getXmitting()) {
                    uicon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_user_radio));
                } else {
                    uicon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_user_radio_green));
                }
        }

        /*if (fm.getXmitting()) {
            uicon.setTextColor(Color.parseColor("#FF0000"));
        } else {
            uicon.setTextColor(Color.parseColor("#00FF00"));
        }

        switch (fm.getClientType()) {
            case FSRoomMember.TYPE_PC:
                uicon.setText("W");
                break;
            case FSRoomMember.TYPE_PARROT:
                uicon.setText("P");
                break;
            default:
                uicon.setText("R");
        }*/

        return view;
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
