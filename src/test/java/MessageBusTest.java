import bgu.spl.mics.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


class MessageBusTest {

    private MessageBusImpl messageBus;
    private MicroService testMicroService1;
    private MicroService testMicroService2;

    @BeforeEach
    void setUp() {
        messageBus = MessageBusImpl.getInstance();

        testMicroService1 = new MicroService("TestMicroService1") {
            @Override
            protected void initialize() {}
        };

        testMicroService2 = new MicroService("TestMicroService2") {
            @Override
            protected void initialize() {}
        };
    }

    // Preconditions: The MessageBus and MicroService are initialized. The MicroService is registered with the MessageBus.
    // Postconditions: The MicroService is unregistered, and attempting to await a message for the unregistered MicroService throws an IllegalStateException.
    @Test
    void testUnregisterMicroService() {
        messageBus.register(testMicroService1);
        messageBus.unregister(testMicroService1);

        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(testMicroService1));
    }

    // Preconditions: The MessageBus and MicroService are initialized. The MicroService is registered and subscribed to an event type.
    // Postconditions: Sending an event of the subscribed type results in the MicroService receiving it.
    @Test
    void testSubscribeEvent() {
        class TestEvent implements Event<Boolean> {}

        messageBus.register(testMicroService1);
        messageBus.subscribeEvent(TestEvent.class, testMicroService1);

        TestEvent event = new TestEvent();
        messageBus.sendEvent(event);

        assertDoesNotThrow(() -> {
            Message message = messageBus.awaitMessage(testMicroService1);
            assertTrue(message instanceof TestEvent);
        });
    }

    // Preconditions: The MessageBus and two MicroServices are initialized. Both MicroServices are registered and subscribed to a broadcast type.
    // Postconditions: Sending a broadcast results in both MicroServices receiving the broadcast.
    @Test
    void testSubscribeBroadcast() {
        class TestBroadcast implements Broadcast {}

        messageBus.register(testMicroService1);
        messageBus.register(testMicroService2);

        messageBus.subscribeBroadcast(TestBroadcast.class, testMicroService1);
        messageBus.subscribeBroadcast(TestBroadcast.class, testMicroService2);

        TestBroadcast broadcast = new TestBroadcast();
        messageBus.sendBroadcast(broadcast);

        assertDoesNotThrow(() -> {
            Message message1 = messageBus.awaitMessage(testMicroService1);
            Message message2 = messageBus.awaitMessage(testMicroService2);

            assertTrue(message1 instanceof TestBroadcast);
            assertTrue(message2 instanceof TestBroadcast);
        });
    }

    // Preconditions: The MessageBus and MicroService are initialized. The MicroService is registered and subscribed to an event type.
    // Postconditions: Sending an event of the subscribed type results in the MicroService receiving it, and a valid Future object is returned.
    @Test
    void testSendEvent() {
        class TestEvent implements Event<String> {}

        messageBus.register(testMicroService1);
        messageBus.subscribeEvent(TestEvent.class, testMicroService1);

        TestEvent event = new TestEvent();
        Future<String> future = messageBus.sendEvent(event);

        assertNotNull(future);
        assertDoesNotThrow(() -> {
            Message message = messageBus.awaitMessage(testMicroService1);
            assertTrue(message instanceof TestEvent);
        });
    }

    // Preconditions: The MessageBus and MicroService are initialized. The MicroService is registered and subscribed to an event type.
    // Postconditions: Completing an event updates the associated Future object with the result, which can then be retrieved.
    @Test
    void testCompleteEvent() {
        class TestEvent implements Event<String> {}

        messageBus.register(testMicroService1);
        messageBus.subscribeEvent(TestEvent.class, testMicroService1);

        TestEvent event = new TestEvent();
        Future<String> future = messageBus.sendEvent(event);

        assertNotNull(future);
        messageBus.complete(event, "Result");

        assertTrue(future.isDone());
        assertEquals("Result", future.get());
    }

    // Preconditions: The MessageBus and MicroService are initialized. The MicroService is not registered with the MessageBus.
    // Postconditions: Attempting to await a message for the unregistered MicroService throws an IllegalStateException.
    @Test
    void testAwaitMessageThrowsExceptionForUnregisteredMicroService() {
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(testMicroService1));
    }

    // Preconditions: The MessageBus is initialized, and no MicroServices are subscribed to the broadcast type.
    // Postconditions: Sending a broadcast does not throw any exceptions.
    @Test
    void testSendBroadcastWithNoSubscribers() {
        class TestBroadcast implements Broadcast {}

        TestBroadcast broadcast = new TestBroadcast();
        assertDoesNotThrow(() -> messageBus.sendBroadcast(broadcast));
    }

    // Preconditions: The MessageBus is initialized, and no MicroServices are subscribed to the event type.
    // Postconditions: Sending an event returns null, indicating no subscribers.
    @Test
    void testSendEventWithNoSubscribers() {
        class TestEvent implements Event<String> {}

        TestEvent event = new TestEvent();
        Future<String> future = messageBus.sendEvent(event);

        assertNull(future);
    }

    // Preconditions: The MessageBus and two MicroServices are initialized. Both MicroServices are registered and subscribed to the same event type.
    // Postconditions: Events are distributed in a round-robin manner between the subscribed MicroServices.
    @Test
    void testRoundRobinEventHandling() {
        class TestEvent implements Event<String> {}

        messageBus.register(testMicroService1);
        messageBus.register(testMicroService2);

        messageBus.subscribeEvent(TestEvent.class, testMicroService1);
        messageBus.subscribeEvent(TestEvent.class, testMicroService2);

        TestEvent event1 = new TestEvent();
        TestEvent event2 = new TestEvent();

        messageBus.sendEvent(event1);
        messageBus.sendEvent(event2);

        assertDoesNotThrow(() -> {
            Message message1 = messageBus.awaitMessage(testMicroService1);
            Message message2 = messageBus.awaitMessage(testMicroService2);

            assertTrue(message1 instanceof TestEvent);
            assertTrue(message2 instanceof TestEvent);
        });
    }
}
