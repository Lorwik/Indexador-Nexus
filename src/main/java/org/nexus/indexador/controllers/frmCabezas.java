package org.nexus.indexador.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import org.nexus.indexador.models.grhData;
import org.nexus.indexador.models.headData;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class frmCabezas {

    @FXML
    public ListView lstHeads;
    @FXML
    public ImageView imgOeste;
    @FXML
    public ImageView imgNorte;
    @FXML
    public ImageView imgEste;
    @FXML
    public ImageView imgSur;
    @FXML
    public TextField txtNGrafico;
    @FXML
    public TextField txtStartX;
    @FXML
    public TextField txtStartY;
    @FXML
    public Label lblNCabezas;
    @FXML
    public Button btnSave;
    @FXML
    public TextField txtFiltro;
    @FXML
    public Button btnAdd;
    @FXML
    public Button btnDelete;

    private headData headDataManager; // Objeto que gestiona los datos de las cabezas, incluyendo la carga y manipulación de los mismos
    private ObservableList<headData> headList; // Lista observable que contiene los datos de los gráficos indexados.

    private org.nexus.indexador.utils.configManager configManager; // Objeto encargado de manejar la configuración de la aplicación, incluyendo la lectura y escritura de archivos de configuración.

    @FXML
    protected void initialize() {

        configManager = org.nexus.indexador.utils.configManager.getInstance();

        headDataManager = new headData(); // Crear una instancia de grhData

        loadHeadData();
        setupHeadListListener();
    }

    private void loadHeadData() {
        try {
            // Llamar al método para leer el archivo binario y obtener la lista de grhData
            headList = headDataManager.readHeadFile();

            // Actualizar el texto de los labels con la información obtenida
            lblNCabezas.setText("Cabezas cargadas: " + headData.getNumHeads());

            // Agregar los índices de gráficos al ListView
            ObservableList<String> headIndices = FXCollections.observableArrayList();
            for (int i = 1; i < headList.size() + 1; i++) {
                headIndices.add(String.valueOf(i));
            }

            lstHeads.setItems(headIndices);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupHeadListListener() {
        // Agregar un listener al ListView para capturar los eventos de selección
        lstHeads.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            // Obtener el índice seleccionado
            int selectedIndex = lstHeads.getSelectionModel().getSelectedIndex();

            if (selectedIndex >= 0) {
                // Obtener el objeto grhData correspondiente al índice seleccionado
                headData selectedHead = headList.get(selectedIndex);
                updateEditor(selectedHead);

                for (int i = 0; i <= 3; i++) {
                    drawHeads(selectedHead, i);
                }
            }
        });
    }

    private void updateEditor(headData selectedHead) {
        // Obtenemos todos los datos
        short Texture = selectedHead.getTexture();
        short StartX = selectedHead.getStartX();
        short StartY = selectedHead.getStartY();

        txtNGrafico.setText(String.valueOf(Texture));
        txtStartX.setText(String.valueOf(StartX));
        txtStartY.setText(String.valueOf(StartY));

    }

    private void drawHeads(headData selectedHead, int heading) {
        // Construir la ruta completa de la imagen para imagePath
        String imagePath = configManager.getGraphicsDir() + selectedHead.getTexture() + ".png";
        File imageFile = new File(imagePath);

        // ¿La imagen existe?
        if (imageFile.exists()) {
            Image staticImage = new Image(imageFile.toURI().toString());

            int textureX2 = 27;
            int textureY2 = 32;
            int textureX1 = selectedHead.getStartX();
            int textureY1 = (heading * textureY2) + selectedHead.getStartY();

            // Verificar que las coordenadas de recorte estén dentro de los límites de la imagen
            if (textureX1 + textureX2 > staticImage.getWidth()) {
                textureX1 = (int) staticImage.getWidth() - textureX2;
            }
            if (textureY1 + textureY2 > staticImage.getHeight()) {
                textureY1 = (int) staticImage.getHeight() - textureY2;
            }

            // Recortar la región adecuada de la imagen completa
            PixelReader pixelReader = staticImage.getPixelReader();
            WritableImage croppedImage = new WritableImage(pixelReader, textureX1, textureY1, textureX2, textureY2);

            // Desactivar la preservación de la relación de aspecto
            imgNorte.setPreserveRatio(false);

            // Mostrar la región recortada en el ImageView correspondiente
            switch (heading) {
                case 0:
                    imgSur.setImage(croppedImage);
                    break;
                case 1:
                    imgNorte.setImage(croppedImage);
                    break;
                case 2:
                    imgOeste.setImage(croppedImage);
                    break;
                case 3:
                    imgEste.setImage(croppedImage);
                    break;
                default:
                    // Dirección desconocida
                    System.out.println("Dirección desconocida: " + heading);
                    break;
            }

        } else {
            // El archivo no existe, mostrar un mensaje de error o registrar un mensaje de advertencia
            System.out.println("displayStaticImage: El archivo de imagen no existe: " + imagePath);

            // Limpiar todos los ImageViews
            imgSur.setImage(null);
            imgNorte.setImage(null);
            imgOeste.setImage(null);
            imgEste.setImage(null);
        }
    }

    public void btnSave_OnAction(ActionEvent actionEvent) {
    }

    @FXML
    private void btnAdd_OnAction() {
        int headCount = headDataManager.getNumHeads() + 1;

        // Incrementar el contador de grhDataManager
        headData.setNumHeads((short) headCount);

        // Crear un nuevo objeto grhData con los valores adecuados
        headData newHeadData = new headData(1, (short) 0, (short) 0, (short) 0);

        // Agregar el nuevo elemento al ListView
        lstHeads.getItems().add(String.valueOf(headCount));

        // Agregar el nuevo elemento al grhList
        headList.add(newHeadData);
    }

    public void btnDelete_OnAction(ActionEvent actionEvent) {
        int selectedIndex = lstHeads.getSelectionModel().getSelectedIndex();

        if (selectedIndex != -1) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmación");
            alert.setHeaderText("¿Estás seguro de que quieres eliminar este elemento?");
            alert.setContentText("Esta acción no se puede deshacer.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                lstHeads.getItems().remove(selectedIndex);
                headList.remove(selectedIndex);
            }
        }
    }
}
