public class OutThread implements Runnable {
      private int window_size;
      private Datagram dp_send;

      /**
        * constructor of inner class, this class handles the sending of the datas
        * @param window_size is the size of the window designed by the user
        */
      public OutThread(int window_size) {
          this.window_size = window_size;
      }

      //this function might be added into the outer class
      public void generateOutPackages() {
          packetsList.clear();
          for(int i = 0; i < window_size; i ++) packetsList.add(packs.generatePackage(nextSeqNum + i));
      }

      public void run() {
          Boolean isEndPackage = false;
          String message;

          try {
              // while there are still data to be transfered or to be recieved
              while(!isTransferComplete) {

                  if(nextSeqNum == base) {
                      generateOutPackages();
                  }

                  // send the packages in the packetList array
                  if(nextSeqNum < base + window_size) {

                      // gain control since we are start sending
                      s.acquire();
                      // set timer if this is the first package in the list
                      if(base == nextSeqNum) setTimer(true);

                      // always get the first package
                      message = packetsList.get(nextSeqNum - offset);
                      // send the package
                      if(message.split("\\|")[0].equals("end")) isEndPackage = true;
                      dp_send = new DatagramPacket(message.getBytes(), message.length(), dest, port);
                      UDPsocket.send(dp_send);

                      // increment the counter
                      if(!isEndPackage) nextSeqNum ++;
                      // leave cs
                      s.release();
                  }
              } catch (IOException e) {
                  System.out.println(e.toString());
              } finally {
                  setTimer(false);
              }
          }
      }
}
