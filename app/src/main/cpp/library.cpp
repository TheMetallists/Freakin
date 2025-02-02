#include "aq_metallists_freundschaft_vocoder_GSMNativeVocoder.h"


#include "library.h"


using namespace cycfi::q;

#include <cstdlib>
#include <cstring>
#include <cstdint>
#include <android/log.h>
#include <cmath>
#include <memory>

#define APPNAME "libFreakin"

extern "C" JNIEXPORT void JNICALL
Java_aq_metallists_freundschaft_vocoder_GSMNativeVocoder_voc_1init(JNIEnv *env, jobject glazz) {
    jclass selfclass = env->GetObjectClass(glazz);
    jfieldID fhVoc = env->GetFieldID(selfclass, "hVoc", "J");
    jfieldID fdoCompressor = env->GetFieldID(selfclass, "doCompressor", "Z");
    jboolean doCompressor = env->GetBooleanField(glazz, fdoCompressor);
    jfieldID fdoLpf = env->GetFieldID(selfclass, "doLpf", "Z");
    jboolean doLpf = env->GetBooleanField(glazz, fdoLpf);


    try {
        CerebralCortex *hCortex = new CerebralCortex(doCompressor, doLpf, env, glazz);

        env->SetLongField(glazz, fhVoc, (long long) hCortex);
    } catch (std::invalid_argument &x) {
        env->ThrowNew(env->FindClass("java/lang/Exception"), "ERROR CREATING JNI OBJECT!");
        return;
    }

    env->DeleteLocalRef(selfclass);
}

