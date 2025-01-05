
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
    }

    // Preconditions: The camera object is initialized in the setUp() method.
    // Postconditions: The camera should be initialized, and its detected objects list should be empty.
    @Test
    public void testInitialization() {
        assertNotNull(camera, "Camera should be initialized.");
        assertTrue(camera.getDetectedObject(0).isEmpty(), "Camera should not detect objects before initialization.");
    }


    // Preconditions: The camera object is initialized with an ID of 1 in the setUp() method.
    // Postconditions: The camera's ID should be equal to 1
    @Test
    public void testGetID()
    {
        assertEquals(1,camera.getId());
    }

    // Preconditions: The camera object is initialized with STATUS.UP in the setUp() method.
    // Postconditions: The camera's status should be UP.
    @Test
    public void testGetStatus()
    {
        assertEquals(STATUS.UP,camera.getStatus());
    }

    // Preconditions: The camera object is initialized with STATUS.UP in the setUp() method.
    // Postconditions: The camera should be active when the status is UP, and inactive when the status is DOWN.
    @Test
    public void testIsActive() {
        assertTrue(camera.isActive(), "Camera should be active when status is UP.");
        camera = new Camera(1, 5, STATUS.DOWN, "cameraKey", "path","");
        assertFalse(camera.isActive(), "Camera should be inactive when status is DOWN.");
    }

    // Preconditions: The camera is initialized with a frequency of 5 in the setUp() method.
    // Postconditions: The camera should send an event at the right time according to her frequency.
    @Test
    public void testShouldSendEvent() {
        assertTrue(camera.shouldSendEvent(9), "Camera should send an event at the correct frequency.");
        assertTrue(camera.shouldSendEvent(7), "Camera should send an event at the correct frequency.");
        assertFalse(camera.shouldSendEvent(4), "Camera should not send an event at incorrect ticks.");
    }


    // Preconditions: The camera is initialized, and the tick is provided as 5.
    // Postconditions: A DetectObjectsEvent should be created for the provided tick.
    @Test
    public void testCreateDetectObjectsEvent() {
        DetectObjectsEvent event = camera.createDetectObjectsEvent(5);
        assertNotNull(event, "DetectObjectsEvent should be created for valid tick.");
    }

    // Preconditions: The camera is initialized with a detected objects list containing an error at tick 8.
    // Postconditions: The camera should correctly detect the error at tick 8 and not detect errors at other ticks.
    @Test
    public void testDetectError() {
        boolean hasError = camera.detectError(10);
        assertFalse(hasError, "camera detect false error");
        hasError= camera.detectError(8);
        assertTrue(hasError,"error in detected object didn't detect by the camera");
    }


    // Preconditions: The camera is initialized with an error at tick 8 in its detected objects list.
    // Postconditions: The error description should be null if no error is detected, and should match the expected error description otherwise.
    @Test
    public void testErrorDescription() {
        String description = camera.errorDescription(10);
        assertNull(description, "Error description should be null if no error is detected.");
        description = camera.errorDescription(8);
        assertEquals("Error",description,"error message");
    }

    // Preconditions: The camera is initialized with a detected objects list containing 3 objects.
    // Postconditions: The size of the detected objects list should be 3.
    @Test
    public void testGetDetectedObjectList()
    {
        assertEquals(3,camera.getDetectedObjectsList().size());
    }

    // Preconditions: The camera is initialized with a detected objects list where tick 2 has 2 detected objects.
    // Postconditions: The size of the detected objects list for tick 2 should be 2.
    @Test
    public void testGetDetectedObjectbyTick()
    {
        assertEquals(2,camera.getDetectedObject(2).size());
    }





}
