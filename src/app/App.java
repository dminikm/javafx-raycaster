package app;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;

public class App extends Application {
    public static void main(String[] args) throws Exception
    {
        launch(args);
    }

    @Override
    public void start(Stage stage) 
    {
        stage.setTitle( "Canvas Example" );
        
        Group root = new Group();
        Scene scene = new Scene( root );
        stage.setScene( scene );
        
        final int width = 1280;
        final int height = 720;

        Canvas canvas = new Canvas( width, height );
        root.getChildren().add( canvas );

        Keyregistry r = Keyregistry.constructInstance(scene);

        Gameloop g = new Gameloop(canvas);
        g.start();


        stage.show();
    }
}