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
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.nexus.indexador.Main;
import org.nexus.indexador.models.grhData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.nexus.indexador.utils.byteMigration;
import org.nexus.indexador.utils.configManager;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Optional;

public class frmMain {

    @FXML
    private ListView<String> lstIndices;

    @FXML
    private ListView<String> lstFrames;

    @FXML
    private Label lblIndices;

    @FXML
    private Label lblVersion; // Nuevo Label agregado

    @FXML
    private TextField txtImagen;

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
    private TextField txtSpeed;

    @FXML
    private TextField txtFiltro;

    @FXML
    private ImageView imgIndice;

    @FXML
    private ImageView imgGrafico;

    @FXML
    private Rectangle rectanguloIndice;

    @FXML
    private Slider sldZoom;

    private ObservableList<grhData> grhList; // Lista observable que contiene los datos de los gráficos indexados.

    private grhData grhDataManager; // Objeto que gestiona los datos de los gráficos, incluyendo la carga y manipulación de los mismos.

    private configManager configManager; // Objeto encargado de manejar la configuración de la aplicación, incluyendo la lectura y escritura de archivos de configuración.

    private static boolean consoleOpen = false; // Variable booleana que indica si la ventana de la consola está abierta o cerrada.

    private int currentFrameIndex = 1; // Índice del frame actual en la animación.

    private Timeline animationTimeline; // Línea de tiempo que controla la animación de los frames en el visor.

    private double orgSceneX, orgSceneY; // Coordenadas originales del cursor del mouse en la escena al presionar el botón del mouse.

    private double orgTranslateX, orgTranslateY; // Valores de traducción originales del ImageView al arrastrar el mouse.

    /**
     * Método de inicialización del controlador. Carga los datos de gráficos y configura el ListView.
     */
    @FXML
    protected void initialize() {
        loadGrhData();
        setupGrhListListener();
        setupFilterTextFieldListener();
        setupSliderZoom();
    }

