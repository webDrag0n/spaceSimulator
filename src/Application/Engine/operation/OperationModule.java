package Application.Engine.operation;

import Application.Engine.world;
import Application.Engine.EngineSettings.Speed;
import Application.stages.MainStage.gameScene.GameScene;
import Application.status.CanvasStatus;
import javafx.application.Platform;
import Application.Engine.physics.physicsPrefabs.Star;
import models.SystemComponents.ThreadModuleModel;

/**
 * Created by lzx on 2017/7/13.
 *
 */
public class OperationModule extends ThreadModuleModel {

    private GameScene gameScene;
    private CanvasStatus canvasStatus;

    public OperationModule(world root_world){
        super(root_world);
    }

    @Override
    public void initialize(){
        //override default initialize block
        gameScene = world.getLauncher().getGameStage().getGameScene();

        canvasStatus = world.getLauncher().getCanvasStatus();
    }

    //determine if a new star should be created
    //left click to new a star
    private void addNewStar(){
        //open the new star lock
        systemStatus.setNewStarExist(true);

        //give the buffer star speed based on the distance mouse dragged
        world.getBufferStar().velocityX = (systemStatus.getDragLine()[2]
                - systemStatus.getDragLine()[0])
                / Speed.getDragSpeedConstant();
        world.getBufferStar().velocityY = (systemStatus.getDragLine()[3]
                - systemStatus.getDragLine()[1])
                / Speed.getDragSpeedConstant();

        //check if the new star lock is opened to avoid unnecessary star list iterations
        //check if there is empty star slot for a new star
        if (systemStatus.isNewStarExist()) {
            for (int i = 0; i < world.getUniverse().getStars().length; i++) {
                //not on screen means it is safe to clear
                if (systemStatus.isNewStarExist() & !world.getUniverse().getStars()[i].inUniverse) {

                    //prepare the empty slot for new star
                    world.getUniverse().getStars()[i].remove();
                    world.getUniverse().getStars()[i].initialize();

                    //give buffer star properties
                    world.getBufferStar().mass = gameScene.getMass();
                    world.getBufferStar().r = gameScene.getRadius();

                    //give the properties of buffer star to the empty star slot
                    world.getUniverse().getStars()[i] = new Star(world.getBufferStar());
                    //remove the buffer star (clear the values to default)
                    world.getBufferStar().remove();

                    //add the star according to the size of window(camera)
                    //and the enlarge scales
                    world.getUniverse().getStars()[i].add(
                            //convert the coordinate on screen to coordinate in the universe
                            //it's hard to explain the math, but it will be easy to understand
                            //once you draw it out on the paper, be careful changing it anyway
                            (world.getCamera().getCenterX() - world.getUniverse().getWidth() / 2)
                                    + (world.getUniverse().getWidth() - world.getCamera().getWidth()) / 2
                                    + systemStatus.getMouse_coordinate()[0] * world.getGraphicsModule().getScaleX(),
                            (world.getCamera().getCenterY() - world.getUniverse().getHeight() / 2)
                                    + (world.getUniverse().getHeight() - world.getCamera().getHeight()) / 2
                                    + systemStatus.getMouse_coordinate()[1] * world.getGraphicsModule().getScaleY()
                    );

                    //change the slot property from empty to full
                    world.getUniverse().getStars()[i].inUniverse = true;

                    //close the new star lock
                    systemStatus.setNewStarExist(false);

                    //refresh the screen
                    world.getGraphicsModule().drawShapes();
                }
            }
        }
        systemStatus.setMouseReleased(false);
    }


