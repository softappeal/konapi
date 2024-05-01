set -e

../gradlew :sample:clean :sample:build

ls ../fonts/font > fonts.txt

scp ../fonts/font/*                                      guru@raspberrypi-1:font
scp ./fonts.txt                                          guru@raspberrypi-1:.

scp ./build/bin/linuxArm64/releaseExecutable/sample.kexe guru@raspberrypi-1:.

ssh                                                      guru@raspberrypi-1 ./sample.kexe
