import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ProgramExecutor extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("main.fxml"));
        Scene mainScene = new Scene(parent);
        primaryStage.getIcons().add(new Image(getClass().getResource("mainIcon.png").toString()));
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }
}
