import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


public class RCClient extends Thread {

	String state;
	String clientState;
	static String serverIP;
	RCNode myInfo = new RCNode();
	ArrayList<RCNode> allClients = new ArrayList<RCNode>();
	List<RCNode> que = new LinkedList<RCNode>();
	ServerSocket ss;
	static int count =0;
	
	public RCClient() {
		// TODO Auto-generated constructor stub
		state = "released";
	}
	
	public static void main(String[] args) {
		RCClient c = new RCClient();
		try {
			InetAddress ip = InetAddress.getLocalHost();			
			System.out.println("IP of client"+ip);
			c.myInfo.ip=ip.getHostAddress();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter Process ID ");
		c.myInfo.processId = sc.nextLine();
		System.out.println("Enter Server IP:");
		serverIP = sc.nextLine();
		
		try {
			Socket s = new Socket(serverIP, 9090);
			ObjectOutputStream out_to_server = new ObjectOutputStream(s.getOutputStream());
			out_to_server.writeObject("join");
            out_to_server.writeObject(c.myInfo);
            s.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//System.out.println("------????-" +Server.others.size());
		try {
			c.ss = new  ServerSocket(9090);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		c.start();
		
		while(true){
			System.out.println("\n1. Request ");			
			System.out.println("2. quit");
			int option2 = Integer.parseInt(sc.nextLine());
			switch(option2){
				case 1:
					System.out.println("Enter Procees Id to enter CS: ");
					String pid = sc.nextLine();
					c.requestCS(pid);
					break;
			/*	case 2:
					c.releaseCS();
					
					break;*/
				case 2:
					System.out.println("Quiting! Thank you.");
					System.exit(0);
					break;
				default:
					System.out.println("Invalid! Enter valid option");
			}
		}
	}
	
	public void releaseCS(){
		System.out.println("Wait..releasing...");
		state = "released";
		int check =1;
		
		if(!que.isEmpty()){
			Socket s6;
			try {
				
				s6 = new Socket(que.get(0).ip,9090);
				ObjectOutputStream out_to_c = new ObjectOutputStream(s6.getOutputStream());
				out_to_c.writeObject("posReply");			
				out_to_c.writeObject("false");	
				
				s6.close();
				//Thread.sleep(1000);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 		
		}
		
		for(int i=1;i<que.size();i++){
			RCNode tem = que.get(i);
			Socket s7;
			try {
				Thread.sleep(500);
				s7= new Socket(tem.ip,9090);
				ObjectOutputStream out_to_c = new ObjectOutputStream(s7.getOutputStream());
				out_to_c.writeObject("posReply");	
				out_to_c.writeObject("true");
				
				s7.close();
				//Thread.sleep(1000);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}					
		}
		que = new  LinkedList<RCNode>();
		System.out.println("Done relase");
	}
	
	public void requestCS(String pid){
		
		count =0;		
		try {
			Socket s2 = new Socket(serverIP, 9090);
			ObjectOutputStream out_to_server = new ObjectOutputStream(s2.getOutputStream());
			out_to_server.writeObject("getlist");
			out_to_server.writeObject(myInfo.ip);
			s2.close();
			Thread.sleep(1200);
			
			//System.out.println("enter reqest cs->"+allClients.size()+" ->"+Server.others.size());
			state = "Wanted";
			String call_ip="";
			for(int i=0;i<allClients.size();i++){
				if(allClients.get(i).processId.equals(pid)){
					call_ip=allClients.get(i).ip;
				}
			}
			//System.out.println("Calling Client"+ call_ip);
			
			int len = allClients.size();
			
			while(count != (len-1)){
				System.out.println("--Wating for replies");
				count =0;
				for(int i=0;i<len;i++){
					RCNode t = allClients.get(i);
					if(!t.ip.equals(myInfo.ip)){
						Socket s4 = new Socket(t.ip,9090);
						ObjectOutputStream out_to_c = new ObjectOutputStream(s4.getOutputStream());
						out_to_c.writeObject("getstate");
						out_to_c.writeObject(myInfo);
						s4.close();
					}
				}
				Thread.sleep(1200);
			}
			
			//System.out.println("end of while-- can enter CS");
			state = "held";
			System.out.println("Using CRITICAL SECTION !!!");
			System.out.println("\nPress 1 to  Release");
			Scanner sc1 = new Scanner(System.in);
			int opt = Integer.parseInt(sc1.nextLine());
			switch(opt){
				case 1:					
					releaseCS();					
					break;				
				default:
					System.out.println("Invalid! Enter valid option");
			}
			
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void broadcast(){
		System.out.println("bbbbbb");
		
		
	}
	
	@SuppressWarnings("unchecked")
	public void run(){
		try{
			ObjectInputStream input_from_client;
			while(true){				
				Socket s_c = ss.accept();
				//System.out.println("Request from some client");
				input_from_client= new ObjectInputStream(s_c.getInputStream());
				String req = (String) input_from_client.readObject();
				//System.out.println("got it "+req);
				if(req.equals("clientinfo")){
					//System.out.println("in client info");
					allClients = (ArrayList<RCNode>) input_from_client.readObject();
					//System.out.println("clinet updated");
				}else if(req.equals("getstate")){
					//System.out.println("getign state");
					RCNode caller = (RCNode) input_from_client.readObject();
					reply(caller);
					//System.out.println("ouy from get satete");
				}else if(req.equals("posReply")){
					String flag = (String) input_from_client.readObject();
					if(flag.equals("true")){
						count =0;
					}else{
						
						count++;
						//Thread.sleep(800);
					}
					
				}
				
			}			
		}catch(Exception e) {
			
		}
	}

	public  void reply(RCNode caller) {
		//System.out.println("check sate");
		// TODO Auto-generated method stub
		if (state.equals("held") || state.equals("wanted")){
			System.out.println("held/wanted");
			que.add(caller);			
		}else{
			Socket s5;
			try {
				s5 = new Socket(caller.ip,9090);
				ObjectOutputStream out_to_c = new ObjectOutputStream(s5.getOutputStream());
				out_to_c.writeObject("posReply");	
				out_to_c.writeObject("false");
				s5.close();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		//System.out.println("out fromr cehck state");
		
	}
}
