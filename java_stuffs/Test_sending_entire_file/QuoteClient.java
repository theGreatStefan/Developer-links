import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class QuoteClient {
	static String seqSent = "";
	static String seqRecv = "";
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
	static String[] packet_arr;
	static byte[][] packet_2DArr;
	static FileOutputStream output;
	static File receivedFile;
	static boolean exit = false;
	static int numPacs;
	static int pacSize;
	static DatagramPacket[] datagramPacket_arr;
	static HashMap<String, byte[]> hm = new HashMap<>();

	public static void main(String[] args) throws IOException {
		//File receivedFile;
		//FileOutputStream output;// = new FileOutputStream("Output.txt");

		if (args.length < 1) {
			System.out.println("java QuoteClient <hostname>");
			return;
		}

		//get a datagram socket.
		socket = new DatagramSocket();

		//send request
		buf = new byte[254];
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

		numPacs = (int)Double.parseDouble(infoStr.substring(0,infoStr.indexOf(",")));
		infoStr = infoStr.substring(infoStr.indexOf(",")+1);

		pacSize = (int)Double.parseDouble(infoStr.substring(0,infoStr.indexOf(",")));
		infoStr = infoStr.substring(infoStr.indexOf(",")+1);
		buf = new byte[pacSize-3];

		receivedFile = new File(infoStr.substring(infoStr.indexOf(",")+2)+"."+infoStr.substring(0, infoStr.indexOf(",")));
		output = new FileOutputStream(receivedFile);

		/*
		 * Receive the actual file in packets
		 */
		packet_arr = new String[4];
		packet_2DArr = new byte[3][254];
		datagramPacket_arr = new DatagramPacket[4];
		sendLimit = 3;
		int limit = sendLimit; //(int)(10/3);
		int nextLimit = limit;
		int j = 0;
		
		//for (int i=0; i<11; i++) {
		while (!exit) {	
			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

			pacnr = new String(packet.getData(), 0, 4);
			seqRecv = seqRecv + pacnr + ",";
			packets_received = packets_received + pacnr+",";

			receivedData = new String(packet.getData(), 4, packet.getLength()-4);
			packet_arr[j] = receivedData;
			datagramPacket_arr[j] = packet;
			hm.put((""+packet.hashCode()),packet.getData());
			System.out.println("\nHashcode: "+packet.hashCode());
			//populate_array(packet, j);	

			//output.write(packet.getData(), 3, packet.getLength()-3);
			//System.out.println("Received packet nr."+ pacnr);
			//System.out.print(receivedData);

			j++;
			int num = Integer.parseInt(pacnr);
			System.out.println("\nnum: "+num+" numPacs: "+numPacs);
			if (num == numPacs) {
				break;
			}
			//if (i == limit) {
			if ((num)%3 == 0 && num != 0) {
			System.out.println("\nReceive TCP\n");
				recvTCP(packet_arr);
			System.out.println("\nReceived Tcp\n");
				limit = limit + nextLimit;
				j = 0;
				packet_arr = new String[4];
				packet_2DArr = new byte[3][254];
				datagramPacket_arr = new DatagramPacket[4];
			}
		}

		//if (numPacs%2 == 0) {
			populate_array(packet, 0);
			recvTCP(packet_arr);
		//}

		//oos.close();
		//ois.close();

		packets_received = "";
		//output.close();
		 
		//socket.close();

	}

	public static void recvTCP(String[] packet_arr) {
		try {
			seqSent = ois.readObject().toString();
			System.out.println("________TEST1________"+seqSent);
			//checkSeq(seqSent);
			seqRecv = "";
			oos.writeObject("0");
			System.out.println("________TEST2________");

			//___________________________________________
			/*
			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

			String pacnr = new String(packet.getData(), 0, 3);
			String receivedData = new String(packet.getData(), 3, packet.getLength()-3);

			System.out.println("Resend_____________: "+receivedData);
			*/

			for (int i=0; i<3; i++) {
				/*if (packet_arr[i] != null) {
					//output.write(packet_arr[i].getData(), 3, packet.getLength()-3);
					//System.out.println("________length: "+packet_arr[i].length());
					output.write(packet_arr[i].getBytes(), 0, packet_arr[i].length());
					//System.out.println("________TEST3________");
					//System.out.println("________Wrote nr."+i);
				}*/

				//if (packet_2DArr[i][0] != null) {
					/*for (int j=4; j < 254; j++) {
						//System.out.println("_____TEST3_____");
						output.write(packet_2DArr[i][j]);
					}*/
				//}
				output.write(datagramPacket_arr[i].getData(), 4, datagramPacket_arr[i].getLength()-4);
			}

		} catch (IOException e) {
			System.out.println("IOException "+e);
		} catch (ClassNotFoundException err) {
			System.out.println("ClassNotFoundException");
		}

		packets_received = "";

	}

	public static void checkSeq(String seqSent) {
		String seqSendAgain = "";
		System.out.println("\nSeqSent: "+seqSent);
		System.out.println("\nSeqRecv: "+seqRecv);
		if (!seqRecv.equals(seqSent)) {
			int j = seqRecv.length();
			int i = 0;
			String subSent;
			String subRecv;
			while (i < j) {
				subSent = seqSent.substring(0, seqSent.indexOf(","));
				seqSent = seqSent.substring(seqSent.indexOf(",")+1);
				subRecv = seqRecv.substring(0, seqRecv.indexOf(","));
				seqRecv = seqRecv.substring(seqRecv.indexOf(",")+1);
				if (!subSent.equals(subRecv)) {
					seqSendAgain = subSent;
				}

				i += 4;
			}
		}
	}

	public static void populate_array(DatagramPacket packet, int j) {
		byte[] bArr = packet.getData();
		for (int i = 0; i<254; i++) {
			packet_2DArr[j][i] = bArr[i];
		}
	}

}
