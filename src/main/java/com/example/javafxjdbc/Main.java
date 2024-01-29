package com.example.javafxjdbc;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Main extends Application {
    //sterownik sqlite
    private static final String DRIVER = "org.sqlite.JDBC";
    //wstępna lokalizacja bazy danych
    public static String DB_URL = "jdbc:sqlite:C:\\baza_pokemon\\pokemonDB.db";

    private static Connection connection;

    private TextField usernameField;
    private PasswordField passwordField;
    private TextArea infoTextArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            //dynamiczne załadowanie sterownika sqlite i połączenie z bazą danych
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(DB_URL);


            primaryStage.setTitle("Login to Pokemon Database!");

            VBox vbox = new VBox(10);
            vbox.setAlignment(Pos.CENTER);
            vbox.setPadding(new Insets(20, 0, 20, 0));

            //dodanie menu do wczytania bazy danych
            MenuBar menuBar = new MenuBar();
            Menu fileMenu = new Menu("Database");
            MenuItem openDatabaseItem = new MenuItem("Load Database");
            fileMenu.getItems().add(openDatabaseItem);
            menuBar.getMenus().add(fileMenu);

            Label titleLabel = new Label("Please Log In");
            titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

            Label usernameLabel = new Label("Username:");
            usernameField = new TextField();

            Label passwordLabel = new Label("Password:");
            passwordField = new PasswordField();

            Button loginButton = new Button("Login");
            loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

            Button registerButton = new Button("Register");
            registerButton.setStyle("-fx-background-color: #008CBA; -fx-text-fill: white;");

            infoTextArea = new TextArea();
            infoTextArea.setEditable(false);
            infoTextArea.setWrapText(true);

            vbox.getChildren().addAll(menuBar, titleLabel, usernameLabel, usernameField, passwordLabel, passwordField, loginButton, registerButton, infoTextArea);

            Scene scene = new Scene(vbox, 300, 310);
            primaryStage.setResizable(false);
            primaryStage.setScene(scene);

            //ustawienie akcji dla przycisków
            loginButton.setOnAction(e -> handleLogin(primaryStage));
            registerButton.setOnAction(e -> handleRegister());
            openDatabaseItem.setOnAction(e -> handleOpenDatabase(primaryStage));

            //ustawienie ikony aplikacji
            primaryStage.getIcons().add(new Image("/pokeball.png"));
            primaryStage.show();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    //metoda odpowiedzialna za logowanie(sprawdzenie czy login i password pasują do uzytkownika w bazie)
    private void handleLogin(Stage primaryStage) {
        //zapisanie loginu i hasla
        String username = usernameField.getText();
        String password = passwordField.getText();

        //jesli jedno z pol jest puste wyswietla komunikat i konczy wykonywane metody
        if (username.isEmpty() || password.isEmpty()) {
            infoTextArea.setText("Please enter both username and password.");
            return;
        }

        //przygotowanie zapytania do bazy uzytkownikow
        String query = "SELECT * FROM users WHERE login=? AND password=?";
        //"try-with-resources"
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            //ustawienie parametrów zapytania SQL za pomocą PreparedStatement
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            //wykonanie zapytania
            ResultSet resultSet = preparedStatement.executeQuery();

            //jesli dane pasuja wywolana jest glowna czesc aplikacji
            if (resultSet.next()) {
                infoTextArea.setText("Login successful");
                primaryStage.close();
                App.main();
            } else {
                infoTextArea.setText("Invalid login or password");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //metoda odpowiedzialna za rejestracje(zapisanie uzytkownika do bazy)
    private void handleRegister() {
        //zapisanie loginu i hasla
        String username = usernameField.getText();
        String password = passwordField.getText();

        //jesli jedno z pol jest puste wyswietla komunikat i konczy wykonywane metody
        if (username.isEmpty() || password.isEmpty()) {
            infoTextArea.setText("Please enter both username and password.");
            return;
        }

        //przygotowanie zapytania do bazy uzytkownikow
        String query = "INSERT INTO users (login, password) VALUES (?, ?)";
        //"try-with-resources"
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            //wykonanie zapytania i sprawdzenie czy rejestracja przebiegla pomyslnie
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                infoTextArea.setText("User registered successfully");
            } else {
                infoTextArea.setText("Registration failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //metoda zajmujaca sie wczytaniem bazy danych z podanego przez uzytkownika miejsca na dysku
    private void handleOpenDatabase(Stage primaryStage) {
        //utworzenie obiektu FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Database File");
        //dodanie filtrow
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("SQLite Database", "*.db"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            try {
                //zamyka aktualne polaczenie jesli takie istnieje
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }

                //ustawienie nowej bazy danych
                DB_URL = "jdbc:sqlite:" + selectedFile.getAbsolutePath();

                //utworzenie polaczenia z baza
                connection = DriverManager.getConnection(DB_URL);
                System.out.println("Polaczono z: " + DB_URL);
                //wyswietlenie komunikatu o polaczeniu z baza
                infoTextArea.setText("Connected to: " + DB_URL);
            } catch (Exception e) {
                e.printStackTrace();
                infoTextArea.setText("Error while connecting to the database");
            }
        }
    }
}

