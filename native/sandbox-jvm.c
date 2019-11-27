#include <stdlib.h>
#include <jni.h>
#ifdef _WIN32
#include <windows.h>
#endif

#include <unistd.h>

#include "Tester.h"

#ifdef _WIN32
void (__cdecl *initialize)(JNIEnv *, jobject, jstring);
jint (__cdecl *sositrans)(JNIEnv *, jobject, jint, jdouble, jdouble, jdouble, jint, jdoubleArray);
#else
JNIEXPORT jint JNICALL SOSITRANS_FUNCTION(JNIEnv *, jobject, jint, jdouble, jdouble, jdouble, jint, jdoubleArray);
JNIEXPORT jboolean JNICALL INITIALIZE_FUNCTION(JNIEnv *, jobject, jstring);
JNIEXPORT jstring JNICALL GETLASTERROR_FUNCTION(JNIEnv *, jobject);
#endif

jint JNICALL MockGetVersion(JNIEnv *env)
{
    return 23;
}

/* JNINativeInterface_ jnini; */
/* jnini = { */
/*     0, 0, 0, 0, //4 reserved pointers */
/*     MockGetVersion */
/* }; */

typedef /*_JNI_IMPORT_OR_EXPORT_*/ jint (JNICALL *JNI_CreateJavaVM_func)(JavaVM **pvm, void **penv, void *args);

void print_last_error() {
    DWORD error = GetLastError();
    LPVOID buffer;
    FormatMessage(
        FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
        NULL,
        error,
        MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
        (LPTSTR) &buffer,
        0,
        NULL);
    fprintf(stderr, "Error: %ld: %s\n", error, (char*) buffer);
}

JNIEnv *create_vm(JavaVM **jvm, const char *jvm_dll_path) {
    JNIEnv *env;    
    JavaVMInitArgs vm_args;
    JavaVMOption options[1];
    vm_args.version = JNI_VERSION_1_2;
    vm_args.nOptions = 1;
    vm_args.ignoreUnrecognized = JNI_TRUE;
    
    options[0].optionString = "-Djava.class.path=c:/src/jdk12/lib";
    /* options[0].optionString = "-Djava.class.path=."; */
    vm_args.options = options;
    HINSTANCE jvm_dll = LoadLibraryExA(jvm_dll_path, NULL, LOAD_LIBRARY_SEARCH_DLL_LOAD_DIR | LOAD_LIBRARY_SEARCH_DEFAULT_DIRS);
    if (jvm_dll == NULL) {
        fprintf(stderr, "Couldn't load %s\n", jvm_dll_path);
        print_last_error();
        exit(1);
    }
    fprintf(stderr, "Creating JVM\n");
    JNI_CreateJavaVM_func JNI_CreateJavaVM_ptr = (JNI_CreateJavaVM_func) GetProcAddress(jvm_dll, "JNI_CreateJavaVM");
    if (JNI_CreateJavaVM_ptr == NULL) {
        fprintf(stderr, "Couldn't get address of JNI_CreateJavaVM\n");
        print_last_error();
        exit(1);
    }
    (*JNI_CreateJavaVM_ptr)(jvm, (void**)&env, &vm_args);
    /* JNI_GetDefaultJavaVMInitArgs(&vm_args); */
    /* JNI_CreateJavaVM(jvm, (void**)&env, &vm_args); */
    fprintf(stderr, "Finished creating JVM\n");
    return env;
}

int main(int argc, const char *argv[]) {
    const char *library = argv[1];
    const char *initPath = argv[2];
    int fromSosi = atoi(argv[3]);
    double x = atof(argv[4]);
    double y = atof(argv[5]);
    int toSosi = atoi(argv[6]);
    const char *jvmDll = argv[7];
    int delay = 0;

    if (argc >= 8) {
        delay = atoi(argv[8]);
        fprintf(stderr, "Sleeping for %d seconds\n", delay);
    }

    sleep(delay);

    JavaVM *jvm = NULL;
    JNIEnv *env = create_vm(&jvm, jvmDll);

    jdoubleArray result = (*env)->NewDoubleArray(env, 3);

#ifdef _WIN32
    HINSTANCE lib = LoadLibraryExA(library, NULL, LOAD_LIBRARY_SEARCH_DLL_LOAD_DIR | LOAD_LIBRARY_SEARCH_SYSTEM32);
    if (lib == NULL) {
        exit(EXIT_FAILURE);
    }
    initialize = (void (__cdecl *)(JNIEnv *, jobject, jstring)) GetProcAddress(lib, INITIALIZE_FUNCTION_S);
    fprintf(stderr, "%s: %p\n", INITIALIZE_FUNCTION_S, initialize);
    sositrans = (jint (__cdecl *)(JNIEnv *, jobject, jint, jdouble, jdouble, jdouble, jint, jdoubleArray)) GetProcAddress(lib, SOSITRANS_FUNCTION_S);
    fprintf(stderr, "%s: %p\n", SOSITRANS_FUNCTION_S, sositrans);
    initialize(env, NULL, (*env)->NewStringUTF(env, initPath));
    int code = sositrans(env, NULL, fromSosi, x, y, 0.0, toSosi, result);
    double *res = (*env)->GetDoubleArrayElements(env, result, 0);
    fprintf(stderr, "(%f, %f, %f) [%d]\n", res[0], res[1], res[2], code);    
#else
    (void) library;
    Java_Tester_initialize(env, NULL, (*env)->NewStringUTF(env, initPath));
    Java_Tester_xSosiTrans(env, NULL, fromSosi, x, y, 0.0, toSosi, result);
    // Java_Tester_getLastError(env, NULL);
#endif
    exit(EXIT_SUCCESS);
}