    /**
     * Carga los datos de gráficos desde archivos binarios y actualiza la interfaz de usuario con la información obtenida.
     * Muestra los índices de gráficos en el ListView y actualiza los textos de los labels con información relevante.
     *
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
     *
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
        float speed = selectedGrh.getSpeed();

        txtImagen.setText(String.valueOf(fileGrh));
        txtPosX.setText(String.valueOf(x));
        txtPosY.setText(String.valueOf(y));
        txtAncho.setText(String.valueOf(width));
        txtAlto.setText(String.valueOf(height));
        txtSpeed.setText(String.valueOf(speed));

        if (nFrames == 1) { // ¿Es estatico?

            txtIndice.setText("Grh" + selectedGrh.getGrh() + "=" + nFrames + "-" + fileGrh + "-" + x + "-" + y + "-" + width + "-" + height);

            lstFrames.getItems().clear();

        } else { // Entonces es animación...

            StringBuilder frameText = new StringBuilder();

            // Agregar los índices de gráficos al ListView
            ObservableList<String> grhIndices = FXCollections.observableArrayList();

            int[] frames = selectedGrh.getFrames();

            for (int i = 1; i < selectedGrh.getNumFrames() + 1; i++) {
                String frame = String.valueOf(frames[i]);
                grhIndices.add(frame);

                frameText.append("-").append(frame);
            }

            lstFrames.setItems(grhIndices);

            txtIndice.setText("Grh" + selectedGrh.getGrh() + "=" + nFrames + frameText + "-" + speed);
        }
    }

    /**
     * Actualiza el visor con el gráfico seleccionado.
     * Si el gráfico es estático, muestra la imagen estática correspondiente. Si es una animación, muestra la animación.
     *
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

        // ¿La imagen existe?
        if (imageFile.exists()) {

            // El archivo existe, cargar la imagen
            Image staticImage = new Image(imageFile.toURI().toString());

            //Mandamos a dibujar el grafico completo en otro ImageView
            drawFullImage(staticImage, selectedGrh);

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
            System.out.println("displayStaticImage: El archivo de imagen no existe: " + imagePath);
        }

    }

    /**
     * Muestra una animación en el ImageView correspondiente al gráfico seleccionado.
     * Configura y ejecuta una animación de fotogramas clave para mostrar la animación.
     * La animación se ejecuta en un bucle infinito hasta que se detenga explícitamente.
     *
     * @param selectedGrh El gráfico seleccionado.
     * @param nFrames     El número total de fotogramas en la animación.
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
                    currentFrameIndex = (currentFrameIndex + 1) % nFrames; // Avanzar al siguiente frame circularmente
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

        if (currentFrameIndex > 0 && currentFrameIndex < frames.length) {
            grhData currentGrh = grhList.get(frames[currentFrameIndex]);

            String imagePath = configManager.getGraphicsDir() + currentGrh.getFileNum() + ".png";

            File imageFile = new File(imagePath);

            // ¿La imagen existe?
            if (imageFile.exists()) {

                Image frameImage = new Image(imagePath);

                //Mandamos a dibujar el grafico completo en otro ImageView
                drawFullImage(frameImage, selectedGrh);

                PixelReader pixelReader = frameImage.getPixelReader();
                WritableImage croppedImage = new WritableImage(pixelReader, currentGrh.getsX(), currentGrh.getsY(), currentGrh.getTileWidth(), currentGrh.getTileHeight());
                imgIndice.setImage(croppedImage);

            } else {
                // El archivo no existe, mostrar un mensaje de error o registrar un mensaje de advertencia
                System.out.println("updateFrame: El archivo de imagen no existe: " + imagePath);

            }
        }
    }

    /**
     * Dibuja la imagen completa del gráfico en el ImageView y dibuja un rectángulo alrededor de la región del índice.
     *
     * @param grhImage    La imagen completa del gráfico.
     * @param selectedGrh El gráfico seleccionado que contiene la información de la región del índice.
     */
    private void drawFullImage(Image grhImage, grhData selectedGrh) {
        // Dibuja la imagen completa del gráfico en el ImageView
        imgGrafico.setImage(grhImage);

        // Dibuja un rectángulo alrededor de la región del índice en la imagen completa del gráfico
        drawRectangle(selectedGrh);
    }

