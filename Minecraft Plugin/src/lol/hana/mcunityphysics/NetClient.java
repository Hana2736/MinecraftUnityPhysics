package lol.hana.mcunityphysics;

import lol.hana.mcunityphysics.NetMessages.NextMessageType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetClient {
    public Socket netSocket;
    public ConcurrentLinkedQueue<Object> incomingMsgs;
    public ConcurrentLinkedQueue<Object> outgoingMsgs;
    public boolean connected = false;

    public NetClient(String host, int port) {
        try {
            //connect TCP socket to host
            netSocket = new Socket(host, port);
            connected = true;
            incomingMsgs = new ConcurrentLinkedQueue<>();
            outgoingMsgs = new ConcurrentLinkedQueue<>();
            Util.sendMsg("Connected to socket!");

            //set up socket I/O
            InputStream in = netSocket.getInputStream();
            OutputStream out = netSocket.getOutputStream();


            //read messages from the socket as they become available and store them for plugin use
            new Thread(() -> {
                while (connected) {
                    try {
                        NetMessages.MessageType msgType = NextMessageType.parseDelimitedFrom(in).getMType();
                        //Util.sendMsg(msgType.toString());
                        switch (msgType) {
                            case mPhysUpdate: {
                                var msg = NetMessages.PhysUpdate.parseDelimitedFrom(in);
                                incomingMsgs.add(msg);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        connected = false;
                        Util.sendMsg("Socket connection lost " + e.getMessage());

                    }
                }
            }).start();

            //send messages from the plugin to the socket
            new Thread(() -> {
                while (connected)
                    try {
                        while (!outgoingMsgs.isEmpty()) {
                            var toSend = outgoingMsgs.poll();
                            var mType = NextMessageType.newBuilder();
                            //i do not like Java.
                            if (toSend instanceof NetMessages.AddPlayer.Builder) {
                                Util.sendMsg("adding player");
                                try {
                                    mType.setMType(NetMessages.MessageType.mAddPlayer);
                                    mType.build().writeDelimitedTo(out);
                                    ((NetMessages.AddPlayer.Builder) toSend).build().writeDelimitedTo(out);
                                    Util.sendMsg("added player");
                                } catch (Exception e) {
                                    Util.sendMsg("Protobuf epic fail: " + e.getMessage());
                                    for (var el : e.getStackTrace()) {
                                        Util.sendMsg(el.toString());
                                    }
                                }

                            } else if (toSend instanceof NetMessages.WorldBlock.Builder) {
                                mType.setMType(NetMessages.MessageType.mWorldBlock);
                                mType.build().writeDelimitedTo(out);
                                ((NetMessages.WorldBlock.Builder) toSend).build().writeDelimitedTo(out);
                            } else if (toSend instanceof NetMessages.PlayerUpdate.Builder) {
                                mType.setMType(NetMessages.MessageType.mPlayerUpdate);
                                mType.build().writeDelimitedTo(out);
                                ((NetMessages.PlayerUpdate.Builder) toSend).build().writeDelimitedTo(out);
                            } else if (toSend instanceof NetMessages.AddPhys.Builder) {
                                mType.setMType(NetMessages.MessageType.mAddPhys);
                                mType.build().writeDelimitedTo(out);
                                ((NetMessages.AddPhys.Builder) toSend).build().writeDelimitedTo(out);
                            }
                        }
                    } catch (Exception e) {
                        Util.sendMsg("Socket connection lost " + e.getMessage());
                        disconnect();
                    }
            }).start();

        } catch (IOException e) {
            Util.sendMsg("Failed to connect socket " + e.getMessage());
        }
    }

    public void sendMsg(Object msg) {
        outgoingMsgs.add(msg);
    }

    public void disconnect() {
        connected = false;
        try {
            netSocket.close();
        } catch (IOException e) {
            //sucks to suck
        }
    }


}
