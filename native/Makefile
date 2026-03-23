
CPP  = g++
CC   = gcc
INSTALL = install
RM = rm -f
STRIP = strip
LINKOBJ  = SosiTransformasjon.o
# LIBS = --no-export-all-symbols
CFLAGS += -O3 -I"${JAVA_HOME}/include"
PREFIX ?= ../lib
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

## BRUKES FOR MAC OPPSETT, lager libsositrans.jnilib
# else
# 	GCC_HOME ?= /opt/homebrew/Cellar/gcc/12.2.0/lib/gcc/12/
# 	CC = /opt/homebrew/bin/gcc-12
# 	SO = libsositrans.jnilib
# 	SHAREDLIB = $(SO)
# 	BIN = $(LINBIN)
# 	CFLAGS += -I $(JAVA_HOME)/include -I ${JAVA_HOME}/include/darwin
# 	LDFLAGS = -fPIC -shared -static-libgcc -lc -lm $(SKTLAN_ARCHIVE) $(GCC_HOME)/libquadmath.a $(GCC_HOME)/libgfortran.a
# 	INSTALLFILES = $(SHAREDLIB)
else
	GCC_HOME ?= /opt/gcc-9.2.0
	JAVA_HOME ?= /opt/java/jdk
	SHAREDLIB = $(SO)
	BIN = $(LINBIN)
	CFLAGS += -I"${JAVA_HOME}/include/linux" -fPIC
	LDFLAGS = -fPIC -shared -static-libgcc -lc -lm $(SKTLAN_ARCHIVE) $(GCC_HOME)/lib64/libquadmath.a $(GCC_HOME)/lib64/libgfortran.a
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
