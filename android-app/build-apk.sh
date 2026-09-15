#!/usr/bin/env bash

set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$project_dir"

if [[ -x "$project_dir/gradlew" ]]; then
    gradle_command=("$project_dir/gradlew")
elif command -v gradle >/dev/null 2>&1; then
    gradle_command=(gradle)
else
    printf '%s\n' "Gradle was not found. Install Gradle or add a Gradle wrapper to this project." >&2
    exit 1
fi

"${gradle_command[@]}" :app:assembleDebug

apk_path="$project_dir/app/build/outputs/apk/debug/app-debug.apk"
if [[ ! -f "$apk_path" ]]; then
    printf '%s\n' "Build completed, but the APK was not found at: $apk_path" >&2
    exit 1
fi

printf 'APK built successfully: %s\n' "$apk_path"