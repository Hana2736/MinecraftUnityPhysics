using System;
using System.Collections.Generic;
using UnityEngine;

public class GameHandler : MonoBehaviour
{
    public GameObject playerPrefab, worldBlockPrefab, physBlockPrefab, worldBlocksContainer;
    private NetServer netServer;

    private double sendTimer;
    private Dictionary<uint, PhysicsBlock> trackedNetObjects;

    private Dictionary<uint, RemotePlayer> trackedNetPlayers;

    // Use this for initialization
    private void Start()
    {
        Debug.Log(Environment.Version);
        netServer = new NetServer(2737);
        trackedNetObjects = new Dictionary<uint, PhysicsBlock>();
        trackedNetPlayers = new Dictionary<uint, RemotePlayer>();
    }

    // Update is called once per frame
    private void Update()
    {
        //Every Minecraft tick, send the state of our physics blocks (20tps)
        sendTimer += Time.deltaTime;
        if (sendTimer >= 0.05d)
        {
            foreach (var id in trackedNetObjects.Keys) sendUpdate(id);
            sendTimer = 0;
        }

        //process incoming messages from Minecraft
        while (!netServer.incomingMsgs.IsEmpty)
        {
            object nextMsg;
            while (!netServer.incomingMsgs.TryDequeue(out nextMsg))
            {
                //burn the thread until we can read what we want
            }
            Type mType = nextMsg.GetType();
            if (mType == typeof(AddPlayer))
            {
                var addPlayerMsg = (AddPlayer)nextMsg;
                var entId = addPlayerMsg.playerID;
                var newPlayerName = addPlayerMsg.playerName;
                var newPlayer = Instantiate(playerPrefab, Vector3.zero, Quaternion.identity);
                //find the nametag within the player
                var nameTag = newPlayer.GetComponentInChildren<TextMesh>();
                nameTag.text = newPlayerName;
                //save the player by their control script
                trackedNetPlayers[entId] = newPlayer.GetComponentInChildren<RemotePlayer>();
            }
            else if (mType == typeof(WorldBlock))
            {
                var worldBlockMsg = (WorldBlock)nextMsg;
                var position = new Vector3(worldBlockMsg.blockCoords[0], worldBlockMsg.blockCoords[1],
                    worldBlockMsg.blockCoords[2]);
                //add a block w/ collision but no physics to Unity scene
                Instantiate(worldBlockPrefab, position, Quaternion.identity, worldBlocksContainer.transform);
            }
            else if (mType == typeof(PlayerUpdate))
            {
                var playerUpdateMsg = (PlayerUpdate)nextMsg;
                var pID = playerUpdateMsg.playerID;
                var pos = netFloatArrToVec(playerUpdateMsg.playerCoords);
                var vel = netFloatArrToVec(playerUpdateMsg.playerVels);
                var player = trackedNetPlayers[pID];
                //set unity player location and estimated velocity
                player.transform.position = pos;
                player.velocity = vel;
            }
            else if (mType == typeof(AddPhys))
            {
                var addPhysMsg = (AddPhys)nextMsg;
                var newBlock =
                    Instantiate(physBlockPrefab, netFloatArrToVec(addPhysMsg.objectCoords), Quaternion.identity)
                        .GetComponent<PhysicsBlock>();
                newBlock.myId = addPhysMsg.objectID;
                trackedNetObjects[newBlock.myId] = newBlock;
            }
        }
    }


    private static Vector3 netFloatArrToVec(float[] arr)
    {
        return new Vector3(arr[0], arr[1], arr[2]);
    }

    private void sendUpdate(uint id)
    {
        var phys = trackedNetObjects[id];
        var pos = phys.myChild.position;
        //send position and rotation matrix (flip pos XZ for notch)
        var rotate = phys.myState;
        var updateMsg = new PhysUpdate
        {
            objectID = id,
            objectCoords = new[] { pos.z, pos.y, pos.x }
        };
        var rotateArray = new float[16];
        int ind = 0;
        for (int row = 0; row < 4; row++)
        {
            for (int col = 0; col < 4; col++)
            {
                rotateArray[ind] = rotate[row, col];
                ind++;
            }
        }
        updateMsg.objectTransMatrixs = rotateArray;
        netServer.sendMsg(updateMsg);
    }
}