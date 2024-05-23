package org.nexus.indexador.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.nexus.indexador.utils.byteMigration;
import org.nexus.indexador.utils.configManager;

import java.io.*;

public class BodyData {

    private int[] Body;
    private short HeadOffsetX;
    private short HeadOffsetY;

    private static short NumBodys;

    public BodyData(int[] body, short headOffsetX, short headOffsetY) {
        this.Body = body;
        this.HeadOffsetX = headOffsetX;
        this.HeadOffsetY = headOffsetY;
    }

    public BodyData() {}

    public int[] getBody() {
        return Body;
    }

    public void setBody(int[] body) {
        Body = body;
    }

    public short getHeadOffsetX() {
        return HeadOffsetX;
    }

    public void setHeadOffsetX(short headOffsetX) {
        HeadOffsetX = headOffsetX;
    }

    public short getHeadOffsetY() {
        return HeadOffsetY;
    }

    public void setHeadOffsetY(short headOffsetY) {
        HeadOffsetY = headOffsetY;
    }

    public static short getNumBodys() {
        return NumBodys;
    }

    public static void setNumBodys(short numBodys) {
        NumBodys = numBodys;
    }

    public ObservableList<BodyData> readBodyFile() throws IOException {
        configManager configManager = org.nexus.indexador.utils.configManager.getInstance();
        ObservableList<BodyData> BodyList = FXCollections.observableArrayList();
        byteMigration byteMigration = org.nexus.indexador.utils.byteMigration.getInstance();
        File archivo = new File(configManager.getInitDir() + "personajes.ind");

        try (RandomAccessFile file = new RandomAccessFile(archivo, "r")) {
            System.out.println("Comenzando a leer desde " + archivo.getAbsolutePath());
            file.seek(0);
            NumBodys = byteMigration.bigToLittle_Short(file.readShort());

            for (int i = 0; i < NumBodys; i++) {
                int[] body = new int[4];
                for (int j = 0; j < 4; j++) {
                    body[j] = byteMigration.bigToLittle_Int(file.readInt());
                }
                short headOffsetX = byteMigration.bigToLittle_Short(file.readShort());
                short headOffsetY = byteMigration.bigToLittle_Short(file.readShort());
                BodyData data = new BodyData(body, headOffsetX, headOffsetY);
                BodyList.add(data);
            }

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            throw e;
        } catch (EOFException e) {
            System.out.println("Fin de fichero");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e;
        }

        return BodyList;
    }
}