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
scp ./build/bin/linuxArm64/releaseExecutable/kopi.kexe guru@raspberrypi:kopi
```

```shell
cat /sys/firmware/devicetree/base/model
```

Raspberry Pi Zero 2 W Rev 1.0
Raspberry Pi 5 Model B Rev 1.0
