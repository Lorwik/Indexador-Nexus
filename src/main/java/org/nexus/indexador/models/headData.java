/**
 * La clase {@code headData} representa la estructura de datos para elementos gráficos de cabeza.
 * Contiene atributos para estándar, textura y coordenadas de inicio.
 * Esta clase proporciona métodos para establecer y obtener estos atributos, y también para leer datos de cabeza desde un archivo.
 */
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

    /**
     * Construye un nuevo objeto {@code headData} con los parámetros especificados.
     *
     * @param std el valor estándar.
     * @param texture el valor de la textura.
     * @param startX la coordenada X de inicio.
     * @param startY la coordenada Y de inicio.
     */
    public headData(int std, short texture, short startX, short startY) {
        Std = std;
        Texture = texture;
        StartX = startX;
        StartY = startY;
    }

    /**
     * Construye un nuevo objeto {@code headData} con parámetros por defecto.
     */
    public headData() {}

    // Métodos GET

    /**
     * Retorna el valor estándar.
     *
     * @return el valor estándar.
     */
    public int getStd() { return Std; }

    /**
     * Retorna el valor de la textura.
     *
     * @return el valor de la textura.
     */
    public short getTexture() { return Texture; }

    /**
     * Retorna la coordenada X de inicio.
     *
     * @return la coordenada X de inicio.
     */
    public short getStartX() { return StartX; }

    /**
     * Retorna la coordenada Y de inicio.
     *
     * @return la coordenada Y de inicio.
     */
    public short getStartY() { return StartY; }

    /**
     * Retorna el número de cabezas.
     *
     * @return el número de cabezas.
     */
    public static short getNumHeads() { return NumHeads; }

    // Métodos SET

    /**
     * Establece el valor estándar.
     *
     * @param std el valor estándar.
     */
    public void setStd(int std) { Std = std; }

    /**
     * Establece el valor de la textura.
     *
     * @param texture el valor de la textura.
     */
    public void setTexture(short texture) { Texture = texture; }

    /**
     * Establece la coordenada X de inicio.
     *
     * @param startX la coordenada X de inicio.
     */
    public void setStartX(short startX) { StartX = startX; }

    /**
     * Establece la coordenada Y de inicio.
     *
     * @param startY la coordenada Y de inicio.
     */
    public void setStartY(short startY) { StartY = startY; }

    /**
     * Establece el número de cabezas.
     *
     * @param numHeads el número de cabezas.
     */
    public static void setNumHeads(short numHeads) { NumHeads = numHeads; }

    /**
     * Lee los datos de cabeza desde un archivo y los devuelve como una lista observable.
     *
     * @return una {@code ObservableList<headData>} que contiene los datos de cabeza leídos del archivo.
     * @throws IOException si ocurre un error de entrada/salida.
     */
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
                headData headData = new headData(std, texture, startx, starty);
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