package org.nexus.indexador.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class frmAdaptador {

    @FXML
    private TextField txtPos;

    @FXML
    private TextField txtGrafico;

    @FXML
    private TextField txtNumAnimaciones;

    @FXML
    private TextArea txtOriginal;

    @FXML
    private TextArea txtAdaptado;

    @FXML
    private void btnAdapter_OnAction() {
        String[] lineas = txtOriginal.getText().split("\\r?\\n");
        String resultado = "";
        long contador;

        // Verificación de la entrada de datos
        if (txtPos.getText().isEmpty()) {
            showAlert("El valor de la posición es nulo.");
            return;
        }

        if (txtGrafico.getText().isEmpty()) {
            showAlert("El valor del gráfico es nulo.");
            return;
        }

        contador = Long.parseLong(txtPos.getText());

        for (String linea : lineas) {
            String lineaGrh = readField(2, linea, 61);
            int fr = Integer.parseInt(readField(1, lineaGrh, 45));

            if (fr == 1) {
                // Recortamos y reemplazamos
                resultado += "Grh" + contador + "=" + fr + "-" + txtGrafico.getText() + "-" +
                        readField(3, lineaGrh, 45) + "-" + readField(4, lineaGrh, 45) + "-" +
                        readField(5, lineaGrh, 45) + "-" + readField(6, lineaGrh, 45) + "\n";

            } else {
                // ¿Es una animación?
                if (txtNumAnimaciones.getText().isEmpty()) {
                    showAlert("Hay animaciones y no se especificó el número de estas.");
                    return;
                }

                String tmp = "Grh" + contador + "=" + fr;

                for (int j = 0; j < lineas.length - Integer.parseInt(txtNumAnimaciones.getText()); j++) {
                    tmp = tmp + "-" + j;
                }

                resultado += tmp + "-" + readField(fr + 2, lineaGrh, 45) + "\n";
            }

            // Aumentamos en 1 el contador
            contador++;
        }

        txtAdaptado.setText(resultado);
    }

    @FXML
    private void btnClear_OnAction() {
        txtAdaptado.clear();
    }

    private String readField(int pos, String text, int sepASCII) {
        String separator = Character.toString((char) sepASCII);
        int lastPos = 0;
        int fieldNum = 0;

        for (int i = 0; i < text.length(); i++) {
            char curChar = text.charAt(i);

            if (Character.toString(curChar).equals(separator)) {
                fieldNum++;

                if (fieldNum == pos) {
                    return text.substring(lastPos, text.indexOf(separator, lastPos));
                }

                lastPos = i + 1;
            }
        }

        fieldNum++;

        if (fieldNum == pos) {
            return text.substring(lastPos);
        }

        return "";
    }

    private void showAlert(String message) {
        // Implementa la lógica para mostrar una alerta en tu aplicación JavaFX
        // Aquí se muestra solo un mensaje en la consola
        System.out.println(message);
    }
}
