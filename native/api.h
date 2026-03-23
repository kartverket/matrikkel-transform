#ifndef _SOSITRANS_API_H
#define _SOSITRANS_API_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Signature: (IDDDI[D)I
 */
JNIEXPORT jint JNICALL SOSITRANS_FUNCTION(JNIEnv *, jobject, jint, jdouble, jdouble, jdouble, jint, jdoubleArray);

/*
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL INITIALIZE_FUNCTION(JNIEnv *, jobject, jstring);

/*
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL GETLASTERROR_FUNCTION(JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif

#endif
