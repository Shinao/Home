# HomeManager

HomeManager is an Android client which unlock my arduino powered door via bluetooth and my raspberry pi security camera via internet. For now.


## Components

Three main components in HomeManager for now :

- __`client`__ - Android widget client to control the security system
- __`door`__ - Arduino bluetooth server controlling the door lock
- __`motioneye`__ - patch for the raspberry pi OS to control the system via an API

## Client process
The Android client is a simple widget used to control the door and camera, the application in itself is a blank UI.
<p align="center"><img src="docs/client_process.png"/></p>


## Schematic
- JY-MCU for bluetooth
- Relay 5V
- Solenoid door lock
- Arduino UNO
- Transformer 9V
<p align="center"><img src="docs/pretty_schematic.png"/></p>


## Notes

The bluetooth communication is "secured" by a system of token, AES being hard to implement on both Arduino and Android without rewriting a few things I've taken a much simpler solution with tokens and MD5 : it's easy to use and has a good enough security.

As for motionEye, a hotfix has been made to allow external call to use a custom API to toggle the alarm system of the camera, in the future it should be taken off since it was in the TODO list of the maintainer.
