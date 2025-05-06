#include <jni.h>
#include "library.h"
#include <android/log.h>

bool NeuroCortex::initialized = false;
int NeuroCortex::filter1_cutoff = 300;
double NeuroCortex::filter1_q = 1.0;
int NeuroCortex::filter2_cutoff = 1000;
double NeuroCortex::filter_gain = 3.0;
double NeuroCortex::comp_treshold = 3.0;
double NeuroCortex::comp_ratio = -12;
double NeuroCortex::comp_release = 1.0 / 4.0;
double NeuroCortex::comp_makeupgain = 0.1;


void NeuroCortex::InitDefaults() {
    if (!NeuroCortex::initialized) {
        __android_log_print(ANDROID_LOG_WARN, "libFreakin",
                            "NeuroCortex loaded default values!");

        NeuroCortex::filter1_cutoff = 300;
        NeuroCortex::filter1_q = 1.0;
        NeuroCortex::filter2_cutoff = 800;
        NeuroCortex::filter_gain = 2.5;

        NeuroCortex::comp_makeupgain = 3.0;
        NeuroCortex::comp_treshold = -12;
        NeuroCortex::comp_ratio = 1.0 / 4.0;
        NeuroCortex::comp_release = 0.1;

        NeuroCortex::initialized = true;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_aq_metallists_freundschaft_vocoder_GSMVocoderOptions_setFilter1Q(JNIEnv *env, jclass clazz,
                                                                      jdouble q) {
    NeuroCortex::filter1_q = q;
}

extern "C"
JNIEXPORT void JNICALL
Java_aq_metallists_freundschaft_vocoder_GSMVocoderOptions_setFilterGain(JNIEnv *env, jclass clazz,
                                                                        jdouble gain) {
    NeuroCortex::filter_gain = gain;
}

extern "C"
JNIEXPORT void JNICALL
Java_aq_metallists_freundschaft_vocoder_GSMVocoderOptions_setCompressorRatio(JNIEnv *env,
                                                                             jclass clazz,
                                                                             jdouble val) {
    NeuroCortex::comp_ratio = val;
}

extern "C"
JNIEXPORT void JNICALL
Java_aq_metallists_freundschaft_vocoder_GSMVocoderOptions_setCompressorMakeupGain(JNIEnv *env,
                                                                                  jclass clazz,
                                                                                  jdouble val) {
    NeuroCortex::comp_makeupgain = val;
}

extern "C"
JNIEXPORT void JNICALL
Java_aq_metallists_freundschaft_vocoder_GSMVocoderOptions_setFilter1Cutoff(JNIEnv *env,
                                                                           jclass clazz,
                                                                           jint cutoff) {
    NeuroCortex::filter1_cutoff = cutoff;
}
extern "C"
JNIEXPORT void JNICALL
Java_aq_metallists_freundschaft_vocoder_GSMVocoderOptions_setFilter2Cutoff(JNIEnv *env,
                                                                           jclass clazz,
                                                                           jint cutoff) {
    NeuroCortex::filter2_cutoff = cutoff;
}
extern "C"
JNIEXPORT void JNICALL
Java_aq_metallists_freundschaft_vocoder_GSMVocoderOptions_setCompressorThreshold(JNIEnv *env,
                                                                                 jclass clazz,
                                                                                 jdouble val) {
    NeuroCortex::comp_treshold = val;
}
extern "C"
JNIEXPORT void JNICALL
Java_aq_metallists_freundschaft_vocoder_GSMVocoderOptions_setCompressorRelease(JNIEnv *env,
                                                                               jclass clazz,
                                                                               jdouble val) {
    NeuroCortex::comp_release = val;
}

extern "C"
JNIEXPORT void JNICALL
Java_aq_metallists_freundschaft_vocoder_GSMVocoderOptions_commit(JNIEnv *env, jclass clazz) {
    NeuroCortex::initialized = true;
}