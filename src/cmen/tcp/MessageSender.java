package cmen.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class MessageSender implements Runnable {

	private TCPChatter owner;
	private boolean isStopped = false;
	public BufferedReader reader;
	
	public MessageSender(TCPChatter owner) {
		reader = new BufferedReader(new InputStreamReader(System.in));
		this.owner = owner;
	}
	
	public TCPChatter getOwner() {
		return owner;
	}
	
	public boolean isStopped() {
		return isStopped;
	}
	
	public void setStop() {
		this.isStopped = true;
	}
	
	public void setStop(boolean val) {
		this.isStopped = val;
	}
	
	/*讀取輸入及送出訊息的thread*/
	@Override
	public void run() {
		OutputStream output = null;
		Socket socket = owner.getSocket();
		try {
			if(socket != null)
				output = socket.getOutputStream();
			
			while (!isStopped) {
				String msg = reader.readLine();
				if (msg.equals("@STOP")) {
					if (output != null) {
						output.flush();
						output.close();
					}
					output = null;
					this.isStopped = true;
					break;
				}
				owner.getLogger().info(msg);
				if (output != null) {
					if(this.isStopped || socket.isClosed() || !socket.isConnected() || socket.isOutputShutdown()) {
						return;
					}
					output.write(msg.getBytes("Utf-8"));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
