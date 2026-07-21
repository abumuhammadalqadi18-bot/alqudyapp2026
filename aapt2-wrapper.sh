#!/bin/bash
exec qemu-x86_64 -L /tmp/amd64-libs /opt/android-sdk/build-tools/36.0.0/aapt2 "$@"
