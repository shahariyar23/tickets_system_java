
package ticketbookingscenewithdb;


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketBookingSceneWithDB extends Application {

    private Connection connection;
    private String currentUser;
    private final String ADMIN_USERNAME = "mostak";  // The admin username

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        connectToDatabase();
        createTables();

        primaryStage.setTitle("Bus Ticket Booking App");

        VBox registrationBox = new VBox(10);
        registrationBox.setPadding(new Insets(20));

        // User Registration Form
        Label registrationLabel = new Label("User Registration");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Button registerButton = new Button("Register");

        registrationBox.getChildren().addAll(registrationLabel, usernameField, passwordField, registerButton);

        // User Login Form
        VBox loginBox = new VBox(10);
        Label loginLabel = new Label("User Login");
        TextField loginUsernameField = new TextField();
        loginUsernameField.setPromptText("Username");
        PasswordField loginPasswordField = new PasswordField();
        loginPasswordField.setPromptText("Password");
        Button loginButton = new Button("Login");

        loginBox.getChildren().addAll(loginLabel, loginUsernameField, loginPasswordField, loginButton);

        // Bus Routes and Schedules UI
        VBox routesBox = new VBox(10);
        routesBox.setPadding(new Insets(20));

        Label routesLabel = new Label("Bus Routes and Schedules");
        TableView<BusRoute> tableView = createTableView();
        routesBox.getChildren().addAll(routesLabel, tableView);

        // Form to add a new route (Only visible to admin)
        HBox addRouteBox = new HBox(10);
        TextField routeField = new TextField();
        routeField.setPromptText("Route");
        TextField fromField = new TextField();
        fromField.setPromptText("From");
        TextField destinationField = new TextField();
        destinationField.setPromptText("To");
        TextField departureTimeField = new TextField();
        departureTimeField.setPromptText("Departure Time");
        TextField fareField = new TextField();
        fareField.setPromptText("Fare");
        Button addRouteButton = new Button("Add Route");

        addRouteBox.getChildren().addAll(routeField, fromField, destinationField, departureTimeField, fareField, addRouteButton);

        // Delete route button (Only visible to admin)
        Button deleteRouteButton = new Button("Delete Selected Route");

        // Buy ticket button
        Button buyTicketButton = new Button("Buy Ticket");
        routesBox.getChildren().addAll(addRouteBox, deleteRouteButton, buyTicketButton);

        // Purchased Tickets UI
        VBox ticketsBox = new VBox(10);
        ticketsBox.setPadding(new Insets(20));
        Label ticketsLabel = new Label("Purchased Tickets");
        TableView<Ticket> ticketsTable = createTicketsTableView();
        ticketsBox.getChildren().addAll(ticketsLabel, ticketsTable);

        // Cancel Ticket Button (User can cancel their own tickets, admin can cancel anyone's)
        Button cancelTicketButton = new Button("Cancel Selected Ticket");
        ticketsBox.getChildren().add(cancelTicketButton);

        // Event handlers for buttons
        registerButton.setOnAction(e -> registerUser(usernameField.getText(), passwordField.getText()));
        loginButton.setOnAction(e -> {
            loginUser(loginUsernameField.getText(), loginPasswordField.getText());
            ticketsTable.getItems().setAll(fetchPurchasedTickets());
        });

        addRouteButton.setOnAction(e -> {
            if (currentUser != null && currentUser.equals(ADMIN_USERNAME)) {
                String route = routeField.getText();
                String from = fromField.getText();
                String destination = destinationField.getText();
                String departureTime = departureTimeField.getText();
                String fare = fareField.getText();

                if (route.isEmpty() || from.isEmpty() || destination.isEmpty() || departureTime.isEmpty() || fare.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields are required.");
                } else {
                    addRoute(route, from, destination, departureTime, Double.parseDouble(fare));
                    tableView.getItems().setAll(fetchRoutes());
                    routeField.clear();
                    fromField.clear();
                    destinationField.clear();
                    departureTimeField.clear();
                    fareField.clear();
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Permission Denied", "Only admin can add routes.");
            }
        });

        deleteRouteButton.setOnAction(e -> {
            if (currentUser != null && currentUser.equals(ADMIN_USERNAME)) {
                BusRoute selectedRoute = tableView.getSelectionModel().getSelectedItem();
                if (selectedRoute != null) {
                    deleteRoute(selectedRoute);
                    tableView.getItems().setAll(fetchRoutes());
                } else {
                    showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a route to delete.");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Permission Denied", "Only admin can delete routes.");
            }
        });

        buyTicketButton.setOnAction(e -> {
            BusRoute selectedRoute = tableView.getSelectionModel().getSelectedItem();
            if (selectedRoute != null && currentUser != null) {
                buyTicket(selectedRoute);
                ticketsTable.getItems().setAll(fetchPurchasedTickets());
            } else if (currentUser == null) {
                showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to buy a ticket.");
            } else {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a route to buy a ticket.");
            }
        });

        cancelTicketButton.setOnAction(e -> {
            Ticket selectedTicket = ticketsTable.getSelectionModel().getSelectedItem();
            if (selectedTicket != null) {
                if (currentUser != null && (selectedTicket.getUser().equals(currentUser) || currentUser.equals(ADMIN_USERNAME))) {
                    cancelTicket(selectedTicket);
                    ticketsTable.getItems().setAll(fetchPurchasedTickets());
                } else {
                    showAlert(Alert.AlertType.WARNING, "Permission Denied", "You cannot cancel this ticket.");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a ticket to cancel.");
            }
        });

        TabPane tabPane = new TabPane();
        Tab registrationTab = new Tab("Registration", registrationBox);
        Tab loginTab = new Tab("Login", loginBox);
        Tab routesTab = new Tab("Routes", routesBox);
        Tab ticketsTab = new Tab("My Tickets", ticketsBox);

        tabPane.getTabs().addAll(registrationTab, loginTab, routesTab, ticketsTab);

        Scene scene = new Scene(tabPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql", "mostak", "mostak");
            System.out.println("Connected to the database.");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(""" 
                CREATE TABLE IF NOT EXISTS Users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(255) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL
                );
            """);

            stmt.execute(""" 
                CREATE TABLE IF NOT EXISTS Routes (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    route VARCHAR(255) NOT NULL,
                    from_location VARCHAR(255) NOT NULL,
                    to_location VARCHAR(255) NOT NULL,
                    departure_time DATETIME NOT NULL,
                    fare DOUBLE NOT NULL
                );
            """);

            stmt.execute(""" 
                CREATE TABLE IF NOT EXISTS Tickets (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    route_id INT NOT NULL,
                    purchase_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES Users(id),
                    FOREIGN KEY (route_id) REFERENCES Routes(id)
                );
            """);

            System.out.println("Tables initialized.");
        } catch (SQLException e) {
            System.err.println("Failed to create tables: " + e.getMessage());
        }
    }

    private void registerUser(String username, String password) {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO Users (username, password) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            System.out.println("User registered successfully!");
        } catch (SQLException e) {
            System.err.println("Registration failed: " + e.getMessage());
        }
    }

    private void loginUser(String username, String password) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Users WHERE username = ? AND password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentUser = username;
                System.out.println("Login successful!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }
        } catch (SQLException e) {
            System.err.println("Login failed: " + e.getMessage());
        }
    }

    private TableView<BusRoute> createTableView() {
        TableView<BusRoute> tableView = new TableView<>();

        TableColumn<BusRoute, String> routeColumn = new TableColumn<>("Route");
        routeColumn.setCellValueFactory(new PropertyValueFactory<>("route"));

        TableColumn<BusRoute, String> fromColumn = new TableColumn<>("From");
        fromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));

        TableColumn<BusRoute, String> destinationColumn = new TableColumn<>("Destination");
        destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destination"));

        TableColumn<BusRoute, String> departureTimeColumn = new TableColumn<>("Departure Time");
        departureTimeColumn.setCellValueFactory(new PropertyValueFactory<>("departureTime"));

        TableColumn<BusRoute, Double> fareColumn = new TableColumn<>("Fare");
        fareColumn.setCellValueFactory(new PropertyValueFactory<>("fare"));

        tableView.getColumns().addAll(routeColumn, fromColumn, destinationColumn, departureTimeColumn, fareColumn);

        tableView.getItems().setAll(fetchRoutes());

        return tableView;
    }

    private TableView<Ticket> createTicketsTableView() {
        TableView<Ticket> tableView = new TableView<>();

        TableColumn<Ticket, String> routeColumn = new TableColumn<>("Route");
        routeColumn.setCellValueFactory(new PropertyValueFactory<>("route"));

        TableColumn<Ticket, String> fromColumn = new TableColumn<>("From");
        fromColumn.setCellValueFactory(new PropertyValueFactory<>("from"));

        TableColumn<Ticket, String> destinationColumn = new TableColumn<>("Destination");
        destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destination"));

        TableColumn<Ticket, String> departureTimeColumn = new TableColumn<>("Departure Time");
        departureTimeColumn.setCellValueFactory(new PropertyValueFactory<>("departureTime"));

        TableColumn<Ticket, String> purchaseTimeColumn = new TableColumn<>("Purchase Time");
        purchaseTimeColumn.setCellValueFactory(new PropertyValueFactory<>("purchaseTime"));

        TableColumn<Ticket, String> user = new TableColumn<>("User Name");
        user.setCellValueFactory(new PropertyValueFactory<>("user"));
        tableView.getColumns().addAll(routeColumn, fromColumn, destinationColumn, departureTimeColumn, purchaseTimeColumn, user);
        return tableView;
    }

    private List<BusRoute> fetchRoutes() {
        List<BusRoute> routes = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM Routes")) {
            while (rs.next()) {
                routes.add(new BusRoute(
                        rs.getInt("id"),
                        rs.getString("route"),
                        rs.getString("from_location"),
                        rs.getString("to_location"),
                        rs.getString("departure_time"),
                        rs.getDouble("fare")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch routes: " + e.getMessage());
        }
        return routes;
    }

    private List<Ticket> fetchPurchasedTickets() {
        List<Ticket> tickets = new ArrayList<>();
        try {
            String sqlQuery = "SELECT Tickets.id, Routes.route, Routes.from_location, Routes.to_location, Routes.departure_time, Tickets.purchase_time, Users.username "
                    + "FROM Tickets JOIN Routes ON Tickets.route_id = Routes.id JOIN Users ON Tickets.user_id = Users.id";

            if (!"babla".equals(currentUser)) {
                sqlQuery += " WHERE Users.username = ?";
            }

            try (PreparedStatement stmt = connection.prepareStatement(sqlQuery)) {
                if (!"babla".equals(currentUser)) {
                    stmt.setString(1, currentUser);
                }

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    tickets.add(new Ticket(
                            rs.getInt("id"),
                            rs.getString("route"),
                            rs.getString("from_location"),
                            rs.getString("to_location"),
                            rs.getString("departure_time"),
                            rs.getString("purchase_time"),
                            rs.getString("username")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch purchased tickets: " + e.getMessage());
        }
        return tickets;
    }

    private void addRoute(String route, String from, String destination, String departureTime, double fare) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO Routes (route, from_location, to_location, departure_time, fare) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setString(1, route);
            stmt.setString(2, from);
            stmt.setString(3, destination);
            stmt.setString(4, departureTime);
            stmt.setDouble(5, fare);
            stmt.executeUpdate();
            System.out.println("Route added successfully!");
        } catch (SQLException e) {
            System.err.println("Failed to add route: " + e.getMessage());
        }
    }

    private void deleteRoute(BusRoute route) {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM Routes WHERE id = ?")) {
            stmt.setInt(1, route.getId());
            stmt.executeUpdate();
            System.out.println("Route deleted successfully!");
        } catch (SQLException e) {
            System.err.println("Failed to delete route: " + e.getMessage());
        }
    }

    private void buyTicket(BusRoute route) {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO Tickets (user_id, route_id) VALUES (?, ?)")) {
            // Assume we have the currentUser as logged in, so get their user ID
            int userId = getUserIdByUsername(currentUser);
            stmt.setInt(1, userId);
            stmt.setInt(2, route.getId());
            stmt.executeUpdate();
            System.out.println("Ticket purchased successfully!");
        } catch (SQLException e) {
            System.err.println("Failed to buy ticket: " + e.getMessage());
        }
    }

    private void cancelTicket(Ticket ticket) {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM Tickets WHERE id = ?")) {
            stmt.setInt(1, ticket.getId());
            stmt.executeUpdate();
            System.out.println("Ticket cancelled successfully!");
        } catch (SQLException e) {
            System.err.println("Failed to cancel ticket: " + e.getMessage());
        }
    }

    private int getUserIdByUsername(String username) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT id FROM Users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static class Ticket {
        private final int id;
        private final String route;
        private final String from;
        private final String destination;
        private final String departureTime;
        private final String purchaseTime;
        private final String user;

        public Ticket(int id, String route, String from, String destination, String departureTime, String purchaseTime, String user) {
            this.id = id;
            this.route = route;
            this.from = from;
            this.destination = destination;
            this.departureTime = departureTime;
            this.purchaseTime = purchaseTime;
            this.user = user;
        }

        public int getId() {
            return id;
        }

        public String getRoute() {
            return route;
        }

        public String getFrom() {
            return from;
        }

        public String getDestination() {
            return destination;
        }

        public String getDepartureTime() {
            return departureTime;
        }

        public String getPurchaseTime() {
            return purchaseTime;
        }

        public String getUser() {
            return user;
        }
    }

    public static class BusRoute {
        private final int id;
        private final String route;
        private final String from;
        private final String destination;
        private final String departureTime;
        private final double fare;

        public BusRoute(int id, String route, String from, String destination, String departureTime, double fare) {
            this.id = id;
            this.route = route;
            this.from = from;
            this.destination = destination;
            this.departureTime = departureTime;
            this.fare = fare;
        }

        public int getId() {
            return id;
        }

        public String getRoute() {
            return route;
        }

        public String getFrom() {
            return from;
        }

        public String getDestination() {
            return destination;
        }

        public String getDepartureTime() {
            return departureTime;
        }

        public double getFare() {
            return fare;
        }
    }
}
