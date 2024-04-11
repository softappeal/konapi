set -e

./gradlew clean build

scp ./test-files/*                                        guru@raspberrypi-1:kopi/test-files
scp ./build/bin/linuxArm64/debugTest/test.kexe            guru@raspberrypi-1:kopi
scp ./app/build/bin/linuxArm64/releaseExecutable/app.kexe guru@raspberrypi-1:kopi

ssh                                                       guru@raspberrypi-1 "cd kopi; ./test.kexe"
