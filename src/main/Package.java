package src.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;


/** This class defines the all the packages needed for a transmission, including start, data, end. */

class Package {
    private int offset; // offset of sequence number
    private int dataSize = 1000;
    private String[] messageType = new String[]{"start", "data", "end", "ack"};
    public ArrayList<String> packages = new ArrayList<>();

    /**
     * class constructor
     * 
     * @param path the path of the file
     * @throws IOException
     */
    public Package(String path) throws IOException {
        this.offset = 0;
        // Read file into a list of well formatted message
        readIntoArray(path);
    }

    /** 
     * Reads the input file into a list of formatted message: <messageType>|<sequenceNo>|<data>|<checksum>
     *  
     * @param path the path of the file
     * @throws IOException 
     */ 

    private void readIntoArray(String path) throws IOException {
        FileInputStream input = new FileInputStream(new File(path));
        int seqNum = offset;
        boolean isEnd = false;
        while(!isEnd) {
            byte[] body = new byte[dataSize];
            int len = input.read(body);
            String msgType, msg, checkSum;
            if(len < dataSize && len > 0) {
                msgType = messageType[2];
                isEnd = true;
            } else {
                // Modify the last package's message type.
                if(len < 0) {
                    String endMsg = packages.get(packages.size() - 1);
                    String[] info = Util.splitPackage(endMsg);
                    info[0] = messageType[2];
                    packages.set(packages.size() - 1, Arrays.toString(info));
                    break;
                }
                msgType = packages.isEmpty() ? messageType[0] : messageType[1];
            }
            
            msg = new String(body, 0, len);
            checkSum = Util.generateChecksum(msgType + "|" + seqNum + "|" + msg + "|");
            packages.add(new String(msgType + "|" + seqNum + "|" + msg + "|" + checkSum));
            seqNum++;
        }
        input.close();
        System.out.println("Packages have been successfully generated...");
    }

    /** Get the well formatted message of the package by index
     * @param index   the index of the package
     * @return the formatted message
     */
    public String generatePackageById(int index) {
        if(index < 0 || index > packages.size() - 1) return null;
        return packages.get(index);
    }

    public int get_offset() {
        return offset;
    }

    public int getLength() {
        return packages.size();
    }

    /** DEPRECATED!!
     * A function to gererate a package
     * @param  seqNo  the seqNo of package will be generated
     * @return package  return the package formatting as <messageType>|<sequenceNo>|<data>|<checksum>
     */
    // public String generatePackage(int seqNo) throws IOException {
    //     String msgType, checkSum;
    //     String msg = "";
    //     byte[] body = new byte[dataSize];
    //     int len = 0;

    //     // if(seqNo != this.offset) {
    //     //     len = file.read(body);
    //     //     //Empty if start or end message
    //     //     if(len < dataSize && len != -1) {
    //     //         byte[] body_last = new byte[len];
    //     //         for(int i = 0; i < len; i ++) body_last[i] = body[i];
    //     //         msg = new String(body_last);
    //     //     } else msg = len < 0 ? "" : new String(body);
    //     // }

    //     // len = file.read(body);
    //     // if(len < dataSize && len != -1) {
    //     //     byte[] body_last = new byte[len];
    //     //     for(int i = 0; i < len; i ++) body_last[i] = body[i];
    //     //     msg = new String(body_last);
    //     // } else msg = new String(body);

    //     // if(seqNo == this.offset) {
    //     //     msgType = messageType[0];
    //     // } else if(len < dataSize) {
    //     //     msgType = messageType[2];
    //     // } else msgType = messageType[1];;

    //     // checkSum = Util.generateChecksum(msgType + "|" + String.valueOf(seqNo) + "|" + msg + "|");
    //     // return msgType + "|" + String.valueOf(seqNo) + "|" + msg + "|" + checkSum;
    //     return "";
    // }
}
