using System.Collections.Concurrent;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using ProtoBuf;
using UnityEngine;

public class NetServer
{
    private readonly NetworkStream clientStream;
    public bool connected;
    public ConcurrentQueue<object> incomingMsgs;
    public ConcurrentQueue<object> outgoingMsgs;
    public Socket serverSocket, clientSocket;

    public NetServer(int port)
    {
        //get the local machine's endpoint, so we can bind and wait for clients
        var hostName = Dns.GetHostName();
        var localhost = Dns.GetHostEntry(hostName);
        var localIpAddress = localhost.AddressList[0];
        var endPoint = new IPEndPoint(localIpAddress, port);
        serverSocket = new Socket(localIpAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
        serverSocket.Bind(endPoint);
        //wait for client to connect
        serverSocket.Listen(100);
        clientSocket = serverSocket.Accept();
        //write immediately
        clientSocket.NoDelay = true;
        incomingMsgs = new ConcurrentQueue<object>();
        outgoingMsgs = new ConcurrentQueue<object>();
        connected = true;
        clientStream = new NetworkStream(clientSocket);
        //handle incoming messages from Minecraft, adding them to the queue for us to handle
        new Thread(() =>
        {
            while (connected)
            {
                //the client will tell us the type of message it is trying to send
                var nextTypeMsg =
                    Serializer.DeserializeWithLengthPrefix<NextMessageType>(clientStream, PrefixStyle.Base128);
                var nextType = nextTypeMsg.mType;
             //   Debug.Log("Expecting "+nextType);
                switch (nextType)
                {
                    //enqueue each type of message after the client tells us
                    case MessageType.mAddPlayer:
                    {
                        DeserializeMessage<AddPlayer>();
                        break;
                    }
                    case MessageType.mWorldBlock:
                    {
                        DeserializeMessage<WorldBlock>();
                        break;
                    }
                    case MessageType.mPlayerUpdate:
                    {
                        DeserializeMessage<PlayerUpdate>();
                        break;
                    }
                    case MessageType.mAddPhys:
                    {
                        DeserializeMessage<AddPhys>();
                        break;
                    }
                }
            }
        }).Start();


        //send messages to Minecraft
        new Thread(() =>
        {
            while (connected)
            {
                object toSend;
                while (outgoingMsgs.IsEmpty || !outgoingMsgs.TryDequeue(out toSend)) Thread.Sleep(1);

                if (toSend is PhysUpdate)
                {
                    var mType = new NextMessageType
                    {
                        mType = MessageType.mPhysUpdate
                    };
                    SerializeMessage<NextMessageType>(mType);
                    SerializeMessage<PhysUpdate>(toSend);
                }
            }
        }).Start();
    }

    public void sendMsg(object msg)
    {
        outgoingMsgs.Enqueue(msg);
    }

    private void DeserializeMessage<T>() where T : class
    {
        incomingMsgs.Enqueue(Serializer.DeserializeWithLengthPrefix<T>(clientStream, PrefixStyle.Base128));
        //Debug.Log("got "+typeof(T));
    }

    private void SerializeMessage<T>(object toSend) where T : class
    {
        Serializer.SerializeWithLengthPrefix(clientStream, (T)toSend, PrefixStyle.Base128);
    }
}