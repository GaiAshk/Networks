import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class TcpMapServer {
	public static void main(String args[]) throws Exception {
		// process arguments
		int port = 30123;
		if (args.length > 1) port = Integer.parseInt(args[1]);
		InetAddress bindAdr = null;
		if (args.length > 0) bindAdr = InetAddress.getByName(args[0]);

		// create and bind listening socket
		ServerSocket listenSock = new ServerSocket(port, 0, bindAdr);

		HashMap <String, String> storage = new HashMap<>();

		while (true) {
			// wait for incoming connection request and create new socket to handle it
			Socket connSock = listenSock.accept();
	
			// create buffered versions of socket's in/out streams
			BufferedInputStream   in = new BufferedInputStream(
						   connSock.getInputStream());
			BufferedOutputStream out = new BufferedOutputStream(
						   connSock.getOutputStream());

			while (true) {
				// Receiving a new packet (request) from client
				byte[] buf = new byte[1024];

				// init buf array to get the command from client
				int nbytes = in.read(buf, 0, buf.length);
				if (nbytes < 0) break;

				// Processing the client's request, and constructing an answer
				String clientRequest = new String(buf, 0, buf.length, "US-ASCII");
				String[] processedRequest = clientRequest.split(":");
				String answerToClient = "";
				if (!checkInput(processedRequest)) {
					answerToClient = "Error:unrecognizable input:" + clientRequest;
				} else {

					// The input is valid, constructing an answer which
					// corresponds to the defined specifications
					String res;
					String clientKey = "";
					if (processedRequest.length > 1) {
						clientKey = processedRequest[1].trim();
					}
					String commandTrimed = processedRequest[0].trim();
					switch (commandTrimed) {
						case "get":
							answerToClient =
									(storage.containsKey(clientKey)) ?
											"ok:" + storage.get(clientKey) :
											"no match";
							break;
						case "remove":
							answerToClient =
									(storage.containsKey(clientKey))?
											"Ok" : "no match";
							storage.remove(clientKey);
							break;
						case "put":
							String clientValue = processedRequest[2].trim();
							res = storage.put(clientKey, clientValue);
							answerToClient = (res == null) ? "Ok" :
									"updated:" + clientKey;
							break;
						case "get all":
							for (Map.Entry<String, String> entry : storage.entrySet()){
								answerToClient += entry.getKey() + ":" + entry.getValue() + "::";
							}
							answerToClient = answerToClient.substring(0, answerToClient.length() - 2);
							break;
					}
					answerToClient = answerToClient.trim();
					answerToClient += "\n";
				}
				buf = answerToClient.getBytes("US-ASCII");
				out.write(buf, 0, buf.length);
				out.flush();
			}
			// close connections and readers
			connSock.close();
			in.close();
			out.close();
		}
	}

	/**
	 * Checks if the client's request complies with the specifications
	 * @param command The different parts of the requested query
	 * @return Whether the request should be processed or not
	 */
	private static boolean checkInput(String[] command) {
		String temp = command[0].trim();
		if (temp.equals("get all") && command.length == 1) {
			return true;
		} else if ((!command[0].equals("get") && !command[0].equals("put")
				&& !command[0].equals("remove")) || command.length > 3)
			return false;
		else if (command[0].equals("get") || command[0].equals("remove"))
			return command.length <= 2;
		return command.length == 3;
	}
}