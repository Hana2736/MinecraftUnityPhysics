package lol.hana.mcunityphysics;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetClient {
    public Socket netSocket;
    public ConcurrentLinkedQueue<String> incomingMsgs;
    public ConcurrentLinkedQueue<String> outgoingMsgs;
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
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            PrintWriter printWriter = new PrintWriter(out, false);

            //read messages from the socket as they become available and store them for plugin use
            new Thread(() -> {
                while (connected) {
                    try {
                        String nextMsg = br.readLine();
                        String[] split = nextMsg.split("<DEL>");
                        for(String msg : split) {
                            String toDo = msg.replace('\n',' ').trim();
                            if(!toDo.isEmpty())
                                incomingMsgs.add(toDo);
                        }

                    } catch (IOException e) {
                        connected = false;
                        Util.sendMsg("Socket connection lost " + e.getMessage());
                    }
                }
            }).start();

            //send messages from the plugin to the socket
            new Thread(() -> {
                while (connected)
                    while (!outgoingMsgs.isEmpty()) {
                        String toSend = outgoingMsgs.poll();
                        toSend = toSend.replace('\n', ' ');
                        printWriter.println(toSend+"<DEL>");
                        printWriter.flush();
                    }
            }).start();

        } catch (IOException e) {
            Util.sendMsg("Failed to connect socket " + e.getMessage());
        }
    }

    public void sendMsg(String msg) {
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
