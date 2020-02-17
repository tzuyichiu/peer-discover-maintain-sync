import java.util.LinkedList;
import java.lang.Thread;

public class SynchronizedQueue
{
    private final LinkedList<String> queue;
    private final int capacity;

    public SynchronizedQueue(int capacity)
    {
        this.queue = new LinkedList<String>();
        this.capacity = capacity;
    }

    public synchronized boolean isEmpty()
    {
        return this.queue.size() == 0;   
    }

    public synchronized boolean isFull() 
    {
        return this.queue.size() == this.capacity;   
    }

    public synchronized void enqueue(String recved) throws InterruptedException
    {
        while (this.isFull())
            this.wait();   
        
        if(this.queue.size() == 0)
            this.notifyAll();
        
        this.queue.add(recved);
    }

    public synchronized String dequeue() throws InterruptedException 
    {
        while (this.isEmpty())
            this.wait();
        
        if(this.queue.size() == this.capacity)
            this.notifyAll();
        
        return this.queue.remove();
    }
}