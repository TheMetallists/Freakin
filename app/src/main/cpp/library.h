#ifndef LIBVOCALCORDS_LIBRARY_H
#define LIBVOCALCORDS_LIBRARY_H

extern "C" {
#include "gsm.h"
}

#include <q/support/literals.hpp>
#include <q/fx/envelope.hpp>
#include <q/fx/dynamic.hpp>
#include "./dsp_cpp_filters/lib/filter_common.h"
#include "./dsp_cpp_filters/lib/filter_includes.h"

void hello();

class NeuroCortex {
public:
    static bool initialized;
    static int filter1_cutoff;
    static double filter1_q;
    static int filter2_cutoff;
    static double filter_gain;

    static double comp_treshold;
    static double comp_ratio;
    static double comp_release;
    static double comp_makeupgain;

    static void InitDefaults();

};

class CerebralCortex {
public:
    CerebralCortex(bool useCompressor, bool useLpf, JNIEnv *env,
                   jobject glazz) throw(std::invalid_argument);

    ~CerebralCortex();

    char *encode(unsigned char *input, int srcLen, int *numPackets) throw(std::invalid_argument);

    char *decode(unsigned char *input, int srcLen, int *numPackets,
                 int *tgtlen) throw(std::invalid_argument);
    jobject glazz;
private:
    // java related fields
    JNIEnv *env;

    jmethodID levelCallback;
    void reportVolume(short report);

    // SFX related fields
    bool doCompressor;
    bool doLpf;
    cycfi::q::compressor *comp;
    cycfi::q::peak_envelope_follower *envil;
    std::unique_ptr<SO_HPF> filtr;
    std::unique_ptr<FO_HPF> filtr2;

    // vocoder related fields
    gsm pVocoder;
};

#endif //LIBVOCALCORDS_LIBRARY_H