extern "C" JNIEXPORT void JNICALL
Java_aq_metallists_freundschaft_vocoder_GSMNativeVocoder_voc_1destroy(JNIEnv *env, jobject glazz) {
    jclass selfclass = env->GetObjectClass(glazz);
    jfieldID fhVoc = env->GetFieldID(selfclass, "hVoc", "J");
    jlong ptr = env->GetLongField(glazz, fhVoc);

    if (ptr < 1) {
        __android_log_print(ANDROID_LOG_WARN, APPNAME,
                            "Vocoder attempt to destroy the universe!: %lld",
                            (long long) ptr);
        env->DeleteLocalRef(selfclass);
        return;
    }

    CerebralCortex *pVocoder = (CerebralCortex *) ptr;

    __android_log_print(ANDROID_LOG_WARN, APPNAME, "CerebralCortex capacity destruction: %lld",
                        (long long) pVocoder);

    delete pVocoder;

    env->SetLongField(glazz, fhVoc, (long long) -2);
    env->DeleteLocalRef(selfclass);
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_aq_metallists_freundschaft_vocoder_GSMNativeVocoder_voc_1encode(JNIEnv *env, jobject glazz,
                                                                     jbyteArray inp) {
    jclass selfclass = env->GetObjectClass(glazz);
    jfieldID fhVoc = env->GetFieldID(selfclass, "hVoc", "J");
    jlong ptr = env->GetLongField(glazz, fhVoc);


    int srcLen = env->GetArrayLength(inp);
    if (srcLen % 640 != 0) {
        env->ThrowNew(env->FindClass("java/lang/Exception"), "INPUT LEN % 325 != 0");
        return NULL;
    }

    unsigned char *srcBuf = new unsigned char[srcLen];
    env->GetByteArrayRegion(inp, 0, srcLen, reinterpret_cast<jbyte *>(srcBuf));

    if (ptr < 1) {
        __android_log_print(ANDROID_LOG_WARN, APPNAME, "Vocoder null: %lld", (long long) ptr);
        env->ThrowNew(env->FindClass("java/lang/Exception"), "VOCODER IS NULL!");
        return NULL;
    }

    auto pVocoder = (CerebralCortex *) ptr;
    if (pVocoder == NULL) {
        env->ThrowNew(env->FindClass("java/lang/Exception"), "VOCODER IS NULL!");
        return NULL;
    }
    pVocoder->glazz = glazz;

    try {
        int numPackets = 0;

        char *bufx = pVocoder->encode(srcBuf, srcLen, &numPackets);
        env->ReleaseByteArrayElements(inp, reinterpret_cast<jbyte *>(srcBuf), JNI_ABORT);
        //delete srcBuf;

        int tgtlen = 65 * numPackets * (int) sizeof(unsigned char);

        jbyteArray arr = env->NewByteArray(tgtlen);
        env->SetByteArrayRegion(arr, 0, tgtlen, reinterpret_cast<jbyte *>(bufx));

        free(bufx);

        return arr;
    } catch (std::invalid_argument &x) {
        env->ThrowNew(env->FindClass("java/lang/Exception"), x.what());
        return NULL;
    }


}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_aq_metallists_freundschaft_vocoder_GSMNativeVocoder_voc_1decode(
        JNIEnv *env, jobject glazz, jbyteArray inp) {
    jclass selfclass = env->GetObjectClass(glazz);
    jfieldID fhVoc = env->GetFieldID(selfclass, "hVoc", "J");
    jlong ptr = env->GetLongField(glazz, fhVoc);
    env->DeleteLocalRef(selfclass);

    int srcLen = env->GetArrayLength(inp);
    if (srcLen % 65 != 0) {
        env->ThrowNew(env->FindClass("java/lang/Exception"), "INPUT LEN % 65 != 0");
        return NULL;
    }

    unsigned char *srcBuf = new unsigned char[srcLen];
    env->GetByteArrayRegion(inp, 0, srcLen, reinterpret_cast<jbyte *>(srcBuf));

    if (ptr < 1) {
        __android_log_print(ANDROID_LOG_WARN, APPNAME, "Vocoder null: %lld", (long long) ptr);
        env->ThrowNew(env->FindClass("java/lang/Exception"), "VOCODER IS NULL!");
        return NULL;
    }

    auto pVocoder = (CerebralCortex *) ptr;
    if (pVocoder == NULL) {
        env->ThrowNew(env->FindClass("java/lang/Exception"), "VOCODER IS NULL!");
        return NULL;
    }
    pVocoder->glazz = glazz;

    try {
        int numPackets = 0;
        int tgtlen = 0;

        char *bufx = pVocoder->decode(srcBuf, srcLen, &numPackets, &tgtlen);
        env->ReleaseByteArrayElements(inp, reinterpret_cast<jbyte *>(srcBuf), JNI_ABORT);
        //delete srcBuf;

        //int tgtlen = 320 * numPackets * (int) sizeof(gsm_signal);

        jbyteArray arr = env->NewByteArray(tgtlen);
        env->SetByteArrayRegion(arr, 0, tgtlen, reinterpret_cast<jbyte *>(bufx));

        /*__android_log_print(ANDROID_LOG_WARN, APPNAME,
                            "[CHARLIE] Fore free");*/
        free(bufx);
        return arr;
    } catch (std::invalid_argument &x) {
        env->ThrowNew(env->FindClass("java/lang/Exception"), x.what());
        return NULL;
    }

}


extern "C"
JNIEXPORT void JNICALL
Java_aq_metallists_freundschaft_vocoder_GSMNativeVocoder_checkIfAlive(JNIEnv *env, jclass clazz) {
    printf("Library alive!\n");
    __android_log_print(ANDROID_LOG_WARN, APPNAME, "Library alive!");
}

CerebralCortex::CerebralCortex(bool useCompressor, bool useLpf, JNIEnv *_env,
                               jobject _glazz) throw(std::invalid_argument) {
    NeuroCortex::InitDefaults();
    this->doCompressor = useCompressor;
    this->doLpf = useLpf;

    // initializing callback portion
    this->env = _env;
    this->glazz = _glazz;
    jclass cls = env->GetObjectClass(glazz);
    env->NewGlobalRef(cls);
    this->levelCallback = env->GetMethodID(cls, "setLevelOnTheLine", "(S)V");

    // SHOULD WE?
    env->DeleteLocalRef(cls);

    this->pVocoder = gsm_create();

    if (!pVocoder) {
        throw std::invalid_argument("Cannot initialize libGSM!");
        return;
    }

    int set_wav = 1;
    gsm_option(this->pVocoder, GSM_OPT_WAV49, &set_wav);


    if (this->doCompressor) {
        long double trash = -12.0;

        this->comp = new compressor(operator ""_dB((long double) NeuroCortex::comp_treshold),
                                    (float) NeuroCortex::comp_ratio);
        this->envil = new peak_envelope_follower(
                operator ""_s((long double) NeuroCortex::comp_release), 8000);
    } else {
        this->comp = nullptr;
    }

    if (this->doLpf) {
        //
        this->filtr = std::make_unique<SO_HPF>();
        this->filtr->calculate_coeffs((float) NeuroCortex::filter1_q, NeuroCortex::filter1_cutoff,
                                      8000);

        this->filtr2 = std::make_unique<FO_HPF>();
        this->filtr2->calculate_coeffs(NeuroCortex::filter2_cutoff, 8000);
    } else {
        this->filtr = nullptr;
    }

    __android_log_print(ANDROID_LOG_WARN, APPNAME,
                        "[CHARLIE] Vocoder object created");
}

CerebralCortex::~CerebralCortex() {
    if (this->pVocoder == nullptr) {
        __android_log_print(ANDROID_LOG_WARN, APPNAME,
                            "Vocoder attempted to destroy the universe!: %lld",
                            (long long) this->pVocoder);
        return;
    }

    __android_log_print(ANDROID_LOG_WARN, APPNAME, "Vocoder instance destroying: %lld",
                        (long long) this->pVocoder);
    gsm_destroy(this->pVocoder);
    /*__android_log_print(ANDROID_LOG_WARN, APPNAME, "Vocoder instance destroyed: %lld",
                        (long long) this->pVocoder);*/
    this->pVocoder = nullptr;

    if (this->comp != nullptr) {
        /*__android_log_print(ANDROID_LOG_WARN, APPNAME, "COMPRESSOR instance destroying: %lld",
                            (long long) this->comp);*/
        delete this->comp;
        delete this->envil;
        this->comp = nullptr;
        this->envil = nullptr;
    }

    if (this->doLpf) {
        /*__android_log_print(ANDROID_LOG_WARN, APPNAME, "COMPRESSOR destroying LPFs: %lld",
                            (long long) this->comp);*/

        SO_HPF *x = this->filtr.release();
        delete x;

        FO_HPF *x2 = this->filtr2.release();
        delete x2;
    }
}

char *CerebralCortex::encode(unsigned char *srcBuf, int srcLen,
                             int *numPackets) throw(std::invalid_argument) {

    gsm_signal *sigInput = (gsm_signal *) srcBuf;

    auto makeup_gain = 3.0f;
    const float div = (1.0f / 32768.0f);
    short levelToReport = -32767;
    for (int i = 0; i < (srcLen / 2); i++) {
        //floatize
        float f = ((float) sigInput[i]) / (float) 32768;
        if (f > 1) f = 1;
        if (f < -1) f = -1;

        if (this->filtr != nullptr) {
            f = filtr->process(f);
            f = filtr2->process(f);
            f = f * (float) NeuroCortex::filter_gain;
        }

        if (this->comp != nullptr) {
            // Envelope
            float dbps = 0.0f - this->envil->operator()(std::abs(f));

            if (dbps < 0) {
                dbps = std::abs(dbps);
            }

            decibel env_outs = decibel(dbps);
            // Compressor
            auto sgain = this->comp->operator()(env_outs).operator float() *
                         (float) NeuroCortex::comp_makeupgain;
            f *= sgain;
        }


        f = f * 32768;
        if (f > 32767) f = 32767;
        if (f < -32768) f = -32768;
        gsm_signal yi = (gsm_signal) f;

        if (abs(sigInput[i]) > levelToReport) {
            levelToReport = abs(sigInput[i]);
        }

        sigInput[i] = yi;
    }

    this->reportVolume(levelToReport);

    (*numPackets) = srcLen / 640;

    if ((*numPackets) != 5) {
        throw std::invalid_argument("NUMPAK != 5");
    }

    unsigned char *outbuf = (unsigned char *) malloc(65 * (*numPackets)); // short

    for (int i = 0; i < (*numPackets); i++) {
        gsm_encode(pVocoder, sigInput + (320 * i), outbuf + (65 * i));
        gsm_encode(pVocoder, sigInput + (320 * i) + 160, outbuf + (65 * i) + 32);
    }

    return (char *) outbuf;
}

char *CerebralCortex::decode(unsigned char *srcBuf, int srcLen, int *numPackets,
                             int *tgtlen) throw(std::invalid_argument) {

    gsm_byte *bitpakt = (gsm_byte *) malloc(sizeof(gsm_byte) * 65);

    (*numPackets) = srcLen / 65;
    if ((*numPackets) != 5) {
        throw std::invalid_argument("NUMPAK != 5");
    }

    (*tgtlen) = 320 * (*numPackets) * (int) sizeof(gsm_signal);
    gsm_signal *outbuf = (gsm_signal *) malloc((*tgtlen)); // short

    for (int i = 0; i < (*numPackets); i++) {
        memcpy(bitpakt, srcBuf + (65 * i), 65);
        int ret = gsm_decode(this->pVocoder, bitpakt, outbuf + (320 * i));
        if (ret < 0) {
            throw std::invalid_argument("GSM CODEC RETURNED -1");
        }

        // rv = rv || gsm_decode(handle, (gsm_byte *)(gsmData + 33), (gsm_signal *)(pcmData + 320));
        ret = ret || gsm_decode(this->pVocoder, bitpakt + 33, outbuf + (320 * i) + 160);
        if (ret < 0) {
            throw std::invalid_argument("GSM CODEC RETURNED -1");
        }
    }

    short levelToReport = 0;
    for (int x = 0; x < (*tgtlen); x++) {
        if (abs(outbuf[x]) > levelToReport) {
            levelToReport = abs(outbuf[x]);
        }
    }

    this->reportVolume(levelToReport);

    free(bitpakt);

    return (char *) outbuf;
}

void CerebralCortex::reportVolume(short report) {
    env->CallVoidMethod(this->glazz, this->levelCallback, (jshort) report);
}

extern "C"
JNIEXPORT void JNICALL
Java_aq_metallists_freundschaft_vocoder_GSMNativeVocoder_crashThisTrash(JNIEnv *env, jclass clazz) {
    int *a = NULL;
    (*a)++;
}