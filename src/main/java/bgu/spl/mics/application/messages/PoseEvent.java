package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;


public class PoseEvent implements Event<Void> {

    private Pose pose;

    /**
     * Constructor for PoseEvent.
     *
     * @param pose The current pose of the robot.
     */
    public PoseEvent(Pose pose) {
        this.pose = pose;
    }

    public Pose getPose() {
        return pose;
    }
}
