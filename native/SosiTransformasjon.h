#ifndef _WIN32
void initfiler_(char* filename, short* filenamelen, short* filenumber, short* err);
void sositrans_(short *sSosiFraSys, double *dXfra, double *dYfra, double *dHfra, short *sSosiTilSys, double *dXtil, double *dYtil, double *dHtil, short *error);
#else
void (__cdecl *InitSktrf)(short *error);
void (__cdecl *FellesPunkt)(char *szFileName, short *sStrlen, short *sErr);
void (__cdecl *SosiTrans)(short *sSosiFraSys, double *dXfra, double *dYfra, double *dHfra, short *sSosiTilSys, double *dXtil, double *dYtil, double *dHtil, short *error);
#endif

     
