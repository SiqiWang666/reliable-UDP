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
        return Long.toHexString(crc.getValue());
    }
    /**
     * Message MUST end with a trailing '|' character.
     * @param message
     * @return isValid  is a valid package
     */
    public static boolean validChecksum(String message) {
        String[] decoded = Package.splitPackage(message);
        String reportedChecksum = generateChecksum(new String(decoded[0] + "|" + decoded[1] + "|"));
        return reportedChecksum.equals(decoded[2]);
    }
}
