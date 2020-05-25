# Reliable UDP

This is an implementation of reliable transport protocol with CLI using UDP datagrams, sliding window and seq-ack mechanism.

## Quick Start

### Docker Image
- Pull down [docker image](https://hub.docker.com/repository/docker/shari666/rudp). The default entrypoint is `sh`, then start Sender by specifying the file you want to transfer. (use `-v` for simplicity).

### Clone Repo
0. Clone this repo to your working directory.
1. Start sender by running `make run file=<filename> port=<receiver-listening-port> address=<receiver-address> -k <ackPort>`, `file`, `port`, `address` and `-k` default to `README`, `33122`, `localhost` and `4567`. (note: start receiver side using python2)
2. If you want to test the program under various network conditions, run `make test` instead (skip step 2). That would show you the detailed log information.

## Usage
```text
Usage of Sender
-f FILE | --file=FILE The file to transfer; (cannot be empty)
-p PORT | --port=PORT The destination port, defaults to 33122
-a ADDRESS | --address=ADDRESS The receiver address or hostname, defaults to localhost
-d | --debug Print debug messages
-h | --help Print this usage message
-k PORT | --ack=PORT Port for listening ack packages, defaults to 4567
```

# Description
```bash
.
├── BasicSender.py
├── Checksum.py
├── Dockerfile
├── InteractiveSender.py
├── JavaSender.py
├── Makefile              # automate build and test process
├── Receiver.py           # receiver source code
├── Sender.py
├── StanfurdSender.py
├── TestHarness.py
├── UnreliableSender.py
├── docker-compose.yml
├── grade.md
├── src                   # sender source code
│   ├─main
│      ├── Package.java   # read file and format message
│      ├── RUDP.java      # reliable UDP implementation
│      ├── Sender.java    # top layer, evoke rudp
│      └── Util.java      # static utility funcs
│   
└── tests                 # test suite
```
## Basic Implementation

Packages have three type: `start, data, ack, end`. The package is formatted as: `msgType|seqNum|data|Checksum` and `ack|nextSeqNum|Checksum`. The data has a maximum size of 1400 bytes. Checksum is calculated by using **CRC32** library. Sender and receiver use GO-BACK-N method. The window size is 5 and the timeout is 500ms.

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