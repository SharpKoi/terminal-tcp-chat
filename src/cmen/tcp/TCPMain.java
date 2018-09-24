package cmen.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cmen.tcp.client.TCPClient;
import cmen.tcp.server.TCPServer;

public class TCPMain {
	public static void main(String[] args) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			if(args[0].equalsIgnoreCase("@server")) {
				while(true) {
					System.out.print("[System]請輸入連接埠(Port): ");
					String portStr = reader.readLine();
					if(isInteger(portStr)) {
						new TCPServer(Integer.parseInt(portStr)).connect();
						break;
					}else {
						if(portStr.equalsIgnoreCase("default")) {
							new TCPServer().connect();
							break;
						}
						System.err.println("[Error]請輸入正確的連接埠格式");
					}
				}
			}else if(args[0].equalsIgnoreCase("@client")) {
				while(true) {
					System.out.print("[System]請輸入對方的網路位址(IP): ");
					String ip = reader.readLine();
					if(ip.equalsIgnoreCase("local")) {
						ip = "127.0.0.1";
					}else if(ip.equalsIgnoreCase("default")) {
						ip = InetAddress.getLocalHost().getHostAddress();
						System.out.println("[System]您當前的網路位址為: " + ip);
					}
					System.out.print("[System]請輸入連接埠(Port): ");
					String portStr = reader.readLine();
					if(isInteger(portStr)) {
						try {
							System.out.println("[System]嘗試連接中，請稍後......");
							new TCPClient(ip, Integer.parseInt(portStr)).connect();
							break;
						} catch (IOException e){
							if(e instanceof UnknownHostException) {
								System.err.println("[Error]找不到該IP位址，請嘗試重新輸入正確的IP");
								continue;
							}
							else if(e instanceof ConnectException) {
								System.err.println("[Error]該服務端的Port與您輸入的不符，請嘗試重新輸入");
								continue;
							}
							e.printStackTrace();
						}
					}else {
						if(portStr.equalsIgnoreCase("default")) {
							System.out.println("[System]嘗試連接中，請稍後......");
							new TCPClient(ip).connect();
							break;
						}
						System.err.println("[Error]請輸入正確的連接埠格式");
					}
					
				}
			}else {
				return;
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
		return;
	}
	
	public static boolean isInteger(String s) {
		Pattern pattern = Pattern.compile("^([1-9]?)(\\d*)$");
		Matcher matcher = pattern.matcher(s);
		return matcher.matches();
	}
}
