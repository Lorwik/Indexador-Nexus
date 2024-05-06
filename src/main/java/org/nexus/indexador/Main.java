package org.nexus.indexador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.nexus.indexador.utils.configManager;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        configManager configManager = org.nexus.indexador.utils.configManager.getInstance();

        //Leemos la configuración
        configManager.readConfig();

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("frmMain.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Indexador Nexus");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}