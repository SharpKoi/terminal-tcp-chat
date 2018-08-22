package cmen.tcp.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import cmen.tcp.ChatThread;

public class TCPClient {
	public static final int DEFAULT_PORT = 9354;
	public static final String DEFAULT_IP = "127.0.0.1";
	private Socket cs;
	private ChatThread ct;
	private Thread chatMain;
	
	public TCPClient() throws IOException {
		this(DEFAULT_IP, DEFAULT_PORT);
	}
	
	public TCPClient(String ip) throws IOException {
		this(ip, DEFAULT_PORT);
	}
	
	public TCPClient(String ip, int port) throws IOException {
		cs = new Socket(ip, port);
		ct = new ChatThread(cs);
	}
	
	public void connect() {
		BufferedInputStream bif = null;
		byte[] data = new byte[2048];
		int length = 0;
		try {
			chatMain = new Thread(ct);
			chatMain.start();
			
			while(this.checkConnection()) {
				//block
				if(ct.isStopped()) {
					break;
				}
				if(checkConnection()) {
					bif = new BufferedInputStream(cs.getInputStream());
					while((length = bif.read(data)) > 0) {
						System.out.println("[Server]" + new String(data, 0, length, "Utf-8"));
					}
					System.out.println("[System]對方似乎已經不在線了，正在關閉所有的通訊程序...");
					this.stopAll();
					System.out.println("[System]已關閉所有連線! bye bye~");
					System.exit(1);
				}else {
					chatMain.interrupt();
					break;
				}
			}
			
		}catch(IOException e) {
			if(!(e instanceof SocketException)) {
				e.printStackTrace();
			}
		} finally {
			try {
				System.out.println("[System]正在嘗試關閉所有通訊程序......");
				this.stopAll();
				System.out.println("[System]已關閉所有連線! bye bye~");
				System.exit(1);
			}catch(IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public boolean checkConnection() {
		if(cs.isClosed() || !cs.isConnected() || cs.isInputShutdown() || cs.isOutputShutdown())
			return false;
		else
			return true;
	}
	
	public void stopAll() throws IOException {
		if(ct != null)
			ct.setStop();
		if(chatMain != null)
			chatMain.interrupt();
		if(cs != null) {
			cs.close();
		}
	}
}
