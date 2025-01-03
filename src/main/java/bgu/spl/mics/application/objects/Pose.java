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
        double yawRadians= this.yaw * (Math.PI/180);
        double globalX = x + (localX * Math.cos(yawRadians)) - (localY * Math.sin(yawRadians));
        double globalY = y + (localX * Math.sin(yawRadians)) + (localY * Math.cos(yawRadians));
        return new double[]{globalX, globalY};
    }
    public double getX()
    {
        return this.x;
    }
    public double getY()
    {
        return this.y;
    }
    public double getYaw()
    {
        return this.yaw;
    }

}
