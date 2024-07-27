/**
 * La clase {@code headData} representa la estructura de datos para elementos gráficos de cabeza.
 * Contiene atributos para estándar, textura y coordenadas de inicio.
 * Esta clase proporciona métodos para establecer y obtener estos atributos, y también para leer datos de cabeza desde un archivo.
 */
package org.nexus.indexador.gamedata.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.nexus.indexador.utils.byteMigration;
import org.nexus.indexador.utils.ConfigManager;

import java.io.*;

public class HeadData {

    private int Std;
    private short Texture;
    private short StartX;
    private short StartY;

    /**
     * Construye un nuevo objeto {@code headData} con los parámetros especificados.
     *
     * @param std el valor estándar.
     * @param texture el valor de la textura.
     * @param startX la coordenada X de inicio.
     * @param startY la coordenada Y de inicio.
     */
    public HeadData(int std, short texture, short startX, short startY) {
        Std = std;
        Texture = texture;
        StartX = startX;
        StartY = startY;
    }

    /**
     * Construye un nuevo objeto {@code headData} con parámetros por defecto.
     */
    public HeadData() {}

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

}