import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class QuoteClient {
	//boolean ontime = false;
	//TimerTask task = new TimerTask(){
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("java QuoteClient <hostname>");
			return;
		}

		//get a datagram socket.
		DatagramSocket socket = new DatagramSocket();

		//send request
		byte[] buf = new byte[256];
		InetAddress address = InetAddress.getByName(args[0]);
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 8000);
		socket.send(packet);

		//get response
		//packet = new DatagramPacket(buf, buf.length);
		//socket.receive(packet);

		//Timer timer = new Timer();

		for (int i=0; i<5; i++) {
			//timer.schedule(task, 1000);

			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			//ontime = true;
			//timer.cancel();
			String received = new String(packet.getData(), 0, packet.getLength());
			System.out.println("Quote nr "+i+": " + received);
		}

		 
		/*if (!ontime) {
			System.out.println("It took to long!");
		}*/

		//display response
		//String received = new String(packet.getData(), 0, packet.getLength());
		//System.out.println("Quote of the moment: " + received);

		socket.close();

	}
	//};

}
