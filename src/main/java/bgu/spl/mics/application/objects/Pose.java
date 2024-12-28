package bgu.spl.mics.application.objects;

/**
 * Represents the robot's pose (position and orientation) in the environment.
 * Includes x, y coordinates and the yaw angle relative to a global coordinate system.
 */
public class Pose {
    private double x;
    private double y;
    private double yaw;
    private int time;

    public Pose(double x, double y, double yaw, int time) {
        this.x = x;
        this.y = y;
        this.yaw = yaw;
        this.time = time;
    }

    public int getTime(){return time;}
    public double[] transformToGlobal(double localX, double localY) {
        double globalX = (float) (x + localX * Math.cos(yaw) - localY * Math.sin(yaw));
        double globalY = (float) (y + localX * Math.sin(yaw) + localY * Math.cos(yaw));
        return new double[]{globalX, globalY};
    }

}
