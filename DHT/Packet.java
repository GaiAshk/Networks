	import java.io.*;
import java.net.*;
import java.util.*;

/** Class for working with DHT packets. */
public class Packet {
	// packet fields - note: all are public
	public String type;		// packet type
	public int ttl;			// time-to-live
	public String key;		// DHT key string
	public String val;		// DHT value string
	public String reason;		// reason for a failure
	public InetSocketAddress clientAdr; // address of original client
	public InetSocketAddress relayAdr; // address of first DHT server
	public int tag;			// tag used to identify packet
	public Pair<Integer,Integer> hashRange;	// range of hash values
	public Pair<InetSocketAddress,Integer> senderInfo;// address, first hash
	public Pair<InetSocketAddress,Integer> succInfo; // address, first hash
	public Pair<InetSocketAddress,Integer> predInfo; // address, first hash

	/** Constructor, initializes fields to default values. */
	public Packet() { clear(); }

	/** Initialize all packet fields.
	 *  Initializes all fields with a standard initial value
	 *  or makes them undefined.
 	 */
	public void clear() {
		type = null; ttl = 100; key = null; val = null;
		reason = null; clientAdr = null; relayAdr = null;
		tag = -1; hashRange = null;
		senderInfo = null; succInfo = null; predInfo = null;
	}

	/** Pack attributes defining packet fields into buffer.
	 *  Fails if the packet type is undefined or if the resulting
	 *  buffer exceeds the allowed length of 1400 bytes.
	 *  @return null on failure, otherwise a byte array
	 *  containing the packet payload.
	 */
	public byte[] pack() {
		if (type == null)  return null;
		byte[] buf;
		try { buf = toString().getBytes("US-ASCII");
		} catch(Exception e) { return null; }
		if (buf.length > 1400) return null;
		return buf;
	}

	/** Unpack attributes defining packet fields from buffer.
	 *  @param buf is a byte array containing the DHT packet
	 *  (or if you like, the payload of a UDP packet).
	 *  @param bufLen is the number of valid bytes in buf
	 */
	public boolean unpack(byte[] buf, int bufLen) {
		// convert buf to a string
		String s; 
		try { s = new String(buf,0,bufLen,"US-ASCII");
		} catch(Exception e) { return false; }

		// divide into lines and check the first line
		String[] lines = s.split("\n");
		if (!lines[0].equals("CSE473 DHTPv0.1")) return false;

		//process remaining lines
		for (int i = 1; i < lines.length; i++) {
			String[] chunks = lines[i].split(":",2);
			if (chunks.length != 2) return false;
			// process the line
			String left = chunks[0];
			String right = chunks[1];
			if (left.equals("type")) {
				type = right;
			} else if (left.equals("ttl")) {
				ttl = Integer.parseInt(right);
			} else if (left.equals("clientAdr")) {
				chunks = right.split(":");
				if (chunks.length != 2) return false;
				clientAdr = new InetSocketAddress(chunks[0],
						Integer.parseInt(chunks[1]));
			} else if (left.equals("succInfo")) {
				chunks = right.split(":");
				if (chunks.length != 3) return false;
				String ip = chunks[0];
				int port = Integer.parseInt(chunks[1]);
				int hash = Integer.parseInt(chunks[2]);
				succInfo = new
					Pair<InetSocketAddress,Integer>(
					new InetSocketAddress(ip,port),hash);
			}
			else if (left.equals("key")){
				key=right;
			} else if (left.equals("value")){
				val=right;
			} else if (left.equals("tag")){
				tag = Integer.parseInt(right);
			} else if (left.equals("relayAdr")){
				chunks = right.split(":");
				if (chunks.length != 2) return false;
				relayAdr = new InetSocketAddress(chunks[0],
						Integer.parseInt(chunks[1]));
			} else if (left.equals("hashRange")){
				chunks = right.split(":");
				if (chunks.length != 2) return false;
				int startRange = Integer.parseInt(chunks[0]);
				int endRange = Integer.parseInt(chunks[1]);
				hashRange = new
						Pair<Integer,Integer>(startRange, endRange);
			} else if (left.equals("reason")){
				reason = right;
			} else if (left.equals("senderInfo")){
				chunks = right.split(":");
				if (chunks.length != 3) return false;
				String ip = chunks[0];
				int port = Integer.parseInt(chunks[1]);
				int hash = Integer.parseInt(chunks[2]);
				senderInfo = new
						Pair<InetSocketAddress,Integer>(
								new InetSocketAddress(ip,port),hash);
			} else if (left.equals("predInfo")){
				chunks = right.split(":");
				if (chunks.length != 3) return false;
				String ip = chunks[0];
				int port = Integer.parseInt(chunks[1]);
				int hash = Integer.parseInt(chunks[2]);
				predInfo = new
						Pair<InetSocketAddress,Integer>(
						new InetSocketAddress(ip,port),hash);
			}
			else {
				// ignore lines that don't match defined field
			}
		}
		return true;
	}

