#include <jni.h>
#include <iostream>
#include "org_episteme_jni_NativeDeviceBridge.h"

// Mock implementation of hardware interaction

extern "C" {

JNIEXPORT jboolean JNICALL Java_org_episteme_jni_NativeDeviceBridge_connectDevice
  (JNIEnv *env, jobject obj, jstring deviceId) {
    if (deviceId == nullptr) {
        return JNI_FALSE;
    }
    const char *nativeString = env->GetStringUTFChars(deviceId, 0);
    if (nativeString == nullptr) {
        return JNI_FALSE;
    }
    std::cout << "Native: Connecting to device " << nativeString << std::endl;
    env->ReleaseStringUTFChars(deviceId, nativeString);
    return JNI_TRUE;
}

JNIEXPORT void JNICALL Java_org_episteme_jni_NativeDeviceBridge_disconnectDevice
  (JNIEnv *env, jobject obj, jstring deviceId) {
    if (deviceId == nullptr) {
        return;
    }
    const char *nativeString = env->GetStringUTFChars(deviceId, 0);
    if (nativeString == nullptr) {
        return;
    }
    std::cout << "Native: Disconnecting from device " << nativeString << std::endl;
    env->ReleaseStringUTFChars(deviceId, nativeString);
}

JNIEXPORT jdouble JNICALL Java_org_episteme_jni_NativeDeviceBridge_readSensorValue
  (JNIEnv *env, jobject obj, jstring deviceId) {
    // Mock random value
    return 42.0 + (rand() % 100) / 10.0;
}

}
