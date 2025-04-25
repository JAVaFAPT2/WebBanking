package service;

import java.io.*;
import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/test";
    private static final String USER = "root";
    private static final String PASS = "root";

    public static void main(String[] args) {
        createTable();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Import data");
            System.out.println("2. Display data");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter file path: ");
                    String filePath = scanner.nextLine();
                    importData(filePath);
                    break;
                case 2:
                    displayData();
                    break;
                case 3:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    private static void createTable() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS t_order (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "customer_name VARCHAR(100), " +
                        "customer_phone VARCHAR(100), " +
                        "customer_email VARCHAR(100), " +
                        "amount FLOAT, " +
                        "status BOOLEAN, " +
                        "created_at DATE)";
            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void importData(String filePath) {
        String sql = "INSERT INTO t_order (customer_name, customer_phone, customer_email, amount, status, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    pstmt.setString(1, data[0].trim());
                    pstmt.setString(2, data[1].trim());
                    pstmt.setString(3, data[2].trim());
                    pstmt.setFloat(4, Float.parseFloat(data[3].trim()));
                    pstmt.setBoolean(5, true);
                    pstmt.setDate(6, new java.sql.Date(System.currentTimeMillis()));
                    pstmt.executeUpdate();
                }
            }
            System.out.println("Data imported successfully!");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void displayData() {
        String sql = "SELECT * FROM t_order";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println("\nOrder ID: " + rs.getInt("id"));
                System.out.println("Customer Name: " + rs.getString("customer_name"));
                System.out.println("Customer Phone: " + rs.getString("customer_phone"));
                System.out.println("Customer Email: " + rs.getString("customer_email"));
                System.out.println("Amount: " + rs.getFloat("amount"));
                System.out.println("Status: " + rs.getBoolean("status"));
                System.out.println("Created At: " + rs.getDate("created_at"));
                System.out.println("------------------------");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}