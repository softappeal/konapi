## Cheatsheet

### Connect

```shell
ssh guru@raspberrypi-1
```

### Update

```
sudo apt update
sudo apt full-upgrade
sudo reboot
```

### Autostart

```
sudo systemctl enable sample
systemctl status sample
```

```
sudo nano /etc/systemd/system/sample.service
```

```
[Unit]
Description=sample

[Service]
WorkingDirectory=/home/guru
ExecStart=/home/guru/sample.kexe

[Install]
WantedBy=multi-user.target
```

### Camera

```
rpicam-vid -t 0 --inline --listen -o tcp://0.0.0.0:8090
vlc tcp/h264://raspberrypi-1:8090

rpicam-vid -t 0 --inline -o udp://0.0.0.0:8090
vlc udp://@:<port> :demux=h264
```
