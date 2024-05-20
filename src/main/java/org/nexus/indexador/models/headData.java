package org.nexus.indexador.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.nexus.indexador.utils.byteMigration;
import org.nexus.indexador.utils.configManager;

import java.io.*;

public class headData {

    private int Std;
    private short Texture;
    private short StartX;
    private short StartY;

    private static short NumHeads;

    //Constructor
    public headData(int std, short texture, short startX, short startY) {
        Std = std;
        Texture = texture;
        StartX = startX;
        StartY = startY;
    }

    //Constructor vacio
    public headData() {}

    //Metodos SET
    public int getStd() { return Std; }
    public short getTexture() { return Texture; }
    public short getStartX() { return StartX; }
    public short getStartY() { return StartY; }
    public static short getNumHeads() { return NumHeads; }

    //Metodos GET
    public void setStd(int std) { Std = std; }
    public void setTexture(short texture) { Texture = texture; }
    public void setStartX(short startX) { StartX = startX; }
    public void setStartY(short startY) { StartY = startY; }
    public static void setNumHeads(short numHeads) { NumHeads = numHeads; }

    public ObservableList<headData> readHeadFile() throws IOException {

        // Obtenemos una instancia de configManager
        configManager configManager = org.nexus.indexador.utils.configManager.getInstance();

        // Creamos una lista observable para almacenar los gráficos leídos del archivo
        ObservableList<headData> HeadList = FXCollections.observableArrayList();

        // Obtenemos una instancia de byteMigration para realizar la conversión de bytes
        byteMigration byteMigration = org.nexus.indexador.utils.byteMigration.getInstance();

        // Creamos un objeto File para el archivo que contiene los datos de los gráficos
        File archivo = new File(configManager.getInitDir() + "head.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            System.out.println("Comenzando a leer desde " + archivo.getAbsolutePath());

            //Nos posicionamos al inicio del fichero
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

                //Creamos un objeto de HeadData
                headData headData = new headData(std,texture,startx,starty);
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
}
