/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class vw_learner_VWLearners */

#ifndef _Included_vw_learner_VWLearners
#define _Included_vw_learner_VWLearners
#ifdef __cplusplus
extern "C"
{
#endif
/*
 * Class:     vw_learner_VWLearners
 * Method:    initialize
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_vw_learner_VWLearners_initialize
(JNIEnv *, jobject, jstring);

/*
 * Class:     vw_learner_VWLearners
 * Method:    closeLearner
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_vw_learner_VWLearners_closeLearner
(JNIEnv *, jobject, jlong);

/*
 * Class:     vw_learner_VWLearners
 * Method:    getReturnType
 * Signature: (J)V
 */
JNIEXPORT jobject JNICALL Java_vw_learner_VWLearners_getReturnType
(JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
