package org.nexus.indexador.controllers;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import org.nexus.indexador.models.grhData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.nexus.indexador.utils.byteMigration;
import org.nexus.indexador.utils.configManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private ImageView imgIndice;

    @FXML
    private ImageView imgGrafico;

    private ObservableList<grhData> grhList;

    private List<Image> animationFrames;
    private AnimationTimer animationTimer;
    private int currentFrameIndex = 0;

    @FXML
    protected void initialize() {

        grhData grhDataManager = new grhData(); // Crear una instancia de grhData
        configManager configManager = org.nexus.indexador.utils.configManager.getInstance();

        // Inicializa la lista de cuadros de animación
        animationFrames = new ArrayList<>();

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

                    /** Editor de Grh **/
                    txtImagen.setText(String.valueOf(selectedGrh.getFileNum()));
                    txtNumFrames.setText(String.valueOf(selectedGrh.getNumFrames()));
                    txtPosX.setText(String.valueOf(selectedGrh.getsX()));
                    txtPosY.setText(String.valueOf(selectedGrh.getsY()));
                    txtAncho.setText(String.valueOf(selectedGrh.getTileWidth()));
                    txtAlto.setText(String.valueOf(selectedGrh.getTileHeight()));

                    /** VISOR **/

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
                    int x = selectedGrh.getsX();
                    int y = selectedGrh.getsY();
                    int width = selectedGrh.getTileWidth();
                    int height = selectedGrh.getTileHeight();
                    PixelReader pixelReader = fullImage.getPixelReader();

                    WritableImage croppedImage = new WritableImage(pixelReader, x, y, width, height);

                    // Mostrar la región recortada en el ImageView
                    imgIndice.setImage(croppedImage);

                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}