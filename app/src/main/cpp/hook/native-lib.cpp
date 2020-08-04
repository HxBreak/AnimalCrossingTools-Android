#include <jni.h>
#include <string>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
// munged from https://github.com/simontime/Resead
#include "android/log.h"
#include <dlfcn.h>
#include "sys/socket.h"

#ifdef __arm__
#define HOOK
#include "dobby.h"
#endif

#ifdef __amd64__
#define HOOK
#include "dobby.h"
#endif


void init() __attribute__((constructor));

//static const MethodInfo* GetMethodFromName (TypeInfo *klass, const char* name, int argsCount);
typedef void *(*Il2CppGetMethodFromName)(void *klass, const char *name, int args_count);

typedef void *(*ClassFunction)(void *klass);

void *old_address;

void *hooked(void *klass, const char *name, int args_count) {
    __android_log_print(ANDROID_LOG_ERROR, "Native", "%p, %s, %d", klass, name, args_count);
    return ((Il2CppGetMethodFromName) old_address)(klass, name, args_count);
}

int disable_function() {
    __android_log_print(ANDROID_LOG_ERROR, "Function", "Disabled Function");
    return -1;
}

void hook() {
#ifdef HOOK
    void* old_connect;
    DobbyHook(reinterpret_cast<void *>(connect), reinterpret_cast<void *>(disable_function), &old_connect);
#endif
}

void init() {
    hook();
}
