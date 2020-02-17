# Peer discovery & maintenance & synchronization

# Overview

This is an individual work done in Ecole Polytechnique, in the course 
INF557: From the Internet to the IoT. It implements an distributed application
using algorithms and patterns of routing protocols which:
- locally discover and maintain a peer set by way of periodical signaling and 
externally triggered events
- create a consistent distributed database

This application is supposed to be able to interoperate on different machines 
(i.e. different IP addresses) on the same local link with the same protocol
specifications.

# Compilation

Simply compile with `javac *.java` and launch `java Main`. The program creates 
a broadcast socket on port 4242 in the local network and launches all the 
handlers: HelloSender, HelloReceiver, SynReceiver, ListReceiver, DubugReceiver.
We update our database every 5 seconds.

When testing with other machines, don't forget to change the ID (myID) inside 
**Info**, whose data must correspond to the machine launching the program 
itself.

# Message handlers

There are three kinds of message: *HelloMessage*, *SynMessage* and 
*ListMessage*. The formats are specified in the corresponding source files. 
HelloSender broadcasts *HelloMessage* periodically, while HelloReceiver 
identifies those sent by others and updates the PeerTable, situated inside the 
class **Info** (my database). Even if the peer already exists in PeerTable, it 
verifies if the peer is well synchronized by checking its sequence number
(version number).

Note that every peer has an expiration time and will be deleted from the 
database. According to situations, every identified peer has a corresponding
peerState, as illustrated by *stateMachine.png*.

*SynMessages* are sent by HelloReceiver to a peer whenever it detects its 
peerState becomes HEARD/INCONSISTENT, and it keeps sending the same SYN until 
the peer's sequence number (version number) is incremented. These messages are 
meant to request new database from the others. SynReceiver is thus implemented 
to generate *ListMessages* to transfer my database. For this purpose, my 
database is split into pieces of length shorter than 255 bytes and included 
into multiple *ListMessages*.

ListReceiver has to assemble all received LIST and check if all messages are 
well received. Once all parts are assembled, it increments the peer's sequece 
number, updates its database in PeerTable. The process of synchronization with 
this peer is thus finished and the peer's peerState becomes SYNCHRONIZED.