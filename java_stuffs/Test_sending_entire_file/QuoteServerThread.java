import java.io.*;
import java.net.*;
import java.util.*;

public class QuoteServerThread extends Thread {
	protected DatagramSocket socket = null;
	protected BufferedReader in = null;
	protected BufferedInputStream bis = null;
	protected boolean moreQuotes = true;
	protected File myFile = null;
	protected ServerSocket senderSocket = new ServerSocket(8000);
	protected Socket recvSocket;
	protected ObjectOutputStream boos = null;
	protected ObjectInputStream bois = null;
	protected String resend = "";
	protected String seqSent = "";
	protected BufferedInputStream resendBis = null;
	protected long packetSize = 259;
	protected DatagramPacket packet;
	protected InetAddress address;
	protected int port;
	protected byte[] buf;
	protected byte[] pac;
	protected byte[] seq;
	protected byte[] info;
	protected String seqString;
	protected int sendLimit;

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
		try {
			buf = new byte[(int)(packetSize)];
			pac = new byte[253];
			seq = new byte[6];
			info = new byte[100];

			sendLimit = 3;	//Amount of packets to be sent before TCP signaling

			//Receive request
			recvSocket = senderSocket.accept();
			boos = new ObjectOutputStream(recvSocket.getOutputStream());
			//bois = new BufferedInputStream(new ObjectInputStream(recvSocket.getInputStream()));
			bois = new ObjectInputStream(recvSocket.getInputStream());
			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

			address = packet.getAddress();
			port = packet.getPort();

			/*
			 * Send the information
			 */
			sendPackets();
				
			/*try {
				sleep(100);
			} catch (InterruptedException e) {

			}

			}*/


		} catch (IOException e) {
				moreQuotes = false;
		}

		socket.close();
	}

	protected void sendPackets() throws IOException {
		info = ("txt, receivedFile").getBytes();
			packet = new DatagramPacket(info, info.length, address, port);
			socket.send(packet);

			double numPacs = Math.ceil((int)(myFile.length())/259);
			System.out.println(numPacs+"");

			int limit = sendLimit; //(int)(numPacs/sendLimit);
			int nextLimit = limit;
			for (double i=0.0; i < numPacs+1; i++) {
				buf = new byte[259];
				pac = new byte[253];
				if (i < 10) {
					System.out.println((int)(i)+"");
					seqString = "00"+(int)(i);
				} else {
					seqString = "0"+(int)(i);
				}
				seqSent = seqSent+seqString+",";

				seq = seqString.getBytes();
				bis.read(pac, 0, pac.length);
				System.arraycopy(seq, 0, buf, 0, seq.length);
				System.arraycopy(pac, 0, buf, seq.length, pac.length);
				System.out.println("Packet nr."+(i+1));
				System.out.println("Bytes sent: " + (i*buf.length));
				//InetAddress address = packet.getAddress();
				//int port = packet.getPort();

				packet = new DatagramPacket(buf, buf.length, address, port);
				socket.send(packet);

				if (i == limit) {
					sendTCP();
					limit = limit + nextLimit;
				}

				try {
					sleep(10);
				} catch (InterruptedException e) {

				}
			}
				
			/*
			 * Send the sequences sent and receive the sequences received
			 */
				
			sendTCP();

			boos.close();
			bois.close();

	}

	protected void sendTCP() {
		int numResend=0;
		long ptr;

		try {
			boos.writeObject(seqSent);
			resend = bois.readObject().toString();

			if (!resend.equals("0")) {
				numResend = Integer.parseInt(resend.substring(0,resend.indexOf(",")));
				resend = resend.substring(resend.indexOf(",")+1);
				for (int i=0; i < numResend; i++) {
					ptr = Long.parseLong(resend.substring(0,resend.indexOf(",")));
					resend = resend.substring(resend.indexOf(",")+1);
					resendPac(ptr);
				}
			}

		} catch (IOException e) {
			System.out.println("IOException");
		} catch (ClassNotFoundException e2) {
			System.out.println("ClassNotFoundException");
		}
			System.out.println("Sender: "+seqSent);
			System.out.println("Receiver: "+resend);
			seqSent = "";

	}

	protected void resendPac(long ptr) {
		try {
			resendBis = new BufferedInputStream(new FileInputStream(myFile));
			resendBis.skip(packetSize*ptr);

			buf = new byte[259];
			pac = new byte[253];
			if (ptr < 10) {
				System.out.println((int)(ptr)+"");
				seqString = "00"+(int)(ptr);
			} else {
				seqString = "0"+(int)(ptr);
			}
			seqSent = seqSent+seqString+",";

			seq = seqString.getBytes();

			resendBis.read(pac, 0, pac.length);
			System.arraycopy(seq, 0, buf, 0, seq.length);
			System.arraycopy(pac, 0, buf, seq.length, pac.length);
			System.out.println("Packet nr."+(ptr+1));
			System.out.println("Bytes sent: " + (ptr*buf.length));

			packet = new DatagramPacket(buf, buf.length, address, port);
			socket.send(packet);

		} catch (IOException e1) {
			System.out.println("IOException in resend.");
		}
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
