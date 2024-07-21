## Cheatsheet

### Connect

```shell
ssh me@pi0
```

### Update

```
sudo apt update
sudo apt full-upgrade
sudo reboot
```

### Enable SSH public key authentication

- Add public key to file `~/.ssh/authorized_keys` of Raspberry Pi.
- Add private key file to directory `~/.ssh` of client.

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
WorkingDirectory=/home/me
ExecStart=/home/me/sample.kexe

[Install]
WantedBy=multi-user.target
```

### Camera

```
rpicam-vid -t 0 --inline --listen -o tcp://0.0.0.0:8090
vlc tcp/h264://pi0:8090

rpicam-vid -t 0 --inline -o udp://0.0.0.0:8090
vlc udp://@:<port> :demux=h264
```
