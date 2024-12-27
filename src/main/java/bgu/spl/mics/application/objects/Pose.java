package bgu.spl.mics.application.objects;

/**
 * Represents the robot's pose (position and orientation) in the environment.
 * Includes x, y coordinates and the yaw angle relative to a global coordinate system.
 */
public class Pose {
    private float x;
    private float y;
    private float yaw;
    private int time;

    public Pose(float x, float y, float yaw, int time) {
        this.x = x;
        this.y = y;
        this.yaw = yaw;
        this.time = time;
    }

    public int getTime(){return time;}
    public double[] transformToGlobal(double localX, double localY) {
        float globalX = (float) (x + localX * Math.cos(yaw) - localY * Math.sin(yaw));
        float globalY = (float) (y + localX * Math.sin(yaw) + localY * Math.cos(yaw));
        return new double[]{globalX, globalY};
    }

}
