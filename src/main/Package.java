package src.main;
import java.util.Random;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.zip.CRC32;

/** This class defines the all the packages needed for a transmission, including start, data, end. */

class Package {
    private FileInputStream file;
    private int offset; // offset of sequence number
    private int dataSize = 1000;
    private String[] messageType = new String[]{"start", "data", "end", "ack"};

    /** class constructor
     * @param path  the path of the file
     */
    public Package(String path) throws FileNotFoundException, IOException {
        offset = new Random().nextInt(10);
        // Read file into a input stream
        this.file = new FileInputStream(new File(path));
    }

    /** A function to gererate a package
     * @param  seqNo  the seqNo of package will be generated
     * @return package  return the package formatting as <messageType>|<sequenceNo>|<data>|<checksum>
     */
    public String generatePackage(int seqNo) throws IOException {
        String msgType, checkSum;
        String msg = "";
        byte[] body = new byte[dataSize];
        int len = 0;

        if(seqNo != this.offset) {
            len = file.read(body);
            //Empty if start or end message
            msg = len < 0 ? "" : new String(body);
        }

        if(seqNo == this.offset) {
            msgType = messageType[0];
        } else if(len < 0) {
            msgType = messageType[2];
        } else msgType = messageType[1];;

        checkSum = Util.generateChecksum(msgType + "|" + String.valueOf(seqNo) + "|" + msg + "|");
        return msgType + "|" + String.valueOf(seqNo) + "|" + msg + "|" + checkSum;
    }

    /** Splits a packet. 
     * For packets without a data field, the data element will be the empty string.
     * @param pack   the input full
     * @return info  an array of the form (msg_type, seqno, data, checksum)
     */
    public static String[] splitPackage(String pack) {
        String[] info = pack.split("\\|");
        return info;
    }

    public int get_offset() {
        return offset;
    }

}
