package org.nexus.indexador.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.nexus.indexador.utils.byteMigration;
import org.nexus.indexador.utils.configManager;

import java.io.*;

public class grhData {
    //Variables
    private int grh;
    private int sX;
    private int sY;
    private int FileNum;
    private int TileWidth;
    private int TileHeight;
    private int NumFrames;
    private int Frames[];
    private int Speed;

    private static int GrhCount;
    private static long Version;

    // Constructor para graficos estaticos
    public grhData(int grh, int NumFrames, int FileNum, int sX, int sY, int TileWidth, int TileHeight){
        this.grh = grh;
        this.NumFrames = NumFrames;
        this.FileNum = FileNum;
        this.sX = sX;
        this.sY = sY;
        this.TileWidth = TileWidth;
        this.TileHeight = TileHeight;
    }

    // Constructor para animaciones
    public grhData(int grh, int NumFrames, int[] Frames, int Speed) {
        this.grh = grh;
        this.NumFrames = NumFrames;
        this.Frames = Frames;
        this.Speed = Speed;
    }

    // Constructor vacio
    public grhData() { }

    //Metodos SET
    public void setFileNum(int FileNum) { this.FileNum = FileNum; }
    public void setNumFrames(int NumFrames) { this.NumFrames = NumFrames; }
    public void setFrames(int[] Frames) { this.Frames = Frames; } //En caso de que sea una animaci�n
    public void setsX(int sX) { this.sX = sX; }
    public void setsY(int sY) { this.sY = sY; }
    public void setTileWidth(int TileWidth) { this.TileWidth = TileWidth; }
    public void setTileHeight(int TileHeight) { this.TileHeight = TileHeight; }
    public void setSpeed(int Speed) { this.Speed = Speed; }
    public void setGrh(int grh) { this.grh = grh; }
    public void setGrhCount(int GrhCount) { this.GrhCount = GrhCount; }
    public void setVersion(long Version) { this.Version = Version; }

    //Metodos GET
    public int getFileNum() { return this.FileNum; }
    public int getNumFrames() { return this.NumFrames; }
    public int[] getFrames() { return this.Frames; } //En caso de que sea una animaci�n
    public int getsX() { return this.sX; }
    public int getsY() { return this.sY; }
    public int getTileWidth() { return this.TileWidth; }
    public int getTileHeight() { return this.TileHeight; }
    public int getSpeed() {return this.Speed; }
    public int getGrh() { return grh; }
    public int getGrhCount() { return GrhCount; }
    public long getVersion() { return Version; }

    /**
     * Lee los datos de un archivo binario que contiene información sobre gráficos (grh) y los convierte en objetos grhData.
     * Cada gráfico puede ser una imagen estática o una animación.
     *
     * @return Una lista observable de objetos grhData que representan los gráficos leídos del archivo.
     * @throws IOException Si ocurre un error de entrada/salida al leer el archivo.
     */
    public ObservableList<grhData> readGrhFile() throws IOException {

        // Obtenemos una instancia de configManager
        configManager configManager = org.nexus.indexador.utils.configManager.getInstance();

        // Creamos una lista observable para almacenar los gráficos leídos del archivo
        ObservableList<grhData> grhList = FXCollections.observableArrayList();

        // Obtenemos una instancia de byteMigration para realizar la conversión de bytes
        byteMigration byteMigration = org.nexus.indexador.utils.byteMigration.getInstance();

        // Creamos un objeto File para el archivo que contiene los datos de los gráficos
        File archivo = new File(configManager.getInitDir() + "Graficos.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            System.out.println("Comenzando a leer desde " + archivo.getAbsolutePath());

            //Nos posicionamos al inicio del fichero
            file.seek(0);

            //Leemos la versión del archivo
            Version = byteMigration.bigToLittle_Int(file.readInt());

            //Leemos la cantidad de Grh indexados
            GrhCount = byteMigration.bigToLittle_Int(file.readInt());

            //Mientras no llegue al final del archivo leemos...
            for (;;) {
                int grh = byteMigration.bigToLittle_Int(file.readInt());
                int numFrames = byteMigration.bigToLittle_Short(file.readShort());

                if (numFrames > 1) { // Es una animación
                    int[] frames = new int[numFrames + 1];
                    for (int i = 1; i <= numFrames; i++) {
                        frames[i] = byteMigration.bigToLittle_Int(file.readInt());
                    }

                    int speed = (int) byteMigration.bigToLittle_Float(file.readFloat());

                    //Creamos un objeto de grhData usando el constructor para animación
                    grhData grhData = new grhData(grh, numFrames, frames, speed);
                    grhList.add(grhData);

                } else { // Es una sola imagen
                    int fileNum = byteMigration.bigToLittle_Int(file.readInt());
                    int x = byteMigration.bigToLittle_Short(file.readShort());
                    int y = byteMigration.bigToLittle_Short(file.readShort());
                    int tileWidth = byteMigration.bigToLittle_Short(file.readShort());
                    int tileHeight = byteMigration.bigToLittle_Short(file.readShort());

                    //Creamos un objeto de grhData usando el constructor para imagenes estáticas
                    grhData grhData = new grhData(grh, numFrames, fileNum, x, y, tileWidth, tileHeight);
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

}
