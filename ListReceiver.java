import java.util.HashMap;

/** 
 * - Ignore the messages not destinated to us.
 * - Fill in the data array at the partNo position.
 * - Check if all messages are received.
 * - Update the corresponding database in PeerTable.
 */
class ListReceiver implements SimpleMessageHandler, Runnable
{
    private static String TAG = "ListReceiver  - ";
    private SynchronizedQueue incoming = new SynchronizedQueue(20);
    private MuxDemuxSimple myMuxDemux = null;
    
    /**
     * Dictionnary associating peerID to its database which we will fill in
     * little by little. At the end (indicating by "lackPartsNo" below), we can
     * then update the corresponding database (in PeerRecord which is in our 
     * PeerTable).
     */
    private HashMap<String, String[]> data = new HashMap<>();

    /**
     * Dictionnary associating peerID to the number of lacking parts.
     * Every value is initiating to TotalParts corresponding to the peer.
     * When it reach 0 then we update the corresponding database.
     */
    private HashMap<String, Integer> lackPartsNo = new HashMap<>();

    public void setMuxDemux(MuxDemuxSimple md)
    {
        myMuxDemux = md;
    }

    public void handleMessage(String recved, String source)
    {
        try
        {
            incoming.enqueue(recved);
        }
        catch (InterruptedException e)
        {
            System.err.println(e);
            Thread.currentThread().interrupt();
            return;
        }
    }
	
    public void run()
    {
        while (!Thread.interrupted())
        {
            String msg;
            try
            {
                msg = incoming.dequeue();
            }
            catch (InterruptedException e)
            {
                System.err.println(e);
                Thread.currentThread().interrupt();
                return;
            }
            
            ListMessage lm;
            try
            {
                lm = new ListMessage(msg);
            }
            catch (IllegalArgumentException e)
            {
                // Not a LIST message (perhaps HELLO, SYN, etc...)
                continue;
            }

            String destID     = lm.getPeerID();
            String senderID   = lm.getSenderID();
            int    sequenceNo = lm.getSequenceNo();
            int    totalParts = lm.getTotalParts();

            if (destID.equals(Info.getMyID()))
            {
                PeerRecord peerRecord     = Info.getPeerRecord(senderID);
                int        peerSequenceNo = peerRecord.getPeerSequenceNo();
                PeerState  peerState      = peerRecord.getPeerState();
            
                if (peerState == PeerState.SYNCHRONIZED)
                {
                    System.err.println(TAG + "no need to synchronize...");
                    continue;
                }
            
                if (peerSequenceNo == sequenceNo)
                {
                    System.err.println(TAG + "no need to synchronize...");
                    peerRecord.setPeerState(PeerState.SYNCHRONIZED);

                    if (Info.DEBUG)
                        System.out.println(TAG + senderID + ": " + 
                            peerState + " -> SYNCHRONIZED");
                    continue;
                }
                
                if (this.data.get(senderID) == null || 
                    this.data.get(senderID).length != totalParts)
                {
                    this.data.put(senderID, new String[totalParts]);
                    this.lackPartsNo.put(senderID, totalParts);
                }
                
                String[] peerDatabase = this.data.get(senderID);
                
                peerDatabase[lm.getPartNo()] = lm.getData();
                int lack = this.lackPartsNo.get(senderID);
                lack--;
                this.lackPartsNo.put(senderID, lack);

                if (lack == 0) // Finished 
                {
                    String[] newData = this.data.get(senderID);
                    Info.updatePeerDatabase(senderID, newData, sequenceNo);
                    
                    peerRecord.setPeerState(PeerState.SYNCHRONIZED);

                    if (Info.DEBUG)
                        System.out.println(TAG + senderID + ": " + 
                            peerState + " -> SYNCHRONIZED");
                }
            }
        }
    }
}