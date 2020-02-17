class DebugReceiver implements SimpleMessageHandler, Runnable
{
    private static String TAG = "DebugReceiver - ";
    private SynchronizedQueue incoming = new SynchronizedQueue(20);
    private MuxDemuxSimple myMuxDemux = null;

    public void setMuxDemux(MuxDemuxSimple md)
    {
        myMuxDemux = md;
    }

    public void handleMessage(String msg, String senderIP)
    {
        try
        {
            // I can do this because HELLO messages don't contain any @
            incoming.enqueue(msg+"@"+senderIP);
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

            // Simply print out the received message
            System.out.println(TAG + msg + " from " + senderIP);
        }
    }
}