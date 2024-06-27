package org.nexus.indexador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.nexus.indexador.controllers.frmCargando;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("frmCargando.fxml"));

        try {
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);

            // Obtener el controlador y pasar el Stage
            frmCargando controller = fxmlLoader.getController();
            controller.setStage(stage);
            controller.init();

            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Indexador Nexus: Iniciando");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar la interfaz de usuario: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}