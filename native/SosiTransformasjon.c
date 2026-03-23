#ifndef TEST
#include "no_statkart_matrikkel_persistens_coordtransform_SkTrans.h"
#else
#include "Tester.h"
#endif
#include "SosiTransformasjon.h"

#include <float.h>

#if defined(_WIN32) || defined(__CYGWIN__)
#define ON_WINDOWS
#endif

#ifdef ON_WINDOWS
#include <windows.h>
#else
#include <stdlib.h>
#endif

#include <libgen.h>
#include <string.h>
#include <stdio.h>
#include <math.h>

#ifdef ON_WINDOWS
#define SOSI_EXPORT __declspec(dllexport)
#define SEPARATOR_CHAR '\\'
#define SEPARATOR "\\"
void (__cdecl *FellesPunkt)(char *szFileName, short *sStrlen, short *sErr);
void (__cdecl *SosiTrans)(short *sSosiFraSys, double *dXfra, double *dYfra, double *dHfra, short *sSosiTilSys, double *dXtil, double *dYtil, double *dHtil, short *error);

#define DLL_FILENAME "skt2lan1_64.dll"
#define FELLES_FUN "__FellesPunkt"
#define TRANS_FUN "__SosiTrans"

#define FELLESPUNKT_FILENAME "lan1_fellesp.bin"

static HINSTANCE hLib = NULL;      // Tranformasjons bibliotek

// Control words for Java and C
static unsigned int control_fp_java;
static unsigned int control_fp_c;
static int control_words_differ = 0;
#else
#define SOSI_EXPORT
#define SEPARATOR_CHAR '/'
#define SEPARATOR "/"
#define TRUE 1
#define FALSE 0

void initfiler_(char* filename, short* filenamelen, short* filenumber, short* err);
void sositrans_(short *sSosiFraSys, double *dXfra, double *dYfra, double *dHfra, short *sSosiTilSys, double *dXtil, double *dYtil, double *dHtil, short *error);

typedef struct {
    int fileno;
    const char *filename;
    const char *desc;
} initfile_t;

initfile_t initfiles[] = {
    {9,  "href2008a.bin", "Hrefmodell NN1954"},
    {10, "lan1_fellesp_20081014.bin", "Fellespunkter"},
    {11, "IGS05N_EUREF89_7PAR_2013.txt", "IGS-fil 1"},
    {12, "Milne_north.bin", "IGS-fil 2"},
    {13, "Milne_east.bin", "IGS-fil 3"},
    {14, "RH2000LU_absup.bin", "IGS-fil 4"},
    {15, "HREF2016B_NN2000_EUREF89.bin", "Hrefmodell NN2000"},
    {16, "NNTrans2016B.bin", "Hrefmodell NNDiff"},
    {17, "Uplift_Svalbard_pdim_pgs_tot.dat", "Svalbards bevegelsesmodell"},
    {18, "arcgp-2006-sk.bin", "Svalbards hrefmodell"},
    {-1, NULL, NULL}
};
#endif

static char * last_error = NULL;

static int debug_enabled = -1;

int debugging() {
    if (debug_enabled < 0) {
        char *enabled = getenv("SOSITRANSFORM_DEBUG");
        debug_enabled = (enabled && strncmp(enabled, "1", 2) == 0);
    }
    return debug_enabled;
}

void debug(const char *format, ...) {
    if (debugging()) {
        fprintf(stderr, "[SosiTransformasjon] ");
        va_list ap;
        va_start(ap, format);
        vfprintf(stderr, format, ap);
        va_end(ap);
        fprintf(stderr, "\n");
    }
}

#define IS_NAN(n) (n != n)

#ifdef ON_WINDOWS
void set_c_control_word() {
    if (control_words_differ) {
        _clearfp();
        _controlfp(control_fp_c, 0xfffff);
    }
}

void set_java_control_word() {
    if (control_words_differ) {
        _clearfp();
        _controlfp(control_fp_java, 0xfffff);
    }
}
#endif

