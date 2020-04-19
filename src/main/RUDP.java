package src.main;

import java.io.IOException;

import java.net.SocketException;
import java.time.Duration;
import java.time.Instant;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RUDP {
    private static boolean debug = false;
    private static long TIMEOUT = 500;
    private InetSocketAddress dest;
    private static DatagramSocket clientSocket;
    private static DatagramSocket serverSocket;
    private static Package packs;

    private static AtomicInteger curr = new AtomicInteger();
    private static int windowSize = 5;
    private static AtomicInteger windowOffset = new AtomicInteger();
    private static AtomicBoolean isComplete = new AtomicBoolean(false);
    private static Instant timer = null;
    private static AtomicBoolean droppedPkg = new AtomicBoolean(false);

    public RUDP(boolean d, String dest, int port, String file_name, int ack) throws SocketException, IOException {
        debug = d;
        this.dest = new InetSocketAddress(InetAddress.getByName(dest), port);
        clientSocket = new DatagramSocket();
        serverSocket = new DatagramSocket(ack);
        // chucks up input file
        packs = new Package(file_name);
    }
    
    public void start() throws SocketException, IOException {
        System.out.println("Bidirectional transfer...");
        
        // TODO initial setup of the variable RTT
        timer = Instant.now();
        bidirectTransfer();
    }

    /**
     * Main functionality of bi-directional transmission. Spawning two threads from the main thread.
     */
    public void bidirectTransfer() {
        if (debug) {
            System.out.println("Then lenght of file is: " + packs.getLength());
        }
        Thread sender = new Thread(new Runnable(){
        
            @Override
            public void run() {
                try{
                    while(!isComplete.get()) {
                        if(droppedPkg.get() || Duration.between(timer, Instant.now()).toMillis() >= TIMEOUT) {
                            // Resend dropped pkg
                            String msg = packs.generatePackageById(windowOffset.get());
                            if(debug) System.out.println("Sending potential dropped package sequence: " + Util.splitPackage(msg)[1]);
                            byte[] buffer = msg.getBytes();
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, dest);
                            clientSocket.send(packet);
                            timer = Instant.now();
                            droppedPkg.set(false);
                        }
                        if(curr.get() < Math.min(windowOffset.get() + windowSize, packs.getLength())) {
                            if(curr.get() == windowOffset.get()) timer = Instant.now();
                            String msg = packs.generatePackageById(curr.get());
                            if(debug) System.out.println("Sending package sequence: " + Util.splitPackage(msg)[1]);
                            byte[] buffer = msg.getBytes();
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, dest);
                            clientSocket.send(packet);
                            curr.getAndIncrement();
                        }
                        Thread.sleep(15);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    clientSocket.close();
                }
            }
        });


        Thread receiver = new Thread(new Runnable(){
        
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(buffer, 1024);
                
                try {
                    while(!isComplete.get()) {
                        serverSocket.receive(receivePacket);
                        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        if (debug) System.out.println("Receiving package: " + message);
                        if(!Util.validChecksum(message)) {
                            if(debug) System.out.println("Checksum failed...");
                            continue;
                        }
                        int ackNum = Integer.parseInt(Util.splitPackage(message)[1]);
                        if(debug) System.out.println("Receiving ack sequence number: " + ackNum);
                        // Duplicate ack
                        if(ackNum < windowOffset.get()) continue;
                        // Ignore not 'ACK' type message
                        if(!Util.splitPackage(message)[0].equals("ack")) continue;

                        // TODO buffer received packages
                        // Receive ack of the end package from receiver side
                        if(ackNum > packs.getLength() - 1) {
                            isComplete.set(true);
                            break;
                        }
                        if(ackNum >= curr.get()) {
                            windowOffset.set(ackNum);
                            curr.set(ackNum);
                        } else {
                            windowOffset.set(ackNum);
                            droppedPkg.set(true);
                        }
                        
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    serverSocket.close();
                }
            }
        });

        sender.start();
        receiver.start();

        if(!sender.isAlive() && !receiver.isAlive()) {
            clientSocket.close();
            serverSocket.close();
            System.out.println("File has been transfered...");
        }
    }

}