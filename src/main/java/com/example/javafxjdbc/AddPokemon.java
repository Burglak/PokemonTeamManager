package com.example.javafxjdbc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AddPokemon {

    public static Pokemon showAndWait(Connection connection) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add Pokemon");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 20, 20));

        TextField nameField = new TextField();
        TextField hpField = createNumberTextField(3); // Ograniczenie do 3 cyfr
        TextField attackField = createNumberTextField(3);
        TextField defenseField = createNumberTextField(3);
        TextField speedField = createNumberTextField(3);

        //typy pokemonow
        ObservableList<String> types = FXCollections.observableArrayList(
                "Normal", "Fighting", "Flying", "Poison", "Ground", "Rock", "Bug", "Ghost",
                "Steel", "Fire", "Water", "Grass", "Electric", "Psychic", "Ice", "Dragon",
                "Dark", "Fairy"
        );
        ComboBox<String> typeComboBox = new ComboBox<>(types);

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
        grid.add(typeComboBox, 1, 5);

        Button addButton = new Button("Add");
        grid.add(addButton, 1, 6);

        addButton.setOnAction(event -> {
            Pokemon newPokemon = addPokemonToDatabase(connection, nameField.getText(),
                    Integer.parseInt(hpField.getText()), Integer.parseInt(attackField.getText()),
                    Integer.parseInt(defenseField.getText()), Integer.parseInt(speedField.getText()),
                    typeComboBox.getValue());

            if (newPokemon != null) {
                dialog.close();
            }
        });

        Scene scene = new Scene(grid, 300, 270);
        dialog.setScene(scene);
        dialog.getIcons().add(new Image("/pokeball.png"));
        dialog.showAndWait();

        return null;
    }

    private static TextField createNumberTextField(int maxDigits) {
        TextField textField = new TextField();
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

    private static Pokemon addPokemonToDatabase(Connection connection, String name, int hp, int attack, int defense,
                                                int speed, String type) {
        String query = "INSERT INTO pokemonComp (name, hp, attack, defense, speed, type) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, hp);
            preparedStatement.setInt(3, attack);
            preparedStatement.setInt(4, defense);
            preparedStatement.setInt(5, speed);
            preparedStatement.setString(6, type);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 1) {
                int generatedId = getGeneratedId(connection);
                return new Pokemon(generatedId, name, hp, attack, defense, speed, type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int getGeneratedId(Connection connection) {
        String query = "SELECT last_insert_rowid()";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}