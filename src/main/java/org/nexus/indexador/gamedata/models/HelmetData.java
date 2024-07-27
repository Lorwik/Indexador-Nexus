/**
 * La clase {@code helmetData} maneja los datos de los cascos, incluyendo la carga desde un archivo y la manipulación de dichos datos.
 */
package org.nexus.indexador.gamedata.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.nexus.indexador.utils.byteMigration;
import org.nexus.indexador.utils.ConfigManager;

import java.io.*;

public class HelmetData {

    private int Std;
    private short Texture;
    private short StartX;
    private short StartY;

    /**
     * Constructor de la clase {@code helmetData}.
     *
     * @param std     el valor estándar del casco.
     * @param texture la textura del casco.
     * @param startX  la coordenada X inicial del casco.
     * @param startY  la coordenada Y inicial del casco.
     */
    public HelmetData(int std, short texture, short startX, short startY) {
        Std = std;
        Texture = texture;
        StartX = startX;
        StartY = startY;
    }

    /**
     * Constructor vacío de la clase {@code helmetData}.
     */
    public HelmetData() {}

    // Métodos GET
    public int getStd() { return Std; }
    public short getTexture() { return Texture; }
    public short getStartX() { return StartX; }
    public short getStartY() { return StartY; }

    // Métodos SET
    public void setStd(int std) { Std = std; }
    public void setTexture(short texture) { Texture = texture; }
    public void setStartX(short startX) { StartX = startX; }
    public void setStartY(short startY) { StartY = startY; }

}