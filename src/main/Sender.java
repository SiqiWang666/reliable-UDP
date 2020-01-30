public class Sender {
    public static void main(String[] argvs) {
        // first check for any command line arguments
        // not sure if this is a good way to implement it
        if(argvs.length != 0) {
            int port = 0;
            int timeout = 0;
            boolean debug = false;
            for(int i = 0; i < argvs.length; i ++) {
                if(argvs[i].equals("-p")) {
                    port = Integer.parseInt(argvs[i + 1]);
                } else if(argvs[i].indexOf("=") != -1 && argvs[i].substring(0, argvs[i].indexOf("=")).equals("--port")) {
                    port = Integer.parseInt(argvs[i].substring(argvs[i].indexOf("=") + 1));
                } else if(argvs[i].equals("-t")) {
                    timeout = Integer.parseInt(argvs[i + 1]);
                } else if(argvs[i].indexOf("=") != -1 && argvs[i].substring(0, argvs[i].indexOf("=")).equals("--timeout")) {
                    timeout = Integer.parseInt(argvs[i].substring(argvs[i].indexOf("=") + 1));
                } else if(argvs[i].equals("-d")) {
                    debug = true;
                }  else if(argvs[i].indexOf("=") != -1 && argvs[i].substring(0, argvs[i].indexOf("=")).equals("--debug")) {
                    debug = true;
                } else {
                    usage();
                }
            }
        }
    }

    private static void usage() {
        System.out.println("BEARS-TP Receiver\n-p PORT | --port=PORT The listen port, defaults to 33122\n-t TIMEOUT | --timeout=TIMEOUT Receiver timeout in seconds\n-d | --debug Print debug messages\n-h | --help Print this usage message");
    }
}
