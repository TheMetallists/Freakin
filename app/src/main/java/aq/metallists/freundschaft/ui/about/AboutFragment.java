package aq.metallists.freundschaft.ui.about;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Locale;

import aq.metallists.freundschaft.BuildConfig;
import aq.metallists.freundschaft.R;
import aq.metallists.freundschaft.service.events.PNetListMessage;
import aq.metallists.freundschaft.service.events.PRequestDataUpdate;
import aq.metallists.freundschaft.tools.DeviceZooHelper;
import aq.metallists.freundschaft.ui.netlist.NetlistAdapter;


public class AboutFragment extends Fragment {
    EditText et;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_about, container, false);

        et = (EditText) root.findViewById(R.id.et_version_report);
        this.doFillAboutText(et);
        return root;
    }

    private void doFillAboutText(EditText et) {
        et.setText(R.string.vreport_loading);
        String format = "The Freakin' app version report:\n" +
                "Package: %s\n" +
                "Version: %s (code: %d)\n" +
                "Flavor: %s (isDebug: %s)\n" +
                "Your Hardware: \"%s\"\n" +
                "Used Driver: \"%s\"";
        String msg = String.format(
                Locale.CANADA, format,
                BuildConfig.APPLICATION_ID,
                BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE,
                BuildConfig.FLAVOR, BuildConfig.DEBUG ? "YES" : "NO",
                DeviceZooHelper.getDeviceDesignator(),
                DeviceZooHelper.getDeviceDriverName(getActivity().getApplicationContext())
        );
        et.setText(msg);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.doFillAboutText(et);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}