package aq.metallists.freundschaft.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Timer;
import java.util.TimerTask;

import aq.metallists.freundschaft.BuildConfig;
import aq.metallists.freundschaft.R;
import aq.metallists.freundschaft.overridable.RadioFlavorModule;

public class SettingsPage extends Fragment {

    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState
    ) {

        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        return root;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.frag_settings, new MyPreferenceFragment())
                .commit();
    }


    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        public MyPreferenceFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            if (!RadioFlavorModule.isAudiophile()) {
                PreferenceCategory cat = (PreferenceCategory) findPreference("sfx_debug");
                if (cat != null) {
                    PreferenceScreen screen = getPreferenceScreen();
                    Preference pref = getPreferenceManager().findPreference("mypreference");
                    screen.removePreference(cat);
                }
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (RadioFlavorModule.isHighTerra() && !sharedPreferences.getBoolean("opt_hytera_mode", false)) {
                sharedPreferences.edit().putBoolean("opt_hytera_mode", true).apply();
            }
            getPreferenceScreen().removeAll();
            addPreferencesFromResource(R.xml.preferences);
        }
    }

}