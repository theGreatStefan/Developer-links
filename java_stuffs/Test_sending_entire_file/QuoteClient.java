import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class QuoteClient {

	public static void main(String[] args) throws IOException {
		double numPacs;
		String packets_received = "";

		File receivedFile;
		FileOutputStream output;// = new FileOutputStream("Output.txt");

		if (args.length < 1) {
			System.out.println("java QuoteClient <hostname>");
			return;
		}

		//get a datagram socket.
		DatagramSocket socket = new DatagramSocket();

		//send request
		byte[] buf = new byte[256];
		byte[] info = new byte[100];
		InetAddress address = InetAddress.getByName(args[0]);
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 8000);
		socket.send(packet);

		//get response
		//packet = new DatagramPacket(buf, buf.length);
		//socket.receive(packet);


		String infoStr = "";
		String receivedData = "";
		String pacnr = "";
		
		/*
		 * Receive the info of the file being transferred i.e. .txt or .mp3
		 */
		packet = new DatagramPacket(info, info.length);
		socket.receive(packet);

		infoStr = new String(packet.getData(), 0, packet.getLength());

		receivedFile = new File(infoStr.substring(infoStr.indexOf(",")+2)+"."+infoStr.substring(0, infoStr.indexOf(",")));
		output = new FileOutputStream(receivedFile);

		/*
		 * Receive the actual file in packets
		 */
		for (int i=0; i<11; i++) {

			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

			pacnr = new String(packet.getData(), 0, 3);
			packets_received = packets_received + pacnr+",";

			receivedData = new String(packet.getData(), 3, packet.getLength()-3);
			output.write(packet.getData(), 3, packet.getLength()-3);
			//System.out.println("Received packet nr."+ pacnr);
			System.out.print(receivedData);
		}
		packets_received = "";
		output.close();
		 
		//display response
		//String received = new String(packet.getData(), 0, packet.getLength());
		//System.out.println("Quote of the moment: " + received);

		socket.close();

	}

}
