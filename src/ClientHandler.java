import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

	public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
	
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String clientUsername;
	private String clientPassword;
	
	
	public ClientHandler(Socket socket) {
		
		try {
			
			this.socket = socket;
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.clientUsername = bufferedReader.readLine(); //this line waits for client Username to be sent over
			
			clientHandlers.add(this);
			this.clientPassword = bufferedReader.readLine();
			//System.out.println(clientPassword);
			if(getClientPassword().equals("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8")) {
				broadcastMessage("SERVER: '" + clientUsername + "' has entered the chat room!");
			}
			else {
				closeEverything(socket, bufferedReader, bufferedWriter);
			}
		
			
		} catch (IOException e) {
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
		
	}
	
	public String getClientUsername () {
		return clientUsername;
		
	}
	
	public String getClientPassword () {
		return clientPassword;
		
	}


	public void broadcastMessage(String messageToSend) {
		for (ClientHandler clientHandler : clientHandlers) {
			try { //clientHandler != this
				if ( clientHandler != this ) {// makes sure not to send the message to the current client but instead all the other connected clients.
					clientHandler.bufferedWriter.write(messageToSend);
					
					clientHandler.bufferedWriter.newLine();
					clientHandler.bufferedWriter.flush();
					 //this ensures even if the message sent is small, the buffer will always be full.	
				}
			}catch (IOException e) {
				closeEverything(socket, bufferedReader, bufferedWriter);
			}
				
				
		}
		
	}

	public void broadcastMessageToCurrentUser(String messageToSend) {
		for (ClientHandler clientHandler : clientHandlers) {
			try { //clientHandler != this
				if ( clientHandler == this)  {//!clientHandler.clientUsername.equals(clientUsername)
					clientHandler.bufferedWriter.write(messageToSend);
					
					clientHandler.bufferedWriter.newLine();
					clientHandler.bufferedWriter.flush();
					 //this ensures even if the message sent is small, the buffer will always be full.	
				}
			}catch (IOException e) {
				closeEverything(socket, bufferedReader, bufferedWriter);
			}
				
				
		}
		
	}


	public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter ) {
		
		removeClientHandler();
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
	public void closeClientSocket(Socket socket ) {
		
		try {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void removeClientHandler() {
		
		clientHandlers.remove(this); //clientUsername
		if(getClientPassword().equals("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8")) {
			broadcastMessage("SERVER: '" + clientUsername + "' has been dissconected!");
			System.out.println(clientUsername + " has disconnected!");
		}
	}



	@Override
	public void run() { //this run method here runs on a separate thread , function: it listens to client messages then broadcasts them 
		
		String messageFromClient;
		while (socket.isConnected()) {
			try {
				
				messageFromClient = bufferedReader.readLine(); // line is a blocking operation so is important to run it in a thread
				if(messageFromClient == null) throw new IOException();
				
				
				broadcastMessage(messageFromClient);
			} catch (IOException e) {
				
				closeEverything(socket, bufferedReader, bufferedWriter);
				break; //important to re-run this while loop and only stops when client disconnects.
			}
		}
		
	}

}
