#!/usr/bin/env bash
set -euo pipefail

VERSION="${VERSION:-4.1}"

DEST="tools/async-profiler"
if [[ -d "${DEST}" ]]; then
  echo "async-profiler already installed in ${DEST}"
  exit 0
fi

OS="$(uname -s)"
ARCH="$(uname -m)"

case "$OS" in
  Linux)  PKGOS=linux ;;
  Darwin) PKGOS=macos ;;
  *) echo "Unsupported OS: $OS" >&2; exit 1 ;;
esac

case "$ARCH" in
  x86_64|amd64) PKGARCH=x64 ;;
  arm64|aarch64) PKGARCH=arm64 ;;
  *) echo "Unsupported CPU: $ARCH" >&2; exit 1 ;;
esac

base="https://github.com/async-profiler/async-profiler/releases/download/v${VERSION}"

CANDIDATES=()
if [[ "$PKGOS" == "macos" ]]; then
  CANDIDATES+=("async-profiler-${VERSION}-macos.zip")
  CANDIDATES+=("async-profiler-${VERSION}-macos-${PKGARCH}.tar.gz")
else
  CANDIDATES+=("async-profiler-${VERSION}-linux-${PKGARCH}.tar.gz")
  CANDIDATES+=("async-profiler-${VERSION}-linux-${PKGARCH}.zip")
fi

tmpdir="$(mktemp -d)"
archive=""
for f in "${CANDIDATES[@]}"; do
  url="${base}/${f}"
  if curl -fsLI "$url" >/dev/null 2>&1; then
    echo "Downloading ${url} ..."
    curl -fsSL "$url" -o "${tmpdir}/${f}"
    archive="${tmpdir}/${f}"
    break
  fi
done

if [[ -z "${archive}" ]]; then
  echo "Failed to locate an async-profiler archive for VERSION=${VERSION}, OS=${PKGOS}, ARCH=${PKGARCH}" >&2
  echo "See release assets here:" >&2
  echo "  https://github.com/async-profiler/async-profiler/releases" >&2
  exit 1
fi

mkdir -p tools
case "${archive}" in
  *.zip)    unzip -q "${archive}" -d tools ;;
  *.tar.gz) tar xzf "${archive}" -C tools ;;
  *) echo "Unknown archive type: ${archive}" >&2; exit 1 ;;
esac

extracted="$(find tools -maxdepth 1 -type d -name "async-profiler-${VERSION}-*" | head -n 1)"
if [[ -z "${extracted}" ]]; then
  echo "Extraction failed" >&2
  exit 1
fi

mv -f "${extracted}" "${DEST}"
echo "Installed async-profiler to ${DEST}"
echo "Binary: ${DEST}/bin/asprof"