/**
 * A ListMessage is a string formatted as follows:
 *      LIST;senderID;sequence#;TotalParts;part#;data;
 * 
 * where:
 *    - senderID is a string of up to 16 characters, containing the characters 
 *      A-Z a-z 0-9 (only), and which represents the symbolic name (as in: 
 *      JuansFileServer) of the sending machine.
 *    - peerID is the senderID of the peer, to which this message is addressed
 *    - sequence#  is an integer. Note that this is not a message sequence 
 *      number, but will be used for indicating the “state evolution” of the 
 *      sender, in future TDs.
 *    - TotalParts is an integer number which indicates how many LIST messages 
 *      will be generated, in order to send the entire database
 *    - part# indicates which, among the TotalParts messages, this message is.
 *    - data is a text string of max 255 characters, which contains (part of)
 *      the database being synchronised.
 */

public class ListMessage
{
    final private String senderID;
    final private String peerID;
    final private int    sequenceNo;
    final private int    totalParts;
    final private int    partNo;
    final private String data;
    
    /**
     * Constructor1 -
     *      takes a string formatted as above, and populates the attributes of 
     *      the ListMessage object accordingly.
     */
    public ListMessage(String s)
    {
        String e;
        
        String[] tokens = s.split(";");
        if (tokens.length < 7)
        {
            e = "wrong format";
            throw new IllegalArgumentException(e);
        }

        if (!tokens[0].equals("LIST"))
        {
            e = "should start with LIST";
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
            this.totalParts = Integer.parseInt(tokens[4]);
            this.partNo = Integer.parseInt(tokens[5]);
        }
        catch (NumberFormatException ne)
        {
            throw new IllegalArgumentException(ne);
        }
        this.data = tokens[6];

        if (this.data.length() > 255 || this.data.length() < 0)
        {
            e = "should be in [0;255] (data=" + tokens[6] +")";
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Constructor2 -
     *      creates a ListMessage object
     */
    public ListMessage(String senderID, String peerID, int sequenceNo, 
                        int totalParts, int partNo, String data)
    {
        String e;

        if (!senderID.matches("\\w+") || senderID.length() > 16)
        {
            e = "should be a word (senderID=" + senderID +")";
            throw new IllegalArgumentException(e);
        }
        
        this.senderID = senderID;
        this.peerID     = peerID;
        this.sequenceNo = sequenceNo;
        this.totalParts = totalParts;
        this.partNo     = partNo;

        if (data.length() > 255 || data.length() < 0)
        {
            e = "should be in [0;255] (data=" + data +")";
            throw new IllegalArgumentException(e);
        }

        this.data = data;
    }

    /** 
     * getListMessageAsEncodedString
     * 
     * @return  a string of the format indicated above, encoding the attributes 
     *          of the ListMessage object, all ready to be sent out over the 
     *          network.
     */
    public String getListMessageAsEncodedString()
    {
        String res = "LIST;";
        res += this.senderID   + ";";
        res += this.peerID     + ";";
        res += this.sequenceNo + ";";
        res += this.totalParts + ";";
        res += this.partNo     + ";";
        res += this.data       + ";";
        return res;
    }

    /**
     * toString -
     *      provide a nice, human-readable, print-out of the object contents.
     */
    public String toString()
    {
        String res = this.getListMessageAsEncodedString() + "\n\n";
        res += "\tsenderID      = " + this.senderID       + "\n";
        res += "\tpeerID        = " + this.peerID         + "\n";
        res += "\tsequenceNo    = " + this.sequenceNo     + "\n";
        res += "\ttotalParts    = " + this.totalParts     + "\n";
        res += "\tpartNo        = " + this.partNo         + "\n";
        res += "\tdata          = " + this.data           + "\n";
        return res;
    }

    /**
     * The six following get methods are needed while receiving a LIST.
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

    public int getTotalParts()
    {
        return this.totalParts;
    }
    
    public int getPartNo()
    {
        return this.partNo;
    }

    public String getData()
    {
        return this.data;
    }

    /**
     * No need to set - final attributes are assigned in the constructor.
     */ 

    /**
     * Testing purpose -
     *      Please comment out the "Info.inPeers(peerID)" constraint.
     */
    /*
    private boolean equals(ListMessage hm)
    {
        if (!this.senderID.equals(hm.getSenderID()))
            return false;
        if (!this.peerID.equals(hm.getPeerID()))
            return false;
        if (this.sequenceNo != hm.getSequenceNo())
            return false;
        if (this.totalParts != hm.getTotalParts())
            return false;
        if (this.partNo != hm.getPartNo())
            return false;
        if (!this.data.equals(hm.getData()))
            return false;
        return true;
    }

    public static void main(String[] args) 
    {
        ListMessage hm1 = new ListMessage("LIST;TzuyiDell;TzuyiHP;7;5;2;ILoveYou");
        ListMessage hm11 = new ListMessage(hm1.getListMessageAsEncodedString());

        if (hm1.equals(hm11))
            System.out.println("OK");
        else
            System.out.println("Not OK");

        ListMessage hm2 = new ListMessage("TzuyiDell", "TzuyiHP", 7, 5, 2, "ILoveYou");
        ListMessage hm22 = new ListMessage(hm2.getListMessageAsEncodedString());

        if (hm2.equals(hm22))
            System.out.println("OK");
        else
            System.out.println("Not OK");
    }
    */
}