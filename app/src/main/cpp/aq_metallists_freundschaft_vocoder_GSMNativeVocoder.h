/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class aq_metallists_freundschaft_vocoder_GSMNativeVocoder */

#ifndef _Included_aq_metallists_freundschaft_vocoder_GSMNativeVocoder
#define _Included_aq_metallists_freundschaft_vocoder_GSMNativeVocoder
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     aq_metallists_freundschaft_vocoder_GSMNativeVocoder
 * Method:    voc_init
 * Signature: ()J
 */
JNIEXPORT void JNICALL Java_aq_metallists_freundschaft_vocoder_GSMNativeVocoder_voc_1init
        (JNIEnv *, jobject);

/*
 * Class:     aq_metallists_freundschaft_vocoder_GSMNativeVocoder
 * Method:    voc_encode
 * Signature: (J[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_aq_metallists_freundschaft_vocoder_GSMNativeVocoder_voc_1encode
        (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     aq_metallists_freundschaft_vocoder_GSMNativeVocoder
 * Method:    voc_decode
 * Signature: (J[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_aq_metallists_freundschaft_vocoder_GSMNativeVocoder_voc_1decode
        (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     aq_metallists_freundschaft_vocoder_GSMNativeVocoder
 * Method:    voc_destroy
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_aq_metallists_freundschaft_vocoder_GSMNativeVocoder_voc_1destroy
        (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif

extern "C"
JNIEXPORT void JNICALL
Java_aq_metallists_freundschaft_vocoder_GSMNativeVocoder_checkIfAlive(JNIEnv *env, jclass clazz);
