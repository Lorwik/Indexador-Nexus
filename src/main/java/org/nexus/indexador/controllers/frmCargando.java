package org.nexus.indexador.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.nexus.indexador.Main;

import java.io.IOException;

public class frmCargando {

    private Stage currentStage;

    public void setStage(Stage stage) {
        this.currentStage = stage;
    }

    @FXML
    protected void initialize() {
        // Inicialización básica si es necesario
    }

    public void init() {
        // Ejecutar la lectura de configuración y apertura de nueva ventana en un hilo separado
        new Thread(() -> {
            org.nexus.indexador.utils.configManager configManager = org.nexus.indexador.utils.configManager.getInstance();

            try {
                // Simular tiempo de carga
                Thread.sleep(2000);

                // Lectura de la configuración
                configManager.readConfig();
            } catch (IOException | InterruptedException e) {
                System.err.println("Error al leer la configuración: " + e.getMessage());
            }

            Platform.runLater(() -> {
                // Crea la nueva ventana
                Stage newStage = new Stage();
                newStage.setTitle("Indexador Nexus");

                // Lee el archivo FXML para la nueva ventana
                try {
                    Parent consoleRoot = FXMLLoader.load(Main.class.getResource("frmMain.fxml"));
                    newStage.setScene(new Scene(consoleRoot));
                    newStage.setResizable(false);

                    // Cerrar la ventana actual (frmCargando)
                    if (currentStage != null) {
                        currentStage.close();
                    }

                    newStage.centerOnScreen();
                    newStage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }
}