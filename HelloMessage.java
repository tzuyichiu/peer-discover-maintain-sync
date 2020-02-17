/**
 * A HelloMessage is a string formatted as follows:
 *      HELLO;senderID;sequence#;HelloInterval;NumPeers;peer1;peer2;...;peerN
 * 
 * where:
 *    - senderID is a string of up to 16 characters, containing the characters 
 *      A-Z a-z 0-9 (only), and which represents the symbolic name (as in: 
 *      JuansFileServer) of the sending machine.
 *    - sequence#  is an integer. Note that this is not a message sequence 
 *      number, but will be used for indicating the “state evolution” of the 
 *      sender, in future TDs.
 *    - HelloInterval is an integer [0;255], which represents the maximum time 
 *      (in seconds) between two successive HELLO emissions from the sender.
 *    - NumPeers is an integer [0;255] which indicates the number of peers 
 *      following.
 *    - peer? is the senderID of a peer, which has been heard and which is not 
 *      expired.
 */

public class HelloMessage
{
    final private String senderID;
    final private int sequenceNo;
    final private int helloInterval;
    private int numPeers;
    private String[] peers = new String[255];
    
    /**
     * Constructor1 -
     *      takes a string formatted as above, and populates the attributes of 
     *      the HelloMessage object accordingly.
     */
    public HelloMessage(String s)
    {
        String e;
        
        String[] tokens = s.split(";");
        if (tokens.length < 5)
        {
            e = "wrong format";
            throw new IllegalArgumentException(e);
        }

        if (!tokens[0].equals("HELLO"))
        {
            e = "should start with HELLO";
            throw new IllegalArgumentException(e);
        }

        this.senderID = tokens[1];

        if (!this.senderID.matches("\\w+") || this.senderID.length() > 16)
        {
            e = "should be a word (senderID=" + tokens[1] +")";
            throw new IllegalArgumentException(e);
        }

        try
        {
            this.sequenceNo = Integer.parseInt(tokens[2]);
            this.helloInterval = Integer.parseInt(tokens[3]);
            this.numPeers = Integer.parseInt(tokens[4]);
        }
        catch (NumberFormatException ne)
        {
            throw new IllegalArgumentException(ne);
        }

        if (this.helloInterval > 255 || this.helloInterval < 0)
        {
            e = "should be in [0;255] (HelloInterval=" + tokens[3] +")";
            throw new IllegalArgumentException(e);
        }
        
        if (this.numPeers > 255 || this.numPeers < 0)
        {
            e = "should be in [0;255] (NumPeers=" + tokens[4] +")";
            throw new IllegalArgumentException(e);
        }

        if (tokens.length != 5 + this.numPeers)
        {
            e = "wrong number of peers";
            throw new IllegalArgumentException(e);
        }
        
        for (int i=0; i<this.numPeers; i++)
            this.peers[i] = tokens[5+i];

        /*
        We don't verify the format of each peer's senderID because
        it would have already been verified when the sender's HELLO message
        was constructed.
        */
    }

    /**
     * Constructor2 -
     *      creates a HelloMessage object which has (at the time of creation) 
     *      no peers associated.
     */
    public HelloMessage(String senderID, int sequenceNo, int helloInterval)
    {
        String e;
        
        if (!senderID.matches("\\w+") || senderID.length() > 16)
        {
            e = "should be a word (senderID=" + senderID +")";
            throw new IllegalArgumentException(e);
        }
        
        this.senderID = senderID;
        this.sequenceNo = sequenceNo;

        if (helloInterval > 255 || helloInterval < 0)
        {
            e = "should be in [0;255] (helloInterval=" + helloInterval +")";
            throw new IllegalArgumentException(e);
        }

        this.helloInterval = helloInterval;
        this.numPeers = 0;
    }

    /** 
     * getHelloMessageAsEncodedString
     * 
     * @return  a string of the format indicated above, encoding the attributes 
     *          of the HelloMessage object, all ready to be sent out over the 
     *          network.
     */
    public String getHelloMessageAsEncodedString()
    {
        String res = "HELLO;";
        res += this.senderID      + ";";
        res += this.sequenceNo    + ";";
        res += this.helloInterval + ";";
        res += this.numPeers;
        for (int i=0; i<this.numPeers; i++)
            res += ";" + this.peers[i];
        return res;
    }

    /**
     * addPeer - add a peer to the HelloMessage object.
     */
    public void addPeer(String peerID)
    {
        if (this.numPeers == 255)
        {
            String e = "addPeer: too many peers (>255)";
            throw new IllegalArgumentException(e);
        }

        this.peers[this.numPeers] = peerID;
        this.numPeers++;
    }

    /**
     * toString -
     *      provide a nice, human-readable, print-out of the object contents.
     */
    public String toString()
    {
        String res = this.getHelloMessageAsEncodedString() + "\n\n";
        res += "\tsenderID      = " + this.senderID      + "\n";
        res += "\tsequenceNo    = " + this.sequenceNo    + "\n";
        res += "\tHelloInterval = " + this.helloInterval + "\n";
        res += "\tNumPeers      = " + this.numPeers      + "\n";
        for (int i=1; i<=this.numPeers; i++)
        {
            if (i <= 9)
                res += "\tpeer" + i + "         = " + this.peers[i-1] + "\n";
            else if (i <= 99)
                res += "\tpeer" + i + "        = "  + this.peers[i-1] + "\n";
            else
                res += "\tpeer" + i + "       = "   + this.peers[i-1] + "\n";
        }
        return res;
    }

    /**
     * The three following get methods are needed while receiving a HELLO.
     */
    public String getSenderID()
    {
        return this.senderID;
    }

    public int getSequenceNo()
    {
        return this.sequenceNo;
    }
    
    public int getHelloInterval()
    {
        return this.helloInterval;
    }

    /**
     * No need to set: 
     *  - final attributes are assigned in the constructor.
     *  - NumPeers and peers will be updated with addPeer.
     */ 

    /**
     * inPeers -
     *      the only method we need to examine peers.
     *
     * @param receiverID the ID of the receiver.
     * @return whether receiverID is listed among the peers.
     */
    public boolean inPeers(String receiverID)
    {
        for (int i=0; i<this.numPeers; i++)
        {
            if (this.peers[i].equals(receiverID))
                return true;
        }
        return false;
    }

    /**
     * Testing purpose.
     */
    /*
    private boolean equals(HelloMessage hm)
    {
        if (!this.senderID.equals(hm.senderID))
            return false;
        if (this.sequenceNo != hm.sequenceNo)
            return false;
        if (this.helloInterval != hm.helloInterval)
            return false;
        if (this.numPeers != hm.numPeers)
            return false;
        for (int i=0; i<this.numPeers; i++)
        {
            if (!this.peers[i].equals(hm.peers[i]))
                return false;
        }
        return true;
    }

    public static void main(String[] args) 
    {
        HelloMessage hm1 = new HelloMessage("HELLO;tzuyi9;1;2;0");
        for (int i=0; i<255; i++)
            hm1.addPeer("hey");

        HelloMessage hm11 = new HelloMessage(hm1.getHelloMessageAsEncodedString());

        if (hm1.equals(hm11))
            System.out.println("OK");
        else
            System.out.println("Not OK");

        HelloMessage hm2 = new HelloMessage("tzuyi10", 42, 253);
        for (int i=0; i<100; i++)
            hm2.addPeer("hey");

        HelloMessage hm22 = new HelloMessage(hm2.getHelloMessageAsEncodedString());

        if (hm2.equals(hm22))
            System.out.println("OK");
        else
            System.out.println("Not OK");
    }
    */
}