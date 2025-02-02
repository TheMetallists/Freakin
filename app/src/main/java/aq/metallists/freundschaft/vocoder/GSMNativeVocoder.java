package aq.metallists.freundschaft.vocoder;

import aq.metallists.freundschaft.overridable.SoundLevelReceiver;

public class GSMNativeVocoder {
    static {
        System.loadLibrary("freundschaft");
    }

    long hVoc;
    public boolean doCompressor = false;
    public boolean doLpf = false;
    public SoundLevelReceiver xlr = null;

    public GSMNativeVocoder(boolean useCompressor, boolean useLPF) {
        this.doCompressor = useCompressor;
        this.doLpf = useLPF;

        this.voc_init();
    }

    public void connectXLRJack(SoundLevelReceiver _xlr) {
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
