package cmen.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class ChatThread implements Runnable {

	private Socket socket;
	private Queue<String> messagePool = new LinkedList<String>();
	private boolean isStopped = false;
	public BufferedReader reader;
	
	public ChatThread() {
		this(null);
	}
	
	public ChatThread(Socket socket) {
		reader = new BufferedReader(new InputStreamReader(System.in));
		this.socket = socket;
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
	
	@Override
	public void run() {
		OutputStream output = null;
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
				messagePool.offer(msg);
				System.out.println("[You]" + msg);
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
