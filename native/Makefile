
CPP  = g++
CC   = gcc
INSTALL = install
RM = rm -f
STRIP = strip
LINKOBJ  = SosiTransformasjon.o
# LIBS = --no-export-all-symbols
CFLAGS += -O3 -I"${JAVA_HOME}/include"
PREFIX ?= ../../all/lib.native64bit
DLL = SosiTransformasjon.dll
SO = libsositrans.so
SKTLAN_ARCHIVE = libskt2lan2.a

ifeq ($(OS),Windows_NT)
	SHAREDLIB = $(DLL)
	JAVA_HOME ?= C:/Program Files/Java/jdk1.8.0_212
	CFLAGS += -I"/mingw64/x86_64-w64-mingw32/include" -I"${JAVA_HOME}/include/win32" -DBUILDING_DLL=1 -Wall
	STATICLIB = libSosiTransformasjon.a
	LDFLAGS = -shared -Wl,--out-implib,$(STATICLIB)
	INSTALLFILES = $(STATICLIB) $(SHAREDLIB)
else
	JAVA_HOME ?= /opt/java/jdk
	SHAREDLIB = $(SO)
	BIN = $(LINBIN)
	CFLAGS += -I"${JAVA_HOME}/include/linux" -fPIC
	LDFLAGS = -fPIC -shared -static-libgcc -lc -lm $(SKTLAN_ARCHIVE) /opt/gcc-8.3.0/lib64/libquadmath.a /opt/gcc-8.3.0/lib64/libgfortran.a
#	LDFLAGS = -shared -static-libgcc -static-libgfortran -L. -Wl,--whole-archive -lskt2lan2 -lgfortran -lquadmath -lm -Wl,--no-whole-archive
	INSTALLFILES = $(SHAREDLIB)
endif

ifdef DEBUG
	CFLAGS += -g3 -ggdb
	CPP += -D__DEBUG__
	CC += -D__DEBUG__
endif

.PHONY: all clean $(LINKOBJ)

all: $(SHAREDLIB)

clean:
	${RM} $(LINKOBJ) $(SO) $(DLL)

$(SHAREDLIB): $(LINKOBJ)
	$(CC) -o $(SHAREDLIB) $^ $(LDFLAGS)

$(LINKOBJ): SosiTransformasjon.c
	$(CC) -c $^ -o $@ $(CFLAGS)

stripped: all
	$(STRIP) $(SHAREDLIB)

install: $(INSTALLFILES)
	$(INSTALL) $^ $(PREFIX)
