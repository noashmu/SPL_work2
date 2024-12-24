package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.DetectObjectsEvent;
import com.sun.org.apache.bcel.internal.generic.RETURN;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private int id;
    private int frequency;
    private STATUS status;
    //maybe need to change the type of the list
    private List<StampedDetectedObjects> detectedObjectsList;

    public Camera(int id, int frequency, STATUS status) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.detectedObjectsList = new ArrayList<StampedDetectedObjects>();
    }

    public boolean isActive() {
        if (status.equals(STATUS.UP))
            return true;
        return false;
    }

    public boolean shouldSendEvent(int currTick) {
        if (currTick >= this.frequency && currTick % this.frequency == 0)
            return true;
        return false;
    }


    public List<DetectedObject> getDetectedObject(int currTick) {
        for (StampedDetectedObjects stampedObject : detectedObjectsList) {
            if (stampedObject.getTime() == currTick) {
                return stampedObject.getDetectedObjects();
            }
        }
        return new ArrayList<>();

    }

    public DetectObjectsEvent createDetectObjectsEvent(int currTick) {

        return new DetectObjectsEvent(getDetectedObject(currTick),currTick);
    }

    public STATUS getStatus() {
        return status;
    }
}
