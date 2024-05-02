set -e

../gradlew :sample:clean :sample:build

ls  ../fonts/font > build/fonts.txt
scp ../fonts/font/* build/fonts.txt ../test-files/*    guru@raspberrypi-1:sample-files
scp build/bin/linuxArm64/releaseExecutable/sample.kexe guru@raspberrypi-1:.
ssh                                                    guru@raspberrypi-1 ./sample.kexe
