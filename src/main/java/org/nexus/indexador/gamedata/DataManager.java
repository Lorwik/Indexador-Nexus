package org.nexus.indexador.gamedata;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.nexus.indexador.gamedata.models.GrhData;
import org.nexus.indexador.gamedata.models.HeadData;
import org.nexus.indexador.gamedata.models.HelmetData;
import org.nexus.indexador.utils.byteMigration;
import org.nexus.indexador.utils.ConfigManager;

import java.io.*;

public class DataManager {

    private GrhData grhData;
    private ObservableList<GrhData> grhList;
    private int GrhCount;
    private int GrhVersion;
    private short NumHeads;
    private short NumHelmets;

    private final ConfigManager configManager;
    private final byteMigration byteMigration;

    private static DataManager instance;

    private DataManager() throws IOException {

        // Obtenemos una instancia de configManager
        configManager = ConfigManager.getInstance();

        // Obtenemos una instancia de byteMigration para realizar la conversión de bytes
        byteMigration = org.nexus.indexador.utils.byteMigration.getInstance();

    }

    public static DataManager getInstance() throws IOException {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    /**
     * Obtiene la lista de gráficos (grh) cargados.
     *
     * @return Una lista observable de objetos GrhData que representan los gráficos cargados.
     */
    public ObservableList<GrhData> getGrhList() {
        return grhList;
    }

    public int getGrhCount() { return GrhCount; }
    public int getGrhVersion() {return GrhVersion;}
    public short getNumHeads() { return NumHeads; }
    public short getNumHelmets() { return NumHelmets; }

    public void setGrhCount(int GrhCount) { this.GrhCount = GrhCount; }
    public void setGrhVersion(int GrhVersion) { this.GrhVersion = GrhVersion; }
    public void setNumHelmets(short numHelmets) { NumHelmets = numHelmets; }
    public void setNumHeads(short numHeads) { NumHeads = numHeads; }

    /**
     * Lee los datos de un archivo binario que contiene información sobre gráficos (grh) y los convierte en objetos grhData.
     * Cada gráfico puede ser una imagen estática o una animación.
     *
     * @return Una lista observable de objetos grhData que representan los gráficos leídos del archivo.
     * @throws IOException Si ocurre un error de entrada/salida al leer el archivo.
     */
    public ObservableList<GrhData> loadGrhData() throws IOException {

        System.out.println("Ejecutando LoadGrhData.");

        grhList = FXCollections.observableArrayList();

        // Creamos un objeto File para el archivo que contiene los datos de los gráficos
        File archivo = new File(configManager.getInitDir() + "graficos.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            System.out.println("Comenzando a leer desde " + archivo.getAbsolutePath());

            //Nos posicionamos al inicio del fichero
            file.seek(0);

            //Leemos la versión del archivo
            GrhVersion = byteMigration.bigToLittle_Int(file.readInt());

            //Leemos la cantidad de Grh indexados
            GrhCount = byteMigration.bigToLittle_Int(file.readInt());

            //Mientras no llegue al final del archivo leemos...
            for (;;) {
                int grh = byteMigration.bigToLittle_Int(file.readInt());
                short numFrames = byteMigration.bigToLittle_Short(file.readShort());

                if (numFrames > 1) { // Es una animación
                    int[] frames = new int[numFrames + 1];
                    for (int i = 1; i <= numFrames; i++) {
                        frames[i] = byteMigration.bigToLittle_Int(file.readInt());
                    }

                    int speed = (int) byteMigration.bigToLittle_Float(file.readFloat());

                    //Creamos un objeto de grhData usando el constructor para animación
                    GrhData grhData = new GrhData(grh, numFrames, frames, speed);
                    grhList.add(grhData);

                } else { // Es una sola imagen
                    int fileNum = byteMigration.bigToLittle_Int(file.readInt());
                    short x = byteMigration.bigToLittle_Short(file.readShort());
                    short y = byteMigration.bigToLittle_Short(file.readShort());
                    short tileWidth = byteMigration.bigToLittle_Short(file.readShort());
                    short tileHeight = byteMigration.bigToLittle_Short(file.readShort());

                    //Creamos un objeto de grhData usando el constructor para imagenes estáticas
                    GrhData grhData = new GrhData(grh, numFrames, fileNum, x, y, tileWidth, tileHeight);
                    grhList.add(grhData);

                }

                // Si he recorrido todos los bytes, salgo del bucle
                if (file.getFilePointer() == file.length()) break;
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            throw e; // Relanzar la excepción para manejarla fuera del método

        } catch (EOFException e) {
            System.out.println("Fin de fichero");

        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e; // Relanzar la excepción para manejarla fuera del método

        }
        return grhList;
    }

    /**
     * Lee los datos de cabeza desde un archivo y los devuelve como una lista observable.
     *
     * @return una {@code ObservableList<headData>} que contiene los datos de cabeza leídos del archivo.
     * @throws IOException si ocurre un error de entrada/salida.
     */
    public ObservableList<HeadData> readHeadFile() throws IOException {

        // Obtenemos una instancia de configManager
        ConfigManager configManager = ConfigManager.getInstance();

        // Creamos una lista observable para almacenar los gráficos leídos del archivo
        ObservableList<HeadData> HeadList = FXCollections.observableArrayList();

        // Obtenemos una instancia de byteMigration para realizar la conversión de bytes
        byteMigration byteMigration = org.nexus.indexador.utils.byteMigration.getInstance();

        // Creamos un objeto File para el archivo que contiene los datos de los gráficos
        File archivo = new File(configManager.getInitDir() + "head.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            System.out.println("Comenzando a leer desde " + archivo.getAbsolutePath());

            // Nos posicionamos al inicio del fichero
            file.seek(0);

            NumHeads = byteMigration.bigToLittle_Short(file.readShort());

            for (int i = 0; i < NumHeads; i++) {

                int std;
                short texture;
                short startx;
                short starty;

                std = byteMigration.bigToLittle_Byte(file.readByte());
                texture = byteMigration.bigToLittle_Short(file.readShort());
                startx = byteMigration.bigToLittle_Short(file.readShort());
                starty = byteMigration.bigToLittle_Short(file.readShort());

                // Creamos un objeto de headData
                HeadData headData = new HeadData(std, texture, startx, starty);
                HeadList.add(headData);
            }

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            throw e; // Relanzar la excepción para manejarla fuera del método

        } catch (EOFException e) {
            System.out.println("Fin de fichero");

        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e; // Relanzar la excepción para manejarla fuera del método
        }
        return HeadList;
    }

    /**
     * Lee los datos de los cascos desde un archivo y los carga en una lista observable.
     *
     * @return una lista observable de objetos {@code helmetData} que contiene los datos de los cascos leídos del archivo.
     * @throws IOException si ocurre un error al leer el archivo.
     */
    public ObservableList<HelmetData> readHelmetFile() throws IOException {

        // Obtenemos una instancia de configManager
        ConfigManager configManager = ConfigManager.getInstance();

        // Creamos una lista observable para almacenar los gráficos leídos del archivo
        ObservableList<HelmetData> HelmetList = FXCollections.observableArrayList();

        // Obtenemos una instancia de byteMigration para realizar la conversión de bytes
        byteMigration byteMigration = org.nexus.indexador.utils.byteMigration.getInstance();

        // Creamos un objeto File para el archivo que contiene los datos de los gráficos
        File archivo = new File(configManager.getInitDir() + "helmet.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            System.out.println("Comenzando a leer desde " + archivo.getAbsolutePath());

            // Nos posicionamos al inicio del fichero
            file.seek(0);

            NumHelmets = byteMigration.bigToLittle_Short(file.readShort());

            for (int i = 0; i < NumHelmets; i++) {

                int std;
                short texture;
                short startx;
                short starty;

                std = byteMigration.bigToLittle_Byte(file.readByte());
                texture = byteMigration.bigToLittle_Short(file.readShort());
                startx = byteMigration.bigToLittle_Short(file.readShort());
                starty = byteMigration.bigToLittle_Short(file.readShort());

                // Creamos un objeto de helmetData
                HelmetData helmetData = new HelmetData(std, texture, startx, starty);
                HelmetList.add(helmetData);
            }

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            throw e; // Relanzar la excepción para manejarla fuera del método

        } catch (EOFException e) {
            System.out.println("Fin de fichero");

        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e; // Relanzar la excepción para manejarla fuera del método
        }
        return HelmetList;
    }

}
