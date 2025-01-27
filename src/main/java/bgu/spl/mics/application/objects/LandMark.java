package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    private String id;
    private String description;
    private List<CloudPoint> coordinates;

    public LandMark(String id, String description) {
        this.id = id;
        this.description = description;
        coordinates = new ArrayList<>();
    }
    public String getId()
    {
        return this.id;
    }

    public String getDescription(){ return this.description; }

    public List<CloudPoint> getCoordinates()
    {
        return this.coordinates;
    }

    public void updateCoordinates(List<CloudPoint> l)
    {
        if (l == null || l.isEmpty()) {
            return;
        }
        if (coordinates.isEmpty()) {
            coordinates.addAll(l);
            return;
        }

        int size = Math.min(coordinates.size(), l.size());
        for (int i = 0; i < size; i++) {
            CloudPoint currentPoint = coordinates.get(i);
            CloudPoint newPoint = l.get(i);

            currentPoint.setX((currentPoint.getX() + newPoint.getX()) / 2.0);
            currentPoint.setY((currentPoint.getY() + newPoint.getY()) / 2.0);
        }

        if (l.size() > size) {
            coordinates.addAll(l.subList(size, l.size()));
        }
    }
}
