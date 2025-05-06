package aq.metallists.freundschaft.vocoder;

import java.util.Locale;

import aq.metallists.freundschaft.overridable.FSSoundInterface;
import aq.metallists.freundschaft.tools.Logger;

public class GSMNativeVocoder {
    static {
        System.loadLibrary("freundschaft");
    }

    long hVoc;
    public boolean doCompressor = false;
    public boolean doLpf = false;
    public FSSoundInterface xlr = null;

    public GSMNativeVocoder(boolean useCompressor, boolean useLPF) {
        this.doCompressor = useCompressor;
        this.doLpf = useLPF;

        this.voc_init();
        Logger.getInstance().i(String.format(Locale.CANADA,"JVOCODER VERIFY: %s",Long.toString(this.hVoc)));
    }

    public void connectXLRJack(FSSoundInterface _xlr) {
        this.xlr = _xlr;
    }

    public void setLevelOnTheLine(short level) {
        if (this.xlr != null) {
            this.xlr.setAudioLevel(level);
        }
    }

    @Override
    public void finalize() {
        this.close();
        try {
            super.finalize();
        } catch (Throwable x) {
        }
    }

    public void close() {
        if (this.hVoc > -1) {
            this.voc_destroy();
        }
    }

    public byte[] encode(byte[] inp) {
        return this.voc_encode(inp);
    }

    public byte[] decode(byte[] inp) {
        return this.voc_decode(inp);
    }

    private native void voc_init();


    private native byte[] voc_encode(byte[] inp);

    private native byte[] voc_decode(byte[] inp);

    private native void voc_destroy();

    public static native void checkIfAlive();

    public static native void crashThisTrash();
}
