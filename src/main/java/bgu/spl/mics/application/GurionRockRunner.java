package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        String configPath = args[0];
        Gson gson = new Gson();
        try{
            JsonObject configJson = JsonParser.parseReader(new FileReader(configPath)).getAsJsonObject();
            int tickTime = configJson.get("TickTime").getAsInt();
            int duration = configJson.get("Duration").getAsInt();

            List<Camera> cameras = new ArrayList<>();
            Type cameraListType = new TypeToken<List<JsonObject>>() {
            }.getType();
            List<JsonObject> cameraConfigs = gson.fromJson(configJson.get("Cameras"), cameraListType);
            for (JsonObject cameraConfig : cameraConfigs) {
                Camera camera = new Camera(
                        cameraConfig.get("id").getAsInt(),
                        cameraConfig.get("frequency").getAsInt(), STATUS.UP,
                        cameraConfig.get("camera_key").getAsString(),
                        cameraConfig.get("camera_datas_path").getAsString()
                        );
                cameras.add(camera);
            }

            List<LiDarWorkerTracker> lidarWorkers = new ArrayList<>();
            List<JsonObject> lidarConfigs = gson.fromJson(configJson.get("LidarWorkers"), cameraListType);
            for (JsonObject lidarConfig : lidarConfigs) {
                LiDarWorkerTracker lidar = new LiDarWorkerTracker(
                        lidarConfig.get("id").getAsInt(),
                        lidarConfig.get("frequency").getAsInt(),STATUS.UP,
                        lidarConfig.get("lidars_data_path").getAsString()
                );
                lidarWorkers.add(lidar);
            }
            String poseDataPath = configJson.get("poseJsonFile").getAsString();
            GPSIMU gpsimu= new GPSIMU(tickTime,STATUS.UP,poseDataPath);

            FusionSlam fusionSlam = FusionSlam.getInstance();

            List<Thread> microservices = new ArrayList<>();
            for (Camera camera : cameras) {
                microservices.add(new Thread(new CameraService(camera)));
            }
            for (LiDarWorkerTracker lidar : lidarWorkers) {
                microservices.add(new Thread(new LiDarService(lidar)));
            }
            microservices.add(new Thread(new PoseService(gpsimu)));
            microservices.add(new Thread(new FusionSlamService(fusionSlam)));

            // Initialize TimeService
            TimeService timeService = new TimeService(tickTime, duration);
            microservices.add(new Thread(timeService));

            //start all services
            for (Thread service : microservices) {
                service.start();
            }

            // Wait for all services to finish
            for (Thread service : microservices) {
                service.join();
            }
//לבדוק מה עושים פה עם הoutput file
        //    String outputPath = configPath.replace("config.json", "output_file.json");
   //         try (FileWriter writer = new FileWriter(outputPath)) {
          //      gson.toJson(fusionSlam.generateOutput(), writer);
           // }
        }
        catch (Exception e)
        {

        }
    }
}