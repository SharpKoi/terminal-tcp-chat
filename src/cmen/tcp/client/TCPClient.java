package cmen.tcp.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import cmen.tcp.ChatLogger;
import cmen.tcp.MessageSender;
import cmen.tcp.TCPChatter;
import cmen.tcp.ChatLogger.SenderType;

public class TCPClient extends TCPChatter {
	public static final int DEFAULT_PORT = 9354;
	public static final String DEFAULT_IP = "127.0.0.1";
	
	public TCPClient() throws IOException {
		this(DEFAULT_IP, DEFAULT_PORT);
	}
	
	public TCPClient(String ip) throws IOException {
		this(ip, DEFAULT_PORT);
	}
	
	public TCPClient(String ip, int port) throws IOException {
		this.socket = new Socket(ip, port);
		this.sender = new MessageSender(this);
		this.logger = new ChatLogger(this);
		logger.start();
	}
	
	@Override
	public String getName() {
		return "Client";
	}
	
	public void connect() {
		BufferedInputStream bif = null;
		byte[] data = new byte[2048];
		int length = 0;
		try {
			chatMain = new Thread(sender);
			chatMain.start();
			
			while(this.checkConnection()) {
				//block
				if(sender.isStopped()) {
					break;
				}
				if(checkConnection()) {
					bif = new BufferedInputStream(this.socket.getInputStream());
					while((length = bif.read(data)) > 0) {
						logger.info(SenderType.SERVER, new String(data, 0, length, "Utf-8"));
					}
					System.out.println(SenderType.SYSTEM.head() + "對方似乎已經不在線了，正在關閉所有的通訊程序...");
					this.stopAll();
					System.out.println(SenderType.SYSTEM.head() + "已關閉所有連線! bye bye~");
					//System.exit(1);
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
				System.out.println(SenderType.SYSTEM.head() + "正在嘗試關閉所有通訊程序......");
				this.stopAll();
				System.out.println(SenderType.SYSTEM.head() + "已關閉所有連線! bye bye~");
				//if(logger != null) logger.shutDown();
				//System.exit(1);
			}catch(IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public boolean checkConnection() {
		if(this.socket.isClosed() || !this.socket.isConnected() || this.socket.isInputShutdown() || this.socket.isOutputShutdown())
			return false;
		else
			return true;
	}
}