    /**
     * Dibuja un rectángulo alrededor de la región específica del gráfico en el ImageView.
     *
     * @param selectedGrh El gráfico seleccionado.
     */
    private void drawRectangle(grhData selectedGrh) {
        // Obtener las dimensiones del ImageView imgGrafico
        double imgWidth = imgGrafico.getBoundsInLocal().getWidth();
        double imgHeight = imgGrafico.getBoundsInLocal().getHeight();

        // Obtener las coordenadas del rectángulo en relación con las coordenadas del ImageView
        double rectX = (selectedGrh.getsX() * imgWidth) / imgGrafico.getImage().getWidth() + 5;
        double rectY = (selectedGrh.getsY() * imgHeight) / imgGrafico.getImage().getHeight() + 5;
        double rectWidth = (selectedGrh.getTileWidth() * imgWidth) / imgGrafico.getImage().getWidth();
        double rectHeight = (selectedGrh.getTileHeight() * imgHeight) / imgGrafico.getImage().getHeight();

        // Configurar las propiedades del rectángulo
        rectanguloIndice.setX(rectX);
        rectanguloIndice.setY(rectY);
        rectanguloIndice.setWidth(rectWidth);
        rectanguloIndice.setHeight(rectHeight);
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
            selectedGrh.setsX(Short.parseShort(txtPosX.getText()));
            selectedGrh.setsY(Short.parseShort(txtPosY.getText()));
            selectedGrh.setTileWidth(Short.parseShort(txtAncho.getText()));
            selectedGrh.setTileHeight(Short.parseShort(txtAlto.getText()));

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
     *
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

    /**
     * Configura el deslizador de zoom.
     * Este método configura un listener para el deslizador de zoom, que ajusta la escala del ImageView
     * según el valor del deslizador.
     */
    private void setupSliderZoom() {
        sldZoom.valueProperty().addListener((observable, oldValue, newValue) -> {
            double zoomValue = newValue.doubleValue();
            // Aplica la escala al ImageView
            imgIndice.setScaleX(zoomValue);
            imgIndice.setScaleY(zoomValue);
        });
    }

    /**
     * Maneja el evento de presionar el mouse.
     * Este método se invoca cuando el usuario presiona el botón del mouse. Si se presiona el botón
     * secundario del mouse (generalmente el botón derecho), registra las coordenadas de la escena
     * iniciales y los valores de traducción del ImageView.
     *
     * @param event El MouseEvent que representa el evento de presionar el mouse.
     */
    @FXML
    private void onMousePressed(MouseEvent event) {
        if (event.isSecondaryButtonDown()) {
            orgSceneX = event.getSceneX();
            orgSceneY = event.getSceneY();
            orgTranslateX = ((ImageView) (event.getSource())).getTranslateX();
            orgTranslateY = ((ImageView) (event.getSource())).getTranslateY();
        }
    }

    /**
     * Maneja el evento de arrastrar el mouse.
     * Este método se invoca cuando el usuario arrastra el mouse después de presionarlo. Si se presiona
     * el botón secundario del mouse (generalmente el botón derecho), calcula el desplazamiento desde
     * la posición inicial y actualiza los valores de traducción del ImageView en consecuencia.
     *
     * @param event El MouseEvent que representa el evento de arrastrar el mouse.
     */
    @FXML
    private void onMouseDragged(MouseEvent event) {
        if (event.isSecondaryButtonDown()) {
            double offsetX = event.getSceneX() - orgSceneX;
            double offsetY = event.getSceneY() - orgSceneY;
            double newTranslateX = orgTranslateX + offsetX;
            double newTranslateY = orgTranslateY + offsetY;

            ((ImageView) (event.getSource())).setTranslateX(newTranslateX);
            ((ImageView) (event.getSource())).setTranslateY(newTranslateY);
        }
    }

    /**
     * Elimina el elemento seleccionado de la lista de índices.
     * Muestra un mensaje de confirmación antes de eliminar el elemento.
     */
    @FXML
    private void btnDelete_OnAction() {
        int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();

        if (selectedIndex != -1) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmación");
            alert.setHeaderText("¿Estás seguro de que quieres eliminar este elemento?");
            alert.setContentText("Esta acción no se puede deshacer.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                lstIndices.getItems().remove(selectedIndex);
                grhList.remove(selectedIndex);
            }
        }
    }

    /**
     * Método que se activa al hacer clic en el botón "Añadir". Incrementa el contador de gráficos (grhCount) en el grhDataManager,
     * crea un nuevo objeto grhData con valores predeterminados y lo agrega tanto al ListView como al grhList.
     *
     * @throws IllegalArgumentException Si ocurre algún error al obtener el contador de gráficos del grhDataManager.
     */
    @FXML
    private void btnAdd_OnAction() {
        int grhCount = grhDataManager.getGrhCount() + 1;

        // Incrementar el contador de grhDataManager
        grhDataManager.setGrhCount(grhCount);

        // Crear un nuevo objeto grhData con los valores adecuados
        grhData newGrhData = new grhData(grhCount, (short) 1, 0, (short) 0, (short) 0, (short) 0, (short) 0);

        // Agregar el nuevo elemento al ListView
        lstIndices.getItems().add(String.valueOf(grhCount));

        // Agregar el nuevo elemento al grhList
        grhList.add(newGrhData);
    }

