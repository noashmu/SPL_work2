package bgu.spl.mics;

import jdk.vm.ci.code.site.Call;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	private static MessageBusImpl instance=null;
	private final Map<Class<? extends Message>, Queue<MicroService>> subscribers;
	private final Map<MicroService, BlockingQueue<Message>> microServicesQueues;
	private final Map<Event<?>,Future<?>> futureForEvents;

	private MessageBusImpl(){
		subscribers=new ConcurrentHashMap<>();
		microServicesQueues=new ConcurrentHashMap<>();
		futureForEvents=new ConcurrentHashMap<>();


	}
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		synchronized (subscribers)
		{
			if (subscribers.get(type)==null)
				subscribers.put(type, new LinkedList<>());

			subscribers.get(type).add(m);
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		synchronized (subscribers)
		{
			if (subscribers.get(type)==null)
				subscribers.put(type, new LinkedList<>());

			subscribers.get(type).add(m);
		}
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> f= (Future<T>) futureForEvents.get(e);
		if(f!=null)
		{
			f.resolve(result);
			futureForEvents.remove(e);
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		synchronized (subscribers) {
			Queue<MicroService> sub = subscribers.get(b.getClass());
			while (!sub.isEmpty())
			{
				MicroService subscriber=sub.remove();
				microServicesQueues.get(subscriber).add(b);
				sub.add(subscriber);
			}
		}

	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		synchronized (subscribers)
		{
			Queue<MicroService> sub = subscribers.get(e.getClass());
			if (sub!=null) {
				MicroService subscriber = sub.remove();
				microServicesQueues.get(subscriber).add(e);
				sub.add(subscriber);
				Future<T> future = new Future<>();
				futureForEvents.put(e, future);
				return future;
			}
		}
		return null;
	}

	@Override
	public void register(MicroService m) {
		if (microServicesQueues.get(m)==null)
		{
			microServicesQueues.put(m,new LinkedBlockingQueue<>());
		}
	}

	@Override
	public void unregister(MicroService m) {
		synchronized (subscribers)
		{
			microServicesQueues.remove(m);
			for (Queue<MicroService> q: subscribers.values())
			{
				q.remove(m);
			}
		}

	}
    //לבדוק אם מותר להשתמש ככה
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		BlockingQueue<Message> queue = microServicesQueues.get(m);
		if (queue == null) {
			throw new IllegalStateException("MicroService is not registered");
		}
		return queue.take(); //לבדוק

	}

	public static MessageBusImpl getInstance() {
		if (instance == null) {
			instance = new MessageBusImpl();
		}
		return instance;
	}


}
