package src.main;

import java.util.zip.CRC32;

class Util {
    /**
     * Message MUST end with a trailing '|' character.
     * @param message
     * @return checksum
     */
    public static String generateChecksum(String message) {
        CRC32 crc = new CRC32();
        crc.update(message.getBytes());
        return String.valueOf(crc.getValue());
    }
    /**
     * Message MUST end with a trailing '|' character.
     * @param message
     * @return isValid  is a valid package
     */
    public static boolean validChecksum(String message) {
        String[] decoded = Util.splitPackage(message);
        String reportedChecksum = generateChecksum(new String(decoded[0] + "|" + decoded[1] + "|"));
        return reportedChecksum.equals(decoded[2]);
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
}
