#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'USAGE'
Usage: build-fex-glibc-rootfs.sh [options]

Build FEX (glibc target) and optionally install it into a Winlator glibc rootfs.

Options:
  --workdir <dir>        Working directory for source/build/output (default: ./build/fex)
  --rootfs <dir>         Rootfs directory to install into (optional)
  --ref <git-ref>        FEX git ref/tag/commit to checkout (default: main)
  --prefix <path>        Install prefix inside stage/rootfs (default: /usr)
  --jobs <n>             Parallel build jobs (default: nproc)
  --clean                Delete existing build directory before configuring
  --no-submodule-update  Skip git submodule update
  --log-file <path>      Write full build log to file (tee output)
  -h, --help             Show this help

Examples:
  ./scripts/build-fex-glibc-rootfs.sh --workdir /tmp/fex-build --ref main
  ./scripts/build-fex-glibc-rootfs.sh --workdir /tmp/fex-build --rootfs /path/to/imagefs --ref FEX-2508
USAGE
}

WORKDIR="$(pwd)/build/fex"
ROOTFS=""
REF="main"
PREFIX="/usr"
JOBS="$(nproc)"
CLEAN=0
UPDATE_SUBMODULES=1
LOG_FILE=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --workdir) WORKDIR="$2"; shift 2 ;;
    --rootfs) ROOTFS="$2"; shift 2 ;;
    --ref) REF="$2"; shift 2 ;;
    --prefix) PREFIX="$2"; shift 2 ;;
    --jobs) JOBS="$2"; shift 2 ;;
    --clean) CLEAN=1; shift ;;
    --no-submodule-update) UPDATE_SUBMODULES=0; shift ;;
    --log-file) LOG_FILE="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
  esac
done

need_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

need_cmd git
need_cmd cmake
need_cmd ninja
need_cmd clang
need_cmd clang++
need_cmd rsync
need_cmd tar
need_cmd zstd

if [[ -n "$ROOTFS" && ! -d "$ROOTFS" ]]; then
  echo "--rootfs path does not exist or is not a directory: $ROOTFS" >&2
  exit 1
fi

if [[ "$PREFIX" != /* ]]; then
  echo "--prefix must be an absolute path inside rootfs (example: /usr)" >&2
  exit 1
fi

if [[ -n "$LOG_FILE" ]]; then
  mkdir -p "$(dirname "$LOG_FILE")"
  exec > >(tee -a "$LOG_FILE") 2>&1
  echo "Logging to: $LOG_FILE"
fi

SRC_DIR="$WORKDIR/src/FEX"
BUILD_DIR="$WORKDIR/build"
STAGE_DIR="$WORKDIR/stage"
OUT_DIR="$WORKDIR/out"

mkdir -p "$WORKDIR" "$WORKDIR/src" "$OUT_DIR"

if [[ ! -d "$SRC_DIR/.git" ]]; then
  git clone https://github.com/FEX-Emu/FEX.git "$SRC_DIR"
fi

pushd "$SRC_DIR" >/dev/null
  git fetch --tags origin
  git checkout "$REF"

  if [[ "$UPDATE_SUBMODULES" -eq 1 ]]; then
    git submodule sync --recursive
    git submodule update --init --recursive
  fi
popd >/dev/null

if [[ "$CLEAN" -eq 1 ]]; then
  rm -rf "$BUILD_DIR" "$STAGE_DIR"
fi

mkdir -p "$BUILD_DIR" "$STAGE_DIR"

cmake -S "$SRC_DIR" -B "$BUILD_DIR" -G Ninja \
  -DCMAKE_BUILD_TYPE=Release \
  -DCMAKE_INSTALL_PREFIX="$PREFIX" \
  -DBUILD_TESTING=OFF \
  -DENABLE_LTO=ON \
  -DCMAKE_C_COMPILER=clang \
  -DCMAKE_CXX_COMPILER=clang++

cmake --build "$BUILD_DIR" --parallel "$JOBS"
DESTDIR="$STAGE_DIR" cmake --install "$BUILD_DIR"

# Collect common runtime files explicitly used by FEX at run-time
RUNTIME_CHECK_PATHS=(
  "$STAGE_DIR$PREFIX/bin/FEXInterpreter"
  "$STAGE_DIR$PREFIX/bin/FEXBash"
  "$STAGE_DIR$PREFIX/bin/FEXLoader"
  "$STAGE_DIR$PREFIX/bin/FEXServer"
)

for p in "${RUNTIME_CHECK_PATHS[@]}"; do
  if [[ ! -e "$p" ]]; then
    echo "Warning: expected runtime file not found: $p" >&2
  fi
done

# Package as artifact for rootfs maintainers.
ARCHIVE_NAME="fex-glibc-${REF//\//-}.tar.zst"
tar --zstd -C "$STAGE_DIR" -cf "$OUT_DIR/$ARCHIVE_NAME" .

echo "Built archive: $OUT_DIR/$ARCHIVE_NAME"

if [[ -n "$ROOTFS" ]]; then
  echo "Installing into rootfs: $ROOTFS"
  rsync -a "$STAGE_DIR/" "$ROOTFS/"
  echo "Installed FEX into rootfs successfully."
fi

echo "Done."
