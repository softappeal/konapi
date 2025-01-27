set -e

../gradlew :sample:clean :sample:build

ls  ../fonts/font > build/fonts.txt
scp ../fonts/font/* build/fonts.txt ../test-files/*    me@pi0:sample-files
scp build/bin/linuxArm64/releaseExecutable/sample.kexe me@pi0:.
ssh                                                    me@pi0 ./sample.kexe
