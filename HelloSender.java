class HelloSender implements SimpleMessageHandler, Runnable
{
    private static String TAG = "HelloSender   - ";
    private SynchronizedQueue incoming = new SynchronizedQueue(20);
    private MuxDemuxSimple myMuxDemux = null;

    public void setMuxDemux(MuxDemuxSimple md)
    {
        myMuxDemux = md;
    }

    public void handleMessage(String recved, String senderIP)
    {}
	
    /**
     * Generate HELLO messages every t milliseconds where:
     *      t = myHelloInterval/2
     */
    public void run()
    {
        while (!Thread.interrupted())
        {
            HelloMessage hm = new HelloMessage(Info.getMyID(), 
                Info.getMySequenceNo(), Info.getMyHelloInterval());
            
            for (String peerID : Info.getPeerSet())
                hm.addPeer(peerID);

            String toSend = hm.getHelloMessageAsEncodedString();
            myMuxDemux.send(toSend);
            
            if (Info.DEBUG)
                System.out.println(TAG + toSend);
            
            try
            {
                Thread.sleep(2000);
            }
            catch (InterruptedException e)
            {
                System.err.println(e);
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}