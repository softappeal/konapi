set -e

./gradlew clean build

scp ./test-files/*                             guru@raspberrypi-1:test-files
scp ./fonts/font/*                             guru@raspberrypi-1:fonts/font
scp ./build/bin/linuxArm64/debugTest/test.kexe guru@raspberrypi-1:.

ssh                                            guru@raspberrypi-1 ./test.kexe
