import java.io.*;
import java.net.*;

/**
 * usage: DhtClient hostName configurationFile command [key] [value] [ttl]
 * this is a class of a client that sends UDP packets to the the DHT Server
 * the client reads from cgf file the servers InterAddress and port and sends this specific server
 * in the DHT the request, the server is responsible to search the DHT and send back the correct response
 **/

public class DhtClient {

    private static boolean debug;	// enables debug messages when true

    public static void main(String args[]) throws Exception {
        //open file to read the servers address and port
        BufferedReader pred = new BufferedReader( new InputStreamReader(
                                new FileInputStream(args[1]), "US-ASCII"));
        // get server address and port number
        String s = pred.readLine();
        String[] chunks = s.split(" ");
        InetAddress serverAdr = InetAddress.getByName(chunks[0]);
        int serverPort = Integer.parseInt(chunks[1]);

        // open datagram socket with specified hostName (args[0])
        DatagramSocket sock = new DatagramSocket(0, InetAddress.getByName(args[0]));
        // Packet class, open new Packet
        Packet p = new Packet();

        // build packet p
        String command = "CSE473 DHTPv0.1\n";
        if (args.length < 4) {
            System.out.println("Usage: DhtClient serverName configurationFile command key [value] [ttl]");
            System.exit(1);
        } else if (args.length >= 4 && args[2].equals("get")){
            p.type = "get";
            p.key = args[3];
            p.tag = 1;

//            command += "type:get\nkey:" + args[3] + "\ntag:1\n";
            if(args.length == 5){
//                command += "ttl:" + args[4] + "\n";
                p.ttl = Integer.parseInt(args[4]);
            }
        } else if (args.length >= 4 && args[2].equals("put")){
            String value = (args.length == 5) ? args[4] : "";

//            command += "type:put\nkey:" + args[3] + "\nvalue:" + value + "\ntag:1\n";

            p.type = "put";
            p.key = args[3];
            p.val = value;
            p.tag = 1;
            if(args.length == 6){
//                command += "ttl:" + args[5] + "\n";
                p.ttl = Integer.parseInt(args[5]);
            }
        } else {
            System.out.println("Usage: DhtClient serverName configurationFile command key [value] [ttl]");
            System.exit(1);
        }

        // build packet addressed to server containing server command
//        byte[] outBuf = command.getBytes("US-ASCII");
//        DatagramPacket outPkt = new DatagramPacket(outBuf,outBuf.length,
//                                                    serverAdr, serverPort);

        p.send(sock, new InetSocketAddress(serverAdr, serverPort), debug);	// send packet to server

        // create buffer and packet for reply, then receive it
        byte[] inBuf = new byte[1000];
//        DatagramPacket inPkt = new DatagramPacket(inBuf,inBuf.length);
//        sock.receive(inPkt);	// wait for reply (this is blocking)

        // clear packet
        p.clear();
        // receive packet from server
        p.receive(sock, debug);
        // print packet received
        System.out.println(p);

        //close the socket
        sock.close();
    }
}