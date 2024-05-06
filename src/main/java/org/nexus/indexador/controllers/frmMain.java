package org.nexus.indexador.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.nexus.indexador.Main;
import org.nexus.indexador.models.grhData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.nexus.indexador.utils.configManager;

import java.io.File;
import java.io.IOException;

public class frmMain {

    @FXML
    private ListView<String> lstIndices;

    @FXML
    private Label lblIndices;

    @FXML
    private Label lblVersion; // Nuevo Label agregado

    @FXML
    private TextField txtImagen;

    @FXML
    private TextField txtNumFrames;

    @FXML
    private TextField txtPosX;

    @FXML
    private TextField txtPosY;

    @FXML
    private TextField txtAncho;

    @FXML
    private TextField txtAlto;

    @FXML
    private TextField txtIndice;

    @FXML
    private ImageView imgIndice;

    @FXML
    private ImageView imgGrafico;

    @FXML
    private MenuItem mnuConsola;

    private ObservableList<grhData> grhList;

    private configManager configManager;

    private static boolean consoleOpen = false; // Variable para rastrear si la ventana de la consola está abierta

    private int currentIndex = 1;
    private Timeline animationTimeline;

    @FXML
    protected void initialize() {

        grhData grhDataManager = new grhData(); // Crear una instancia de grhData
        configManager = org.nexus.indexador.utils.configManager.getInstance(); // Inicializar configManager

        try {
            // Llamar al método para leer el archivo binario y obtener la lista de grhData
            grhList = grhDataManager.readGrhFile();

            // Actualizar el texto de los labels con la información obtenida
            lblIndices.setText("Indices cargados: " + grhDataManager.getGrhCount());
            lblVersion.setText("Versión de Indices: " + grhDataManager.getVersion());

            // Agregar los índices de gráficos al ListView
            ObservableList<String> grhIndices = FXCollections.observableArrayList();
            for (grhData grh : grhList) {
                String indice = String.valueOf(grh.getGrh());
                if (grh.getNumFrames() > 1) {
                    indice += " (Animación)"; // Agregar indicación de animación
                }
                grhIndices.add(indice);
            }
            lstIndices.setItems(grhIndices);

            // Agregar un listener al ListView para capturar los eventos de selección
            lstIndices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                // Obtener el índice seleccionado
                int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();

                if (selectedIndex >= 0) {
                    // Obtener el objeto grhData correspondiente al índice seleccionado
                    grhData selectedGrh = grhList.get(selectedIndex);

                    // Obtenemos todos los datos
                    int fileGrh = selectedGrh.getFileNum();
                    int nFrames = selectedGrh.getNumFrames();
                    int x = selectedGrh.getsX();
                    int y = selectedGrh.getsY();
                    int width = selectedGrh.getTileWidth();
                    int height = selectedGrh.getTileHeight();

                    /** Editor de Grh **/
                    txtImagen.setText(String.valueOf(fileGrh));
                    txtNumFrames.setText(String.valueOf(nFrames));
                    txtPosX.setText(String.valueOf(x));
                    txtPosY.setText(String.valueOf(y));
                    txtAncho.setText(String.valueOf(width));
                    txtAlto.setText(String.valueOf(height));
                    txtIndice.setText("Grh" + selectedGrh.getGrh() + "=" + nFrames + "-" + fileGrh + "-" + x + "-" + y + "-" + width + "-" + height);

                    /** VISOR **/

                    // ¿Es animación o imagen estática?
                    if (nFrames == 1) {

                        // Construir la ruta completa de la imagen para imagePath
                        String imagePath = configManager.getGraphicsDir() + selectedGrh.getFileNum() + ".png";

                        // Cargar la imagen completa desde el recurso
                        Image fullImage = new Image(imagePath);

                        File imageFile = new File(imagePath);

                        if (imageFile.exists()) {
                            // El archivo existe, cargar la imagen
                            Image image = new Image(imageFile.toURI().toString());
                            imgGrafico.setImage(image);

                        } else {
                            // El archivo no existe, mostrar un mensaje de error o registrar un mensaje de advertencia
                            System.out.println("El archivo de imagen no existe: " + imagePath);
                        }

                        // Recortar la región adecuada de la imagen completa
                        PixelReader pixelReader = fullImage.getPixelReader();
                        WritableImage croppedImage = new WritableImage(pixelReader, x, y, width, height);

                        // Establecer el tamaño preferido del ImageView para que coincida con el tamaño de la imagen
                        imgIndice.setFitWidth(width); // Ancho de la imagen
                        imgIndice.setFitHeight(height); // Alto de la imagen

                        // Desactivar la preservación de la relación de aspecto
                        imgIndice.setPreserveRatio(false);

                        // Mostrar la región recortada en el ImageView
                        imgIndice.setImage(croppedImage);

                    } else { // Animacion

                        // Configurar la animación
                        if (animationTimeline != null) {
                            animationTimeline.stop();
                        }

                        animationTimeline = new Timeline(
                                new KeyFrame(Duration.ZERO, event -> {
                                    // Actualizar la imagen en el ImageView con el frame actual
                                    updateFrame(selectedGrh);
                                    currentIndex = (currentIndex + 1) % nFrames; // Avanzar al siguiente frame circularmente
                                }),
                                new KeyFrame(Duration.millis(100)) // Ajustar la duración según sea necesario
                        );
                        animationTimeline.setCycleCount(Animation.INDEFINITE); // Repetir la animación indefinidamente
                        animationTimeline.play(); // Iniciar la animación
                    }
                }
            });

        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    private void updateFrame(grhData selectedGrh) {
        int[] frames = selectedGrh.getFrames(); // Obtener el arreglo de índices de los frames de la animación

        if (currentIndex > 0) {

            grhData currentGrh = grhList.get(frames[currentIndex]);
            String imagePath = configManager.getGraphicsDir() + currentGrh.getFileNum() + ".png";

            Image frameImage = new Image(imagePath);
            PixelReader pixelReader = frameImage.getPixelReader();
            WritableImage croppedImage = new WritableImage(pixelReader, currentGrh.getsX(), currentGrh.getsY(), currentGrh.getTileWidth(), currentGrh.getTileHeight());
            imgIndice.setImage(croppedImage);
        }
    }

    // Método para manejar la acción cuando se hace clic en el elemento del menú "Consola"
    @FXML
    private void openConsoleWindow() {
        if (!consoleOpen) {
            // Crea la nueva ventana
            Stage consoleStage = new Stage();
            consoleStage.setTitle("Consola");

            // Lee el archivo FXML para la ventana
            try {
                Parent consoleRoot = FXMLLoader.load(Main.class.getResource("frmConsola.fxml"));
                consoleStage.setScene(new Scene(consoleRoot));
                consoleStage.setResizable(false);
                consoleStage.show();

                consoleOpen = true; // Actualiza el estado para indicar que la ventana de la consola está abierta

                // Listener para detectar cuándo se cierra la ventana de la consola
                consoleStage.setOnCloseRequest(event -> {
                    consoleOpen = false; // Actualiza el estado cuando se cierra la ventana de la consola
                });

            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }
}