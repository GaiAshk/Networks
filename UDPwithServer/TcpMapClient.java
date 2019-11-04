import java.io.*;
import java.net.*;

public class TcpMapClient {
	public static void main(String args[]) throws Exception {
		// connect to remote server default port is 30123
		int port = 30123;
		if (args.length > 1) port = Integer.parseInt(args[1]);
		Socket sock = new Socket(args[0], port);
		
		// create buffered reader & writer for socket's in/out streams
		BufferedReader  in = new BufferedReader(new InputStreamReader(
				    	 sock.getInputStream(),"US-ASCII"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				    	 sock.getOutputStream(),"US-ASCII"));

		// create buffered reader for System.in
		BufferedReader sysin = new BufferedReader(new InputStreamReader(
					   System.in));

		String line;
		while (true) {
			line = sysin.readLine();
			if (line == null || line.length() == 0) break;

			// write line on socket and print reply to System.out
			out.write(line); out.newLine(); out.flush();
			String serverAnswer = in.readLine();
			System.out.println(serverAnswer);
		}
		//close all Readers and sockets
		sock.close();
		in.close();
		out.close();
		sysin.close();
	}
}