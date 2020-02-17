/** 
 * An HashMap PeerTable inside the "static" class "Info" will associate the 
 * received peerID with the corresponding PeerRecord.
 */
public class PeerRecord
{
    final private String    peerID;
    final private String    peerIP;
    final private long      expirationTime;
    private       PeerState peerState;
    private       Database  peerDatabase;

    /**
     * Constructor called when the senderID doesn't exist inside PeerTable.
     * This created instance will then be stored inside.
     */
    public PeerRecord(String peerID, String peerIP, int helloInterval)
    {
        this.peerID         = peerID;
        this.peerIP         = peerIP;
        this.expirationTime = System.currentTimeMillis() + 
                                ((long) helloInterval)*1000;
        this.peerState      = PeerState.HEARD;
        this.peerDatabase   = new Database();
    }

    public String getPeerIP()
    {
        return this.peerIP;
    }

    public int getPeerSequenceNo()
    {
        return this.peerDatabase.getDatabaseSequenceNo();
    }
    
    public long getExpirationTime()
    {
        return this.expirationTime;
    }

    public PeerState getPeerState()
    {
        return this.peerState;
    }

    public String[] getPeerData()
    {
        return this.peerDatabase.getData();
    }

    /**
     * getSyn - called after updating PeerState into HEARD/INCONSISTENT.
     * @return the syn message as encoded string associated to the peer
     */
    public String getSyn()
    {
        SynMessage synMessage = new SynMessage(Info.getMyID(), this.peerID, 
            this.getPeerSequenceNo());
        return synMessage.getSynMessageAsEncodedString();
    }
    
    /** 
     * setPeerState - 
     *      called while updating the PeerTable (cf. Info class).
     */
    public void setPeerState(PeerState newState)
    {
        this.peerState = newState;
    }

    /**
     * updatePeerDatabase - 
     *      called after receiving all LIST messages in response to the SYN 
     *      message we would have sent to the corresponding peer.
     * @param newData    should be the complete data to be updated
     * @param sequenceNo the new sequenceNo
     */
    public void updatePeerDatabase(String[] newData, int sequenceNo)
    {
        this.peerDatabase.updateDatabase(newData, sequenceNo);
    }
}