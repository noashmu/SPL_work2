package bgu.spl.mics.application.objects;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    private int systemRuntime;
    private int numOfDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;
    private int tickTime;
    private Map<Integer,StampedDetectedObjects> lastFramesCameras;
    private Map<Integer,TrackedObject> lastFramesLidars;

    private static class StatisticalFolderHolder {
        private static final StatisticalFolder instance = new StatisticalFolder();
    }

    private StatisticalFolder() {
        this.systemRuntime = 0;
        this.numOfDetectedObjects = 0;
        this.numTrackedObjects = 0;
        this.numLandmarks = 0;
        this.tickTime = 0;
        this.lastFramesCameras = new ConcurrentHashMap<>();
        this.lastFramesLidars = new ConcurrentHashMap<>();
    }

    public static StatisticalFolder getInstance() {return StatisticalFolderHolder.instance;}

    public void setTickTime(int tickTime) {this.tickTime = tickTime;}

    public void updateLastFramesLidars(int id, TrackedObject trackedObject) {
        this.lastFramesLidars.put(id, trackedObject);
    }

    public void updateLastFramesCameras(int id, StampedDetectedObjects detectedObject) {
        this.lastFramesCameras.put(id, detectedObject);
    }

    public synchronized void incrementRuntime() {
        this.systemRuntime=this.systemRuntime+tickTime;
    }

    public synchronized void subRuntime(){
        this.systemRuntime=this.systemRuntime-tickTime;
    }

    public synchronized void addDetectedObjects(int count) {
        this.numOfDetectedObjects += count;
    }

    public synchronized void addTrackedObjects(int count) {
        this.numTrackedObjects += count;
    }

    public synchronized void addLandmark() {
        this.numLandmarks++;
    }

    public synchronized void createOutputFile(String filePath, List<DetectedObject> detectedObjects,
                                              ArrayList<ArrayList<CloudPoint>> cloudPoints, List<Pose> robotPoses) {
            String jsonContent = "{";
            jsonContent += "\"systemRuntime\": " + systemRuntime + ",";
            jsonContent += "\"numDetectedObjects\": " + numOfDetectedObjects + ",";
            jsonContent += "\"numTrackedObjects\": " + numTrackedObjects + ",";
            jsonContent += "\"numLandmarks\": " + numLandmarks + "," + "\n";
            jsonContent += "\"LandMarks\": { \n";
            for(LandMark l:FusionSlam.getInstance().getLandmarks()){
                jsonContent += "    \""+l.getId()+"\"" +":{"+"\"id\":" +"\""+ l.getId()+"\""+","  + "\"description\":" +"\""+ l.getDescription() +"\""+","+ "\"coordinates\":[";
                for (CloudPoint point : l.getCoordinates()){
                    jsonContent += "{"+"\"x\":" +point.getX() + ",\"y\":" +point.getY() + "},";
                }
                if (!l.getCoordinates().isEmpty()) {
                    jsonContent = jsonContent.substring(0, jsonContent.length() - 1);
                }
                jsonContent += "]},"+"\n";
            }

            jsonContent = jsonContent.substring(0, jsonContent.length() - 2);

            jsonContent += "\n"+"}}";
            final String filePath2 = filePath + "/output_file.json";
            final String js2=jsonContent;
            Thread writerThread = new Thread(() -> {
                //      filePath = filePath + "/output.json";
                try (FileWriter file = new FileWriter(filePath2)) {
                    file.write(js2);
                    file.flush();
                } catch (IOException e) {
                    System.err.println("Error creating the output file: " + e.getMessage());
                }
            });
            writerThread.start();


    }

        public synchronized void createOutputFileError(String filePath, String errorDescription, String errorSource,
                                                  ArrayList<ArrayList<CloudPoint>> cloudPoints, List<Pose> robotPoses) {
            String jsonContent = "{";
            jsonContent += "\"error\": \"" + errorDescription + "\",\n";
            jsonContent += "  \"faultySensor\": \"" + errorSource + "\",\n";
            jsonContent += "  \"lastCamerasFrame\": {"+"\n";

            if(!lastFramesCameras.isEmpty()){
                for(Map.Entry<Integer, StampedDetectedObjects> entry : lastFramesCameras.entrySet()){
                    jsonContent += "    \"Camera" +entry.getKey()+"\": {";
                    jsonContent += "\"time\": " + entry.getValue().getTime() + ",";
                    jsonContent += "\"detectedObjects\": [";
                    List<DetectedObject> d =entry.getValue().getDetectedObjects();
                    jsonContent += "{\"id\": \"" + d.get(d.size()-1).getId() + "\",";
                    jsonContent += "\"description\": \"" + d.get(d.size()-1).getDescription() + "\"}";
                    jsonContent += "]}," + "\n";
                }
                jsonContent = jsonContent.substring(0, jsonContent.length() - 2);
            }

            jsonContent += "\n";
            jsonContent += "  },\n";
            jsonContent += "  \"lastLiDarWorkerTrackersFrame\": {"+"\n";

            int index= 0;
            if(!lastFramesLidars.isEmpty()){
                for(Map.Entry<Integer, TrackedObject> entry : lastFramesLidars.entrySet()){
                    jsonContent += "    \"LiDarWorkerTracker" + entry.getKey() +"\": [";
                    jsonContent += "{\"id\": \"" + entry.getValue().getId() + "\",";
                    jsonContent += "\"time\": " + entry.getValue().getTime() + ",";
                    jsonContent += "\"description\": \"" + entry.getValue().getDescription() + "\",";
                    jsonContent += "\"coordinates\": [";
                    if (!cloudPoints.isEmpty() && index!=cloudPoints.size()-1) {
                        ArrayList<CloudPoint> pointArr=  entry.getValue().getCoordinates();
                        for (CloudPoint point : pointArr) {

                            jsonContent += "{\"x\": " + point.getX() + ",\"y\": " + point.getY() + "},";

                        }
                        jsonContent = jsonContent.substring(0, jsonContent.length() - 1);
                        jsonContent += "]}],\n";
                        index++;
                    }
                }
            }

            jsonContent = jsonContent.substring(0, jsonContent.length() - 2);
            jsonContent += "\n  },\n";
            jsonContent += "  \"poses\": [";
            if (robotPoses!=null && !robotPoses.isEmpty()) {
                for (Pose pose : robotPoses) {
                    if (pose!=null) {
                        jsonContent += "{\"time\": " + pose.getTime() + ",";
                        jsonContent += "\"x\": " + pose.getX() + ",";
                        jsonContent += "\"y\": " + pose.getY() + ",";
                        jsonContent += "\"yaw\": " + pose.getYaw() + "},";
                    }
                }
                jsonContent = jsonContent.substring(0, jsonContent.length() - 1);
            }
            jsonContent += "],";
            jsonContent += "\n";

            jsonContent += "  \"statistics\": {";
            jsonContent += "\"systemRuntime\": " + this.systemRuntime + ",";
            jsonContent += "\"numDetectedObjects\": " + this.numOfDetectedObjects+ ",";
            jsonContent += "\"numTrackedObjects\": " +this.numTrackedObjects + ",";
            jsonContent += "\"numLandmarks\": " + this.numLandmarks + ",";
            jsonContent += "\"landMarks\": {";
            for (LandMark landMark : FusionSlam.getInstance().getLandmarks()) {
                jsonContent += "\"" + landMark.getId() + "\": {";
                jsonContent += "\"id\": \"" + landMark.getId() + "\",";
                jsonContent += "\"description\": \"" + landMark.getDescription() + "\",";
                jsonContent += "\"coordinates\": [";
                for (CloudPoint point : landMark.getCoordinates()) {
                    jsonContent += "{\"x\": " + point.getX() + ",\"y\": " + point.getY() + "},";
                }
                if (!landMark.getCoordinates().isEmpty()) {
                    jsonContent = jsonContent.substring(0, jsonContent.length() - 1);
                }
                jsonContent += "]},";
            }
            if (!FusionSlam.getInstance().getLandmarks().isEmpty()) {
                jsonContent = jsonContent.substring(0, jsonContent.length() - 1);
            }
            jsonContent += "}}";
            jsonContent += "}";

            final String filePath2 = filePath + "/OutputError.json";
            final String js2=jsonContent;
            Thread writerThread = new Thread(() -> {
                //      filePath = filePath + "/output.json";
                try (FileWriter file = new FileWriter(filePath2)) {
                    file.write(js2);
                    file.flush();
                } catch (IOException e) {
                    System.err.println("Error creating the output file: " + e.getMessage());
                }
            });
            writerThread.start();
        }
    }
