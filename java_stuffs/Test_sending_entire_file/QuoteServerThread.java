import java.io.*;
import java.net.*;
import java.util.*;

public class QuoteServerThread extends Thread {
	protected DatagramSocket socket = null;
	protected BufferedReader in = null;
	protected BufferedInputStream bis = null;
	protected boolean moreQuotes = true;
	protected File myFile = null;

	public QuoteServerThread() throws IOException {
		this("QuoteServerThread");
	}

	public QuoteServerThread(String name) throws IOException {
		super(name);
		socket = new DatagramSocket(8000);

		try {
			in = new BufferedReader(new FileReader("one-liners.txt"));
			myFile = new File("one-liners.txt");
			bis = new BufferedInputStream(new FileInputStream(myFile));
			System.out.println("The file is "+(int)(myFile.length())+" Bytes big.");
		} catch (FileNotFoundException e) {
			System.out.println("Could not read file.");
		}

	}

	public void run() {
		int count;
		//while (moreQuotes) {
			try {
				byte[] buf = new byte[256];

				//Receive request
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);


				double numPacs = Math.ceil((int)(myFile.length())/256);
				System.out.println(numPacs+"");

				for (double i=0.0; i < numPacs+1; i++) {
					bis.read(buf, 0, buf.length);
					System.out.println("Packet nr."+(i+1));
					System.out.println("Bytes sent: " + (i*buf.length));
					InetAddress address = packet.getAddress();
					int port = packet.getPort();

					packet = new DatagramPacket(buf, buf.length, address, port);
					socket.send(packet);

					try {
						sleep(100);
					} catch (InterruptedException e) {

					}
				}
				
				/*count = 5;

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

				}*/


			} catch (IOException e) {
				moreQuotes = false;
			}

		//}
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
