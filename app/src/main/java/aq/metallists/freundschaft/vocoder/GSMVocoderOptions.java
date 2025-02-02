package aq.metallists.freundschaft.vocoder;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import aq.metallists.freundschaft.R;

public class GSMVocoderOptions {

    public static void setOptionsFromPreferences(SharedPreferences pfm, Context ctx) throws Exception {
        int filter1cutoff = Integer.parseInt(pfm.getString("opt_2_sfx_filter1_cutoff", ctx.getString(R.string.opt_sfx_filter1_cutoff_default)));
        GSMVocoderOptions.setFilter1Cutoff(filter1cutoff);

        int filter2cutoff = Integer.parseInt(pfm.getString("opt_2_sfx_filter2_cutoff", ctx.getString(R.string.opt_sfx_filter2_cutoff_default)));
        GSMVocoderOptions.setFilter2Cutoff(filter2cutoff);

        double filter1q = Double.parseDouble(pfm.getString("opt_2_sfx_filter1_q", ctx.getString(R.string.opt_sfx_filter1_q_default)));
        GSMVocoderOptions.setFilter1Q(filter1q);

        double filtergain = Double.parseDouble(pfm.getString("opt_2_sfx_filte_multiplier", ctx.getString(R.string.opt_sfx_filte_multiplier_default)));
        GSMVocoderOptions.setFilterGain(filtergain);


        double opt_compressor_mgain = Double.parseDouble(pfm.getString("opt_2_compressor_mgain", ctx.getString(R.string.opt_compressor_mgain_default)));
        GSMVocoderOptions.setCompressorMakeupGain(opt_compressor_mgain);

        double opt_compressor_treshold = Double.parseDouble(pfm.getString("opt_2_compressor_treshold", ctx.getString(R.string.opt_compressor_treshold_default)));
        GSMVocoderOptions.setCompressorThreshold(opt_compressor_treshold);

        double opt_compressor_ratio = Double.parseDouble(pfm.getString("opt_2_compressor_ratio", ctx.getString(R.string.opt_compressor_ratio_default)));
        GSMVocoderOptions.setCompressorRatio(opt_compressor_ratio);

        double opt_compressor_release = Double.parseDouble(pfm.getString("opt_2_compressor_release", ctx.getString(R.string.opt_compressor_release_default)));
        GSMVocoderOptions.setCompressorRelease(opt_compressor_release);

        GSMVocoderOptions.commit();
    }

    native static void setFilter1Cutoff(int cutoff);

    native static void setFilter1Q(double q);

    native static void setFilter2Cutoff(int cutoff);

    native static void setFilterGain(double gain);

    native static void setCompressorThreshold(double val);

    native static void setCompressorRatio(double val);

    native static void setCompressorRelease(double val);

    native static void setCompressorMakeupGain(double val);

    native static void commit();

}
