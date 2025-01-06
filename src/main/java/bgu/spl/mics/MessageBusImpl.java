package bgu.spl.mics;

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
	private final Map<Class<? extends Message>, Queue<MicroService>> subscribers;
	private final Map<MicroService, BlockingQueue<Message>> microServicesQueues;
	private final Map<Event<?>,Future<?>> futureForEvents;

	private static class MessageBusImplHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	private MessageBusImpl(){
		subscribers=new ConcurrentHashMap<>();
		microServicesQueues=new ConcurrentHashMap<>();
		futureForEvents=new ConcurrentHashMap<>();


	}
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		synchronized (subscribers)
		{
            subscribers.computeIfAbsent(type, k -> new LinkedList<>());

			subscribers.get(type).add(m);
			microServicesQueues.putIfAbsent(m, new LinkedBlockingQueue<>());
		}
	}


	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		synchronized (subscribers)
		{
            subscribers.computeIfAbsent(type, k -> new LinkedList<>());

			subscribers.get(type).add(m);
			microServicesQueues.putIfAbsent(m, new LinkedBlockingQueue<>());
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
		Queue<MicroService> subscribersList = subscribers.get(b.getClass());
		if (subscribersList != null) {
			List<MicroService> safeCopy;
			synchronized (subscribersList) {
				safeCopy = new ArrayList<>(subscribersList);
			}
			for (MicroService subscriber : safeCopy) {
				BlockingQueue<Message> queue = microServicesQueues.get(subscriber);
				if (queue != null) {
					queue.add(b);
				}
			}
		}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		synchronized (subscribers)
		{
			Queue<MicroService> sub = subscribers.get(e.getClass());
			if (sub!=null && !sub.isEmpty()) {
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
		microServicesQueues.putIfAbsent(m, new LinkedBlockingQueue<>());
	}

	@Override
	public void unregister(MicroService m) {
		microServicesQueues.remove(m);
		for (Queue<MicroService> queue : subscribers.values()) {
			synchronized (queue) {
				queue.remove(m);
			}
		}

	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		BlockingQueue<Message> queue = microServicesQueues.get(m);
		if (queue == null) {
			throw new IllegalStateException("MicroService is not registered");
		}
		return queue.take();
	}

	public static MessageBusImpl getInstance() {
		return MessageBusImplHolder.instance;
	}
}
