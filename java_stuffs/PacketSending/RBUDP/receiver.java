import java.io.*;
import java.net.*;
import java.util.*;

public class receiver {
	//UDP
	static DatagramSocket dg_socket = null;
	static DatagramPacket packet;
	static InetAddress address;
	static String hostName = "127.0.0.1";
	static int port = 8000;

	//TCP
	static ObjectOutputStream oos;
	static ObjectInputStream ois;
	static Socket recvSocket;

	//Other
	static File receivedFile;
	static FileOutputStream output;
	static byte[] buf;
	static String fileName = "newFile.mp3";
	static int numPacs;
	static int packetSize = 512;
	static HashMap<String, byte[]> hm;
	static String sentCodes = "";
	static String recvdCodes = "";
	static String order = "";
	static String infoStr;
	static int pacSize;

	public static void main(String[] args) {
		//Send a request
		sendReq();

		//Receive the packets
		recvPackets();
	}

	public static void sendReq() {
		buf = new byte[packetSize];
		try {
			//UDP
			dg_socket = new DatagramSocket();

			address = InetAddress.getByName(hostName);
			packet = new DatagramPacket(buf, buf.length, address, port);
			dg_socket.send(packet);

			//TCP
			recvSocket = new Socket(hostName, port);
			oos = new ObjectOutputStream(recvSocket.getOutputStream());
			ois = new ObjectInputStream(recvSocket.getInputStream());

			receivedFile = new File(fileName);
			output = new FileOutputStream(receivedFile);

			hm = new HashMap<>();


		} catch (SocketException e1) {
			System.out.println("SocketException.");
		} catch (UnknownHostException e2) {
			System.out.println("UnknownHostException.");
		} catch (IOException e3) {
			System.out.println("IOException when sending request.");
		}

	}

	public static void recvPackets() {
		int i = 0;
		byte[] write_buf;
		byte[] seq = new byte[4];
		String sequence;
		int packetKey;
		try {

			while (i < 130) {
				packet = new DatagramPacket(buf, buf.length);
				dg_socket.receive(packet);

				System.out.println("Bytes received: "+packet.getData().toString());
				//int hash_val = packet.hashCode();
				System.out.println("HashCode for packet "+(i+1)+": "+packet.hashCode());

				sequence = new String(packet.getData(), 0, 4);
				seq[0] = packet.getData()[0];
				seq[1] = packet.getData()[1];
				seq[2] = packet.getData()[2];
				seq[3] = packet.getData()[3];

				packetKey = fromByteArray(seq);
				System.out.println("This seq was sent and received: "+packetKey);
				// (i != 2) {
					hm.put(""+packetKey, packet.getData());
					recvdCodes = recvdCodes+packetKey+",";
				//
				
				if (i%4 == 0 && i != 0) {
					System.out.println("Go to TCP");
					recvTCP(0);
					writeToFile(order);
					recvdCodes = "";
				}

				seq = new byte[4];
				buf = new byte[packetSize];
				i++;
			}

			recvTCP(0);
			writeToFile(order);


		} catch (IOException e1) {
			System.out.println("IOException when receiving packets");
		}


	}

	public static void recvTCP(int num) {
		String sendSeq = "";
		String curr = "";
		int counter = 0;
		try {
			sentCodes = ois.readObject().toString();
			//System.out.println("Codes apparently sent: "+sentCodes);
			//System.out.println("Codes acctually recvd: "+recvdCodes);
			if (num == 0) {
				order = sentCodes;
			}

			while (!sentCodes.equals("")) {
				curr = sentCodes.substring(0, sentCodes.indexOf(","));
				if (!hm.containsKey(curr)) {
					System.out.println("!!!!!!!!!!!!OH NO !!!!!!!!!!!!!, "+curr);
					sendSeq = sendSeq+curr+",";
					counter++;
				}
				sentCodes = sentCodes.substring(sentCodes.indexOf(",")+1);
			}
			System.out.println("exit while");

			if (!sendSeq.equals("")) {
				oos.writeObject(sendSeq);
				recvLostPackets(counter);
			} else {
				oos.writeObject(sendSeq);
				/*if (num == 0) {
					writeToFile(order);
				}*/
			}
			recvdCodes = "";
			

		} catch (IOException e1) {
			System.out.println("IOException when receiving TCP.");
		} catch (ClassNotFoundException e1) {
			System.out.println("ClassNotFoundException when receiving TCP.");
		}


	}

	public static void recvLostPackets(int counter) {
		int i = 0;
		byte[] write_buf;
		byte[] seq = new byte[4];
		String sequence;
		recvdCodes = "";
		try {

			while (i < counter) {
				packet = new DatagramPacket(buf, buf.length);
				dg_socket.receive(packet);

				//System.out.println("Bytes received: "+packet.getData().toString());
				//int hash_val = packet.hashCode();
				//System.out.println("HashCode for packet "+(i+1)+": "+packet.hashCode());

				sequence = new String(packet.getData(), 0, 4);
				seq[0] = packet.getData()[0];
				seq[1] = packet.getData()[1];
				seq[2] = packet.getData()[2];
				seq[3] = packet.getData()[3];
				System.out.println("From byte array====="+fromByteArray(seq)+"-------"+packet.getData());
				hm.put(""+fromByteArray(seq), packet.getData());
				recvdCodes = recvdCodes+fromByteArray(seq)+",";
				System.out.println("Successfuly inserted 3");	
				
				buf = new byte[packetSize];
				seq = new byte[4];
				i++;
			}

		recvTCP(-1);

		} catch (IOException e1) {
			System.out.println("IOException when receiving packets");
		}

	}

	/*
	 * Stole from https://stackoverflow.com/questions/7619058/convert-a-byte-array-to-integer-in-java-and-vice-versa
	 */
	public static int fromByteArray(byte[] bytes) {
		return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	}

	public static void writeToFile(String sequence) {
		byte[] write_buf;
		String curr;
		try {
			while (!sequence.equals("")) {
				curr = sequence.substring(0, sequence.indexOf(","));
				sequence = sequence.substring(sequence.indexOf(",")+1);

				write_buf = new byte[packetSize];
				write_buf = hm.get(""+curr);
				System.out.println("_-------------------_"+curr+"----------"+write_buf);
			//	output.write(("packet"+curr).getBytes());
				output.write(write_buf, 4, write_buf.length-4);
			}
		} catch (IOException e1) {

		}
		
	}

}
