package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.TrackedObjectsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    private int id;
    private int frequency;
    private STATUS status;
    private List<TrackedObject> lastTrackedObjects;

    public LiDarWorkerTracker(int id, int frequency, STATUS status) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        lastTrackedObjects = new ArrayList<TrackedObject>();
    }
    public int getId()
    {
        return this.id;
    }

    public List<TrackedObject> getLastTrackedObjects(){
        return this.lastTrackedObjects;
    }

//    public void setLastTrackedObjects(List<DetectedObject> detectedObjectList){
//        this.lastTrackedObjects = new ArrayList<TrackedObject>()
//    }

    public boolean shouldSendEvent(int currTick) {
        if (currTick >= this.frequency && currTick % this.frequency == 0)
            return true;
        return false;
    }

}