SOSI_EXPORT
int sositrans(int srcSosi, double x, double y, double z, int destSosi, double *result) {
    short sSosiFraSys = (short) srcSosi;
    double dFraX = x;
    double dFraY = y;
    double dFraZ = IS_NAN(z) ? 0.0 : z;
    short sSosiTilSys = (short) destSosi;

    double dTilX = 45;
    double dTilY = 88;
    double dTilZ = 79;

    short sErr = 0;

#ifdef ON_WINDOWS
    set_c_control_word();
    (*SosiTrans)(&sSosiFraSys, &dFraX, &dFraY, &dFraZ, &sSosiTilSys, &dTilX, &dTilY, &dTilZ, &sErr);
    set_java_control_word();
#else
    sositrans_(&sSosiFraSys, &dFraX, &dFraY, &dFraZ, &sSosiTilSys, &dTilX, &dTilY, &dTilZ, &sErr);
#endif
    result[0] = dTilX;
    result[1] = dTilY;
    result[2] = dTilZ;

    debug("Transformert [sosi: %d, pos: (%lf, %lf, %lf)] -> [sosi: %d, pos: (%lf, %lf, %lf)]. Error: %d",
          sSosiFraSys, dFraX, dFraY, dFraZ, sSosiTilSys, result[0], result[1], result[2], sErr);

    return sErr;
}

JNIEXPORT jint JNICALL SOSITRANS_FUNCTION(JNIEnv* env, jobject obj_this, jint fraSosiSys, jdouble fraX, jdouble fraY, jdouble fraZ, jint tilSosiSys, jdoubleArray return_value) {
    double *result = (*env)->GetDoubleArrayElements(env, return_value, 0);
    int retval = sositrans(fraSosiSys, fraX, fraY, fraZ, tilSosiSys, result);
    (*env)->ReleaseDoubleArrayElements(env, return_value, result, 0);
    return retval;
}

void store_last_error(const char *format, ...) {
    const size_t max_size = 4096;
    char message[max_size];
    va_list ap;
    va_start(ap, format);
    int written = vsnprintf(message, max_size, format, ap);
    if (written == max_size) {
        message[max_size - 1] = '\0';
    }
    va_end(ap);
    if (last_error != NULL) {
        free(last_error);
    }
#ifdef ON_WINDOWS
    DWORD error = GetLastError();
    LPVOID buffer = NULL;
    FormatMessage(
        FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
        NULL,
        error,
        MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
        (LPTSTR) &buffer,
        0,
        NULL);
    free(last_error);
    last_error = (char*) malloc((strlen(buffer) + written + 3) * sizeof(char));
    strcpy(last_error, message);
    strcat(last_error, ": ");
    strcat(last_error, buffer);
    LocalFree(buffer);
#else
    free(last_error);
    last_error = (char*) malloc(strlen(message) * sizeof(char));
    strcpy(last_error, message);
    // TODO: Add strerror(errno) here
#endif
}

void normalize(char *path) {
    char *src = path;
    char *dst = path;
    char *end = path + strlen(path) * sizeof(char);
    if (path[0] == '.' && path[1] == SEPARATOR_CHAR) {
        src += 2 * sizeof(char);
    }
    for (; src < end; src++) {
        if (src < (end - 3 * sizeof(char))
            && *src == SEPARATOR_CHAR
            && *(src + 1) == '.'
            && *(src + 2) == SEPARATOR_CHAR)
        {
            src += 2 * sizeof(char);
        }
        dst = src;
        dst++;
    }
}

/**
 * Initialiserings-funksjon. Her lastes ekstern dll og
 * funksjons-innganger blir gitt. Får også satt floating point flagg
 * riktig i forhold til dll-en.
 */
