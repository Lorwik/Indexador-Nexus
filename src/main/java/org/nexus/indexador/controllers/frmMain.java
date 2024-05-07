package org.nexus.indexador.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.nexus.indexador.Main;
import org.nexus.indexador.models.grhData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.nexus.indexador.utils.configManager;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
    private TextField txtFiltro;

    @FXML
    private ImageView imgIndice;

    @FXML
    private ImageView imgGrafico;

    private ObservableList<grhData> grhList;

    private grhData grhDataManager;

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
        setupFilterTextFieldListener();
    }

    /**
     * Carga los datos de gráficos desde archivos binarios y actualiza la interfaz de usuario con la información obtenida.
     * Muestra los índices de gráficos en el ListView y actualiza los textos de los labels con información relevante.
     * @throws IOException Sí ocurre un error durante la lectura de los archivos binarios.
     */
    private void loadGrhData() {
        grhDataManager = new grhData(); // Crear una instancia de grhData
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

    /**
     * Muestra una imagen estática en el ImageView correspondiente al gráfico seleccionado.
     * Si el archivo de imagen existe, carga la imagen y la muestra en el ImageView.
     * Además, recorta la región adecuada de la imagen completa para mostrar solo la parte relevante del gráfico.
     * Si el archivo de imagen no existe, imprime un mensaje de advertencia.
     *
     * @param selectedGrh El gráfico seleccionado.
     */
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

    /**
     * Muestra una animación en el ImageView correspondiente al gráfico seleccionado.
     * Configura y ejecuta una animación de fotogramas clave para mostrar la animación.
     * La animación se ejecuta en un bucle infinito hasta que se detenga explícitamente.
     *
     * @param selectedGrh El gráfico seleccionado.
     * @param nFrames El número total de fotogramas en la animación.
     */
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

    /**
     * Actualiza el fotograma actual en el ImageView durante la reproducción de una animación.
     * Obtiene el siguiente fotograma de la animación y actualiza el ImageView con la imagen correspondiente.
     *
     * @param selectedGrh El gráfico seleccionado.
     */
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

    /**
     * Método para manejar la acción cuando se hace clic en el elemento del menú "Consola"
     */
    @FXML
    private void mnuConsola() {
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

    /**
     * Exporta los datos de gráficos al archivo "graficos.ini" en el directorio de exportación configurado.
     * Los datos exportados incluyen el número total de gráficos, la versión de los índices y la información detallada de cada gráfico.
     * Si se produce algún error durante el proceso de exportación, se imprime un mensaje de error.
     */
    @FXML
    private void mnuExportGrh() {

        configManager configManager = org.nexus.indexador.utils.configManager.getInstance();

        File file = new File(configManager.getExportDir() + "graficos.ini");

        System.out.println("Exportando indices, espera...");

        try (BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(file))) {
            bufferWriter.write("[INIT]");
            bufferWriter.newLine();
            bufferWriter.write("NumGrh=" + grhDataManager.getGrhCount());
            bufferWriter.newLine();
            bufferWriter.write("Version=" + grhDataManager.getVersion());
            bufferWriter.newLine();
            bufferWriter.write("[GRAPHICS]");
            bufferWriter.newLine();

            for (grhData grh : grhList) {
                if (grh.getNumFrames() > 1) {
                    bufferWriter.write("Grh" + grh.getGrh() + "=" + grh.getNumFrames() + "-");

                    int[] frames = grh.getFrames();

                    for (int i = 1; i < grh.getNumFrames() + 1; i++) {
                        bufferWriter.write(frames[i] + "-");
                    }

                    bufferWriter.write(String.valueOf(grh.getSpeed()));

                } else {
                    bufferWriter.write("Grh" + grh.getGrh() + "=" + grh.getNumFrames() + "-" +
                            grh.getFileNum() + "-" + grh.getsX() + "-" + grh.getsY() + "-" +
                            grh.getTileWidth() + "-" + grh.getTileHeight());
                }
                bufferWriter.newLine();

            }

            System.out.println("Indices exportados!");

        } catch (IOException e) {
            // Manejar la excepción de manera adecuada, proporcionando un mensaje de error útil para el usuario
            System.err.println("Error al exportar los datos de gráficos: " + e.getMessage());
        }
    }

    /**
     * Cierra la aplicación
     */
    @FXML
    private void mnuClose() {
        Platform.exit();
    }

    @FXML
    private void mnuCode() {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(new URI("https://github.com/Lorwik/Indexador-Nexus"));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("El navegador web no es compatible.");
            }
        } else {
            System.out.println("La funcionalidad de escritorio no es compatible.");
        }
    }

    /**
     * Guarda los cambios realizados en los datos del gráfico seleccionado en la lista.
     * Obtiene el índice seleccionado de la lista y actualiza los atributos del objeto grhData correspondiente con los valores ingresados en los campos de texto.
     * Si no hay ningún índice seleccionado, no se realizan cambios.
     * Se imprime un mensaje indicando que los cambios se han aplicado con éxito.
     */
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

    /**
     * Configura un listener para el TextField de filtro para detectar cambios en su contenido.
     */
    private void setupFilterTextFieldListener() {
        // Agregar un listener al TextField de filtro para detectar cambios en su contenido
        txtFiltro.textProperty().addListener((observable, oldValue, newValue) -> {
            filterIndices(newValue); // Llamar al método para filtrar los índices
        });
    }

    /**
     * Filtra los índices en el ListView según el texto proporcionado.
     * @param filterText El texto utilizado para filtrar los índices.
     */
    private void filterIndices(String filterText) {
        if (!filterText.isEmpty()) {
            // El texto de filtro no está vacío
            try {
                int filterIndex = Integer.parseInt(filterText);

                // Buscar el índice en la lista de índices
                for (int i = 0; i < grhList.size(); i++) {
                    if (grhList.get(i).getGrh() == filterIndex) {
                        // Seleccionar el índice correspondiente en el ListView
                        lstIndices.getSelectionModel().select(i);
                        lstIndices.scrollTo(i); // Desplazar el ListView para mostrar el índice seleccionado
                        return; // Salir del bucle una vez que se encuentre el índice
                    }
                }

                // Si no se encuentra el índice, limpiar la selección en el ListView
                lstIndices.getSelectionModel().clearSelection();

            } catch (NumberFormatException e) {
                // En caso de que el texto de filtro no sea un número, no hacer nada
            }
        } else {
            // Si el texto de filtro está vacío, limpiar la selección en el ListView
            lstIndices.getSelectionModel().clearSelection();
        }
    }
}