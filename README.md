# Bus Ticket Booking Application

This project is a **Bus Ticket Booking Application** developed using Java and JavaFX. It provides a user-friendly graphical interface for users to register, log in, view available bus routes, book tickets, and manage purchased tickets. It also includes admin functionalities for managing routes and tickets.

## Features

### General Features
- **User Registration & Login:** Users can create accounts and log in to the system.
- **View Bus Routes:** Displays bus routes, schedules, and fares.
- **Book Tickets:** Users can book tickets for available routes.
- **Cancel Tickets:** Users can cancel their booked tickets.

### Admin Features
- **Add Routes:** Admins can add new bus routes.
- **Delete Routes:** Admins can delete existing bus routes.
- **Manage Tickets:** Admins can view and cancel tickets for any user.

## Prerequisites

### Software Requirements
- **Java Development Kit (JDK)**: Version 8 or higher.
- **JavaFX SDK**: Ensure that JavaFX libraries are properly configured in your project.
- **MySQL**: Installed and running locally or on a remote server.

### Database Configuration
- Create a MySQL database named `mysql`.
- Use the following credentials in the code (modify if needed):
  - **Username:** `Your user name`
  - **Password:** `your password`

### Database Tables
The application creates the following tables if they don't exist:
1. **Users**:
   ```sql
   CREATE TABLE Users (
       id INT AUTO_INCREMENT PRIMARY KEY,
       username VARCHAR(255) UNIQUE NOT NULL,
       password VARCHAR(255) NOT NULL
   );
   ```
2. **Routes**:
   ```sql
   CREATE TABLE Routes (
       id INT AUTO_INCREMENT PRIMARY KEY,
       route VARCHAR(255) NOT NULL,
       from_location VARCHAR(255) NOT NULL,
       to_location VARCHAR(255) NOT NULL,
       departure_time DATETIME NOT NULL,
       fare DOUBLE NOT NULL
   );
   ```
3. **Tickets**:
   ```sql
   CREATE TABLE Tickets (
       id INT AUTO_INCREMENT PRIMARY KEY,
       user_id INT NOT NULL,
       route_id INT NOT NULL,
       purchase_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (user_id) REFERENCES Users(id),
       FOREIGN KEY (route_id) REFERENCES Routes(id)
   );
   ```

## How to Run the Application

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd <repository-folder>
   ```

2. **Set Up the Database**
   - Start your MySQL server.
   - Update the database connection URL, username, and password in the `connectToDatabase` method if needed.
   - Run the application to initialize the database tables.

3. **Configure JavaFX**
   - Ensure JavaFX libraries are added to your project.
   - Update your IDE settings to include JavaFX runtime libraries.

4. **Run the Application**
   - Compile and execute the `TicketBookingSceneWithDB` class.
   - Use an IDE like IntelliJ IDEA or Eclipse, or run via the command line:
     ```bash
     javac TicketBookingSceneWithDB.java
     java TicketBookingSceneWithDB
     ```

## Application Workflow

### Registration & Login
- Users can register using a unique username and password.
- Admin username is predefined as `admin`.

### Routes Management
- Users can view available routes.
- Admins can add or delete routes.

### Tickets Management
- Users can book and cancel their own tickets.
- Admins can cancel any user's tickets.

## Code Structure

### Main Components
- **Database Connection**: Connects to MySQL and initializes tables.
- **UI Design**: Built using JavaFX components like `VBox`, `HBox`, `TabPane`, `TableView`.
- **Event Handlers**: Handle button clicks for registration, login, ticket booking, and route management.

### Key Classes
- **`TicketBookingSceneWithDB`**: Main class containing the application logic.
- **`BusRoute`**: Represents a bus route.
- **`Ticket`**: Represents a ticket.

## License
This project is licensed under the MIT License. Feel free to use, modify, and distribute.

## Contact
For any questions or suggestions, please create an issue on the GitHub repository or reach out to the project maintainer.

---

Enjoy using the **Bus Ticket Booking Application**!

