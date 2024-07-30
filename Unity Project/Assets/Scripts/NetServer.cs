using System.Collections.Concurrent;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

public class NetServer
{
    public bool connected;
    public ConcurrentQueue<string> incomingMsgs, outgoingMsgs;
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
        incomingMsgs = new ConcurrentQueue<string>();
        outgoingMsgs = new ConcurrentQueue<string>();
        connected = true;

        //handle incoming messages from Minecraft, adding them to the queue for us to handle
        new Thread(() =>
        {
            while (connected)
            while (true)
            {
                var buffer = new byte[8192];
                var received = clientSocket.Receive(buffer, SocketFlags.None);
                var response = Encoding.ASCII.GetString(buffer, 0, received);

                if (response.EndsWith("\n") /* is end of message */)
                {
                    //split the message into chunks and add it to be handled
                    var split = response.Replace("\n", "").Split("<DEL>");
                    foreach (var msg in split)
                    {
                        var trim = msg.Replace("<DONE>", "").Trim();
                        if (trim.Length != 0)
                            incomingMsgs.Enqueue(trim);
                    }
                    break;
                }
            }
        }).Start();


        //send messages to Minecraft
        new Thread(() =>
        {
            while (connected)
            {
                string toSend;
                while (!outgoingMsgs.TryDequeue(out toSend))
                {
                    //burn thread until we can read it
                }
                var echoBytes = Encoding.ASCII.GetBytes(toSend + "<DEL>\n");
                clientSocket.Send(echoBytes);
            }
        }).Start();
    }

    public void sendMsg(string msg)
    {
        msg = msg.Replace("\n", "");
        outgoingMsgs.Enqueue(msg);
    }
}