import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.*;

import bgu.spl.mics.application.services.FusionSlamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FusionSlamTest {

    private FusionSlam fusionSlam;
    private FusionSlamService fusionSlamService;
    private MessageBusImpl messageBus;

    @BeforeEach
    public void setUp() {
        fusionSlam = FusionSlam.getInstance();
        messageBus = MessageBusImpl.getInstance();
        fusionSlamService = new FusionSlamService(fusionSlam, "path/config");
        messageBus.register(fusionSlamService);
    }

    @Test
    public void testAddLandMark() {
        LandMark landmark = new LandMark("1", "Landmark 1");
        fusionSlam.addLandMark(landmark);

        assertEquals(1, fusionSlam.getLandmarks().size(), "Expected one landmark to be added");
        assertEquals("1", fusionSlam.getLandmarks().get(0).getId(), "Expected landmark ID to match");
    }

    @Test
    public void testIsNewLandmark() {
        LandMark landmark = new LandMark("1", "Landmark 1");
        fusionSlam.addLandMark(landmark);

        TrackedObject trackedObject = new TrackedObject("1", 10, "Tracked Landmark");
        assertFalse(fusionSlam.isNewLandmark(trackedObject), "Expected trackedObject to match existing landmark");

        TrackedObject newTrackedObject = new TrackedObject("2", 15, "New Tracked Landmark");
        assertTrue(fusionSlam.isNewLandmark(newTrackedObject), "Expected newTrackedObject to be identified as new");
    }

    @Test
    public void testUpdateLandMark() {
        LandMark landmark = new LandMark("1", "Landmark 1");
        fusionSlam.addLandMark(landmark);

        ArrayList<CloudPoint> updatedCoordinates = new ArrayList<>();
        updatedCoordinates.add(new CloudPoint(10, 20));
        updatedCoordinates.add(new CloudPoint(30, 40));

        TrackedObject trackedObject = new TrackedObject("1", 10, "Updated Landmark");
        trackedObject.getCoordinates().addAll(updatedCoordinates);

        fusionSlam.updateLandMark(trackedObject);

        List<CloudPoint> coordinates = fusionSlam.getLandmarks().get(0).getCoordinates();
        assertEquals(2, coordinates.size(), "Expected landmark coordinates to be updated.");
        assertEquals(10, coordinates.get(0).getX(), "Expected updated x-coordinate to match.");
        assertEquals(20, coordinates.get(0).getY(), "Expected updated y-coordinate to match.");
    }

    @Test
    public void testAddPose() {
        Pose pose = new Pose(5.0, 10.0, Math.PI / 4, 1);
        fusionSlam.addPose(pose);

        List<Pose> poses = fusionSlam.getPoses();
        assertEquals(1, poses.size(), "Expected one pose to be added.");
        assertEquals(pose, poses.get(0), "Expected pose to match the added pose.");
    }

    @Test
    public void testGetCurrentPose() {
        Pose pose1 = new Pose(5.0, 10.0, Math.PI / 4, 1);
        Pose pose2 = new Pose(15.0, 20.0, Math.PI / 2, 2);

        fusionSlam.addPose(pose1);
        fusionSlam.addPose(pose2);

      //  Pose currentPose = fusionSlam.getCurrentPose();
        //  assertEquals(pose2, currentPose, "Expected the most recent pose to be returned.");
    }
    @Test
    public void testHandleTrackedObjectsEvent() throws InterruptedException {
        // Prepare test data
        List<TrackedObject> trackedObjects = new ArrayList<>();
        TrackedObject obj1 = new TrackedObject("1", 10, "Landmark 1");
        TrackedObject obj2 = new TrackedObject("2", 15, "Landmark 2");
        trackedObjects.add(obj1);
        trackedObjects.add(obj2);

        TrackedObjectsEvent event = new TrackedObjectsEvent(trackedObjects, 5);
        messageBus.subscribeEvent(TrackedObjectsEvent.class, fusionSlamService);

        // Send the event
        Thread serviceThread = new Thread(fusionSlamService::run);
        serviceThread.start();
        messageBus.sendEvent(event);

        // Wait and verify
        Thread.sleep(100); // Allow time for processing
        assertEquals(2, fusionSlam.getLandmarks().size(), "Expected 2 landmarks to be added");
        assertTrue(fusionSlam.getLandmarks().stream().anyMatch(l -> l.getId().equals("1")), "Expected landmark 1 to be added");
        assertTrue(fusionSlam.getLandmarks().stream().anyMatch(l -> l.getId().equals("2")), "Expected landmark 2 to be added");

        // Stop the service
        serviceThread.interrupt();
        serviceThread.join();
    }

    @Test
    public void testHandlePoseEvent() throws InterruptedException {
        // Prepare test data
        Pose pose = new Pose(5.0, 10.0, Math.PI / 4, 1);
        PoseEvent event = new PoseEvent(pose);
        messageBus.subscribeEvent(PoseEvent.class, fusionSlamService);

        // Send the event
        Thread serviceThread = new Thread(fusionSlamService::run);
        serviceThread.start();
        messageBus.sendEvent(event);

        // Wait and verify
        Thread.sleep(100); // Allow time for processing
        assertEquals(1, fusionSlam.getPoses().size(), "Expected 1 pose to be added");
        assertEquals(pose, fusionSlam.getPoses().get(0), "Expected pose to match the added pose");

        // Stop the service
        serviceThread.interrupt();
        serviceThread.join();
    }

    @Test
    public void testHandleTickBroadcast() throws InterruptedException {
        // Simulate the sensor count reaching 0
        fusionSlam.decreseSensorCount(); // Assuming a sensor count of 1 initially

        TickBroadcast tickBroadcast = new TickBroadcast(1);
        messageBus.subscribeBroadcast(TickBroadcast.class, fusionSlamService);

        // Start the service
        Thread serviceThread = new Thread(fusionSlamService::run);
        serviceThread.start();
        messageBus.sendBroadcast(tickBroadcast);

        // Wait and verify
        Thread.sleep(100); // Allow time for processing
        assertFalse(serviceThread.isAlive(), "FusionSlamService should terminate when sensor count reaches 0");

        // Cleanup
        serviceThread.interrupt();
        serviceThread.join();
    }

    @Test
    public void testHandleCrashedBroadcast() throws InterruptedException {
        // Prepare test data
        List<DetectedObject> detectedObjects = new ArrayList<>();
        ArrayList<ArrayList<CloudPoint>> cloudPoints = new ArrayList<>();
        List<Pose> poses = new ArrayList<>();
        detectedObjects.add(new DetectedObject("1",  "Crashed Landmark"));
        StampedDetectedObjects s= new StampedDetectedObjects(10,detectedObjects);
        poses.add(new Pose(0, 0, 0, 1));
        ArrayList<CloudPoint> c= new ArrayList<>();
        c.add(new CloudPoint(0,0));
        cloudPoints.add(c);

        CrashedBroadcast crashedBroadcast = new CrashedBroadcast("Error detected", "Camera1", s, cloudPoints, poses);
        messageBus.subscribeBroadcast(CrashedBroadcast.class, fusionSlamService);

        // Start the service
        Thread serviceThread = new Thread(fusionSlamService::run);
        serviceThread.start();
        messageBus.sendBroadcast(crashedBroadcast);

        // Wait and verify
        Thread.sleep(100); // Allow time for processing
        assertFalse(serviceThread.isAlive(), "FusionSlamService should terminate on receiving CrashedBroadcast");

        // Cleanup
        serviceThread.interrupt();
        serviceThread.join();
    }
}

