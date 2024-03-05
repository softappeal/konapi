./gradlew clean build
scp ./build/bin/linuxArm64/releaseExecutable/kopi.kexe guru@raspberrypi-1:kopi
ssh guru@raspberrypi-1 ./kopi/kopi.kexe
