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
ssh guru@raspberrypi-1
```

```shell
scp ./build/bin/linuxArm64/releaseExecutable/kopi.kexe guru@raspberrypi-1:kopi
```

```shell
cat /sys/firmware/devicetree/base/model
```

- Raspberry Pi Zero 2 W Rev 1.0
- Raspberry Pi 5 Model B Rev 1.0

autostart

```
sudo nano /etc/rc.local

#!/bin/sh -e
sudo /home/guru/kopi/kopi.kexe &
exit 0
```
