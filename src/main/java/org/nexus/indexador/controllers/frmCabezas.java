package org.nexus.indexador.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.nexus.indexador.models.grhData;
import org.nexus.indexador.models.headData;

import java.io.IOException;

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

    private headData headDataManager; // Objeto que gestiona los datos de las cabezas, incluyendo la carga y manipulación de los mismos
    private ObservableList<headData> headList; // Lista observable que contiene los datos de los gráficos indexados.

    @FXML
    protected void initialize() {
        headDataManager = new headData(); // Crear una instancia de grhData

        loadHeadData();
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

}
