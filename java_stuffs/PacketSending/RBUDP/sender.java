import java.io.*;
import java.net.*;
import java.util.*;

public class sender {
	//UDP
	static DatagramSocket dg_socket = null;
	static DatagramPacket packet;
	static InetAddress address;
	static int port;

	//TCP
	static ServerSocket senderSocket = null;
	static Socket recvSocket;
	static ObjectOutputStream oos = null;
	static ObjectInputStream ois = null;

	//Other
	static BufferedInputStream bis = null;
	static File myFile;
	static int packetSize = 512;
	static byte[] buf;
	static HashMap<String, byte[]> hm = new HashMap<>();
	static int sendLimit = 4;
	static String sentCodes = "";
	static ArrayList<Integer> sentCodes_arr;
	static String fileName = "person_cheering.mp3";


	public static void main(String[] args) {
		initiate();

		//send packets
		sendPackets();

	}

	public static void initiate() {
		//get the file to send
		getFile();

		//set buffer
		buf = new byte[packetSize];

		//receive request for file
		try {
			//UDP connection
			packet = new DatagramPacket(buf, buf.length);
			dg_socket.receive(packet);
			address = packet.getAddress();
			port = packet.getPort();

			sentCodes_arr = new ArrayList<>();

			//TCP connection
			senderSocket = new ServerSocket(8000);
			recvSocket = senderSocket.accept();
			oos = new ObjectOutputStream(recvSocket.getOutputStream());
			ois = new ObjectInputStream(recvSocket.getInputStream());
		} catch (IOException e) {
			System.out.println("IOException");
		}
	}

	public static void getFile() {
		try {
			dg_socket = new DatagramSocket(8000);
			myFile = new File(fileName);
			bis = new BufferedInputStream(new FileInputStream(myFile));
			System.out.println("# File accepted, size: "+(int)(myFile.length()));

		} catch (SocketException e1) {
			System.out.println("SocketException.");
		} catch (FileNotFoundException e2) {
			System.out.println("FileNotFoundException.");
		}

	}

	public static void sendPackets() {
		double numPacs = Math.ceil((int)(myFile.length())/packetSize);
		System.out.println("# Sending "+numPacs+" packets.");
		int hash_val;
		byte[] save_buf = new byte[packetSize];
		byte[] seq = new byte[4];
		byte[] pac = new byte[packetSize-4];
		byte[] info = new byte[100];
		String sequence;
		System.out.println(seq.length+"       "+pac.length);

		/*String type = fileName.substring(fileName.indexOf(".")+1);
		info = (numPacs+","+packetSize+","+type).getBytes();
		packet = new DatagramPacket(info, info.length);*/

		try {

			for (int i=0; i < (int)(numPacs)+1; i++) {
				//bis.read(buf, 0, buf.length);
				bis.read(pac, 0, pac.length);

				sequence = i+"";
				seq = intToByteArray(i);

				System.arraycopy(seq, 0, buf, 0, seq.length);
				System.arraycopy(pac, 0, buf, seq.length, pac.length);

				packet = new DatagramPacket(buf, buf.length, address, port);
				dg_socket.send(packet);
				//sentCodes = sentCodes + packet.hashCode()+",";
				//hash_val = packet.hashCode();
				//System.out.println("hash_val: "+packet.hashCode());


				System.out.println("Bytes sent: "+packet.getData().toString());
				System.out.println("--------------"+fromByteArray(seq));
				hm.put(""+fromByteArray(seq), packet.getData());

				sentCodes = sentCodes + i+",";
				
				//System.out.println("HashCode for packet "+(i+1)+": "+packet.hashCode());
				//sentCodes_arr.add(packet.hashCode());

				if (i%sendLimit == 0 && i != 0) {
					sendTCP();

					sentCodes = "";
				}

				buf = new byte[packetSize];			
				pac = new byte[packetSize-4];
				seq = new byte[4];
			}

			sendTCP();

		} catch (IOException e) {
			System.out.println("IOException when sending packets.");
		}

	}

	public static void sendTCP() {
		String reply;
		try {
			oos.writeObject(sentCodes);
			reply = ois.readObject().toString();


			if (!reply.equals("")) {
				System.out.println("Time to resend some packets");
				resendPackets(reply);
			}
			//System.out.println("The receiver said: " + reply);

			sentCodes = "";

		} catch (IOException e1) {
			System.out.println("IOException when sending TCP.");
		} catch (ClassNotFoundException e2) {
			System.out.println("ClassNotFoundException when receiving TCP.");
		}

	}

	public static void resendPackets(String pacs) {
		byte[] buf = new byte[packetSize];
		String sequence;
		sentCodes = "";

		try {
			while (!pacs.equals("")) {
				sequence = pacs.substring(0, pacs.indexOf(","));
				pacs = pacs.substring(pacs.indexOf(",")+1);
	
				System.out.println("resending packet nr."+sequence);
				buf = hm.get(sequence);
				packet = new DatagramPacket(buf, buf.length, address, port);
				dg_socket.send(packet);
	
	
				buf = new byte[packetSize];
				sentCodes = sentCodes+sequence+",";
				System.out.println("Resent: "+sequence);
				sequence = "";


			}
		System.out.println("Exit while loop");	
		sendTCP();

		} catch (IOException e1) {
			System.out.println("IOException in resending packets");
		}

	}

	/*
	 * From https://stackoverflow.com/questions/2183240/java-integer-to-byte-array
	 */
	public static final byte[] intToByteArray(int value) {
	    return new byte[] {
            (byte)(value >>> 24),
            (byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)value};
	}

	public static int fromByteArray(byte[] bytes) {
		return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	}

}
