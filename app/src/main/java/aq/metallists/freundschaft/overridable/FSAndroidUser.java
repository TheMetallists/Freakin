package aq.metallists.freundschaft.overridable;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import aq.metallists.freundschaft.R;

public class FSAndroidUser extends FSUser {
    private SharedPreferences sp;

    public FSAndroidUser(Context ctx) {
        super("", "");
        this.sp = PreferenceManager.getDefaultSharedPreferences(ctx);


        this.email = this.sp.getString("acc_email", ctx.getString(R.string.acc_def_email));
        this.password = this.sp.getString("acc_pass", ctx.getString(R.string.acc_def_passwd));
        this.callsign = this.sp.getString("acc_callsign", ctx.getString(R.string.acc_def_callsign));
        this.decaription = this.sp.getString("acc_description", ctx.getString(R.string.acc_def_descr));
        this.city = this.sp.getString("acc_country", ctx.getString(R.string.acc_def_city));
        this.country = this.sp.getString("acc_city", ctx.getString(R.string.acc_def_country));
    }
}
