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


    @Test
    void testUnregisterMicroService() {
        messageBus.register(testMicroService1);
        messageBus.unregister(testMicroService1);

        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(testMicroService1));
    }

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

    @Test
    void testAwaitMessageThrowsExceptionForUnregisteredMicroService() {
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(testMicroService1));
    }

    @Test
    void testSendBroadcastWithNoSubscribers() {
        class TestBroadcast implements Broadcast {}

        TestBroadcast broadcast = new TestBroadcast();
        assertDoesNotThrow(() -> messageBus.sendBroadcast(broadcast));
    }

    @Test
    void testSendEventWithNoSubscribers() {
        class TestEvent implements Event<String> {}

        TestEvent event = new TestEvent();
        Future<String> future = messageBus.sendEvent(event);

        assertNull(future);
    }

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
