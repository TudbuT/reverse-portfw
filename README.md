Reverse-PortForward
===================

This tool bypasses port restrictions of your router 
using some not-very-powerful server (those are 
really cheap.)

---

### How to set it up:

1. Buy some cheap server online, it will only need
   1. Enough disk space to run a java program 
   About 1GB free after OS is installed)
   2. 500MB RAM or more
   3. Flexible port settings
   4. Not much CPU power
2. Download reverse-portfw.jar to it
3. Run the jar like this:
`java -jar reverse-portfw.jar server <port> <key>`
4. Download reverse-portfw.jar to your destination server
5. Run it like this:
`java -jar reverse-portfw.jar client <ip of your bridge
server> <port> <port to redirect (on local machine)> 
<key>`
6. To restart, end BOTH processes (remote and on your local
server) and restart them.

---

### Applications and special features:

- Minecraft servers tested and functional.
- HTTP tested and functional.
- Some third-party protocols tested and functional.
- This is not an HTTP-Proxy. It will work with any TCP 
protocol that isn't reliant on TCPNODELAY.
- No disconnects, even when the sockets stay open for hours.
- Fast
- Little ping increase in normal applications
- A 1ms waiting delay before sending is built in to 
reduce stress and increase efficiency by waiting for 
further data.