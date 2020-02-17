interface SimpleMessageHandler extends Runnable
{
    public void setMuxDemux(MuxDemuxSimple md);
    public void handleMessage(String recved, String senderIP);
}