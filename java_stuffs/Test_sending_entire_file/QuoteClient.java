import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class QuoteClient {
	static String seqSent = "";
	static ObjectOutputStream oos;
	static ObjectInputStream ois;
	static String packets_received = "";
	static int sendLimit;
	static Socket sock;
	static DatagramSocket socket;
	static byte[] buf;
	static byte[] info;
	static InetAddress address;
	static DatagramPacket packet;

	public static void main(String[] args) throws IOException {
		double numPacs;

		File receivedFile;
		FileOutputStream output;// = new FileOutputStream("Output.txt");

		if (args.length < 1) {
			System.out.println("java QuoteClient <hostname>");
			return;
		}

		//get a datagram socket.
		socket = new DatagramSocket();

		//send request
		buf = new byte[256];
		info = new byte[100];

		sock = new Socket(args[0], 8000);
		oos = new ObjectOutputStream(sock.getOutputStream());
		ois = new ObjectInputStream(sock.getInputStream());

		address = InetAddress.getByName(args[0]);
		packet = new DatagramPacket(buf, buf.length, address, 8000);
		socket.send(packet);

		//get response

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
		sendLimit = 3;
		int limit = sendLimit; //(int)(10/3);
		int nextLimit = limit;
		for (int i=0; i<11; i++) {

			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

			pacnr = new String(packet.getData(), 0, 3);
			packets_received = packets_received + pacnr+",";

			receivedData = new String(packet.getData(), 3, packet.getLength()-3);
			output.write(packet.getData(), 3, packet.getLength()-3);
			//System.out.println("Received packet nr."+ pacnr);
			System.out.print(receivedData);

			if (i == limit) {
				recvTCP();
				limit = limit + nextLimit;
			}
		}

		/*try {
			String seqSent = ois.readObject().toString();
			oos.writeObject(packets_received);
		} catch (IOException e) {
			System.out.println("IOException");
		} catch (ClassNotFoundException err) {
			System.out.println("ClassNotFoundException");
		}
			packets_received = "";*/
		recvTCP();

		oos.close();
		ois.close();

		packets_received = "";
		output.close();
		 
		socket.close();

	}

	public static void recvTCP() {
		try {
			seqSent = ois.readObject().toString();
			oos.writeObject("0");

			//___________________________________________
			/*
			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

			String pacnr = new String(packet.getData(), 0, 3);
			String receivedData = new String(packet.getData(), 3, packet.getLength()-3);

			System.out.println("Resend_____________: "+receivedData);
			*/

		} catch (IOException e) {
			System.out.println("IOException");
		} catch (ClassNotFoundException err) {
			System.out.println("ClassNotFoundException");
		}

		packets_received = "";

	}

}
