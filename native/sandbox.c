#include <stdlib.h>
#ifdef _WIN32
#include <windows.h>
#endif

#include <stdio.h>
#include <unistd.h>

#ifdef _WIN32
int (__cdecl *initialize)(const char *init_path);
int (__cdecl *sositrans)(int, double, double, double, int, double*);
#endif

#ifdef _WIN32
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
#endif

int main(int argc, const char *argv[]) {
    const char *library = argv[1];
    const char *initPath = argv[2];
    int fromSosi = atoi(argv[3]);
    double x = atof(argv[4]);
    double y = atof(argv[5]);
    int toSosi = atoi(argv[6]);
    int delay = 0;

    if (argc >= 8) {
        delay = atoi(argv[8]);
        fprintf(stderr, "Sleeping for %d seconds\n", delay);
    }

    sleep(delay);

    double result[3];
    int retval = 0;

#ifdef _WIN32
    fprintf(stderr, "Attempting to load %s\n", library);
    HINSTANCE lib = LoadLibraryExA(library, NULL, LOAD_LIBRARY_SEARCH_DLL_LOAD_DIR | LOAD_LIBRARY_SEARCH_SYSTEM32);
    if (lib == NULL) {
        fprintf(stderr, "Failed to load %s\n", library);
        exit(EXIT_FAILURE);
    }
    initialize = (int (__cdecl *)(const char *)) GetProcAddress(lib, "initialize");
    fprintf(stderr, "initialize: %p\n", initialize);
    sositrans = (int (__cdecl *)(int, double, double, double, int, double*)) GetProcAddress(lib, "sositrans");
    fprintf(stderr, "sositrans: %p\n", sositrans);
    int init_res = initialize(initPath);
    fprintf(stderr, "Initialization: %d\n", init_res);
    retval = sositrans(fromSosi, x, y, 0.0, toSosi, result);
#else
    (void) library;
    extern int initialize(const char*);
    extern int sositrans(int, double, double, double, int, double*);
    int init_res = initialize(initPath);
    fprintf(stderr, "Initialization: %d\n", init_res);
    retval = sositrans(fromSosi, x, y, 0.0, toSosi, result);
#endif
    fprintf(stderr, "(%f, %f, %f) [%d]\n", result[0], result[1], result[2], retval);
    fprintf(stderr, "Success!\n");
    exit(EXIT_SUCCESS);
}
