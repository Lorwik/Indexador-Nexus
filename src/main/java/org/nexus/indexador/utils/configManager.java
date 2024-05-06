package org.nexus.indexador.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class configManager {

    // Instancia única de ByteMigration
    private static configManager instance;

    private String graphicsDir;
    private String initDir;

    private static final String CONFIG_FILE_PATH = "src/main/resources/config.ini";

    public configManager() {}

    public static configManager getInstance() {
        if (instance == null) {
            instance = new configManager();
        }
        return instance;
    }

    // Getters y setters
    public String getGraphicsDir() {
        return graphicsDir;
    }

    public void setGraphicsDir(String graphicsDir) {
        this.graphicsDir = graphicsDir;
    }

    public String getInitDir() {
        return initDir;
    }

    public void setInitDir(String initDir) {
        this.initDir = initDir;
    }

    public void readConfig() throws IOException {
        File configFile = new File(CONFIG_FILE_PATH);
        if (configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        if (key.equals("Graficos")) {
                            graphicsDir = value;
                        } else if (key.equals("Init")) {
                            initDir = value;
                        }
                    }
                }
            }
        }
    }

    public void writeConfig() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE_PATH))) {
            writer.write("Graficos=" + graphicsDir);
            writer.newLine();
            writer.write("Init=" + initDir);
        }
    }

}
