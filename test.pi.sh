set -e

./gradlew clean build

scp ./test.files/*                                                  guru@raspberrypi-1:konapi/test.files
scp ./build/bin/linuxArm64/debugTest/test.kexe                      guru@raspberrypi-1:konapi
scp ./demo.app/build/bin/linuxArm64/releaseExecutable/demo.app.kexe guru@raspberrypi-1:konapi

ssh                                                                 guru@raspberrypi-1 "cd konapi; ./test.kexe"
