#!/bin/sh
# Runs *inside* the gcc:9 container (invoked by rebuild-linux-lib.sh). Builds
# libsositrans.so from the current native/SosiTransformasjon.c and the vendored
# native/libskt2lan2.a via native/Makefile's own Linux recipe (the same one used for
# local/manual builds - see native/README.md), then verifies none of its functions are
# exported as global symbols with names that collide with HotSpot's internal
# debugger-helper symbols.
#
# See the incident note in SosiTransformasjon.c (the `debug`/`debugging` functions) for
# why this check exists: a non-static `debug()` here previously collided, via ELF symbol
# interposition, with JDK 25 libjvm.so's own globally exported `debug` symbol, causing raw
# "Executing debug" lines to be written straight to stdout on every call.
#
# Neither Debian's packaged libgfortran.a/libquadmath.a nor the ones bundled in this image's
# own /usr/local/lib64 are position-independent (PIC), so they cannot be statically linked
# into libsositrans.so (a shared object) - linking fails with
# "relocation ... can not be used when making a shared object; recompile with -fPIC".
# The original /opt/gcc-9.2.0 toolchain this was built with (an internal Kartverket build,
# predating this repo and not recorded anywhere we have access to - see the incident
# write-up for how this was traced) must have been configured with PIC-enabled static
# runtime libs. Since that exact toolchain can no longer be reproduced, this script instead
# builds matching libgfortran/libquadmath static libraries from GCC's own source with
# -fPIC, using the same GCC 9.5.0 version already bundled in this gcc:9 image for ABI
# compatibility with the vendored native/libskt2lan2.a (built with GCC 9.x).

set -eu

GCC_VERSION=9.5.0
WORKDIR=/tmp/gcc-pic-build

# gcc:9 is based on Debian 11 (bullseye), which is EOL: deb.debian.org no longer serves it,
# only the archive.debian.org snapshot does (whose Release files are also intentionally
# past their Valid-Until date, hence disabling that check).
sed -i 's|deb.debian.org|archive.debian.org|g; s|security.debian.org|archive.debian.org|g' /etc/apt/sources.list
echo 'Acquire::Check-Valid-Until "false";' > /etc/apt/apt.conf.d/99archive

# Needed to compile SosiTransformasjon.c (for jni.h/jni_md.h), not for the libgfortran/
# libquadmath build below.
apt-get update -qq
apt-get install -y --no-install-recommends openjdk-17-jdk-headless

echo "--- Downloading GCC ${GCC_VERSION} source (for a PIC-enabled libgfortran/libquadmath only; not rebuilding gcc itself) ---"
mkdir -p "$WORKDIR"
cd "$WORKDIR"
curl -sSfL "https://ftp.gnu.org/gnu/gcc/gcc-${GCC_VERSION}/gcc-${GCC_VERSION}.tar.xz" -o gcc.tar.xz
tar xf gcc.tar.xz
cd "gcc-${GCC_VERSION}"
./contrib/download_prerequisites

echo "--- Configuring+building a throwaway GCC with --with-pic (only used to obtain PIC static libgfortran.a/libquadmath.a) ---"
mkdir -p build
cd build
../configure \
    --prefix=/opt/gcc-pic \
    --enable-languages=c,fortran \
    --disable-multilib \
    --disable-bootstrap \
    --disable-shared \
    --with-pic
# Only need libgfortran + libquadmath (and their prerequisite stage1 compiler), not the
# full bootstrap/docs/etc - this is still a substantial build (tens of minutes).
make -j"$(nproc)" all-target-libgfortran all-target-libquadmath

PIC_LIBGFORTRAN=$(find "$WORKDIR/gcc-${GCC_VERSION}/build" -name 'libgfortran.a' | head -1)
PIC_LIBQUADMATH=$(find "$WORKDIR/gcc-${GCC_VERSION}/build" -name 'libquadmath.a' | head -1)
echo "Built PIC libgfortran.a: $PIC_LIBGFORTRAN"
echo "Built PIC libquadmath.a: $PIC_LIBQUADMATH"

# Stage the PIC archives under a GCC_HOME-shaped directory (lib64/lib{gfortran,quadmath}.a)
# so that native/Makefile's existing Linux build recipe - the same one used for local/manual
# builds - can be pointed at them via its GCC_HOME variable. This keeps the compile/link
# flags and the static-archive link order defined in exactly one place (the Makefile)
# instead of duplicating them here.
GCC_STAGE_HOME="$WORKDIR/gcc-home"
mkdir -p "$GCC_STAGE_HOME/lib64"
cp "$PIC_LIBGFORTRAN" "$PIC_LIBQUADMATH" "$GCC_STAGE_HOME/lib64/"

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

cd /work/native
make clean
make GCC_HOME="$GCC_STAGE_HOME" JAVA_HOME="$JAVA_HOME"

echo "--- Verifying no colliding global debug-helper symbols are exported ---"
bad_symbols="debug debugging nm pp ps flush find help universe verify ndebug"
status=0
for sym in $bad_symbols; do
    if nm -D --defined-only libsositrans.so | awk '{print $NF}' | grep -qx "$sym"; then
        echo "ERROR: symbol '$sym' is globally exported from libsositrans.so" >&2
        status=1
    fi
done

echo "--- Verifying the result is fully statically linked (no libgfortran/libquadmath runtime dependency) ---"
if ldd libsositrans.so | grep -qE 'libgfortran|libquadmath'; then
    echo "ERROR: libsositrans.so still dynamically depends on libgfortran/libquadmath" >&2
    status=1
fi

exit "$status"
