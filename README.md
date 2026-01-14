# MinecraftUnityPhysics
### Real-Time Rigid-Body Dynamics Integration for Voxel Environments

[![Demo Video](https://img.shields.io/badge/View-Demo-blue?style=for-the-badge&logo=twitter)](https://fixupx.com/hana2736_/status/1817891539193876815)

**MinecraftUnityPhysics** is a cross-platform bridge that offloads spatial physics calculations from Minecraft to the Unity Engine. By leveraging Unity’s physics engine, this project introduces high-fidelity rigid-body dynamics, complex collisions, and real-time gravity simulations into the Minecraft world.

---

## 🚀 Technical Overview
This project operates on a **Client-Server architecture** where Minecraft acts as the visual frontend and Unity serves as the authoritative physics host.

*   **Synchronization:** Real-time data streaming via high-frequency socket communication (Default Port: `2737`).
*   **Math & Mapping:** Implements 1:1 coordinate mapping and 4x4 transform matrix calculations to align Unity’s world space with Minecraft’s voxel grid.
*   **Extensibility:** Built to be integrated into Minecraft games that require complex phsyics, like building destruction or projectile tracking.

## 🛠️ Built With
- **Unity 2022:** (Developed on Pro, compatible with Personal)
- **C#:** Unity-side physics simulation and socket server.
- **Java:** Minecraft-side integration and command handling.

---

## 🎮 Command Documentation

The integration is controlled via a custom in-game CLI suite:

| Command | Description | Usage |
| :--- | :--- | :--- |
| `loadscene` | Initializes the Minecraft world boundaries within the Unity context. | `/loadscene <ip:port> <radius>` |
| `addblock` | Spawns a physics-enabled block at specific coordinates. | `/addblock <material> <x> <y> <z>` |
| `fillblock` | Batch-creates physics objects (Requires Sudo plugin). | `/fillblock <mat> <x1> <y1> <z1> <x2> <y2> <z2>` |
| `setmatrix` | Directly modifies the 4x4 transform matrix of an object (Debug). | `/setmatrix <ID> <matrix_values_1_16>` |

---

## ⚙️ Installation & Setup
1. **Unity Side:** Open the Unity project, set the host port, and enter Play mode to start the physics server.
2. **Minecraft Side:** Compile the plugin, changing the host port if needed, and connect to the Unity instance using `/loadscene`.
3. **Materials:** Ensure `material` names match Minecraft Spigot materials.