    @Override
    public void run() {
        while (!isExit()) {

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (systemStatus.isMousePressed()) {
                systemStatus.setMouseReleased(false);
                switch (systemStatus.getActivatedMouseButton()) {
                    case PRIMARY:
                        double dragline[] = systemStatus.getDragLine();
                        dragline[0] = systemStatus.getMouse_coordinate()[0];
                        dragline[1] = systemStatus.getMouse_coordinate()[1];
                        systemStatus.setDragLine(dragline);
                        break;
                }
            }

            if (systemStatus.isMouseReleased()) {
                systemStatus.setMousePressed(false);
                switch (systemStatus.getActivatedMouseButton()) {
                    case PRIMARY:
                        double dragline[] = systemStatus.getDragLine();
                        dragline[2] = systemStatus.getMouse_coordinate()[0];
                        dragline[3] = systemStatus.getMouse_coordinate()[1];
                        systemStatus.setDragLine(dragline);
                        addNewStar();
                        break;
                    case SECONDARY:
                        world.getPhysicsModule().clear();
                        break;
                    case MIDDLE:
                        //change pause value if middle button pressed
                        world.getPhysicsModule().setPause(!world.getPhysicsModule().isPause());
                        break;
                }
            }

            if (systemStatus.isMouseScrolled()) {
                systemStatus.setMouseReleased(false);
                systemStatus.setMousePressed(false);
                //on mouse wheel rolling back (minimize)
                if (systemStatus.getMouseScrollValue() < 0) {

                    //change the size of the camera (+)
                    world.getCamera().setWidth(world.getCamera().getWidth() + Speed.getSizeChangeSpeed());
                    world.getCamera().setHeight(world.getCamera().getHeight()
                            + Speed.getSizeChangeSpeed() * systemStatus.getHeightWidthScale());

                    //move the camera to the mouse coordinate to create an effect
                    world.getCamera().setCenterX(world.getCamera().getCenterX()
                                    - (systemStatus.getMouse_coordinate()[0] - canvasStatus.getCanvasWidth() / 2)
                                    / canvasStatus.getCanvasWidth() * Speed.getCameraMoveSpeed()
                    );
                    world.getCamera().setCenterY(world.getCamera().getCenterY()
                            - (systemStatus.getMouse_coordinate()[1] - canvasStatus.getCanvasHeight() / 2)
                            / canvasStatus.getCanvasHeight() * Speed.getCameraMoveSpeed()
                    );
                } else if (systemStatus.getMouseScrollValue() > 0) {
                    //on mouse wheel rolling back (enlarge)
                    world.getCamera().setWidth(world.getCamera().getWidth() - Speed.getSizeChangeSpeed());
                    world.getCamera().setHeight(world.getCamera().getHeight()
                            - Speed.getSizeChangeSpeed() * systemStatus.getHeightWidthScale()
                    );

                    //move the camera to the mouse coordinate to create an effect
                    world.getCamera().setCenterX(world.getCamera().getCenterX()
                            + (systemStatus.getMouse_coordinate()[0] - canvasStatus.getCanvasWidth() / 2)
                            / canvasStatus.getCanvasWidth() * Speed.getCameraMoveSpeed());
                    world.getCamera().setCenterY(world.getCamera().getCenterY()
                            + (systemStatus.getMouse_coordinate()[1] - canvasStatus.getCanvasHeight() / 2)
                            / canvasStatus.getCanvasHeight() * Speed.getCameraMoveSpeed());
                }

                //calculate the scale between camera and original camera
                world.getGraphicsModule().setScaleX(world.getCamera().getWidth() / world.getCamera().getOriginalWidth());
                world.getGraphicsModule().setScaleY(world.getCamera().getHeight() / world.getCamera().getOriginalHeight());

                //refresh the screen
                world.getGraphicsModule().drawShapes();
            }

            systemStatus.setMouseReleased(false);
            systemStatus.setMousePressed(false);
            systemStatus.setMouseScrolled(false);

            gameScene.getCreateStarMenu().getSettingBtn().setVisible(systemStatus.isCreateStarMenuOut());
            gameScene.getCreateStarMenu().getMassSlider().setVisible(systemStatus.isCreateStarMenuOut());
            gameScene.getCreateStarMenu().getRaiusSlider().setVisible(systemStatus.isCreateStarMenuOut());

            if (systemStatus.isSettingStageOut()) {
                Platform.runLater(() -> world.getLauncher().getSettingStage().show());
            }else {
                Platform.runLater(() -> world.getLauncher().getSettingStage().hide());
            }

            Platform.runLater(() -> gameScene.getStatusBar().update());
        }

    }
}
