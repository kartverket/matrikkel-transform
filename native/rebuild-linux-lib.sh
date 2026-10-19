#!/bin/sh
# Rebuilds lib/libsositrans.so (the Linux x86_64 binary actually used in prod) from the
# current native/SosiTransformasjon.c, using a gcc:9 container to stay close to the
# GCC 9.2.0 toolchain that native/libskt2lan2.a was originally linked against.
#
# This replaces the old Jenkins job (deleted in TH-2541) that used to rebuild and upload
# this binary. It is a manual, on-demand step: run it and commit the resulting
# lib/libsositrans.so whenever native/SosiTransformasjon.c changes. Requires Docker.
#
# Works under plain sh/dash/ash as well as Windows (msys2/git-bash), since it only shells
# out to docker and does not rely on any bash-only features.
#
# Usage:
#   native/rebuild-linux-lib.sh

set -eu

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
repo_root=$(CDPATH= cd -- "$script_dir/.." && pwd)

# --platform linux/amd64 is required: prod runs Linux x86_64, but on an Apple Silicon (or
# other arm64) host, Docker would otherwise silently pull/run the arm64 variant of gcc:9 and
# produce a binary for the wrong architecture.
#
# Under Git Bash/MSYS2, docker.exe's argument conversion would otherwise also rewrite the
# container-side /work/... script path below into a Windows host path, so the container
# can't find it. MSYS_NO_PATHCONV=1 disables that conversion entirely; we then convert only
# the host-side mount source ourselves (repo_root itself is left untouched, since it's also
# used unconverted for the plain-sh cp below).
volume_src="${repo_root}"
if command -v cygpath >/dev/null 2>&1; then
    volume_src=$(cygpath -w "${repo_root}")
fi

MSYS_NO_PATHCONV=1 docker run --rm --platform linux/amd64 -v "${volume_src}:/work" gcc:9 sh /work/native/docker-build-linux-inner.sh

cp "${repo_root}/native/libsositrans.so" "${repo_root}/lib/libsositrans.so"
echo "Rebuilt and copied libsositrans.so to lib/. Review and commit the result."
