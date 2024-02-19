./gradlew clean build
scp ./build/bin/linuxArm64/releaseExecutable/kopi.kexe guru@raspberrypi:kopi
ssh guru@raspberrypi ./kopi/kopi.kexe
