/**
 * A SynMessage is a string formatted as follows:
 *      SYN;senderID;peerID;sequence#;
 * 
 * where:
 *    - senderID is a string of up to 16 characters, containing the characters 
 *      A-Z a-z 0-9 (only), and which represents the symbolic name (as in: 
 *      JuansFileServer) of the sending machine.
 *    - peerID is the senderID of a peer, to which this message is addressed
 *    - sequence# is the sequence# received in a HELLO message generated from 
 *      the peer.
 */

public class SynMessage
{
    final private String senderID;
    final private String peerID;
    final private int sequenceNo;
    
    /**
     * Constructor1 -
     *      takes a string formatted as above, and populates the attributes of 
     *      the SynMessage object accordingly.
     */
    public SynMessage(String s)
    {
        String e;
        
        String[] tokens = s.split(";");
        if (tokens.length < 4)
        {
            e = "wrong format";
            throw new IllegalArgumentException(e);
        }

        if (!tokens[0].equals("SYN"))
        {
            e = "should start with SYN";
            throw new IllegalArgumentException(e);
        }

        this.senderID = tokens[1];

        if (!this.senderID.matches("\\w+") || this.senderID.length() > 16)
        {
            e = "should be a word (senderID=" + tokens[1] +")";
            throw new IllegalArgumentException(e);
        }

        this.peerID = tokens[2];
        try
        {
            this.sequenceNo = Integer.parseInt(tokens[3]);
        }
        catch (NumberFormatException ne)
        {
            throw new IllegalArgumentException(ne);
        }
    }

    /**
     * Constructor2 -
     *      creates a SynMessage object with peerID already in PeerTable
     */
    public SynMessage(String senderID, String peerID, int sequenceNo)
    {
        String e;
        
        if (!senderID.matches("\\w+") || senderID.length() > 16)
        {
            e = "should be a word (senderID=" + senderID +")";
            throw new IllegalArgumentException(e);
        }

        if (!Info.inPeers(peerID))
        {
            e = "should be a peer in PeerTable (peerID=" + peerID + ")";
            throw new IllegalArgumentException(e);
        }
        
        this.senderID   = senderID;
        this.peerID     = peerID;
        this.sequenceNo = sequenceNo;
    }

    /** 
     * getSynMessageAsEncodedString
     * 
     * @return  a string of the format indicated above, encoding the attributes 
     *          of the SynMessage object, all ready to be sent out over the 
     *          network.
     */
    public String getSynMessageAsEncodedString()
    {
        String res = "SYN;";
        res += this.senderID   + ";";
        res += this.peerID     + ";";
        res += this.sequenceNo + ";";
        return res;
    }

    /**
     * toString -
     *      provide a nice, human-readable, print-out of the object contents.
     */
    public String toString()
    {
        String res = this.getSynMessageAsEncodedString() + "\n\n";
        res += "\tsenderID      = " + this.senderID      + "\n";
        res += "\tpeerID        = " + this.peerID        + "\n";
        res += "\tsequenceNo    = " + this.sequenceNo    + "\n";
        return res;
    }

    /**
     * The three following get methods are needed while receiving a SYN.
     */
    public String getSenderID()
    {
        return this.senderID;
    }

    public String getPeerID()
    {
        return this.peerID;
    }

    public int getSequenceNo()
    {
        return this.sequenceNo;
    }

    /**
     * No need to set - final attributes are assigned in the constructor.
     */ 

    /**
     * Testing purpose -
     *      Please comment out the "Info.inPeers(peerID)" constraint.
     */
    /*
    private boolean equals(SynMessage hm)
    {
        if (!this.senderID.equals(hm.getSenderID()))
            return false;
        if (!this.peerID.equals(hm.getPeerID()))
            return false;
        if (this.sequenceNo != hm.getSequenceNo())
            return false;
        return true;
    }

    public static void main(String[] args) 
    {
        SynMessage hm1 = new SynMessage("SYN;TzuyiDell;TzuyiHP;0;");
        SynMessage hm11 = new SynMessage(hm1.getSynMessageAsEncodedString());

        if (hm1.equals(hm11))
            System.out.println("OK");
        else
            System.out.println("Not OK");

        SynMessage hm2 = new SynMessage("TzuyiMac", "TzuyiDell", 253);
        SynMessage hm22 = new SynMessage(hm2.getSynMessageAsEncodedString());

        if (hm2.equals(hm22))
            System.out.println("OK");
        else
            System.out.println("Not OK");
    }
    */
}