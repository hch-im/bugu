/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class edu_wayne_cs_bugu_util_NativeLib */

#ifndef _Included_edu_wayne_cs_bugu_util_NativeLib
#define _Included_edu_wayne_cs_bugu_util_NativeLib
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     edu_wayne_cs_bugu_util_NativeLib
 * Method:    getCPUIdleTime
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_edu_wayne_cs_bugu_util_NativeLib_getCPUIdleTime
  (JNIEnv *, jobject, jint);

/*
 * Class:     edu_wayne_cs_bugu_util_NativeLib
 * Method:    getCPUIOWaitTime
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_edu_wayne_cs_bugu_util_NativeLib_getCPUIOWaitTime
  (JNIEnv *, jobject, jint);

/*
 * Class:     edu_wayne_cs_bugu_util_NativeLib
 * Method:    getPageSize
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_edu_wayne_cs_bugu_util_NativeLib_getPageSize
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