    /**
     * Guarda los datos de los gráficos en memoria en un archivo binario.
     * Los datos incluyen la versión del archivo, la cantidad de gráficos indexados y la información de cada gráfico.
     * Si el archivo no existe, se crea. Si existe, se sobrescribe.
     * Se utilizan las instancias de `configManager` y `byteMigration` para manejar la configuración y la conversión de bytes.
     *
     * @throws IOException Si ocurre un error de entrada/salida al intentar escribir en el archivo.
     */
    @FXML
    private void mnuIndexbyMemory() throws IOException {
        // Obtener instancias de configManager y byteMigration
        configManager configManager = org.nexus.indexador.utils.configManager.getInstance();
        byteMigration byteMigration = org.nexus.indexador.utils.byteMigration.getInstance();

        // Crear un objeto File para el archivo donde se guardarán los datos de los gráficos
        File archivo = new File(configManager.getInitDir() + "Graficos.ind");

        // Imprimir mensaje de inicio
        System.out.println("Iniciando el guardado de índices desde memoria.");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "rw")) {
            // Posicionarse al inicio del archivo
            file.seek(0);

            // Escribir la versión del archivo
            file.writeInt(byteMigration.bigToLittle_Int(grhDataManager.getVersion()));

            // Escribir la cantidad de gráficos indexados
            file.writeInt(byteMigration.bigToLittle_Int(grhDataManager.getGrhCount()));

            // Escribir cada gráfico en el archivo
            for (grhData grh : grhList) {
                // Escribir el número de gráfico y el número de frames
                file.writeInt(byteMigration.bigToLittle_Int(grh.getGrh()));
                file.writeShort(byteMigration.bigToLittle_Short(grh.getNumFrames()));

                // Si es una animación, escribir los frames y la velocidad
                if (grh.getNumFrames() > 1) {
                    int[] frames = grh.getFrames();
                    for (int i = 1; i <= grh.getNumFrames(); i++) {
                        file.writeInt(byteMigration.bigToLittle_Int(frames[i]));
                    }
                    file.writeFloat(byteMigration.bigToLittle_Float(grh.getSpeed()));
                } else { // Si es una imagen estática, escribir el resto de los datos
                    file.writeInt(byteMigration.bigToLittle_Int(grh.getFileNum()));
                    file.writeShort(byteMigration.bigToLittle_Short(grh.getsX()));
                    file.writeShort(byteMigration.bigToLittle_Short(grh.getsY()));
                    file.writeShort(byteMigration.bigToLittle_Short(grh.getTileWidth()));
                    file.writeShort(byteMigration.bigToLittle_Short(grh.getTileHeight()));
                }
            }

            // Imprimir mensaje de éxito
            System.out.println("¡Índices guardados!");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e; // Relanzar la excepción para manejarla fuera del método
        }
    }

    @FXML
    private void btnAddFrame_OnAction() {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Introduce el numero del indice");
        dialog.setHeaderText("Por favor, introduce un Grh:");
        dialog.setContentText("Grh:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int numero = Integer.parseInt(result.get());

                // Obtenemos el índice seleccionado en la lista de indices:
                int selectedIndex = lstIndices.getSelectionModel().getSelectedIndex();

                if (selectedIndex >= 0) {

                    // Solo podemos añadir los indices estáticos
                    if (grhList.get(numero).getNumFrames() == 1) {

                        grhList.get(selectedIndex).setNumFrames((short) (grhList.get(selectedIndex).getNumFrames() + 1));

                        int[] frames = grhList.get(selectedIndex).getFrames();

                        int[] newFrames = Arrays.copyOf(frames, frames.length + 1);
                        newFrames[frames.length] = numero;

                        // Establecer el nuevo array utilizando el método setFrames(), si está disponible
                        grhList.get(selectedIndex).setFrames(newFrames);

                        updateEditor(grhList.get(selectedIndex));


                    } else {
                        System.out.println("El indice seleccionado no es valido.");

                    }

                } else {
                    System.out.println("Indice invalido. Solo se aceptan indices desde el 1 hasta el " + grhDataManager.getGrhCount());
                }

            } catch (NumberFormatException e) {
                System.out.println("Error: Entrada inválida. Introduce un número válido.");

            }

        } else {
            System.out.println("Operación cancelada.");
        }

    }

    @FXML
    private void btnRemoveFrame_OnAction() {

    }
}