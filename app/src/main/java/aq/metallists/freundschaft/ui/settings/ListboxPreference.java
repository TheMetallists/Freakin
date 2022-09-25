package aq.metallists.freundschaft.ui.settings;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class ListboxPreference extends ListPreference {
    public ListboxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListboxPreference(Context context) {
        super(context);
    }

    private String defaultSummary = null;

    @Override
    public void setValue(String text) {
        if (defaultSummary == null && getSummary() != null)
            defaultSummary = getSummary().toString();

        super.setValue(text);

        if (defaultSummary != null) {
            text = text + " /" + defaultSummary + "/";
        }

        setSummary(text);
    }
}