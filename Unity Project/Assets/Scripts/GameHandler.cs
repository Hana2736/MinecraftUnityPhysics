using System;
using System.Collections.Generic;
using System.Text;
using UnityEngine;

public class GameHandler : MonoBehaviour
{
    public GameObject playerPrefab, worldBlockPrefab, physBlockPrefab, worldBlocksContainer;
    private NetServer netServer;

    private double sendTimer;
    private Dictionary<int, PhysicsBlock> trackedNetObjects;

    private Dictionary<int, RemotePlayer> trackedNetPlayers;

    // Use this for initialization
    private void Start()
    {
        Debug.Log(Environment.Version);
        netServer = new NetServer(2737);
        trackedNetObjects = new Dictionary<int, PhysicsBlock>();
        trackedNetPlayers = new Dictionary<int, RemotePlayer>();
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
            string nextMsg;
            while (!netServer.incomingMsgs.TryDequeue(out nextMsg))
            {
                //burn the thread until we can read what we want
            }
            var parts = nextMsg.Split("??");
            //Debug.Log("Msg:  " + nextMsg);
            switch (parts[0])
            {
                //if the Minecraft server wants to add a human to our simulation
                case "AddPlayer":
                {
                    //AddPlayer -> 0 -> Hana2736
                    var entId = int.Parse(parts[1]);
                    var newPlayerName = parts[2];
                    var newPlayer = Instantiate(playerPrefab, Vector3.zero, Quaternion.identity);
                    //find the nametag within the player
                    var nameTag = newPlayer.GetComponentInChildren<TextMesh>();
                    nameTag.text = newPlayerName;
                    //save the player by their control script
                    trackedNetPlayers[entId] = newPlayer.GetComponentInChildren<RemotePlayer>();
                    break;
                }
                //if the Minecraft server is sending us block data
                case "WorldBlock":
                {
                    //WorldBlock -> x -> y -> z
                    var position = string3ToVec(parts[1], parts[2], parts[3]);
                    //add a block w/ collision but no physics to Unity scene
                    Instantiate(worldBlockPrefab, position, Quaternion.identity, worldBlocksContainer.transform);
                    break;
                }
                //players moving in the world
                case "PlayerUpdate":
                {
                    //PlayerUpdate -> playerID -> x -> y -> z -> vX -> vY -> vZ
                    var pID = int.Parse(parts[1]);
                    var pos = string3ToVec(parts[2], parts[3], parts[4]);
                    var vel = string3ToVec(parts[5], parts[6], parts[7]);
                    var player = trackedNetPlayers[pID];
                    //set unity player location and estimated velocity
                    player.transform.position = pos;
                    player.velocity = vel;
                    break;
                }
                case "AddPhys":
                {
                    //AddPhys -> id -> x -> y -> z
                    var newBlock = Instantiate(physBlockPrefab, string3ToVec(parts[2], parts[3], parts[4]),
                        Quaternion.identity).GetComponent<PhysicsBlock>();
                    newBlock.myId = int.Parse(parts[1]);
                    trackedNetObjects[newBlock.myId] = newBlock;
                    break;
                }
            }
        }
    }

    private static Vector3 string3ToVec(string x, string y, string z)
    {
        var dx = float.Parse(x);
        var dy = float.Parse(y);
        var dz = float.Parse(z);
        return new Vector3(dx, dy, dz);
    }

    private void sendUpdate(int id)
    {
        //PhysUpdate -> id -> posX -> posY -> posZ -> matrix0 -> matrix1 ->.... matrix15
        var update = new StringBuilder("PhysUpdate??");
        var phys = trackedNetObjects[id];
        var pos = phys.myChild.position;
        var rotate = phys.myState;
        //send position and rotation matrix (flip pos XZ for notch)
        //this code is really nasty, maybe i will consider a protobuf rewrite
        update.Append(id).Append("??")
            .Append(pos.z).Append("??").Append(pos.y).Append("??").Append(pos.x).Append("??")
            .Append(rotate.m00).Append("??").Append(rotate.m01).Append("??").Append(rotate.m02).Append("??")
            .Append(rotate.m03).Append("??")
            .Append(rotate.m10).Append("??").Append(rotate.m11).Append("??").Append(rotate.m12).Append("??")
            .Append(rotate.m13).Append("??")
            .Append(rotate.m20).Append("??").Append(rotate.m21).Append("??").Append(rotate.m22).Append("??")
            .Append(rotate.m23).Append("??")
            .Append(rotate.m30).Append("??").Append(rotate.m31).Append("??").Append(rotate.m32).Append("??")
            .Append(rotate.m33);
        
        netServer.sendMsg(update.ToString());
    }
}