# MinecraftUnityPhysics
Uses Unity Engine to add physics blocks to Minecraft

Unity 2022 Pro was used to create this. It should work in the free version. Default host port is 2737.

[Demo](https://fixupx.com/hana2736_/status/1817891539193876815)

# Commands

## loadscene

description- Initialize Minecraft scene in the Unity physics context

usage- /loadscene \<ip:port> \<radius>

## addblock

description- Add Unity physics block at coordinates

usage- /addblock \<material> \<x> \<y> \<z>

## fillblock

description- Add Unity physics block at coordinates like /fill (requires Sudo plugin)

usage- /fillblock \<material> \<x> \<y> \<z> .... <2>

## setmatrix

description- Set transform matrix of block (used for debugging, mostly)

usage- /setmatrix \<ID> \<4x4matrix vals 1-16>....
