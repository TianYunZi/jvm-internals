#include <jni.h>
#include <pthread.h>
#include <stdint.h>
#include <stdio.h>

/* 单主线程教学调用；共享槽只在创建原生线程之前写入、join之后释放。 */
static JavaVM *vm;
static jobject strong_ref;
static jweak weak_ref;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *loaded_vm, void *reserved) {
    (void)reserved;
    vm = loaded_vm;
    return JNI_VERSION_1_8;
}

JNIEXPORT jboolean JNICALL Java_org_example_jvminternals_ch32_NativeJourney_acquire
  (JNIEnv *env, jclass type, jobject value) {
    (void)type;
    strong_ref = (*env)->NewGlobalRef(env, value);
    if (strong_ref == NULL) {
        return JNI_FALSE;
    }
    weak_ref = (*env)->NewWeakGlobalRef(env, value);
    if (weak_ref == NULL) {
        (*env)->DeleteGlobalRef(env, strong_ref);
        strong_ref = NULL;
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

JNIEXPORT jlong JNICALL Java_org_example_jvminternals_ch32_NativeJourney_sumCritical
  (JNIEnv *env, jclass type, jintArray values) {
    (void)type;
    /* 先把弱引用提升为局部强引用；此例还保留全局强引用，不制造回收竞态。 */
    jobject local = (*env)->NewLocalRef(env, weak_ref);
    if (local == NULL) {
        return -1;
    }
    (*env)->DeleteLocalRef(env, local);
    jsize length = (*env)->GetArrayLength(env, values);
    jboolean copied = JNI_FALSE;
    jint *elements = (*env)->GetPrimitiveArrayCritical(env, values, &copied);
    if (elements == NULL) {
        return -1;
    }
    jlong total = 0;
    /* 临界区内只访问这段数组；不打印、不阻塞、不回调或调用其他JNI函数。 */
    for (jsize index = 0; index < length; index++) {
        total += elements[index];
    }
    (*env)->ReleasePrimitiveArrayCritical(env, values, elements, JNI_ABORT);
    printf("CH32 临界区已释放 isCopy=%s\n", copied == JNI_TRUE ? "true" : "false");
    fflush(stdout);
    return total;
}

struct CallbackContext {
    jclass type;
    jmethodID callback;
    int status;
};

/* 原生线程必须取得自己的JNIEnv，并在退出前分离；类型由调用方全局引用保持。 */
static void *run_callback(void *argument) {
    struct CallbackContext *context = argument;
    JNIEnv *env = NULL;
    JavaVMAttachArgs args = {JNI_VERSION_1_8, "ch32-attached", NULL};
    context->status = (*vm)->AttachCurrentThread(vm, (void **)&env, &args);
    if (context->status != JNI_OK) {
        return NULL;
    }
    (*env)->CallStaticVoidMethod(env, context->type, context->callback);
    if ((*env)->ExceptionCheck(env)) {
        /* 无Java调用栈的外部线程在本边界报告异常；不携带待处理异常继续JNI工作。 */
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        context->status = JNI_ERR;
    }
    jint detached = (*vm)->DetachCurrentThread(vm);
    if (detached != JNI_OK) {
        context->status = detached;
    }
    return NULL;
}

JNIEXPORT jint JNICALL Java_org_example_jvminternals_ch32_NativeJourney_attachedCallback
  (JNIEnv *env, jclass type) {
    struct CallbackContext context = {NULL, NULL, JNI_ERR};
    context.callback = (*env)->GetStaticMethodID(env, type, "onNativeThread", "()V");
    if (context.callback == NULL) {
        return JNI_ERR;
    }
    context.type = (*env)->NewGlobalRef(env, type);
    if (context.type == NULL) {
        return JNI_ERR;
    }
    pthread_t worker;
    int created = pthread_create(&worker, NULL, run_callback, &context);
    if (created == 0) {
        /* 成功创建的线程必须join后才销毁栈上上下文和全局类引用。 */
        int joined = pthread_join(worker, NULL);
        if (joined != 0) {
            /* 此编程错误不能安全返回并留下悬空栈指针，终止当前进程。 */
            (*env)->FatalError(env, "CH32 cannot join owned native thread");
        }
    } else {
        fprintf(stderr, "CH32 创建本地线程失败：%d\n", created);
    }
    (*env)->DeleteGlobalRef(env, context.type);
    return context.status;
}

JNIEXPORT void JNICALL Java_org_example_jvminternals_ch32_NativeJourney_release
  (JNIEnv *env, jclass type) {
    (void)type;
    if (weak_ref != NULL) {
        (*env)->DeleteWeakGlobalRef(env, weak_ref);
        weak_ref = NULL;
    }
    if (strong_ref != NULL) {
        (*env)->DeleteGlobalRef(env, strong_ref);
        strong_ref = NULL;
    }
}

/* 固定int32_t契约；同步调用一次回调，不在返回后保留函数指针。 */
int32_t ch32_transform(int32_t value, int32_t (*callback)(int32_t)) {
    return callback(value) * 2;
}
