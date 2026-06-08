set -e

../gradlew clean :sample:build

ls  ../konapi/fonts/font > build/fonts.txt
scp ../konapi/fonts/font/* build/fonts.txt ../konapi/test-files/* me@pi0:sample-files
scp build/bin/linuxArm64/releaseExecutable/sample.kexe            me@pi0:.
ssh                                                               me@pi0 ./sample.kexe
