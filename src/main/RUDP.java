package src.main;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphor;


public class RUDP {

    // set up the global variable
    private Boolean debug = false;
    private InetAddress dest;
    private int port;
    private DatagramSocket UDPsocket;
    private final int TIMEOUT = 500;
    private Package packs;

    private int offset;
    private int base;
    private int nextSeqNum;
    private LinkedList<String> packetsList;
    private Timer timer;
    private Semaphore s;
    private boolean isTransferComplete = false;

    public RUDP(Boolean debug, String dest, int port, String file_name) throws SocketException, IOException {
        this.debug = debug;
        this.port = port;
        this.UDPsocket = new DatagramSocket(8000);
        this.dest = dest == "localhost" ? InetAddress.getLocalHost() : InetAddress.getByName(dest);
        // DatagramPacket dp_send, dp_receive;
        // this.UDPsocket.setSoTimeout(TIMEOUT);

        // create the package class instance
        this.packs = new Package(file_name);
        this.base = this.packs.get_offset();
        this.offset = this.packs.get_offset();
        this.nextSeqNum = this.base;
        this.packetsList = new LinkedList<String>();
        this.s = new Semaphore(1);

        start();
    }

    public void setTimer(boolean isNewTimer){
        if (timer != null) timer.cancel();
        if (isNewTimer){
            timer = new Timer();
            timer.schedule(new Timeout(), TIMEOUT);
        }
    }

    private void start() throws IOException {
        Boolean ack = false;
        String recieved_message;

        // generate the package info
        String message = packs.generatePackage(packs.get_offset());
        String[] info = packs.splitPackage(message);
        int seq_no = Integer.parseInt(info[1]);

        while(!ack) {
            recieved_message = recieve();
            if(recieved_message != null) {
                int ack_no = Integer.parseInt(recieved_message.split("\\|")[1]);
                // test if the ack's sequence number match
                if(ack_no == (seq_no + 1)) ack = true;
            }
        }

        // while(!isFinished) {
        //
        //     // send and receive for every package
        //     // resend if did not get acknoledgement
        //
        //     while(!ack) {
        //         recieved_message = recieve();
        //         if(recieved_message != null) {
        //             int ack_no = Integer.parseInt(recieved_message.split("\\|")[1]);
        //             // test if the ack's sequence number match
        //             if(ack_no == (seq_no + 1)) {
        //                 ack = true;
        //                 if(info[0].equals("end")) isFinished = true;
        //             }
        //         }
        //     }
        //     ack = false;
        //     // generate the package and get the package info for next package
        //     message = packs.generatePackage(seq_no + 1);
        //     info = packs.splitPackage(message);
        //     seq_no = Integer.parseInt(info[1]);
        // }
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
        } catch(IOException e) {
            System.out.println(e.toString());
            return null;
        }
    }

    // send a package of data
    private void send(String message) {
        DatagramPacket dp_send = new DatagramPacket(message.getBytes(), message.length(), this.dest, this.port);
        try{
            UDPsocket.send(dp_send);
        } catch(IOException e) {
            System.out.println(e.toString());
        }
    }

    public class Timeout extends TimerTask{
        public void run(){
            try{
                s.acquire();
                nextSeqNum = base;
                s.release();
            } catch(InterruptedException e){
              e.printStackTrace();
            }
        }
    }

}
