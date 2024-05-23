/**
 * La clase {@code frmCascos} es un controlador para la interfaz gráfica de usuario (GUI) relacionada con los datos de cascos.
 * Esta clase maneja la interacción del usuario con la interfaz y gestiona la carga, visualización y manipulación de los datos de los cascos.
 */
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
import org.nexus.indexador.models.helmetData;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class frmCascos {

    @FXML
    public ListView lstHelmets;
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
    public Label lblNCascos;
    @FXML
    public Button btnSave;
    @FXML
    public Button btnAdd;
    @FXML
    public Button btnDelete;

    private helmetData helmetDataManager; // Objeto que gestiona los datos de los cascos, incluyendo la carga y manipulación de los mismos
    private ObservableList<helmetData> helmetList; // Lista observable que contiene los datos de los gráficos indexados.

    private org.nexus.indexador.utils.configManager configManager; // Objeto encargado de manejar la configuración de la aplicación, incluyendo la lectura y escritura de archivos de configuración.

    /**
     * Inicializa el controlador, cargando la configuración y los datos de los cascos.
     */
    @FXML
    protected void initialize() {
        configManager = org.nexus.indexador.utils.configManager.getInstance();
        helmetDataManager = new helmetData(); // Crear una instancia de helmetData
        loadHelmetData();
        setupHelmetListListener();
    }

    /**
     * Carga los datos de los cascos desde un archivo y los muestra en la interfaz.
     */
    private void loadHelmetData() {
        try {
            // Llamar al método para leer el archivo binario y obtener la lista de helmetData
            helmetList = helmetDataManager.readHelmetFile();

            // Actualizar el texto de los labels con la información obtenida
            lblNCascos.setText("Cascos cargados: " + helmetData.getNumHelmets());

            // Agregar los índices de gráficos al ListView
            ObservableList<String> helmetIndices = FXCollections.observableArrayList();
            for (int i = 1; i < helmetList.size() + 1; i++) {
                helmetIndices.add(String.valueOf(i));
            }

            lstHelmets.setItems(helmetIndices);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configura un listener para el ListView, manejando los eventos de selección de ítems.
     */
    private void setupHelmetListListener() {
        // Agregar un listener al ListView para capturar los eventos de selección
        lstHelmets.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            // Obtener el índice seleccionado
            int selectedIndex = lstHelmets.getSelectionModel().getSelectedIndex();

            if (selectedIndex >= 0) {
                // Obtener el objeto helmetData correspondiente al índice seleccionado
                helmetData selectedHelmet = helmetList.get(selectedIndex);
                updateEditor(selectedHelmet);

                for (int i = 0; i <= 3; i++) {
                    drawHelmets(selectedHelmet, i);
                }
            }
        });
    }

    /**
     * Actualiza el editor de la interfaz con los datos del casco seleccionado.
     *
     * @param selectedHelmet el objeto helmetData seleccionado.
     */
    private void updateEditor(helmetData selectedHelmet) {
        // Obtenemos todos los datos
        short Texture = selectedHelmet.getTexture();
        short StartX = selectedHelmet.getStartX();
        short StartY = selectedHelmet.getStartY();

        txtNGrafico.setText(String.valueOf(Texture));
        txtStartX.setText(String.valueOf(StartX));
        txtStartY.setText(String.valueOf(StartY));
    }

    /**
     * Dibuja las imágenes de los cascos en las diferentes vistas (Norte, Sur, Este, Oeste).
     *
     * @param selectedHelmet el objeto helmetData seleccionado.
     * @param helmeting la dirección en la que se debe dibujar el casco (0: Sur, 1: Norte, 2: Oeste, 3: Este).
     */
    private void drawHelmets(helmetData selectedHelmet, int helmeting) {
        // Construir la ruta completa de la imagen para imagePath
        String imagePath = configManager.getGraphicsDir() + selectedHelmet.getTexture() + ".png";
        File imageFile = new File(imagePath);

        // ¿La imagen existe?
        if (imageFile.exists()) {
            Image staticImage = new Image(imageFile.toURI().toString());

            int textureX2 = 27;
            int textureY2 = 32;
            int textureX1 = selectedHelmet.getStartX();
            int textureY1 = (helmeting * textureY2) + selectedHelmet.getStartY();

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
            switch (helmeting) {
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
                    System.out.println("Dirección desconocida: " + helmeting);
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

    /**
     * Maneja el evento de acción del botón "Guardar". Aplica los cambios al objeto helmetData seleccionado.
     *
     * @param actionEvent el evento de acción del botón.
     */
    public void btnSave_OnAction(ActionEvent actionEvent) {
        // Obtenemos el índice seleccionado en la lista:
        int selectedHelmetIndex = lstHelmets.getSelectionModel().getSelectedIndex();

        // Nos aseguramos de que el índice es válido
        if (selectedHelmetIndex >= 0) {
            // Obtenemos el objeto helmetData correspondiente al índice seleccionado
            helmetData selectedHelmet = helmetList.get(selectedHelmetIndex);

            // Comenzamos aplicar los cambios:
            selectedHelmet.setTexture(Short.parseShort(txtNGrafico.getText()));
            selectedHelmet.setStartX(Short.parseShort(txtStartX.getText()));
            selectedHelmet.setStartY(Short.parseShort(txtStartY.getText()));

            System.out.println(("¡Cambios aplicados!"));
        }
    }

    /**
     * Maneja el evento de acción del botón "Agregar". Agrega un nuevo objeto helmetData a la lista.
     */
    @FXML
    private void btnAdd_OnAction() {
        int helmetCount = helmetDataManager.getNumHelmets() + 1;

        // Incrementar el contador de helmetDataManager
        helmetData.setNumHelmets((short) helmetCount);

        // Crear un nuevo objeto helmetData con los valores adecuados
        helmetData newHelmetData = new helmetData(2, (short) 0, (short) 0, (short) 0);

        // Agregar el nuevo elemento al ListView
        lstHelmets.getItems().add(String.valueOf(helmetCount));

        // Agregar el nuevo elemento a helmetList
        helmetList.add(newHelmetData);
    }

    /**
     * Maneja el evento de acción del botón "Eliminar". Elimina el objeto helmetData seleccionado de la lista.
     *
     * @param actionEvent el evento de acción del botón.
     */
    public void btnDelete_OnAction(ActionEvent actionEvent) {
        int selectedIndex = lstHelmets.getSelectionModel().getSelectedIndex();

        if (selectedIndex != -1) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmación");
            alert.setHeaderText("¿Estás seguro de que quieres eliminar este elemento?");
            alert.setContentText("Esta acción no se puede deshacer.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                lstHelmets.getItems().remove(selectedIndex);
                helmetList.remove(selectedIndex);
            }
        }
    }
}