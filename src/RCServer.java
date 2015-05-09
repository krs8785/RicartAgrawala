import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class RCServer extends Thread{
	
	public static ArrayList<RCNode> others = new ArrayList<RCNode>();
	boolean check= true;
	ServerSocket ss;
	
	public RCServer() {
		// TODO Auto-generated constructor stub
		try {
			ss = new ServerSocket(9090);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run(){
		try{
			while(check){
				Socket s = ss.accept();
				//System.out.println("client came in");
				ObjectInputStream input_from_client= new ObjectInputStream(s.getInputStream());
                String req = (String) input_from_client.readObject();
                //System.out.println("req from for "+req);
                if(req.equals("join")){
                	//System.out.println("join called");
                	RCNode temp = (RCNode) input_from_client.readObject();
                    //System.out.println("-->>"+temp.ip+" ->>"+temp.processId);
                    others.add(temp);
                    
                    for(int i=0;i<others.size();i++){
                    	System.out.println(" -->> "+others.get(i).ip+ " -- "+others.get(i).processId);
                    }                	
                }else if(req.equals("getlist")){
                	//System.out.println("getlist in ");
                	String clientip = (String) input_from_client.readObject();
                	Socket ss3 = new Socket(clientip,9090);
                	ObjectOutputStream out_to_client = new ObjectOutputStream(ss3.getOutputStream());
                	out_to_client.writeObject("clientinfo");
                	out_to_client.writeObject(others);
                	//System.out.println("done otheres");
                }
				
			}
		}catch(Exception e){
			
		}
	}
	
	public static void main(String[] args) throws UnknownHostException {
		InetAddress ip = InetAddress.getLocalHost();
		System.out.println("\nThe Server IP is:" + ip);
		RCServer s = new RCServer();
		s.start();
	}
}
