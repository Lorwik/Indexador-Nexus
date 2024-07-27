package org.nexus.indexador.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.util.Duration;
import org.nexus.indexador.gamedata.DataManager;
import org.nexus.indexador.gamedata.models.BodyData;
import org.nexus.indexador.gamedata.models.GrhData;
import org.nexus.indexador.utils.ConfigManager;
import org.nexus.indexador.utils.AnimationState;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class frmCuerpos {

    @FXML
    public ListView lstBodys;
    @FXML
    public ImageView imgOeste;
    @FXML
    public ImageView imgNorte;
    @FXML
    public ImageView imgEste;
    @FXML
    public ImageView imgSur;
    @FXML
    public TextField txtNorte;
    @FXML
    public TextField txtEste;
    @FXML
    public TextField txtSur;
    @FXML
    public TextField txtOeste;
    @FXML
    public TextField txtHeadOffsetX;
    @FXML
    public TextField txtHeadOffsetY;
    @FXML
    public Label lblNCuerpos;
    @FXML
    public Button btnSave;
    @FXML
    public Button btnAdd;
    @FXML
    public Button btnDelete;

    private BodyData bodyDataManager; // Objeto que gestiona los datos de las cabezas, incluyendo la carga y manipulación de los mismos
    private ObservableList<BodyData> bodyList;
    private ObservableList<GrhData> grhList;

    private ConfigManager configManager;
    private DataManager dataManager;

    private Map<Integer, AnimationState> animationStates = new HashMap<>();

    /**
     * Inicializa el controlador, cargando la configuración y los datos de los cuerpos.
     */
    @FXML
    protected void initialize() throws IOException {
        configManager = ConfigManager.getInstance();
        dataManager = DataManager.getInstance();

        bodyDataManager = new BodyData(); // Crear una instancia de headData

        animationStates.put(0, new AnimationState());
        animationStates.put(1, new AnimationState());
        animationStates.put(2, new AnimationState());
        animationStates.put(3, new AnimationState());

        loadBodyData();
        setupHeadListListener();
    }

    /**
     * Carga los datos de los cuerpos desde un archivo y los muestra en la interfaz.
     */
    private void loadBodyData() {
        // Llamar al método para leer el archivo binario y obtener la lista de headData
        bodyList = dataManager.getBodyList();

        // Actualizar el texto de los labels con la información obtenida
        lblNCuerpos.setText("Cuerpos cargados: " + dataManager.getNumBodys());

        // Agregar los índices de gráficos al ListView
        ObservableList<String> bodyIndices = FXCollections.observableArrayList();
        for (int i = 1; i < bodyList.size() + 1; i++) {
            bodyIndices.add(String.valueOf(i));
        }

        lstBodys.setItems(bodyIndices);

    }

    /**
     * Configura un listener para el ListView, manejando los eventos de selección de ítems.
     */
    private void setupHeadListListener() {
        // Agregar un listener al ListView para capturar los eventos de selección
        lstBodys.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            // Obtener el índice seleccionado
            int selectedIndex = lstBodys.getSelectionModel().getSelectedIndex();

            if (selectedIndex >= 0) {
                // Obtener el objeto headData correspondiente al índice seleccionado
                BodyData selectedBody = bodyList.get(selectedIndex);
                updateEditor(selectedBody);

                for (int i = 0; i <= 3; i++) {
                    drawBodys(selectedBody, i);
                }
            }
        });
    }

    /**
     * Actualiza el editor de la interfaz con los datos de la cabeza seleccionada.
     *
     * @param selectedBody el objeto headData seleccionado.
     */
    private void updateEditor(BodyData selectedBody) {
        // Obtenemos todos los datos
        int grhBodys[] = selectedBody.getBody();
        short HeadOffsetX = selectedBody.getHeadOffsetX();
        short HeadOffsetY = selectedBody.getHeadOffsetY();

        txtNorte.setText(String.valueOf(grhBodys[0]));
        txtEste.setText(String.valueOf(grhBodys[1]));
        txtSur.setText(String.valueOf(grhBodys[2]));
        txtOeste.setText(String.valueOf(grhBodys[3]));
        txtHeadOffsetX.setText(String.valueOf(HeadOffsetX));
        txtHeadOffsetY.setText(String.valueOf(HeadOffsetY));
    }

    /**
     * Dibuja las imágenes de los cuerpos en las diferentes vistas (Norte, Sur, Este, Oeste).
     *
     * @param selectedBody el objeto headData seleccionado.
     * @param heading la dirección en la que se debe dibujar la cabeza (0: Sur, 1: Norte, 2: Oeste, 3: Este).
     */
    private void drawBodys(BodyData selectedBody, int heading) {
        int[] Bodys = selectedBody.getBody();
        grhList = dataManager.getGrhList();

        GrhData selectedGrh = grhList.get(Bodys[heading]);
        int nFrames = selectedGrh.getNumFrames();

        AnimationState animationState = animationStates.get(heading);
        Timeline animationTimeline = animationState.getTimeline();

        if (animationTimeline != null) {
            animationTimeline.stop();
        }

        animationTimeline.getKeyFrames().clear();
        animationState.setCurrentFrameIndex(0);

        animationTimeline.getKeyFrames().add(
                new KeyFrame(Duration.ZERO, event -> {
                    updateFrame(selectedGrh, heading);
                    animationState.setCurrentFrameIndex((animationState.getCurrentFrameIndex() + 1) % nFrames);
                })
        );

        animationTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(100)));
        animationTimeline.setCycleCount(Animation.INDEFINITE);
        animationTimeline.play();
    }

    /**
     * Actualiza el fotograma actual en el ImageView durante la reproducción de una animación.
     * Obtiene el siguiente fotograma de la animación y actualiza el ImageView con la imagen correspondiente.
     *
     * @param selectedGrh El gráfico seleccionado.
     */
    private void updateFrame(GrhData selectedGrh, int heading) {
        int[] frames = selectedGrh.getFrames();
        AnimationState animationState = animationStates.get(heading);
        int currentFrameIndex = animationState.getCurrentFrameIndex();

        if (currentFrameIndex >= 0 && currentFrameIndex < frames.length) {
            GrhData currentGrh = grhList.get(frames[currentFrameIndex]);

            String imagePath = configManager.getGraphicsDir() + currentGrh.getFileNum() + ".png";
            File imageFile = new File(imagePath);

            if (imageFile.exists()) {
                Image frameImage = new Image(imagePath);
                PixelReader pixelReader = frameImage.getPixelReader();
                WritableImage croppedImage = new WritableImage(pixelReader, currentGrh.getsX(), currentGrh.getsY(), currentGrh.getTileWidth(), currentGrh.getTileHeight());

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
                }
            } else {
                System.out.println("updateFrame: El archivo de imagen no existe: " + imagePath);
            }
        }
    }

    public void btnSave_OnAction(ActionEvent actionEvent) {
    }

    public void btnAdd_OnAction(ActionEvent actionEvent) {
    }

    public void btnDelete_OnAction(ActionEvent actionEvent) {
    }
}
