# Setup promox Masquerading (NAT) with iptables

### Setup on laptop via wifi

#### setup on debian first.

https://pve.proxmox.com/wiki/Install_Proxmox_VE_on_Debian_12_Bookworm

#### if the wifi base on linux distribute which is not working: 

setup with other provider wifi.

https://www.linuxbabe.com/debian/connect-to-wi-fi-from-terminal-on-debian-wpa-supplicant

https://wiki.debian.org/WiFi/HowToUse#wpa_supplicant

https://pve.proxmox.com/pve-docs/chapter-sysadmin.html#_masquerading_nat_with_span_class_monospaced_iptables_span

config with interface enp4s0, wlp5s0

```Shell
auto lo
iface lo inet loopback

iface enp4s0 inet manual

allow-hotplug wlp5s0
iface wlp5s0 inet static
        address 192.168.0.24/24
        netmask 255.255.255.0
        gateway 192.168.0.1

auto vmbr0
#private sub network
iface vmbr0 inet static
        address  10.10.10.1/24
        bridge-ports none
        bridge-stp off
        bridge-fd 0

        post-up   echo 1 > /proc/sys/net/ipv4/ip_forward
        post-up   iptables -t nat -A POSTROUTING -s '10.10.10.0/24' -o wlp5s0 -j MASQUERADE
        post-down iptables -t nat -D POSTROUTING -s '10.10.10.0/24' -o wlp5s0 -j MASQUERADE

```