	/** Basic validity checking for received packets.
	 *  @return true on success, false on failure;
	 *  on failure, place an explanatory String in the reason field
	 *  of the packet
	 */
	public boolean check() {
		reason = null;
		if (type == null) {
			reason = "every packet must include a type";
			return false;
		} else if ((type.equals("get") || type.equals("put")) && 
		     	 (key == null || tag == -1)) {
			reason = "gets and puts require key and tag";
			return false;
		}
		return true;
	}

	/** Create String representation of packet.
	 *  The resulting String is produced using the defined
	 *  attributes and is formatted with one field per line,
	 *  allowing it to be used as the actual buffer contents.
	 */
	public String toString() {
		// every packet starts with this line
		StringBuffer s = new StringBuffer("CSE473 DHTPv0.1\n");
		// build the packet by this logic
		if (type != null) {
			s.append("type:"); s.append(type); s.append("\n");
		}
		if (key != null) {
			s.append("key:"); s.append(key); s.append("\n");
		}
		if (relayAdr != null) {
			s.append("relayAdr:");
			s.append(relayAdr.getAddress().getHostAddress());
			s.append(":"); s.append(relayAdr.getPort());
			s.append("\n");
		}
		if (hashRange != null) {
			s.append("hashRange:"); s.append(hashRange.left);
			s.append(":"); s.append(hashRange.right);
			s.append("\n");
		}
		if (senderInfo != null) {
			s.append("senderInfo:");
			s.append(senderInfo.left.getAddress().getHostAddress());
			s.append(":"); s.append(senderInfo.left.getPort());
			s.append(":"); s.append(senderInfo.right);
			s.append("\n");
		}
		if (succInfo != null) {
			s.append("succInfo:");
			s.append(succInfo.left.getAddress().getHostAddress());
			s.append(":"); s.append(succInfo.left.getPort());
			s.append(":"); s.append(succInfo.right);
			s.append("\n");
		}
		if (predInfo != null) {
			s.append("predInfo:");
			s.append(predInfo.left.getAddress().getHostAddress());
			s.append(":"); s.append(predInfo.left.getPort());
			s.append(":"); s.append(predInfo.right);
			s.append("\n");
		}
		if (clientAdr != null) {
			s.append("clientAdr:");
			s.append(clientAdr.getAddress().getHostAddress());
			s.append(":"); s.append(clientAdr.getPort());
			s.append("\n");
		}
		if (tag != -1) {
			s.append("tag:"); s.append(tag); s.append("\n");
		}
		if (val != null) {
			s.append("value:"); s.append(val); s.append("\n");
		}
		if (reason != null) {
			s.append("reason:"); s.append(reason); s.append("\n");
		}
		if (ttl != -1) {
			s.append("ttl:"); s.append(ttl); s.append("\n");
		}
		return s.toString();
	}
		
	/** Send the packet to a specified destination.
	 *  Packs the various packet fields into a buffer
	 *  before sending. Does no validity checking.
	 *  @param sock is the socket on which the packet is sent
	 *  @param dest is the socket address of the destination
	 *  debug is a flag; if true, the packet is printed before it is sent
	 *  @return true on success, false on failure
	 */
	public boolean send(DatagramSocket sock, InetSocketAddress dest,
			    boolean debug) {
		if (debug) {
			System.out.println("" + sock.getLocalSocketAddress() +
				" sending packet to " + dest + "\n" +
				toString());
			System.out.flush();
		}
		byte[] buf = this.pack();
		if (buf == null) return false;
                DatagramPacket pkt = new DatagramPacket(buf, buf.length);
		pkt.setSocketAddress(dest);
		try { sock.send(pkt); } catch(Exception e) { return false; }
		return true;
	}
		
	/** Get the next packet on the socket.
	 *
	 * Receives the next datagram from the socket and
	 * unpacks it.
	 * @param sock is the socket on which the packet is received
	 * @param debug is a flag; if it is true, the received
	 * packet is printed
	 * @return the sender's socket address on success and null on failure
	 */
	public InetSocketAddress receive(DatagramSocket sock, boolean debug) {
		clear();
		byte[] buf = new byte[2000];
		DatagramPacket pkt = new DatagramPacket(buf, buf.length);
		try {
			sock.receive(pkt);
		} catch(Exception e) {
			System.out.println("receive exception: " + e);
			return null;
		}
	
		if (!unpack(buf,pkt.getLength())) {
			System.out.println("error while unpacking packet");
			return null;
		}
		ttl--;
		if (debug) {
			System.out.println(sock.getLocalSocketAddress() +
				" received packet from " + 
				pkt.getSocketAddress() + "\n" + toString());
			System.out.flush();
		}
		if (ttl < 0) {
			return null;
		}
		return (InetSocketAddress) pkt.getSocketAddress();
	}
}