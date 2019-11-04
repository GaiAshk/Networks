import java.io.*;
import java.net.*;

/**
 * usage: UdpEchoClient serverName port string
 *
 * Send a packet to the named server:port containing the given string.
 * Wait for reply packet and print its contents.
 **/

public class MapClient {
    public static void main(String args[]) throws Exception {

        // get server address, port number and command to server, if they
        // don't exist enter default values
        InetAddress serverAdr =(args.length == 0)? null :
                                                InetAddress.getByName(args[0]);
        //port
        int port = (args.length <= 1) ? -1 : Integer.parseInt(args[1]);
        //server command
        String command;
        if (args.length <= 2) {
            command = "";
        } else if (args.length <= 3) {
            command = args[2];
        } else if (args.length <= 4) {
            command = args[2] + ":" + args[3] + ":" ;
        } else if (args.length <= 5) {
            command = args[2] + ":" + args[3] + ":" + args[4];
        } else {
            command = "";
        }

        // open datagram socket
        DatagramSocket sock = new DatagramSocket();
        // build packet addressed to server containing server command
        byte[] outBuf = command.getBytes("US-ASCII");
        DatagramPacket outPkt = new DatagramPacket(outBuf,outBuf.length,
                                                    serverAdr, port);
        sock.send(outPkt);	// send packet to server

        // create buffer and packet for reply, then receive it
        byte[] inBuf = new byte[1000];
        DatagramPacket inPkt = new DatagramPacket(inBuf,inBuf.length);
        sock.receive(inPkt);	// wait for reply (this is blocking)

        // print buffer contents and close socket
        String reply = new String(inBuf,0,inPkt.getLength(),"US-ASCII");
        System.out.println(reply);
        sock.close();
    }
}