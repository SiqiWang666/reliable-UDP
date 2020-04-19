# Reliable UDP

This is an implementation of reliable transport protocol with CLI using UDP datagrams, sliding window and seq-ack mechanism.

## Team Members

Mingyu Ma, Siqi Wang

## Quick Start

0. Clone this repo to your working directory.
1. Start sender by running `make run file=<filename> port=<receiver-listening-port> address=<receiver-address>`, `file`, `port` and `address` have default value of `README`, `33122` and `localhost`. (note: start receiver side using python2)
2. If you want to test the program under various network conditions, run `make test` instead (skip step 2). That would show you the test result.

# Description
```bash
  .
  |-Receiver.py            # receiver source code
  |-tests                  # test cases
  |-Makefile               # automate build and test process
  |-src                    # sender source code
     |-Package.java        # read file and format message
     |-Sender.java         # evoke rudp
     |-RUDP.java           # reliable UDP implementation
     |_Util.java           # static utility funcs
```
## Basic Implementation

Packages have three type: `start, data, ack, end`. The package is formatted as: `msgType|seqNum|data|Checksum` and `ack|nextSeqNum|Checksum`. The data has a maximum size of 1400 bytes. Checksum is calculated by using CRC32 library. Sender and receiver use GO-BACK-N method. The window size is 5 and the timeout is 500ms.

## Extra Credit (Bi-directional Transfer)

The sender achieves bi-directional transfer by using two threads. One is responsible for sending packages and the other is responsible for receiving ack packages.

# Challenges

- The output file in receriver side have extra whitespaces in the last line.

**Solution**: The last data package is a special case because the size might be smaller than the package's data size. So, after reading from the file, we need to extract the valid part.

- How to design a multithreaded `Sender` and how to solve the synchronization of shared variable?

**Solution**: We use two thread, `sender` and `receiver`, defined in `start()` method. For the shared variable, we choose to use Atomic class for minimum synchronization.

- We are doing repetitive resending the whole slots inside the window when the window offset moves and time is out.

**Solution**: Once the time is out or the ack next sequence num is smaller than `curr` pointer, we selectively resend the potential dropped package instead of the entire window.

- When the transimission is completed the sockets are not closed (wasting resource) ?

**Solution**: The solution is to add `socket.close()` to the `finally` block. So no matter there is an exception raised or the transmission is completed, `socket.close()` will be executed.