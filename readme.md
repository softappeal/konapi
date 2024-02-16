### Trying out Kotlin Native with Raspberry Pi ...

- Inspired by
    - https://github.com/ktgpio/ktgpio
    - https://github.com/ktgpio/ktgpio-samples

```shell
sudo apt update
sudo apt full-upgrade
sudo reboot
```

```shell
ssh guru@raspberrypi
```

```shell
scp ./build/bin/linuxArm64/debugExecutable/kopi.kexe guru@raspberrypi:kopi
```
