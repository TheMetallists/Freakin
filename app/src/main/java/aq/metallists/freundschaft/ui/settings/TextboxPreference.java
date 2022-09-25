package aq.metallists.freundschaft.ui.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import aq.metallists.freundschaft.overridable.RadioFlavorModule;

public class TextboxPreference extends EditTextPreference {
    public TextboxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextboxPreference(Context context) {
        super(context);
    }

    private String defaultSummary = null;

    @Override
    public void setText(String text) {
        if (defaultSummary == null) {
            if (getSummary() != null)
                defaultSummary = getSummary().toString();
            else
                defaultSummary = "";
        }

        super.setText(text);

        if (defaultSummary != null && defaultSummary.length() > 0) {
            text = text + " /" + defaultSummary + "/";
        }

        setSummary(text);
    }

    @Override
    protected void onClick() {
        boolean isHytera = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("opt_hytera_mode", false);
        if (RadioFlavorModule.isHighTerra()) {
            isHytera = true;
        }

        if (isHytera) {
            EditTextDialog edic = new EditTextDialog(getContext());
            edic.loadProperty(getKey(), getText());
            edic.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    String value = edic.getValue();
                    if (value != null) {
                        setText(value);
                    }
                }
            });
            edic.show();
        } else {
            super.onClick();
        }
    }
}