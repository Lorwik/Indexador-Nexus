package org.nexus.indexador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.nexus.indexador.utils.configManager;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        configManager configManager = org.nexus.indexador.utils.configManager.getInstance();

        try {// Lectura de la configuración
            configManager.readConfig();
        } catch (IOException e) {
            System.err.println("Error al leer la configuración: " + e.getMessage());
        }

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("frmMain.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("Indexador Nexus");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar la interfaz de usuario: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}