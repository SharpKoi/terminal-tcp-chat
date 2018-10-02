package cmen.tcp.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.SocketException;

import cmen.tcp.ChatLogger;
import cmen.tcp.MessageSender;
import cmen.tcp.TCPChatter;
import cmen.tcp.ChatLogger.SenderType;

public class TCPServer extends TCPChatter {
	public static final int DEFAULT_PORT = 9354;
	
	private int port;
	private ServerSocket svSocket = null;
	private BufferedInputStream bif = null;
	
	public TCPServer() {
		this(DEFAULT_PORT);
	}
	
	public TCPServer(int port) {
		this.port = port;
		this.logger = new ChatLogger(this);
		this.sender = new MessageSender(this);
		this.chatMain = new Thread(sender);
		logger.start();
	}
	
	@Override
	public String getName() {
		return "Server";
	}
	
	public void connect() {
		int length = 0;
		byte[] data = new byte[2048];
		
		try {
			svSocket = new ServerSocket(port);
			logger.info(SenderType.SYSTEM, "您好，一切都已為您準備就緒了!");
			logger.info(SenderType.SYSTEM, "正在監聽客戶端......");
			socket = svSocket.accept();
			
			socket.getOutputStream().write("Hello World!!".getBytes("Utf-8"));
			logger.info(SenderType.SYSTEM, "成功監聽到客戶端的連線!");
			
			InputStream inputStream = socket.getInputStream();
			chatMain.start();
			
			while(!sender.isStopped()) {
				if(!socket.isClosed() && !svSocket.isClosed() && socket.isConnected() && !socket.isInputShutdown() && !socket.isOutputShutdown()) {
					bif = new BufferedInputStream(inputStream);
					while((length = bif.read(data)) > 0) {
						logger.info(SenderType.CLIENT, new String(data, 0, length, "Utf-8"));
					}
					System.out.println(SenderType.SYSTEM.head() + "對方似乎已經不在線了，正在關閉所有的通訊程序...");
					this.stopAll();
					System.out.println(SenderType.SYSTEM.head() + "已關閉所有連線! bye bye~");
					if(logger != null)
						logger.shutDown();
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
				//System.exit(1);
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void stopAll() throws IOException {
		super.stopAll();
		if(svSocket != null)
			svSocket.close();
	}
}
