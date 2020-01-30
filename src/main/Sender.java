public class Sender {
    public static void main(String[] argvs) {
        // first check for any command line arguments
        // not sure if this is a good way to implement it
        if(argvs.length != 0) {
            int port = 33122;
            String address = "localhost";
            boolean debug = false;
            String file_name = null;
            for(int i = 0; i < argvs.length; i ++) {
                if(argvs[i].equals("-p")) {
                    port = Integer.parseInt(argvs[i + 1]);
                } else if(argvs[i].indexOf("=") != -1 && argvs[i].substring(0, argvs[i].indexOf("=")).equals("--port")) {
                    port = Integer.parseInt(argvs[i].substring(argvs[i].indexOf("=") + 1));
                } else if(argvs[i].equals("-a")) {
                    address = argvs[i + 1];
                } else if(argvs[i].indexOf("=") != -1 && argvs[i].substring(0, argvs[i].indexOf("=")).equals("--address")) {
                    address = argvs[i].substring(argvs[i].indexOf("=") + 1);
                } else if(argvs[i].equals("-d")) {
                    debug = true;
                } else if(argvs[i].indexOf("=") != -1 && argvs[i].substring(0, argvs[i].indexOf("=")).equals("--debug")) {
                    debug = true;
                } else if(argvs[i].equals("-f")) {
                    file_name = argvs[i + 1];
                } else if(argvs[i].indexOf("=") != -1 && argvs[i].substring(0, argvs[i].indexOf("=")).equals("--file")) {
                    file_name = argvs[i].substring(argvs[i].indexOf("=") + 1);
                } else {
                    usage();
                }
            }
        }
        
    }

    private static void usage() {
        System.out.println("BEARS-TP Sender\n-f FILE | --file=FILE The file to transfer; if empty reads from STDIN\n-p PORT | --port=PORT The destination port, defaults to 33122\n-a ADDRESS | --address=ADDRESS The receiver address or hostname, defaults to localhost\n-d | --debug Print debug messages\n-h | --help Print this usage message");
    }
}
