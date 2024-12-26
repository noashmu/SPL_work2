package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.List;


public class TrackedObjectsEvent implements Event<Boolean> {

    private List<TrackedObject> trackedObjects;
    private int time;

    /**
     * Constructor for TrackedObjectsEvent.
     *
     * @param trackedObjects A list of tracked objects with their coordinates.
     * @param time The time when the tracked objects were recorded.
     */
    public TrackedObjectsEvent(List<TrackedObject> trackedObjects, int time) {
        this.trackedObjects = trackedObjects;
        this.time = time;
    }

    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }

    public int getTime() {
        return time;
    }
}
