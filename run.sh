./gradlew clean build
scp ./build/bin/linuxArm64/debugTest/test.kexe guru@raspberrypi-1:kopi
ssh guru@raspberrypi-1 ./kopi/test.kexe
