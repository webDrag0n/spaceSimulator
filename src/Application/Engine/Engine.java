package Application.Engine;

import Application.Engine.graphics.GraphicsModuleModel;
import Application.Engine.operation.OperationModuleModel;
import Application.Engine.physics.PhysicsModuleModel;
import Application.Launcher;
import models.PhysicsComponents.Camera;
import models.PhysicsComponents.Star;
import models.PhysicsComponents.Universe;

/**
 * Created by lzx on 2017/6/13.
 * initialize at start, then every components are created under it
 */
public class Engine {

    private Launcher launcher;

    private PhysicsModuleModel physicsModule;
    private GraphicsModuleModel graphicsModule;
    private OperationModuleModel operationModule;

    //used components
    //generate a universe
    private Universe universe;

    //use a bufferStar for new input star
    private Star bufferStar;
    //define a camera used for display
    private Camera camera;


    public Engine(Launcher starter) {
        launcher = starter;
        //call the function to initialize all the stars in the universe
        initialize();
    }

    //function used to initialize all the stars to their default value
    private void initialize() {
        //initialize the universe
        universe = new Universe(10000, 10000);


        //initialize the buffer star
        bufferStar = new Star();

        //initialize the camera
        camera = new Camera(
                1000,
                560,
                universe.getWidth() / 2,
                universe.getHeight() / 2
        );


        //>>>>>>>>>>>>>>>>>>|[THREADS]|<<<<<<<<<<<<<<<<<<<<
        //physics module
        physicsModule = new PhysicsModuleModel(this);
        Thread physics = new Thread(physicsModule);
        physics.start();
        //halt until game canvas is initialized

        //graphics module
        graphicsModule = new GraphicsModuleModel(this);
        Thread graphics = new Thread(graphicsModule);
        graphics.start();
        //halt until game canvas is initialized

        operationModule = new OperationModuleModel(this);
        Thread operation = new Thread(operationModule);
        operation.start();
        //halt until game canvas is initialized
    }

    //getter and setters
    public Launcher getLauncher(){
        return launcher;
    }

    public Star getBufferStar() {
        return bufferStar;
    }

    public Camera getCamera() {
        return camera;
    }

    public Universe getUniverse() {
        return universe;
    }

    public PhysicsModuleModel getPhysicsModule(){
        return physicsModule;
    }

    public GraphicsModuleModel getGraphicsModule(){
        return graphicsModule;
    }

    public void setUniverse(Universe universe) {
        this.universe = universe;
    }
}
