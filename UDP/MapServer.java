import java.io.*;
import java.net.*;
import java.util.HashMap;

public class MapServer {
    public static void main(String args[]) throws Exception {

        //get port number or set it to default
        //check if port number given is valid
        int port;
        if(args.length > 0) {
            int argumentPort = Integer.parseInt(args[0]);
            if (argumentPort > 0 && argumentPort < 65536) {
                port = argumentPort;
            } else {
                port = 30123;
            }
        } else {
            port = 30123;
        }
        // open a UDP socket on port given
        DatagramSocket sock = new DatagramSocket(port);
        System.out.println("Server running on port " + sock.getLocalPort());
        //init hash map, out packet
        HashMap<String, String> map = new HashMap<>();
        DatagramPacket outPkt;

        while (true) {
            // create a DatagramPacket for receiving packets
            byte[] buf = new byte[1000];
            DatagramPacket pkt = new DatagramPacket(buf, buf.length);
            // response array to construct the response to client
            byte[] response = new byte[1000];

            sock.receive(pkt); // wait for incoming packet (blocking)

            //get clients port and address
            InetAddress clientAdd = pkt.getAddress();
            int clientPort = pkt.getPort();

            //take the packets information and process it
            String message = new String(buf);
            String [] splitedMessage = message.split(":", 3);
            String command = splitedMessage[0]; String key; String value;

            // PUT command
            if(command.equals("put") && splitedMessage.length > 2) {
                key = splitedMessage[1];
                value = splitedMessage[2];

                //create response
                if(map.containsKey(key)){
                    response = ("updated:" + key).
                            getBytes("US-ASCII");
                } else {
                    response = "Ok".getBytes("US-ASCII");
                }

                //insert the key and value to the map, if needed this will
                // update and existing key
                map.put(key, value);
                //create response packet
                outPkt = new DatagramPacket(response,response.length,
                        clientAdd, clientPort);
                //send response
                sock.send(outPkt);
            //GET command
            } else if (command.equals("get") && splitedMessage.length > 1){
                key = splitedMessage[1];

                //create response to client
                if(map.containsKey(key)){
                    response = ("ok:" + map.get(key)).
                            getBytes("US-ASCII");
                } else {
                    response = "no match".getBytes("US-ASCII");
                }

                //create response packet
                outPkt = new DatagramPacket(response,response.length,
                        clientAdd, clientPort);
                //send response
                sock.send(outPkt);
            // REMOVE command
            } else if (command.equals("remove") && splitedMessage.length > 1){
                key = splitedMessage[1];

                //create response to client
                if(map.containsKey(key)){
                    response = "Ok".getBytes("US-ASCII");
                    //remove key from map if key is in map
                    map.remove(key);
                } else {
                    response = "no match".getBytes("US-ASCII");
                }
                //create response packet
                outPkt = new DatagramPacket(response,response.length,
                        clientAdd, clientPort);
                //send response
                sock.send(outPkt);
            //if command is NOT VALID
            } else {
                String payload = new String(pkt.getData(), 0, pkt.getLength());
                response = ("Error: unrecognizable input: {" + payload + "}").
                        getBytes("US-ASCII");
                //create response packet
                outPkt = new DatagramPacket(response,response.length,
                        clientAdd, clientPort);
                //send response
                sock.send(outPkt);
            }
        }
    }
}