#include <jni.h>

/* 仅调用Java的有界sleep；保留本地帧以观察固定，不创建本地线程。 */
JNIEXPORT void JNICALL Java_org_example_jvminternals_ch28_VirtualJourney_callBack
  (JNIEnv *env, jclass journey) {
    jmethodID pause = (*env)->GetStaticMethodID(env, journey, "pauseWithinNative", "()V");
    if (pause == NULL) {
        return; /* 查找异常仍由Java调用边界接收。 */
    }
    (*env)->CallStaticVoidMethod(env, journey, pause);
}
