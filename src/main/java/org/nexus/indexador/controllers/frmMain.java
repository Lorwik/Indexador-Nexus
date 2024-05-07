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

    private ObservableList<grhData> grhList;

    private configManager configManager;

    private static boolean consoleOpen = false; // Variable para rastrear si la ventana de la consola está abierta

    private int currentIndex = 1;
    private Timeline animationTimeline;

    /**
     * Método de inicialización del controlador. Carga los datos de gráficos y configura el ListView.
     */
    @FXML
    protected void initialize() {
        loadGrhData();
        setupGrhListListener();
    }

    /**
     * Carga los datos de gráficos desde archivos binarios y actualiza la interfaz de usuario con la información obtenida.
     * Muestra los índices de gráficos en el ListView y actualiza los textos de los labels con información relevante.
     * @throws IOException Sí ocurre un error durante la lectura de los archivos binarios.
     */
    private void loadGrhData() {
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configura un listener para el ListView para capturar los eventos de selección.
     * Cuando se selecciona un índice de gráfico, actualiza el editor y el visor con la información correspondiente.
     */
    private void setupGrhListListener() {
        // Agregar un listener al ListView para capturar los eventos de selección
        lstIndices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            // Detenemos la animación actual si existe
            if (animationTimeline != null) {
                animationTimeline.stop();
            }

            // Obtener el índice seleccionado
            int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();

            if (selectedIndex >= 0) {
                // Obtener el objeto grhData correspondiente al índice seleccionado
                grhData selectedGrh = grhList.get(selectedIndex);
                updateEditor(selectedGrh);
                updateViewer(selectedGrh);
            }
        });
    }

    /**
     * Actualiza el editor con la información del gráfico seleccionado.
     * Muestra los detalles del gráfico seleccionado en los campos de texto correspondientes.
     * @param selectedGrh El gráfico seleccionado.
     */
    private void updateEditor(grhData selectedGrh) {
        // Obtenemos todos los datos
        int fileGrh = selectedGrh.getFileNum();
        int nFrames = selectedGrh.getNumFrames();
        int x = selectedGrh.getsX();
        int y = selectedGrh.getsY();
        int width = selectedGrh.getTileWidth();
        int height = selectedGrh.getTileHeight();

        txtImagen.setText(String.valueOf(fileGrh));
        txtNumFrames.setText(String.valueOf(nFrames));
        txtPosX.setText(String.valueOf(x));
        txtPosY.setText(String.valueOf(y));
        txtAncho.setText(String.valueOf(width));
        txtAlto.setText(String.valueOf(height));
        txtIndice.setText("Grh" + selectedGrh.getGrh() + "=" + nFrames + "-" + fileGrh + "-" + x + "-" + y + "-" + width + "-" + height);
    }

    /**
     * Actualiza el visor con el gráfico seleccionado.
     * Si el gráfico es estático, muestra la imagen estática correspondiente. Si es una animación, muestra la animación.
     * @param selectedGrh El gráfico seleccionado.
     */
    private void updateViewer(grhData selectedGrh) {
        int nFrames = selectedGrh.getNumFrames();
        if (nFrames == 1) {
            displayStaticImage(selectedGrh);
        } else {
            displayAnimation(selectedGrh, nFrames);
        }
    }

    private void displayStaticImage(grhData selectedGrh) {
        // Construir la ruta completa de la imagen para imagePath
        String imagePath = configManager.getGraphicsDir() + selectedGrh.getFileNum() + ".png";
        File imageFile = new File(imagePath);

        if (imageFile.exists()) {
            // El archivo existe, cargar la imagen
            Image staticImage = new Image(imageFile.toURI().toString());
            imgGrafico.setImage(staticImage);

            // Recortar la región adecuada de la imagen completa
            PixelReader pixelReader = staticImage.getPixelReader();
            WritableImage croppedImage = new WritableImage(pixelReader, selectedGrh.getsX(), selectedGrh.getsY(), selectedGrh.getTileWidth(), selectedGrh.getTileHeight());

            // Establecer el tamaño preferido del ImageView para que coincida con el tamaño de la imagen
            imgIndice.setFitWidth(selectedGrh.getTileWidth()); // Ancho de la imagen
            imgIndice.setFitHeight(selectedGrh.getTileHeight()); // Alto de la imagen

            // Desactivar la preservación de la relación de aspecto
            imgIndice.setPreserveRatio(false);

            // Mostrar la región recortada en el ImageView
            imgIndice.setImage(croppedImage);

        } else {
            // El archivo no existe, mostrar un mensaje de error o registrar un mensaje de advertencia
            System.out.println("El archivo de imagen no existe: " + imagePath);
        }

    }

    private void displayAnimation(grhData selectedGrh, int nFrames) {
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

    @FXML
    private void saveGrhData() {
        // Obtenemos el índice seleccionado en la lista:
        int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();

        // Nos aseguramos de que el índice es válido
        if (selectedIndex >= 0) {
            // Obtenemos el objeto grhData correspondiente al índice seleccionado
            grhData selectedGrh = grhList.get(selectedIndex);

            // Comenzamos aplicar los cambios:
            selectedGrh.setFileNum(Integer.parseInt(txtImagen.getText()));
            selectedGrh.setNumFrames((Integer.parseInt(txtNumFrames.getText())));
            selectedGrh.setsX(Integer.parseInt(txtPosX.getText()));
            selectedGrh.setsY(Integer.parseInt(txtPosY.getText()));
            selectedGrh.setTileWidth(Integer.parseInt(txtAncho.getText()));
            selectedGrh.setTileHeight(Integer.parseInt(txtAlto.getText()));

            System.out.println(("Cambios aplicados!"));

        }
    }
}