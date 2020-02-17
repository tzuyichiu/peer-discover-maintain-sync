/** 
 * - Ignore the messages we sent ourselves or of other types than HELLO.
 * - Update PeerTable with other peer's messages.
 * - If PeerState becomes HEARD/INCONSISTENT then send back a SYN Message.
 * - We'll keep sending the same SYN until peer's sequence# is incremented.
 */
class HelloReceiver implements SimpleMessageHandler, Runnable
{
    private static String TAG = "HelloReceiver - ";
    private SynchronizedQueue incoming = new SynchronizedQueue(20);
    private MuxDemuxSimple myMuxDemux = null;

    public void setMuxDemux(MuxDemuxSimple md)
    {
        myMuxDemux = md;
    }

    public void handleMessage(String recved, String source)
    {
        try
        {
            incoming.enqueue(recved+"@"+source);
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
            String msgAndSenderIP;
            try
            {
                msgAndSenderIP = incoming.dequeue();
            }
            catch (InterruptedException e)
            {
                System.err.println(e);
                Thread.currentThread().interrupt();
                return;
            }

            int index_at    = msgAndSenderIP.lastIndexOf("@"); 
            String msg      = msgAndSenderIP.substring(0, index_at);
            String senderIP = msgAndSenderIP.substring(index_at+1);

            HelloMessage hm;
            try
            {
                hm = new HelloMessage(msg);
            }
            catch (IllegalArgumentException e)
            {
                // Not a HELLO message (perhaps SYN, LIST, etc...)
                continue;
            }

            String peerID = hm.getSenderID();
            if (!peerID.equals(Info.getMyID()))
            {
                Info.updatePeerTable(hm, senderIP);
                PeerRecord peerRecord = Info.getPeerRecord(peerID);
                
                if (peerRecord.getPeerState() != PeerState.SYNCHRONIZED)
                {    
                    // send the same SYN until sequenceNo is incremented
                    new Thread(() ->
                    {
                        int sequenceNo = peerRecord.getPeerSequenceNo();
                        
                        while (peerRecord.getPeerSequenceNo() == sequenceNo)
                        {
                            SynMessage sm = new SynMessage(
                                Info.getMyID(), peerID, sequenceNo);
                            
                            String toSend = sm.getSynMessageAsEncodedString();
                            myMuxDemux.send(toSend);
                            
                            if (Info.DEBUG)
                                System.out.println(TAG + toSend);

                            try
                            {
                                Thread.sleep(6000);
                            }
                            catch (InterruptedException e)
                            {
                                System.err.println(e);
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                    }).start();
                }
            }
        }
    }
}