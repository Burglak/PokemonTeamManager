package com.example.javafxjdbc;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class App {
    //sterownik do sqlite
    private static final String DRIVER = "org.sqlite.JDBC";
    //wstępna lokalizacja bazy danych
    private static String DB_URL = "jdbc:sqlite:D:\\baza_pokemon\\pokemonDB.db";
    private static Connection connection;

    //tabele odzwierciedlajace wyglad bazy danych(pokemonUser i pokemonComp)
    private TableView<Pokemon> tableView1 = new TableView<>();
    private TableView<Pokemon> tableView2 = new TableView<>();

    public static void main() {
        App app = new App();
        Stage stage = new Stage();
        app.start(stage);
    }

    public void start(Stage primaryStage) {
        try {
            //przypisanie lokacji bazy danych podanej przy logowaniu
            DB_URL = Main.DB_URL;

            //dynamiczne załadowanie sterownika sqlite i połączenie z bazą danych
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(DB_URL);

            primaryStage.setTitle("PokemonTeamManager");
            //menuBar
            MenuBar menuBar = new MenuBar();
            Menu menuFile = new Menu("Menu");
            MenuItem menuItemRefresh = new MenuItem("Refresh");
            MenuItem menuItemClose = new MenuItem("Exit");
            menuFile.getItems().addAll(menuItemRefresh, menuItemClose);
            menuBar.getMenus().add(menuFile);

            //tabele TableView
            createTableView(tableView1, "pokemonUser");
            createTableView(tableView2, "pokemonComp");
            tableView1.setStyle("-fx-control-inner-background: lightblue;");
            tableView2.setStyle("-fx-control-inner-background: lightblue;");

            //etykiety do tabel
            Label labelTeam = new Label("TEAM");
            Label labelComputer = new Label("COMPUTER");

            //ustawienie stylu dla napisow
            labelTeam.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white;");
            labelComputer.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white;");

            //dodanie napisow z wyrownaniem do srodka
            VBox teamVBox = new VBox(labelTeam, tableView1);
            VBox computerVBox = new VBox(labelComputer, tableView2);
            teamVBox.setAlignment(Pos.CENTER);
            computerVBox.setAlignment(Pos.CENTER);

            //przyciski miedzy oknami
            Button button1 = new Button(">");
            button1.setMinSize(50, 50);
            Button button2 = new Button("<");
            button2.setMinSize(50, 50);

            //przyciski do zarzadzania tableView2(tabela pokemonComp)
            Button button3 = new Button("Add");
            Button button4 = new Button("Remove");
            Button button5 = new Button("Update");

            //border pane
            BorderPane borderPane = new BorderPane();
            borderPane.setTop(menuBar);

            //ustawienie tla korzystajac z klasy "main-background", ktora znajduje sie w pliku style.css
            borderPane.getStyleClass().add("main-background");

            //przyciski między oknami
            VBox buttonsBetweenTables = new VBox(button1, button2);
            buttonsBetweenTables.setSpacing(10);
            buttonsBetweenTables.setPadding(new Insets(10));
            borderPane.setCenter(buttonsBetweenTables);

            //dodanie funkcjonalności do przycisków
            //menu -> close - zamyka aplikacje
            menuItemClose.setOnAction(event -> primaryStage.close());
            ////menu -> refresh - odswieza tabele
            menuItemRefresh.setOnAction(event -> refreshTables());
            //przycisk ">" - wywolanie metody movePokemon z ponizszymi parametrami
            button1.setOnAction(event -> movePokemon(tableView1, tableView2, "pokemonUser", "pokemonComp"));
            //przycisk "<" - wywolanie metody movePokemon z ponizszymi parametrami
            button2.setOnAction(event -> movePokemon(tableView2, tableView1, "pokemonComp", "pokemonUser"));
            //przycisk "add" - dodanie pokemona do tableViev2(tabela pokemonComp)
            button3.setOnAction(event -> {
                Pokemon newPokemon = AddPokemon.showAndWait(connection);
                if (newPokemon != null) {
                    tableView2.getItems().add(newPokemon);
                }
                refreshTables();
            });
            //przycisk "remove" - usuniecie pokemona z tableViev2(tabela pokemonComp)
            button4.setOnAction(event -> removePokemon(tableView2, "pokemonComp"));
            //przycisk "update" - zaktualizowanie danych pokemona z tableViev2(tabela pokemonComp)
            button5.setOnAction(event -> {
                //przypisanie wybranego pokemona do zmiennej
                Pokemon selectedPokemon = tableView2.getSelectionModel().getSelectedItem();
                //jesli wybrano pokemona - wywolanie metody showAndWait ktora przyjmuje ponizsze parametry
                if (selectedPokemon != null) {
                    UpdatePokemon.showAndWait(connection, selectedPokemon);
                    refreshTables();
                } else {
                    //wyswietlenie komunikatu bledu - nie wybrano pokemona do aktualizacji
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warning");
                    alert.setHeaderText(null);
                    alert.setContentText("Please select a Pokemon to update.");
                    alert.showAndWait();
                }
                refreshTables();
            });

            //dodanie przestrzeni pomiedzy tabelami
            HBox hBox = new HBox(teamVBox, buttonsBetweenTables, computerVBox);
            hBox.setSpacing(20);
            hBox.setPadding(new Insets(10));
            borderPane.setBottom(hBox);

            //przyciski nad drugim oknem
            HBox buttonsUnderSecondTable = new HBox(button3, button4, button5);
            buttonsUnderSecondTable.setSpacing(10);
            buttonsUnderSecondTable.setPadding(new Insets(10));
            borderPane.setRight(buttonsUnderSecondTable);

            //wyświetlanie sceny / dodanie ikony / dodanie arkusza styli
            Scene scene = new Scene(borderPane, 920, 400);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.getIcons().add(new Image("/pokeball.png"));
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //metoda ustawiajaca wlasciwosci tabeli
    private void createTableView(TableView<Pokemon> tableView, String DBType) {

        //utworzenie kolumn
        TableColumn<Pokemon, Integer> idPokemonColumn = new TableColumn<>("ID");
        TableColumn<Pokemon, String> nameColumn = new TableColumn<>("Name");
        TableColumn<Pokemon, Integer> hpColumn = new TableColumn<>("HP");
        TableColumn<Pokemon, Integer> attackColumn = new TableColumn<>("Attack");
        TableColumn<Pokemon, Integer> defenseColumn = new TableColumn<>("Defense");
        TableColumn<Pokemon, Integer> speedColumn = new TableColumn<>("Speed");
        TableColumn<Pokemon, String> typeColumn = new TableColumn<>("Type");

        //przypisanie wartosci
        idPokemonColumn.setCellValueFactory(new PropertyValueFactory<>("idPokemon"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        hpColumn.setCellValueFactory(new PropertyValueFactory<>("hp"));
        attackColumn.setCellValueFactory(new PropertyValueFactory<>("attack"));
        defenseColumn.setCellValueFactory(new PropertyValueFactory<>("defense"));
        speedColumn.setCellValueFactory(new PropertyValueFactory<>("speed"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        //ukrycie idPokemon
        idPokemonColumn.setVisible(false);

        //ustawienie szerokości dla wszystkich kolumn
        setColumnWidth(idPokemonColumn, 0);
        setColumnWidth(nameColumn, 100);
        setColumnWidth(hpColumn, 50);
        setColumnWidth(attackColumn, 50);
        setColumnWidth(defenseColumn, 50);
        setColumnWidth(speedColumn, 50);
        setColumnWidth(typeColumn, 100);

        tableView.getColumns().addAll(idPokemonColumn, nameColumn, hpColumn, attackColumn, defenseColumn, speedColumn, typeColumn);
        //pobranie rekordow z odpowiedniej tabeli bazy
        if (DBType.equals("pokemonUser"))
            tableView.setItems(getPokemonData("pokemonUser"));
        if (DBType.equals("pokemonComp"))
            tableView.setItems(getPokemonData("pokemonComp"));
    }

    //metoda wykorzystywana podczas tworzenia tabel - ustawia minimalna i preferowana szerokosc kolumn
    private void setColumnWidth(TableColumn<?, ?> column, double width) {
        column.setMinWidth(width);
        column.setPrefWidth(width);
    }

    //metoda odpowiadajaca za pobieranie pokemonow z bazy i zwracanie ich w postaci ObservableList
    private ObservableList<Pokemon> getPokemonData(String tableName) {
        //utworzenie ObservableList
        ObservableList<Pokemon> pokemonList = FXCollections.observableArrayList();

        //przygotowanie zapytania do bazy
        String query = "SELECT * FROM " + tableName;
        //"try-with-resources"
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            //wykonanie zapytania do bazy
            ResultSet resultSet = preparedStatement.executeQuery();

            //pobranie wszystkich rekordow i przypisanie ich do listy
            while (resultSet.next()) {
                int idPokemon = resultSet.getInt("idPokemon");
                String name = resultSet.getString("name");
                int hp = resultSet.getInt("hp");
                int attack = resultSet.getInt("attack");
                int defense = resultSet.getInt("defense");
                int speed = resultSet.getInt("speed");
                String type = resultSet.getString("type");

                pokemonList.add(new Pokemon(idPokemon, name, hp, attack, defense, speed, type));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pokemonList;
    }

    //metoda zajmujaca sie prznoszeniem pokemonow z jednej tabeli do drugiej(pokemonComp -> pokemonUser & pokemonUser -> pokemonComp)
    //przyjmuje argumenty "tabele z ktorej przenosimy", "tabele do ktorej przenosimy", "nazwe tabeli z bazy danych z ktorej przenosimy", "nazwe tabeli z bazy danych do ktorej przenosimy"
    private void movePokemon(TableView<Pokemon> sourceTable, TableView<Pokemon> targetTable, String sourceTableName, String targetTableName) {
        //sprawdza czy liosc pokemonow w tabeli user nie jest wieksza niz 6
        if (!(targetTable.getItems().size() >= 6 && targetTable == tableView1)) {
            Pokemon selectedPokemon = sourceTable.getSelectionModel().getSelectedItem();
            if (selectedPokemon != null) {
                //usuwa z widoku tabeli
                sourceTable.getItems().remove(selectedPokemon);

                //sprawdza czy liosc pokemonow w tabeli user nie jest wieksza niz 6
                if (!(targetTable.getItems().size() >= 6 && targetTable == tableView1)) {
                    //dodaje do tabeli docelowej
                    targetTable.getItems().add(selectedPokemon);

                    //aktualizuje baze
                    updateDatabase(selectedPokemon, sourceTableName, targetTableName);

                    refreshTables();
                } else {
                    //wyswietla komunikat, że tabela user ma juz 6 pokemonow
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information");
                    alert.setHeaderText(null);
                    alert.setContentText("The target table already has 6 Pokemon. You can't add more.");
                    alert.showAndWait();
                }
            }
        } else {
            //wyswietla komunikat, że tabela user ma juz 6 pokemonow
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("You can't have more than 6 Pokemons in your team!");
            alert.showAndWait();
        }
    }

    //metoda usuwajaca pokemona z tabeli
    private void removePokemon(TableView<Pokemon> targetTable, String targetTableName) {
        Pokemon selectedPokemon = targetTable.getSelectionModel().getSelectedItem();
        if (selectedPokemon != null) {
            //usuwa z widoku
            targetTable.getItems().remove(selectedPokemon);
            //usuwa z bazy danych
            deleteFromDatabase(selectedPokemon, targetTableName);

            refreshTables();
        }
    }
    //metoda zajmujaca sie usuwaniem z bazy danych
    private void deleteFromDatabase(Pokemon pokemon, String tableName) {
        //przygotowanie zapytania
        String query = "DELETE FROM " + tableName + " WHERE idPokemon = ?";
        //"try-with-resources"
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, pokemon.getIdPokemon());
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //metoda aktualizujaca dane w bazie
    private void updateDatabase(Pokemon pokemon, String sourceTableName, String targetTableName) {
        //usuwa z jednej tabeli
        //przygotowanie zapytania
        String deleteQuery = "DELETE FROM " + sourceTableName + " WHERE idPokemon = ?";
        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
            deleteStatement.setInt(1, pokemon.getIdPokemon());
            deleteStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //dodaje do drugiej tabeli
        //przygotowanie zapytania
        String insertQuery = "INSERT INTO " + targetTableName + " (name, hp, attack, defense, speed, type) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
            insertStatement.setString(1, pokemon.getName());
            insertStatement.setInt(2, pokemon.getHp());
            insertStatement.setInt(3, pokemon.getAttack());
            insertStatement.setInt(4, pokemon.getDefense());
            insertStatement.setInt(5, pokemon.getSpeed());
            insertStatement.setString(6, pokemon.getType());

            insertStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //metoda odpowiedzialna za ponowne wczytywanie danych z tabeli(odswiezanie)
    private void refreshTables() {
        tableView1.getItems().clear();
        tableView1.setItems(getPokemonData("pokemonUser"));
        tableView2.getItems().clear();
        tableView2.setItems(getPokemonData("pokemonComp"));
    }
}
