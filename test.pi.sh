set -e

./gradlew :clean :build

scp test-files/*                             me@pi0:test-files
scp build/bin/linuxArm64/debugTest/test.kexe me@pi0:.
ssh                                          me@pi0 ./test.kexe
