
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.CameraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CameraTest {

    private Camera camera;
    private CameraService cameraService;
    MessageBusImpl messageBus;


    @BeforeEach
    public void setUp() {
        String cameraKey = "camera1";
        camera = new Camera(1, 5, STATUS.UP, cameraKey);
        List<DetectedObject> l= new ArrayList<>();
        DetectedObject d1 = new DetectedObject("Wall_1","Wall");
        DetectedObject d2 = new DetectedObject("Wall_3","Wall");
        l.add(d1);
        l.add(d2);
        List<DetectedObject> l2= new ArrayList<>();
        StampedDetectedObjects s2 = new StampedDetectedObjects(2,l);
        DetectedObject d3 = new DetectedObject("Chair_1","Chair");
        l2.add(d3);
        StampedDetectedObjects s1 =new StampedDetectedObjects(4, l2);
        camera.getDetectedObjectsList().add(s1);
        camera.getDetectedObjectsList().add(s2);
        List<DetectedObject> l3= new ArrayList<>();
        l3.add(new DetectedObject("ERROR","Error"));
        StampedDetectedObjects s3 = new StampedDetectedObjects(8,l3);
        camera.getDetectedObjectsList().add(s3);
        cameraService= new CameraService(camera);
        messageBus=MessageBusImpl.getInstance();
        messageBus.register(cameraService);

    }

    @Test
    public void testInitialization() {
        assertNotNull(camera, "Camera should be initialized.");
        assertTrue(camera.getDetectedObject(0).isEmpty(), "Camera should not detect objects before initialization.");
    }

    @Test
    public void testIsActive() {
        assertTrue(camera.isActive(), "Camera should be active when status is UP.");
        camera = new Camera(1, 5, STATUS.DOWN, "cameraKey", "path","");
        assertFalse(camera.isActive(), "Camera should be inactive when status is DOWN.");
    }

    @Test
    public void testShouldSendEvent() {
        assertTrue(camera.shouldSendEvent(9), "Camera should send an event at the correct frequency.");
        assertTrue(camera.shouldSendEvent(7), "Camera should send an event at the correct frequency.");
        assertFalse(camera.shouldSendEvent(4), "Camera should not send an event at incorrect ticks.");
    }


    @Test
    public void testCreateDetectObjectsEvent() {
        DetectObjectsEvent event = camera.createDetectObjectsEvent(5);
        assertNotNull(event, "DetectObjectsEvent should be created for valid tick.");
    }

    @Test
    public void testDetectError() {
        boolean hasError = camera.detectError(10);
        assertFalse(hasError, "camera detect false error");
        hasError= camera.detectError(8);
        assertTrue(hasError,"error in detected object didn't detect by the camera");
    }

    @Test
    public void testErrorDescription() {
        String description = camera.errorDescription(10);
        assertNull(description, "Error description should be null if no error is detected.");
        description = camera.errorDescription(8);
        assertEquals("Error",description,"error message");
    }

    @Test
    void testHandleTickBroadcast() throws InterruptedException {
        TickBroadcast tickBroadcast = new TickBroadcast(4);
        new Thread(cameraService).start();

        camera.TurnOnCamera();
        camera.setCountDetected(5);

    //    messageBus.subscribeEvent(DetectObjectsEvent.class, cameraService);
        messageBus.sendBroadcast(tickBroadcast);

        //messageBus.sendEvent(camera.createDetectObjectsEvent(tickBroadcast.getTick()));

        // Allow some time for processing
        Thread.sleep(100);

        Message message = messageBus.awaitMessage(cameraService);
        assertTrue(message instanceof DetectObjectsEvent, "CameraService should send DetectObjectsEvent in response to TickBroadcast.");

    }

//
//    @Test
//    void testHandleCrashedBroadcast() throws InterruptedException {
//        // Simulate a CrashedBroadcast
//        CrashedBroadcast crashedBroadcast = new CrashedBroadcast(
//                "camera failure",
//                "Camera1",
//                null,
//                null,
//                null
//        );
//        messageBus.sendBroadcast(crashedBroadcast);
//
//        new Thread(cameraService::run).start();
//
//        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(cameraService),
//                "CameraService should terminate upon receiving a CrashedBroadcast.");
//    }
//    @Test
//    void testTerminateOnTerminatedBroadcast() throws InterruptedException {
//        messageBus.sendBroadcast(new bgu.spl.mics.application.messages.TerminatedBroadcast());
//
//        new Thread(cameraService::run).start();
//
//        // Ensure CameraService processes the TerminatedBroadcast and terminates
//        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(cameraService),
//                "CameraService should terminate upon receiving a TerminatedBroadcast.");
//    }

}
