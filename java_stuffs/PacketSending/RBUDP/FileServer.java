import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer extends Thread{
	
	private ServerSocket ss;
        private receiverFrame rec;
	
	public FileServer(int port) {
		try {
			ss = new ServerSocket(port);
                        rec = new receiverFrame(7000);
                        rec.setVisible(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		while (true) {
			try {
				Socket clientSock = ss.accept();
				saveFile(clientSock);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveFile(Socket clientSock) throws IOException {
                String filename = "test.mp3";
                rec.recvngFile();
		DataInputStream dis = new DataInputStream(clientSock.getInputStream());
		FileOutputStream fos = new FileOutputStream(filename);
		byte[] buffer = new byte[4096];
		
		int filesize = 6144000; // Send file size in separate msg
		int read = 0;
		int totalRead = 0;
		int remaining = filesize;
                int progress = 0;
		while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
			totalRead += read;
			remaining -= read;
			System.out.println("read " + totalRead + " bytes.");
                        progress = 100*(totalRead)/filesize;
                        rec.updateProgress(progress);
			fos.write(buffer, 0, read);
		}
		
		fos.close();
		dis.close();
                rec.fileRecvd(filename);
	}
	
	public static void main(String[] args) {
		FileServer fs = new FileServer(7000);
		fs.start();
	}

}
