import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class rUDP {

    // set up the global variable
    private Boolean debug = false;
    private InetAddress dest;
    private int port;
    private DatagramSocket UDPsocket;
    private final int TIMEOUT = 500;

    public rUDP(Boolean debug, String dest, int port, String[] packages) {
        this.debug = debug;
        this.port = port;
        this.UDPsocket = new DatagramSocket(8000);
        this.dest = InetAddress.getByName(dest);
        // DatagramPacket dp_send, dp_receive;
        this.UDPsocket.setSoTimeout(TIMEOUT);
    }

    private void start(String[] packages) {
        Boolean ack = false;
        String recieved_message;

        // send the packages in sequence
        for(String i : packages) {
            // send and receive for every package
            // resend if did not get acknoledgement
            while(!ack) {
                send(i);
                recieved_message = recieve();
                if(recieved_message != null) ack = true;
            }
            ack = false;
        }
    }

    // send a package of data
    private void send(String package) {
        DatagramPacket dp_send = new DatagramPacket(package.getBytes(), package.length(), this.dest, this.port);
        UDPsocket.send(dp_send);
    }

    // try to recieve some data but times out if could not get a response in 500
    // if times out then return null
    private String recieve() {
        byte[] buffer = new byte[1024];
        DatagramPacket dp_receive = new DatagramPacket(buffer, 1024);
        try {
            UDPsocket.receive(dp_receive);
            return new String(dp_receive.getData(), 0, dp_receive.getLength());
        } catch(InterruptedIOException e) {
            System.out.println(e.toString());
            return null;
        }
    }
}
