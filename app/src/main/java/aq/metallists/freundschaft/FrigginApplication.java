package aq.metallists.freundschaft;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.DialogConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.data.StringFormat;

import ru.ivanarh.jndcrash.NDCrash;
import ru.ivanarh.jndcrash.NDCrashError;
import ru.ivanarh.jndcrash.NDCrashService;
import ru.ivanarh.jndcrash.NDCrashUnwinder;


@AcraCore(buildConfigClass = BuildConfig.class)
public class FrigginApplication extends Application {
    public static CoreConfigurationBuilder getAcraBuilder(Context ctx) {
        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(ctx);
        builder.setBuildConfigClass(BuildConfig.class).setReportFormat(StringFormat.JSON);
        builder.getPluginConfigurationBuilder(DialogConfigurationBuilder.class)
                .setResText(R.string.acra_sendmail_required)
                .setEnabled(true);
        builder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class)
                .setMailTo("themetallists@freemail.hu")
                .setSubject("ACRA ERROR REPORT")
                .setReportAsFile(false)
                .setEnabled(true);

        return builder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final String reportPath = getExternalFilesDir("crashes").getAbsolutePath() + "/crash.txt"; // Example.
        final NDCrashError error = NDCrash.initializeOutOfProcess(
                this,
                reportPath,
                NDCrashUnwinder.libunwind,
                NDCrashService.class);
        if (error == NDCrashError.ok) {
            // Initialization is successful.
        } else {
            // Initialization failed, check error value.
            Toast.makeText(this.getApplicationContext(), R.string.nda_trashcan_selferror, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        ACRA.init(this, FrigginApplication.getAcraBuilder(this));
    }

}