SOSI_EXPORT
int initialize(const char *init_path) {
#ifdef ON_WINDOWS
    char *library_path = (char*) malloc((strlen(init_path) + 1) * sizeof(char));
    strcpy(library_path, init_path);
    library_path = dirname(library_path);
    size_t library_path_len = strlen(library_path);
    char *fullpath = (char*) malloc((library_path_len + strlen(DLL_FILENAME) + 2) * sizeof(char));
    strcpy(fullpath, library_path);
    strcat(fullpath, SEPARATOR);
    strcat(fullpath, DLL_FILENAME);
    normalize(fullpath);
    control_fp_java = _controlfp(0, 0);
    debug("Attempting to load library %s", fullpath);
    hLib = LoadLibraryExA(fullpath, NULL, LOAD_LIBRARY_SEARCH_DLL_LOAD_DIR | LOAD_LIBRARY_SEARCH_SYSTEM32); // LOAD_LIBRARY_SEARCH_DEFAULT_DIRS
    if (hLib == 0) {
        debug("Failed to load dll '%s'", fullpath);
        store_last_error("Failed to load dll '%s'", fullpath);
        return FALSE;
    }
    debug("%s loaded successfully", fullpath);
    free(library_path);
    free(fullpath);

    control_fp_c = _controlfp(0, 0);
    control_words_differ = (control_fp_c != control_fp_java);
    debug("Control words for C (0x%X) and Java (0x%X) %s", control_fp_c, control_fp_java, control_words_differ ? "differ" : "are identical");

    debug("Locating address of FellesPunkt symbol %s", FELLES_FUN);
    FellesPunkt = (void (__cdecl *)(char *szFileName, short *sStrlen, short *sErr)) GetProcAddress(hLib, FELLES_FUN);
    if (FellesPunkt == 0) {
        debug("Failed to locate " FELLES_FUN " symbol");
        store_last_error("Failed to locate " FELLES_FUN " symbol");
        return FALSE;
    }
    debug("Found FellesPunkt symbol");

    debug("Locating address of SosiTrans symbol %s", TRANS_FUN);
    SosiTrans = (void(__cdecl *)(short *sSosiFraSys, double *dXfra, double *dYfra, double *dHfra, short *sSosiTilSys, double *dXtil, double *dYtil, double *dHtil, short *sErr)) GetProcAddress(hLib, TRANS_FUN);
    if (SosiTrans == 0) {
        debug("Failed to locate " TRANS_FUN " symbol");
        store_last_error("Failed to locate " TRANS_FUN " symbol");
        return FALSE;
    }
    debug("Found SosiTrans symbol");

    short err = 0;
    size_t pathlen = strlen(init_path);
    char *fellespath = (char*) malloc((pathlen + strlen(FELLESPUNKT_FILENAME) + 2) * sizeof(char));
    strcpy(fellespath, init_path);
    strcat(fellespath, SEPARATOR);
    strcat(fellespath, FELLESPUNKT_FILENAME);
    short fellespath_len = strlen(fellespath);
    debug("Initializing FellesPunkt file %s", fellespath);
    FellesPunkt(fellespath, &fellespath_len, &err);

    if (err != 0) {
        debug("Fellespunkt(%s, %d, err) failed with error code %d", fellespath, fellespath_len, err);
        store_last_error("Fellespunkt(%s, %d, err) failed with error code %d", fellespath, fellespath_len, err);
        free(fellespath);
        return FALSE;
    }

    free(fellespath);
    set_java_control_word();
    debug("FellesPunkt initalization successful");
#else
    char *path = getenv("INITPATH");
    size_t pathlen = strlen(init_path);
    short err, len, fileno;
    char fullpath[strlen(init_path) + 0xff];
    for(initfile_t *next = initfiles; next->fileno != -1; next++) {
        strcpy(fullpath, init_path);
        strcat(fullpath, SEPARATOR);
        strcat(fullpath, next->filename);
        len = strlen(fullpath);
        fileno = next->fileno;
        initfiler_((char*) fullpath, &len, &fileno, &err);
        if (err != 0) {
            debug("Failed to initialize %s (%s)\n", fullpath, next->desc);
            store_last_error("Failed to initialize %s (%s)\n", fullpath, next->desc);
            return FALSE;
        }
        debug("Initialized %s (%s)", next->filename, next->desc);
    }
#endif
    return TRUE;
}

JNIEXPORT jboolean JNICALL INITIALIZE_FUNCTION(JNIEnv *env, jobject obj_this, jstring init_path) {
    debug("Entered upper level initialize");
    const char *path = (*env)->GetStringUTFChars(env, init_path, 0);
    int result = initialize(path);
    (*env)->ReleaseStringUTFChars(env, init_path, path);
    debug("upper level initialize finished %s", (result == TRUE) ? "successfully" : "with failure");
    return result == TRUE;
}

JNIEXPORT jstring JNICALL GETLASTERROR_FUNCTION(JNIEnv *env, jobject obj_this) {
    size_t err_len = last_error == NULL ? 0 : strlen(last_error);
#ifdef ON_WINDOWS
    wchar_t result[err_len];
    mbstowcs(result, last_error, err_len);
    return (*env)->NewString(env, (jchar*) result, (jsize) err_len);
#else
    jclass strClass = (*env)->FindClass(env, "java/lang/String");
    jmethodID ctorID = (*env)->GetMethodID(env, strClass, "<init>", "([BLjava/lang/String;)V");

    jbyteArray bytes = (*env)->NewByteArray(env, err_len);
    (*env)->SetByteArrayRegion(env, bytes, 0, err_len, (jbyte*) last_error);
    return (*env)->NewObject(env, strClass, ctorID, bytes, (*env)->NewStringUTF(env, "UTF-8"));
#endif
}
