
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CameraTest {

    private Camera camera;

    @BeforeEach
    public void setUp() {
        // Mock data file path and cameraKey. Replace with actual test file paths as needed.
        String filePath = "src/test/resources/camera_test_data.json";
        String cameraKey = "camera1";
        camera = new Camera(1, 5, STATUS.UP, cameraKey, filePath,"");
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
        assertTrue(camera.shouldSendEvent(5), "Camera should send an event at the correct frequency.");
        assertFalse(camera.shouldSendEvent(4), "Camera should not send an event at incorrect ticks.");
    }

    @Test
    public void testGetDetectedObjects() {
        List<DetectedObject> objects = camera.getDetectedObject(10);
        assertNotNull(objects, "Detected objects list should not be null.");
        assertEquals(0, objects.size(), "No objects should be detected for invalid tick.");
    }

    @Test
    public void testCreateDetectObjectsEvent() {
        DetectObjectsEvent event = camera.createDetectObjectsEvent(5);
        assertNotNull(event, "DetectObjectsEvent should be created for valid tick.");
    }

    @Test
    public void testDetectError() {
        boolean hasError = camera.detectError(10);
        assertFalse(hasError, "Camera should not detect errors if none are present.");
    }

    @Test
    public void testErrorDescription() {
        String description = camera.errorDescription(10);
        assertNull(description, "Error description should be null if no error is detected.");
    }
}
