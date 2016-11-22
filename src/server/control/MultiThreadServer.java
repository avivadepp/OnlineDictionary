package server.control;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import javax.swing.*;

import client.control.UserDatabase;

public class MultiThreadServer extends JFrame{
	private static final long serialVersionUID = 1L;
	
	private JTextArea jta = new JTextArea();
	
	public MultiThreadServer() {
		setLayout(new BorderLayout());
		add(new JScrollPane(jta), BorderLayout.CENTER);
		setTitle("Server");
		setSize(500, 300);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		
		try {
			ServerSocket serverSocket = new ServerSocket(8000);
			jta.append("MultiThreadServer started at " + new Date() + '\n');
			
			int clientNo = 1;
			
			while(true) {
				Socket socket = serverSocket.accept();
				
				jta.append("Starting thread for client " + clientNo + " at " + new Date() + '\n');
				
				InetAddress inetAddress = socket.getInetAddress();
				jta.append("Client " + clientNo + "'s host name is " + inetAddress.getHostName() + '\n');
				jta.append("Client " + clientNo + "'s IP Address is " + inetAddress.getHostAddress() + '\n');
				
				HandleAClient task = new HandleAClient(socket);
				
				new Thread(task).start();
				
				clientNo++;
			}
		}
		catch(IOException ex) {
			System.out.println(ex);
		}
	}
	
	class HandleAClient implements Runnable {
		private Socket socket;
		UserDatabase ud = new UserDatabase();
		public HandleAClient(Socket socket) {
			this.socket = socket;
		}
		public void run() {
//			System.out.println("0");
			ud.createConnection();
			try {
//				DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
//				DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
				BufferedWriter outputToClient = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while(true) {
			//		double res = inputFromClient.readDouble();
			//		System.out.println(res);
					
//					System.out.println(inputFromClient.readChar());
//					String words[] = inputFromClient.read().split("\\s");
					String words[] = inputFromClient.readLine().split("\\s");
					if (words[0].equals("0")) {  //登录
						if(!ud.nameExists(words[1])) {
							outputToClient.write("0\n");
						}
						else if(ud.nameExists(words[1])) {
							if (ud.passwordCorrectly(words[1], words[2])) {
								outputToClient.write("1\n");
							}
							else {
								outputToClient.write("2\n");
							}
						}
					}
					else if (words[0].equals("1")) {  //注册
//						System.out.println("1");
						String name = words[1];
						if (ud.nameExists(name)) {
//							outputToClient.writeInt(0);
							outputToClient.write("0\n");
						}
						else {
							String password = words[2];
							String password2 = words[3];
							if (password.length() < 6) {
								outputToClient.write("1\n");
							}
							else if (!password.equals(password2)) {
								outputToClient.write("2\n");
							}
							else {
								ud.insert(name, password);
								outputToClient.write("3\n");
								
							}
						}
					}
					else if (words[0].equals("2")) {  //游客登录
						
					}
					outputToClient.flush();
			//		outputToClient.writeChars("ok !!");
					jta.append("requests received from client:");
					for (int i = 0; i < words.length; i++) {
						jta.append(" " + words[i]);
					}
					jta.append("\n");
				}
			}
			catch(IOException e){
				System.out.println(e);
			}
		}
	}
}
