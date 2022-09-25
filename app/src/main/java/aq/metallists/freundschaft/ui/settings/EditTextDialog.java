package aq.metallists.freundschaft.ui.settings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import aq.metallists.freundschaft.R;

public class EditTextDialog extends Dialog implements View.OnClickListener {
    private Context ctx;

    public EditTextDialog(@NonNull Context context) {
        super(context);

        this.ctx = context;
    }

    String propName = null;
    String value = null;

    public String getValue() {
        return value;
    }

    public void loadProperty(String _propName, String _value) {
        propName = _propName;
        value = PreferenceManager.getDefaultSharedPreferences(ctx).getString(_propName, _value);
        if (edit != null) {
            edit.setText(value);
        }
    }

    Button save, abort;
    EditText edit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.string_settings_dialog);

        save = (Button) findViewById(R.id.frm_apply);
        abort = (Button) findViewById(R.id.frm_abort);

        save.setOnClickListener(this);
        abort.setOnClickListener(this);

        edit = (EditText) findViewById(R.id.frm_edittext);
        if (value != null) {
            edit.setText(value);
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.frm_abort) {
            dismiss();
        } else if (v.getId() == R.id.frm_apply) {
            if (propName != null) {
                PreferenceManager.getDefaultSharedPreferences(ctx)
                        .edit()
                        .putString(propName, edit.getText().toString())
                        .apply();

                value = edit.getText().toString();

                dismiss();
            }
        }

    }
}
