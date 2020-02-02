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
import java.util.concurrent.Semaphore;


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

    private Boolean isEndPackage = false;
    private int totalSequenceNo;

    public RUDP(Boolean debug, String dest, int port, String file_name) throws SocketException, IOException {
        this.debug = debug;
        this.port = port;
        this.UDPsocket = new DatagramSocket(8000);
        this.dest = dest == "localhost" ? InetAddress.getLocalHost() : InetAddress.getByName(dest);
        // DatagramPacket dp_send, dp_receive;
        //this.UDPsocket.setSoTimeout(TIMEOUT);

        // create the package class instance
        this.packs = new Package(file_name);
        this.base = this.packs.get_offset();
        this.offset = this.packs.get_offset();
        this.totalSequenceNo = this.offset + 1;
        this.nextSeqNum = this.base;
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
            send(message);
            recieved_message = recieve();
            if(recieved_message != null) {
                int ack_no = Integer.parseInt(recieved_message.split("\\|")[1]);
                // test if the ack's sequence number match
                if(ack_no == (seq_no + 1)) ack = true;
            }
        }
        this.base += 1;
        this.nextSeqNum += 1;

        OutThread out = new OutThread(5);
        new Thread(out).start();
        ReceiveThread receive = new ReceiveThread();
        receive.start();

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

    public class OutThread implements Runnable {
          private int window_size;
          private DatagramPacket dp_send;
          /**
            * constructor of inner class, this class handles the sending of the datas
            * @param window_size is the size of the window designed by the user
            */
          public OutThread(int window_size) {
              this.window_size = window_size;
              packetsList = new LinkedList<String>();
          }
          //this function might be added into the outer class
          public void generateOutPackages() {
              packetsList.clear();
              String next_message;
              try{
                  for(int i = 0; i < window_size; i ++) {
                      next_message = packs.generatePackage(totalSequenceNo);

                      totalSequenceNo += 1;
                      packetsList.add(next_message);
                      if(packs.splitPackage(next_message)[0].equals("end")) {
                          break;
                      }
                  }
              } catch(IOException e) {
                  e.printStackTrace();
                  System.out.println(e.toString());
              }
          }

          public void run() {

              String message;

              try {
                  // while there are still data to be transfered or to be recieved
                  while(!isTransferComplete) {
                      // try {
                      //     Thread.sleep(5);
                      // } catch(InterruptedException interrupt_e) {
                      //     interrupt_e.printStackTrace();
                      // }
                      // System.out.println(nextSeqNum + " " + base + " " + totalSequenceNo + " " + nextSeqNum);
                      if(totalSequenceNo == base && nextSeqNum == base) {
                          generateOutPackages();
                      }

                      // send the packages in the packetList array
                      if(nextSeqNum < base + window_size) {
                          // gain control since we are start sending
                          try {
                              s.acquire();
                              // set timer if this is the first package in the list
                              if(base == nextSeqNum) setTimer(true);
                              // get the next package
                              if(nextSeqNum - base < 0) break;
                              message = packetsList.get(nextSeqNum - base);
                              // send the package
                              if(message.split("\\|")[0].equals("end")) isEndPackage = true;
                              dp_send = new DatagramPacket(message.getBytes(), message.length(), dest, port);
                              UDPsocket.send(dp_send);
                              // increment the counter
                              if(!isEndPackage) nextSeqNum ++;
                              // leave cs
                              s.release();
                          } catch(InterruptedException interrupt_e) {
                              interrupt_e.printStackTrace();
                              System.out.println(interrupt_e.toString());
                          }
                      } else {
                          try {
                              Thread.sleep(5);
                          } catch(InterruptedException interrupt_e) {
                              interrupt_e.printStackTrace();
                          }
                      }
                  }
              } catch (IOException e) {
                  e.printStackTrace();
                  System.out.println(e.toString());
              } finally {
                  setTimer(false);
              }
          }
    }


    public class ReceiveThread extends Thread {
        // private DatagramSocket receiveSocket;
        // // Receive thread constructor
        // public ReceiveThread(DatagramSocket socket) {
        //     this.receiveSocket = socket;
        // }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(buffer, 1024);
            String message;
            try {
                while(!isTransferComplete) {
                    UDPsocket.receive(receivePacket);

                    // check if the package is valid
                    message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    if(!Util.validChecksum(message)) continue;
                    int ackNum = Integer.parseInt(Package.splitPackage(message)[1]);

                    // end ackNum
                    if(isEndPackage && ackNum == totalSequenceNo) {
                        isTransferComplete = true;
                    }
                    else if(ackNum < base) { // duplicate ackNum
                        s.acquire();
                        setTimer(false);
                        nextSeqNum = base;
                        s.release();
                    } else {
                        // normal ackNum
                        s.acquire();
                        base = ackNum;
                        if(base == nextSeqNum) setTimer(false);
                        else setTimer(true);
                        s.release();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.toString());
            } finally {
                // UDPsocket.close();
            }

        }
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
