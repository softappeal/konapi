set -e

./gradlew clean build

scp ./test-files/*                             guru@raspberrypi-1:konapi/test-files
scp ./fonts/font/*                             guru@raspberrypi-1:konapi/fonts/font
scp ./build/bin/linuxArm64/debugTest/test.kexe guru@raspberrypi-1:konapi

ssh                                            guru@raspberrypi-1 "cd konapi; ./test.kexe"
