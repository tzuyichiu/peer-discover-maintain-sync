import java.util.HashMap;

/** 
 * - Create a thread to generate LIST messages to transfer my database.
 * - Ignore the messages not destinated to us or we are already processing.
 */
class SynReceiver implements SimpleMessageHandler, Runnable
{
    private static String TAG = "SynReceiver   - ";
    private SynchronizedQueue incoming = new SynchronizedQueue(20);
    private MuxDemuxSimple myMuxDemux = null;

    /**
     * Dictionnary associating peerID to the thread we created which sends
     * LIST messages to transfer my database. While the corresponding thread
     * is still processing, we will ignore other SYN messages.
     */
    private HashMap<String, Thread> myThreads = new HashMap<>();

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
            
            SynMessage sm;
            try
            {
                sm = new SynMessage(msg);
            }
            catch (IllegalArgumentException e)
            {
                // Not a SYN message (perhaps HELLO, LIST, etc...)
                continue;
            }

            String senderID     = sm.getSenderID();
            String destID       = sm.getPeerID();
            int    sequenceNo   = sm.getSequenceNo();

            String myID         = Info.getMyID();
            int    mySequenceNo = Info.getMySequenceNo();

            if (destID.equals(myID) && sequenceNo != mySequenceNo)
            {
                Thread t = this.myThreads.get(senderID);
                if (t == null || !t.isAlive())
                {
                    // generate LIST messages containing my data
                    Thread sendListThread = new Thread(() ->
                    {
                        String[] myData = Info.getMyData();
                        
                        for (int i=0; i<myData.length; i++)
                        {
                            ListMessage lm = new ListMessage(
                                myID, senderID, mySequenceNo, 
                                myData.length, i, myData[i]); 
                            myMuxDemux.send(lm.getListMessageAsEncodedString());
                        }
                    });
                    this.myThreads.put(senderID, sendListThread);
                    sendListThread.start();
                }
            }
        }
    }
}