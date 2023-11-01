import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;

import java.nio.file.FileSystems;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Client {
	
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String username;
	private String password;

	public static final boolean DEBUG = true;
    private static final int TLS_PORT = 2839;
    private static final String TLS_HOST = "localhost";
    static String path = FileSystems.getDefault().getPath("ClientKeyStore.jks").toAbsolutePath().toString();
    private static final String TRUSTTORE_LOCATION = path;
    private static final String TRUSTTORE_PASSWORD = "password";
    
   
    
    
    
	public Client(SSLSocket socket, String username, String password) {
		try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
            this.password = password;
           
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }


	public Client() {
		// TODO Auto-generated constructor stub
	}


	public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
		try {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
			if (socket != null) {
				socket.close();
			}
		}catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void sendMessage() {
		try {
            bufferedWriter.write(username);
            
            bufferedWriter.newLine();
            bufferedWriter.flush();
            bufferedWriter.write(password);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            try (Scanner scanner = new Scanner(System.in)) {
				while (socket.isConnected()) {
				    String messageToSend = scanner.nextLine();
				    bufferedWriter.write(username + ": " + messageToSend);
				    bufferedWriter.newLine();
				    bufferedWriter.flush();
				    
				}
			}
        } catch (IOException e) {
            closeEverything(socket,bufferedReader, bufferedWriter);
            
        }
    } 
	
	public void listenForMessage() { // this method will be a blocking operation so must be run as a thread
		new Thread (new Runnable() {
            @Override
            public void run() { 
                String msgFromGroupChat;
                
                while (socket.isConnected()) {
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        if(msgFromGroupChat == null) throw new IOException() ;
                        System.out.println(msgFromGroupChat);
                    } catch (IOException e) {
                    	closeEverything(socket,bufferedReader, bufferedWriter);
                    	System.exit(0);
                        
                        
                    }
                }
            }
            
        }).start();
    }
	
	public String hashString(String string) throws NoSuchAlgorithmException{
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] messageDigest = md.digest(string.getBytes());
		BigInteger bigInt = new BigInteger(1,messageDigest);
		return bigInt.toString(16);
	}
	
	public static void main(String[] args)throws IOException , UnknownHostException, NoSuchAlgorithmException  {
	
		try (Scanner scanner = new Scanner(System.in)) {

			
			System.out.println("Please enter your Username for the GroupChat: ");
			String username = scanner.nextLine();
			
			System.out.println("Please enter the chat room master password(use password): ");
			System.out.println("If the wrong password is used then the program will shutdown!");
			String password = scanner.nextLine();
			Client hash = new Client();
		    String Hashedpassword = hash.hashString(password) ;
			
		
			
			System.setProperty("javax.net.ssl.trustStore", TRUSTTORE_LOCATION);
			System.setProperty("javax.net.ssl.trustStorePassword", TRUSTTORE_PASSWORD);
			SSLSocketFactory f = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket socket = (SSLSocket) f.createSocket(TLS_HOST, TLS_PORT);
			Client client = new Client(socket, username, Hashedpassword);
			
			client.listenForMessage();
			
			client.sendMessage();
			if (socket.isClosed()) {
				System.out.println("You are not allowed to enter this chat room with your current IP!");
				System.exit(0);
			}
			
			
		
				
		
			
			
			
			

			
			
			
			
			
			
			
		}
   }
	
  
}
