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

    @BeforeEach
    public void setUp() {
        fusionSlam = FusionSlam.getInstance();
        fusionSlam.setLandmarks(new ArrayList<>());
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

          Pose currentPose = fusionSlam.getCurrentPose(3);
          assertEquals(pose2, currentPose, "Expected the most recent pose to be returned.");

    }
}

