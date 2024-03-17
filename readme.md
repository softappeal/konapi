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

autostart

```
sudo nano /etc/rc.local

#!/bin/sh -e
sudo /home/guru/kopi/kopi.kexe &
exit 0
```

camera

```
rpicam-vid -t 0 --inline --listen -o tcp://0.0.0.0:8090
vlc tcp/h264://raspberrypi-1:8090

rpicam-vid -t 0 --inline -o udp://0.0.0.0:8090
vlc udp://@:<port> :demux=h264
```
