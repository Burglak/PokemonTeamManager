package com.example.javafxjdbc;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;


//klasa zajmujaca sie aktualizacja pokemonow (UPDATE)
public class UpdatePokemon {

    public static void showAndWait(Connection connection, Pokemon selectedPokemon) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Update Pokemon");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 20, 20));
        grid.setVgap(10);
        grid.setHgap(10);

        TextField nameField = createTextField(selectedPokemon.getName());
        TextField hpField = createNumberTextField(selectedPokemon.getHp(), 3);
        TextField attackField = createNumberTextField(selectedPokemon.getAttack(), 3);
        TextField defenseField = createNumberTextField(selectedPokemon.getDefense(), 3);
        TextField speedField = createNumberTextField(selectedPokemon.getSpeed(), 3);
        TextField typeField = createTextField(selectedPokemon.getType());

        // Dodaj etykiety i pola tekstowe do siatki
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("HP:"), 0, 1);
        grid.add(hpField, 1, 1);
        grid.add(new Label("Attack:"), 0, 2);
        grid.add(attackField, 1, 2);
        grid.add(new Label("Defense:"), 0, 3);
        grid.add(defenseField, 1, 3);
        grid.add(new Label("Speed:"), 0, 4);
        grid.add(speedField, 1, 4);
        grid.add(new Label("Type:"), 0, 5);
        grid.add(typeField, 1, 5);

        Button updateButton = new Button("Update");
        grid.add(updateButton, 1, 6);

        //dodanie akcji do przycisku update
        updateButton.setOnAction(event -> {
            if (validateFields(nameField, hpField, attackField, defenseField, speedField, typeField)) {
                updatePokemonInDatabase(connection, selectedPokemon.getIdPokemon(),
                        nameField.getText(), Integer.parseInt(hpField.getText()),
                        Integer.parseInt(attackField.getText()), Integer.parseInt(defenseField.getText()),
                        Integer.parseInt(speedField.getText()), typeField.getText());
                window.close();
            } else {
                System.out.println("Invalid data. Please check the fields.");
            }
        });

        Scene scene = new Scene(grid, 300, 270);
        window.setScene(scene);
        window.getIcons().add(new Image("/pokeball.png"));
        window.showAndWait();
    }

    private static TextField createTextField(String initialValue) {
        TextField textField = new TextField(initialValue);
        return textField;
    }

    private static TextField createNumberTextField(int initialValue, int maxDigits) {
        TextField textField = new TextField(String.valueOf(initialValue));
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                textField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > maxDigits) {
                textField.setText(newValue.substring(0, maxDigits));
            }
        });
        return textField;
    }

    private static boolean validateFields(TextField... fields) {
        for (TextField field : fields) {
            if (field.getText().isEmpty()) {
                return false; //pole jest puste
            }
        }
        return true; // wszystkie pola są wypełnione
    }

    private static void updatePokemonInDatabase(Connection connection, int idPokemon,
                                                String name, int hp, int attack, int defense,
                                                int speed, String type) {
        String query = "UPDATE pokemonComp SET name=?, hp=?, attack=?, defense=?, speed=?, type=? WHERE idPokemon=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, hp);
            preparedStatement.setInt(3, attack);
            preparedStatement.setInt(4, defense);
            preparedStatement.setInt(5, speed);
            preparedStatement.setString(6, type);
            preparedStatement.setInt(7, idPokemon);

            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}