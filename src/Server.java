
import java.io.IOException;

import java.net.ServerSocket;
import java.nio.file.FileSystems;
import java.util.Arrays;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Server {
	public static final boolean DEBUG = true; // C:\\Users\\lamba\\secure-network-eclipse\\SecureServerChat\\src\\Keys\\ServerKeyStore.jks
	static String path = FileSystems.getDefault().getPath("ServerKeyStore.jks").toAbsolutePath().toString();
    private static final String KEYSTORE_LOCATION = path;
	private static final String KEYSTORE_PASSWORD = "password";
    private static final int TLS_PORT = 2839;
	private ServerSocket serverSocket; 
	private String[] AllowedClients = {"/127.0.0.1"};
	//private String[] AllowedClients = {"/0.0.0.0","/localhost"};
	
	public void startServer() {
		try {
			ServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(TLS_PORT);
            serverSocket.setEnabledProtocols(new String[]{"TLSv1.3"});
            
            
			while (!serverSocket.isClosed()) {
				SSLSocket socket = (SSLSocket) serverSocket.accept();
				
				String ClientAddress = socket.getInetAddress().toString();
				
				if (Arrays.asList(AllowedClients).contains(ClientAddress)  ) {
					ClientHandler clientHandler = new ClientHandler(socket);
					if(clientHandler.getClientPassword().equals("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8")) {
						Thread thread = new Thread(clientHandler);
		                thread.start(); 
		                System.out.println("'" + clientHandler.getClientUsername() + "' has connected from: " + socket.getInetAddress());
		                clientHandler.broadcastMessageToCurrentUser("You Connected Successfully!");
					}else {
						
						
						System.out.println("Stopped a client '"+ clientHandler.getClientUsername() +"' who was using the wrong chat room password from connecting! Their address: "+ socket.getInetAddress());
						clientHandler.closeClientSocket(socket);
						
						
					}
					
					
	                
				}
				else {
					
					System.out.println("Stopped an unauthorized client with address: "+ socket.getInetAddress() +" from conecting to the chat room!");
					
					socket.close();
				}
                
			}
		} catch (IOException e) {
			closeServerSocket();
			
			
		}
		
	}
	
	
	public void closeServerSocket() {
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)throws IOException{
	
			
		System.setProperty("javax.net.ssl.keyStore", KEYSTORE_LOCATION);
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);
		Server server = new Server();
	
		server.startServer();
		

		
	}
	
}
