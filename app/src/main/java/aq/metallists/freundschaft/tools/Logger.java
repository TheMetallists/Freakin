package aq.metallists.freundschaft.tools;

import android.annotation.SuppressLint;
import android.util.Log;

public class Logger {
    private static final String LOG_TAG = "libFreakin";
    private boolean isVerbose = false;

    @SuppressLint("LogTagMismatch")
    private Logger() {
        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            this.isVerbose = true;
        } else {
            Log.i(LOG_TAG, "VERBOSE logging disabled for tag 'libFreakin'");
        }
    }

    static Logger inst = null;

    public static Logger getInstance() {
        if (inst == null) {
            inst = new Logger();
        }

        return inst;
    }

    public void v(String message) {
        if (isVerbose) {
            Log.v(LOG_TAG, message);
        }
    }

    public void v(String message, Throwable garbage) {
        if (isVerbose) {
            Log.v(LOG_TAG, message, garbage);
        }
    }

    public void i(String m) {
        Log.i(LOG_TAG, m);
    }

    public void e(String m) {
        Log.e(LOG_TAG, m);
    }

    public void e(String m, Throwable garbage) {
        Log.e(LOG_TAG, m, garbage);
    }

    public void w(String m) {
        Log.w(LOG_TAG, m);
    }

    public void w(String m, Throwable garbage) {
        Log.w(LOG_TAG, m, garbage);
    }


}
