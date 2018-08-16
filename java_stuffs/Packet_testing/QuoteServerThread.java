import java.io.*;
import java.net.*;
import java.util.*;

public class QuoteServerThread extends Thread {
	protected DatagramSocket socket = null;
	protected BufferedReader in = null;
	protected boolean moreQuotes = true;

	public QuoteServerThread() throws IOException {
		this("QuoteServerThread");
	}

	public QuoteServerThread(String name) throws IOException {
		super(name);
		socket = new DatagramSocket(8000);

		try {
			in = new BufferedReader(new FileReader("one-liners.txt"));
			//File myFile = new File("one-liners.txt");
			//System.out.println((int)(myFile.length())+"");
		} catch (FileNotFoundException e) {
			System.out.println("Could not read file.");
		}

	}

	public void run() {
		int count;
		while (moreQuotes) {
			try {
				byte[] buf = new byte[256];

				//Receive request
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				
				count = 5;

				//Send a certain amount before waiting for client again
				while (count > 0) {
				//figure out response
				String dstring = null;
				if (in == null) {
					dstring = new Date().toString();
				} else {
					dstring = getNextQuote();
				}

				buf = dstring.getBytes();

				//Send the response to the client at "address" and "port"
				InetAddress address = packet.getAddress();
				int port = packet.getPort();
				packet = new DatagramPacket(buf, buf.length, address, port);
				socket.send(packet);

				count--;

				try {
					sleep(100);
				} catch (InterruptedException e) {

				}

				}


			} catch (IOException e) {
				moreQuotes = false;
			}

		}
		socket.close();
	}

	protected String getNextQuote() {
		String returnVal = null;
		try {
			if ((returnVal = in.readLine()) == null) {
				in.close();
				moreQuotes = false;
				returnVal = "No more Quotes";
			}

		} catch (IOException e) {
			returnVal = "IOException occured in server.";
		}

		return returnVal;
	}

}
