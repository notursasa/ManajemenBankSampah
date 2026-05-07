package com.mycompany.manajemenbanksampah;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.chart.*;

import java.sql.*;
import java.util.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.stream.Collectors;
import javafx.stage.FileChooser;
import java.io.*;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;



public class App extends Application {

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/bank_sampah_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = ""; 

    private Connection conn;
    private WasteDepositDAO wasteDepositDAO;
    private UserDAO userDAO;
    
    private RewardDAO rewardDAO;
    private PointsTransactionDAO pointsTransactionDAO;

    
    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
    
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void sendDiscordNotification(String content) {
        try {
            String webhookUrl = "https://discord.com/api/webhooks/1378089006339850401/bEDITg_0q7ZQWtt921Sg0Cbl6lOew8PC92pVMbp1qc6b-2ib-m6FED-VelUvgLhL-7lE"; // Ganti dengan Webhook kamu

            String jsonPayload = "{\"content\": \"" + escapeJson(content) + "\"}";

            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Discord Webhook Response Code: " + responseCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "");
    }
    
    // === STYLING METHODS ===
    private void applyPrimaryButtonStyle(Button button) {
        button.setStyle(
            "-fx-background-color: #2E7D32;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12px 24px;" +
            "-fx-background-radius: 8px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        );
        
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: #388E3C;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12px 24px;" +
            "-fx-background-radius: 8px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 3);"
        ));
        
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: #2E7D32;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12px 24px;" +
            "-fx-background-radius: 8px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);"
        ));
    }
    
    private void applySecondaryButtonStyle(Button button) {
        button.setStyle(
            "-fx-background-color: #FFF;" +
            "-fx-text-fill: #2E7D32;" +
            "-fx-font-size: 14px;" +
            "-fx-border-color: #2E7D32;" +
            "-fx-border-width: 2px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-cursor: hand;"
        );
        
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: #E8F5E8;" +
            "-fx-text-fill: #2E7D32;" +
            "-fx-font-size: 14px;" +
            "-fx-border-color: #2E7D32;" +
            "-fx-border-width: 2px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-cursor: hand;"
        ));
        
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: #FFF;" +
            "-fx-text-fill: #2E7D32;" +
            "-fx-font-size: 14px;" +
            "-fx-border-color: #2E7D32;" +
            "-fx-border-width: 2px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-cursor: hand;"
        ));
    }
    
    private void applyTextFieldStyle(TextField field) {
        field.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-padding: 12px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-border-color: #E0E0E0;" +
            "-fx-border-width: 1px;" +
            "-fx-background-color: #FAFAFA;"
        );
        
        field.setOnMouseEntered(e -> field.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-padding: 12px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-border-color: #2E7D32;" +
            "-fx-border-width: 2px;" +
            "-fx-background-color: #FAFAFA;"
        ));
        
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                    "-fx-font-size: 14px;" +
                    "-fx-padding: 12px;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-border-color: #2E7D32;" +
                    "-fx-border-width: 2px;" +
                    "-fx-background-color: #FAFAFA;"
                );
            } else {
                field.setStyle(
                    "-fx-font-size: 14px;" +
                    "-fx-padding: 12px;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-border-color: #E0E0E0;" +
                    "-fx-border-width: 1px;" +
                    "-fx-background-color: #FAFAFA;"
                );
            }
        });
    }
    
    private void applyCardStyle(VBox container) {
        container.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);" +
            "-fx-padding: 30px;"
        );
    }

    // --- USER MODEL ---
    public static class User {
        public String id;
        public String name;
        public String email;
        public String phoneNumber;
        public String address;
        public String passwordHash;
        public String role;

        public User(String id, String name, String email, String phoneNumber, String address, String passwordHash, String role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.address = address;
            this.passwordHash = passwordHash;
            this.role = role;
        }

        @Override
        public String toString() {
            return name + " (" + email + ") - " + role;
        }
    }
    
    // --- USER DAO ---
    public static class UserDAO {
        private Connection conn;
        
        public UserDAO(Connection conn) {
            this.conn = conn;
        }
        
        public void insertUser(User user) {
            String sql = "INSERT INTO users (id, name, email, phone_number, address, password_hash, role) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.id);
                stmt.setString(2, user.name);
                stmt.setString(3, user.email);
                stmt.setString(4, user.phoneNumber);
                stmt.setString(5, user.address);
                stmt.setString(6, user.passwordHash);
                stmt.setString(7, user.role);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        public List<User> getAllUsers() {
            List<User> users = new ArrayList<>();
            String sql = "SELECT * FROM users";

            try (Statement stmt = conn.createStatement(); 
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    User u = new User(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone_number"),
                            rs.getString("address"),
                            rs.getString("password_hash"),
                            rs.getString("role")
                    );
                    users.add(u);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return users;
        }
        
        public User findByEmail(String email) {
            String sql = "SELECT * FROM users WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone_number"),
                            rs.getString("address"),
                            rs.getString("password_hash"),
                            rs.getString("role")
                        );
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
        
        public int getTotalPointsForUser(String userId) {
            int earned = 0;
            int redeemed = 0;

            try {
                // Hitung poin yang diperoleh
                String sqlEarned = "SELECT SUM(points_earned) as total FROM waste_deposits WHERE user_id = ? AND status = 'verified'";
                try (PreparedStatement stmt = conn.prepareStatement(sqlEarned)) {
                    stmt.setString(1, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            earned = rs.getInt("total");
                        }
                    }
                }

                // Hitung poin yang telah ditukar
                String sqlRedeemed = "SELECT SUM(amount) as total FROM points_transactions WHERE user_id = ? AND type = 'redeem'";
                try (PreparedStatement stmt = conn.prepareStatement(sqlRedeemed)) {
                    stmt.setString(1, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            redeemed = rs.getInt("total");
                        }
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return earned - redeemed;
        }

        
        public double getTotalWeightForUser(String userId) {
            String sql = "SELECT SUM(weight_kg) as total FROM waste_deposits WHERE user_id = ? AND status = 'verified'";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("total");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0.0;
        }
    }
    
    // WASTE DEPOSIT MODEL
    public static class WasteDeposit {
        public String id;
        public String userId;
        public int categoryId;
        public double weightKg;
        public int pointsEarned;
        public LocalDateTime dateDeposited;
        public String status;

        public WasteDeposit(String id, String userId, int categoryId, double weightKg, int pointsEarned, LocalDateTime dateDeposited, String status) {
            this.id = id;
            this.userId = userId;
            this.categoryId = categoryId;
            this.weightKg = weightKg;
            this.pointsEarned = pointsEarned;
            this.dateDeposited = dateDeposited;
            this.status = status;
        }

        @Override
        public String toString() {
            return "Setoran[" + weightKg + " kg, " + pointsEarned + " poin, status=" + status + "]";
        }
    }
    
    // WASTE DAO
    public static class WasteDepositDAO {
        private Connection conn;
        
        public List<WasteDeposit> getAllDeposits() {
            List<WasteDeposit> deposits = new ArrayList<>();
            String sql = "SELECT * FROM waste_deposits ORDER BY date_deposited DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    WasteDeposit deposit = new WasteDeposit(
                        rs.getString("id"),
                        rs.getString("user_id"),
                        rs.getInt("category_id"),
                        rs.getDouble("weight_kg"),
                        rs.getInt("points_earned"),
                        rs.getTimestamp("date_deposited").toLocalDateTime(),
                        rs.getString("status")
                    );
                    deposits.add(deposit);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return deposits;
        }
        
        public WasteDepositDAO(Connection conn) {
            this.conn = conn;
        }

        public void insertDeposit(WasteDeposit deposit) {
            String sql = "INSERT INTO waste_deposits (id, user_id, category_id, weight_kg, points_earned, date_deposited, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, deposit.id);
                stmt.setString(2, deposit.userId);
                stmt.setInt(3, deposit.categoryId);
                stmt.setDouble(4, deposit.weightKg);
                stmt.setInt(5, deposit.pointsEarned);
                stmt.setTimestamp(6, Timestamp.valueOf(deposit.dateDeposited));
                stmt.setString(7, deposit.status);
                stmt.executeUpdate(); // ⬅️ WAJIB ADA
            } catch (SQLException e) {
                System.err.println("❌ GAGAL INSERT DATABASE:");
                e.printStackTrace();
            }
        }

        
        public List<WasteDeposit> getDepositsByUser(String userId) {
            List<WasteDeposit> deposits = new ArrayList<>();
            String sql = "SELECT * FROM waste_deposits WHERE user_id = ? ORDER BY date_deposited DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        WasteDeposit deposit = new WasteDeposit(
                            rs.getString("id"),
                            rs.getString("user_id"),
                            rs.getInt("category_id"),
                            rs.getDouble("weight_kg"),
                            rs.getInt("points_earned"),
                            rs.getTimestamp("date_deposited").toLocalDateTime(),
                            rs.getString("status")
                        );
                        deposits.add(deposit);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return deposits;
        }
    }
    
    public static class Reward {
        public int id;
        public String name;
        public int pointsCost;
        public int stock;

        public Reward(int id, String name, int pointsCost, int stock) {
            this.id = id;
            this.name = name;
            this.pointsCost = pointsCost;
            this.stock = stock;
        }

        @Override
        public String toString() {
            return name + " (" + pointsCost + " poin)";
        }
    }
    
    public static class RewardDAO {
        private Connection conn;

        public RewardDAO(Connection conn) {
            this.conn = conn;
        }

        public List<Reward> getAllRewards() {
            List<Reward> rewards = new ArrayList<>();
            String sql = "SELECT * FROM rewards WHERE stock > 0";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rewards.add(new Reward(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("points_cost"),
                        rs.getInt("stock")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return rewards;
        }
        
        public List<PopularRewardStat> getPopularRewardsStats() {
            List<PopularRewardStat> list = new ArrayList<>();
            String sql = "SELECT r.name, COUNT(*) AS total_redeemed FROM points_transactions t JOIN rewards r ON t.description LIKE CONCAT('%', r.name) WHERE t.type = 'redeem' GROUP BY r.name ORDER BY total_redeemed DESC";

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    list.add(new PopularRewardStat(
                        rs.getString("name"),
                        rs.getInt("total_redeemed")
                    ));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return list;
        }

        
        public void insertReward(String name, int pointsCost, int stock) {
            String sql = "INSERT INTO rewards (name, points_cost, stock) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setInt(2, pointsCost);
                stmt.setInt(3, stock);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void updateReward(int id, String name, int pointsCost, int stock) {
            String sql = "UPDATE rewards SET name = ?, points_cost = ?, stock = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setInt(2, pointsCost);
                stmt.setInt(3, stock);
                stmt.setInt(4, id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void deleteReward(int id) {
            String sql = "DELETE FROM rewards WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        public void reduceStock(int rewardId) {
            String sql = "UPDATE rewards SET stock = stock - 1 WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, rewardId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static class PointsTransaction {
        public String id, userId, type, description;
        public int amount;
        public LocalDateTime date;

        // Tambahan opsional untuk hasil join
        public String userName;

        public PointsTransaction(String id, String userId, String type, int amount, String description, LocalDateTime date) {
            this.id = id;
            this.userId = userId;
            this.type = type;
            this.amount = amount;
            this.description = description;
            this.date = date;
        }
    }


    

    public static class PointsTransactionDAO {
        private Connection conn;

        public PointsTransactionDAO(Connection conn) {
            this.conn = conn;
        }

        public void insertTransaction(String userId, String type, int amount, String description) {
            String sql = "INSERT INTO points_transactions (id, user_id, type, amount, description, date) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, UUID.randomUUID().toString());
                stmt.setString(2, userId);
                stmt.setString(3, type);
                stmt.setInt(4, amount);
                stmt.setString(5, description);
                stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public List<PointsTransaction> getRedeemTransactionsForUser(String userId) {
            List<PointsTransaction> list = new ArrayList<>();
            String sql = "SELECT * FROM points_transactions WHERE user_id = ? AND type = 'redeem' ORDER BY date DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(new PointsTransaction(
                            rs.getString("id"),
                            rs.getString("user_id"),
                            rs.getString("type"),
                            rs.getInt("amount"),
                            rs.getString("description"),
                            rs.getTimestamp("date").toLocalDateTime()
                        ));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return list;
        }

        // 🔹 NEW: Ambil semua transaksi penukaran dengan nama user (untuk admin)
        public List<PointsTransaction> getAllRedeemTransactionsWithUserName() {
            List<PointsTransaction> list = new ArrayList<>();
            String sql = " SELECT t.*, u.name AS user_name FROM points_transactions t JOIN users u ON t.user_id = u.id WHERE t.type = 'redeem' ORDER BY t.date DESC";

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    PointsTransaction tx = new PointsTransaction(
                        rs.getString("id"),
                        rs.getString("user_id"),
                        rs.getString("type"),
                        rs.getInt("amount"),
                        rs.getString("description"),
                        rs.getTimestamp("date").toLocalDateTime()
                    );
                    tx.userName = rs.getString("user_name"); // hasil JOIN
                    list.add(tx);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return list;
        }
    }

    
    



    
    // Data class for TableView
    public static class DepositHistoryItem {
        private final SimpleStringProperty date;
        private final SimpleStringProperty category;
        private final SimpleStringProperty weight;
        private final SimpleIntegerProperty points;
        private final SimpleStringProperty status;

        public DepositHistoryItem(String date, String category, String weight, int points, String status) {
            this.date = new SimpleStringProperty(date);
            this.category = new SimpleStringProperty(category);
            this.weight = new SimpleStringProperty(weight);
            this.points = new SimpleIntegerProperty(points);
            this.status = new SimpleStringProperty(status);
        }

        public String getDate() { return date.get(); }
        public String getCategory() { return category.get(); }
        public String getWeight() { return weight.get(); }
        public int getPoints() { return points.get(); }
        public String getStatus() { return status.get(); }
    }
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Membuka koneksi database
            conn = connect();

            // Inisialisasi DAO
            userDAO = new UserDAO(conn);
            wasteDepositDAO = new WasteDepositDAO(conn);
            
            rewardDAO = new RewardDAO(conn);
            pointsTransactionDAO = new PointsTransactionDAO(conn);


            showLoginScreen(primaryStage);

        } catch (SQLException e) {
            System.err.println("Gagal terhubung ke database: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR, "Tidak bisa terhubung ke database. Cek koneksi atau konfigurasi.");
            alert.showAndWait();
        }
    }
    
    private void showLoginScreen(Stage primaryStage) {
        // Main container with gradient background
        StackPane mainContainer = new StackPane();
        mainContainer.setStyle("-fx-background: linear-gradient(to bottom right, #E8F5E8, #C8E6C9);");
        mainContainer.setAlignment(Pos.CENTER); // pusatkan seluruh isi

        // Center card container
        VBox centerCard = new VBox(20);
        centerCard.setMaxWidth(400);
        centerCard.setMaxHeight(600);
        centerCard.setAlignment(Pos.CENTER);
        applyCardStyle(centerCard);

        // Title
        Label titleLabel = new Label("🌱 Bank Sampah");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#2E7D32"));
        titleLabel.setAlignment(Pos.CENTER);

        Label subtitleLabel = new Label("Kelola sampah, raih keuntungan");
        subtitleLabel.setFont(Font.font("System", 14));
        subtitleLabel.setTextFill(Color.web("#666666"));
        subtitleLabel.setAlignment(Pos.CENTER);

        // Login form
        VBox loginForm = new VBox(15);
        loginForm.setAlignment(Pos.CENTER);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setMaxWidth(300);
        applyTextFieldStyle(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);
        applyTextFieldStyle(passwordField);

        Button loginBtn = new Button("Masuk");
        loginBtn.setMaxWidth(300);
        applyPrimaryButtonStyle(loginBtn);

        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        Button switchToRegisterBtn = new Button("Belum punya akun? Daftar disini");
        applySecondaryButtonStyle(switchToRegisterBtn);

        loginForm.getChildren().addAll(
            emailField, passwordField, loginBtn, statusLabel, switchToRegisterBtn
        );

        // Register form
        VBox registerForm = new VBox(15);
        registerForm.setAlignment(Pos.CENTER);
        registerForm.setVisible(false);

        Button backToLoginBtn = new Button("← Kembali");
        applySecondaryButtonStyle(backToLoginBtn);
        backToLoginBtn.setOnAction(e -> {
            registerForm.setVisible(false);
            loginForm.setVisible(true);
            titleLabel.setText("🌱 Bank Sampah");
            subtitleLabel.setText("Kelola sampah, raih keuntungan");
        });

        TextField regNameField = new TextField();
        regNameField.setPromptText("Nama Lengkap");
        regNameField.setMaxWidth(300);
        applyTextFieldStyle(regNameField);

        TextField regEmailField = new TextField();
        regEmailField.setPromptText("Email");
        regEmailField.setMaxWidth(300);
        applyTextFieldStyle(regEmailField);

        TextField regPhoneField = new TextField();
        regPhoneField.setPromptText("Nomor Telepon");
        regPhoneField.setMaxWidth(300);
        applyTextFieldStyle(regPhoneField);

        TextField regAddressField = new TextField();
        regAddressField.setPromptText("Alamat");
        regAddressField.setMaxWidth(300);
        applyTextFieldStyle(regAddressField);

        PasswordField regPasswordField = new PasswordField();
        regPasswordField.setPromptText("Password");
        regPasswordField.setMaxWidth(300);
        applyTextFieldStyle(regPasswordField);

        Button registerBtn = new Button("Daftar");
        registerBtn.setMaxWidth(300);
        applyPrimaryButtonStyle(registerBtn);

        Label regStatusLabel = new Label();
        regStatusLabel.setTextFill(Color.RED);

        Button switchToLoginBtn = new Button("Sudah punya akun? Masuk disini");
        applySecondaryButtonStyle(switchToLoginBtn);

        registerForm.getChildren().addAll(
            backToLoginBtn,
            regNameField, regEmailField, regPhoneField, regAddressField,
            regPasswordField, registerBtn, regStatusLabel, switchToLoginBtn
        );

        // Add forms to center card
        centerCard.getChildren().addAll(
            titleLabel, subtitleLabel, loginForm, registerForm
        );

        mainContainer.getChildren().add(centerCard);

        // Event handlers
        switchToRegisterBtn.setOnAction(e -> {
            loginForm.setVisible(false);
            registerForm.setVisible(true);
            titleLabel.setText("🌱 Daftar Akun");
            subtitleLabel.setText("Bergabung dengan komunitas hijau");
        });

        switchToLoginBtn.setOnAction(e -> {
            registerForm.setVisible(false);
            loginForm.setVisible(true);
            titleLabel.setText("🌱 Bank Sampah");
            subtitleLabel.setText("Kelola sampah, raih keuntungan");
        });

        loginBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (email.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Email dan password harus diisi!");
                return;
            }

            User user = userDAO.findByEmail(email);
            if (user != null && sha256(password).equals(user.passwordHash)) {
                if ("admin".equals(user.role)) {
                    showAdminDashboard(primaryStage, user);
                } else {
                    showUserDashboard(primaryStage, user);
                }
            } else {
                statusLabel.setText("Email atau password salah!");
            }
        });

        registerBtn.setOnAction(e -> {
            String name = regNameField.getText().trim();
            String email = regEmailField.getText().trim();
            String phone = regPhoneField.getText().trim();
            String address = regAddressField.getText().trim();
            String password = regPasswordField.getText().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                regStatusLabel.setText("Nama, email, dan password wajib diisi!");
                return;
            }

            if (userDAO.findByEmail(email) != null) {
                regStatusLabel.setText("Email sudah terdaftar!");
                return;
            }

            String hashedPassword = sha256(password);
            String id = UUID.randomUUID().toString();
            User newUser = new User(id, name, email, phone, address, hashedPassword, "user");
            userDAO.insertUser(newUser);

            regStatusLabel.setTextFill(Color.GREEN);
            regStatusLabel.setText("Pendaftaran berhasil! Silakan login.");

            // Clear form
            regNameField.clear();
            regEmailField.clear();
            regPhoneField.clear();
            regAddressField.clear();
            regPasswordField.clear();
        });

        Scene scene = new Scene(mainContainer, 1024, 720);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bank Sampah - Login");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    
    private void showUserDashboard(Stage primaryStage, User user) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background: linear-gradient(to bottom right, #E8F5E8, #C8E6C9);");

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");

        Label welcomeLabel = new Label("Selamat datang, " + user.name + "! 👋");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        welcomeLabel.setTextFill(Color.web("#2E7D32"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Keluar");
        applySecondaryButtonStyle(logoutBtn);
        logoutBtn.setOnAction(e -> showLoginScreen(primaryStage));

        header.getChildren().addAll(welcomeLabel, spacer, logoutBtn);

        // Stats cards
        HBox statsContainer = new HBox(20);
        statsContainer.setPadding(new Insets(30));
        statsContainer.setAlignment(Pos.CENTER);

        int totalPoints = userDAO.getTotalPointsForUser(user.id);
        double totalWeight = userDAO.getTotalWeightForUser(user.id);

        VBox pointsCard = createStatsCard("Total Poin", String.valueOf(totalPoints), "🏆", "#FF9800");
        VBox weightCard = createStatsCard("Total Berat", String.format("%.1f kg", totalWeight), "⚖️", "#2196F3");
        VBox depositsCard = createStatsCard("Jumlah Setoran", String.valueOf(wasteDepositDAO.getDepositsByUser(user.id).size()), "📦", "#4CAF50");

        statsContainer.getChildren().addAll(pointsCard, weightCard, depositsCard);

        // Action buttons
        HBox actionButtons = new HBox(20);
        actionButtons.setPadding(new Insets(0, 30, 30, 30));
        actionButtons.setAlignment(Pos.CENTER);

        Button depositBtn = new Button("💰 Setor Sampah");
        Button historyBtn = new Button("📋 Riwayat Setoran");
        Button redeemBtn = new Button("🎁 Tukar Reward");
        Button rewardHistoryBtn = new Button("📜 Riwayat Reward");
        Button redeemHistoryBtn = new Button("🧾 Riwayat Penukaran");




        
        

        
        depositBtn.setPrefWidth(200);
        historyBtn.setPrefWidth(200);
        redeemBtn.setPrefWidth(200);
        rewardHistoryBtn.setPrefWidth(200);
        

        
        applyPrimaryButtonStyle(depositBtn);
        applyPrimaryButtonStyle(historyBtn);
        applyPrimaryButtonStyle(redeemBtn);
        applyPrimaryButtonStyle(rewardHistoryBtn);
        



        depositBtn.setOnAction(e -> showWasteDepositForm(primaryStage, user));
        historyBtn.setOnAction(e -> showDepositHistory(primaryStage, user));
        redeemBtn.setOnAction(e -> showRedeemRewardScreen(primaryStage, user));
        rewardHistoryBtn.setOnAction(e -> showRedeemHistoryScreen(primaryStage, user));
        


        actionButtons.getChildren().addAll(depositBtn, historyBtn, redeemBtn, rewardHistoryBtn);


        // Layout
        VBox centerContent = new VBox();
        centerContent.getChildren().addAll(statsContainer, actionButtons);

        root.setTop(header);
        root.setCenter(centerContent);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bank Sampah - Dashboard");
    }
    
    private void showAdminDashboard(Stage primaryStage, User admin) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background: linear-gradient(to bottom right, #E8F5E8, #C8E6C9);");

        VBox container = new VBox(30);
        container.setPadding(new Insets(50));
        container.setAlignment(Pos.CENTER);

        // Title
        Label title = new Label("👩‍💼 Dashboard Admin");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#2E7D32"));

        // Button: Verifikasi Setoran
        Button verifyBtn = new Button("✅ Verifikasi Setoran");
        verifyBtn.setPrefWidth(250);
        applyPrimaryButtonStyle(verifyBtn);
        verifyBtn.setOnAction(e -> showAdminDepositVerification(primaryStage, admin));

        // Button: Kelola Reward
        Button manageRewardBtn = new Button("🎁 Kelola Reward");
        manageRewardBtn.setPrefWidth(250);
        applyPrimaryButtonStyle(manageRewardBtn);
        manageRewardBtn.setOnAction(e -> showAdminRewardManagementScreen(primaryStage, admin));
        
        // Button: lihat riwayat
        Button redeemLogBtn = new Button("📜 Lihat Riwayat Penukaran");
        redeemLogBtn.setPrefWidth(250);
        applyPrimaryButtonStyle(redeemLogBtn);
        redeemLogBtn.setOnAction(e -> showAllRedeemTransactionsScreen(primaryStage, admin));
        
        // Button: Statistik
        Button statsBtn = new Button("📊 Statistik Reward Populer");
        statsBtn.setPrefWidth(250);
        applyPrimaryButtonStyle(statsBtn);
        statsBtn.setOnAction(e -> showPopularRewardStats(primaryStage, admin));
        
        


        // Button: Logout
        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setPrefWidth(250);
        applySecondaryButtonStyle(logoutBtn);
        logoutBtn.setOnAction(e -> showLoginScreen(primaryStage));

        container.getChildren().addAll(title, verifyBtn, manageRewardBtn, redeemLogBtn, statsBtn, logoutBtn);
        root.setCenter(container);

        Scene scene = new Scene(root, 1024, 720);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bank Sampah - Admin");
    }

    
    private void showAdminDepositVerification(Stage primaryStage, User admin) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        TableView<WasteDeposit> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<WasteDeposit, String> userCol = new TableColumn<>("Pengguna");
        userCol.setCellValueFactory(cd -> new SimpleStringProperty(
            userDAO.findByEmail(userDAO.getAllUsers().stream()
                .filter(u -> u.id.equals(cd.getValue().userId))
                .map(u -> u.email).findFirst().orElse("-")).name)
        );

        TableColumn<WasteDeposit, String> categoryCol = new TableColumn<>("Kategori");
        categoryCol.setCellValueFactory(cd -> new SimpleStringProperty(getCategoryName(cd.getValue().categoryId)));

        TableColumn<WasteDeposit, String> weightCol = new TableColumn<>("Berat (kg)");
        weightCol.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().weightKg)));

        TableColumn<WasteDeposit, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().status));

        TableColumn<WasteDeposit, Void> actionCol = new TableColumn<>("Aksi");
        actionCol.setCellFactory(param -> new TableCell<>() {
            final Button verifyBtn = new Button("Verifikasi");

            {
                applyPrimaryButtonStyle(verifyBtn);
                verifyBtn.setOnAction(e -> {
                    WasteDeposit deposit = getTableView().getItems().get(getIndex());
                    updateDepositStatus(deposit.id, "verified");
                    showAlert("Setoran diverifikasi!", Alert.AlertType.INFORMATION);
                    showAdminDepositVerification(primaryStage, admin); // refresh
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !"pending".equals(getTableView().getItems().get(getIndex()).status)) {
                    setGraphic(null);
                } else {
                    setGraphic(verifyBtn);
                }
            }
        });

        table.getColumns().addAll(userCol, categoryCol, weightCol, statusCol, actionCol);

        List<WasteDeposit> pending = wasteDepositDAO.getAllDeposits()
            .stream().filter(d -> "pending".equals(d.status))
            .collect(Collectors.toList());
        table.setItems(FXCollections.observableArrayList(pending));

        Button backBtn = new Button("← Kembali");
        applySecondaryButtonStyle(backBtn);
        backBtn.setOnAction(e -> showAdminDashboard(primaryStage, admin));

        VBox container = new VBox(20, backBtn, table);
        container.setPadding(new Insets(10));
        root.setCenter(container);

        Scene scene = new Scene(root, 1024, 720);
        primaryStage.setScene(scene);
    }
    
    public void updateDepositStatus(String id, String newStatus) {
        String sql = "UPDATE waste_deposits SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setString(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    private VBox createStatsCard(String title, String value, String icon, String color) {
        VBox card = new VBox(10);
        card.setPrefWidth(250);
        card.setPrefHeight(120);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);" +
            "-fx-padding: 20px;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(32));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web(color));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", 14));
        titleLabel.setTextFill(Color.web("#666666"));

        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);
        return card;
    }

    private void showWasteDepositForm(Stage primaryStage, User user) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background: linear-gradient(to bottom right, #E8F5E8, #C8E6C9);");

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");

        Button backBtn = new Button("← Kembali");
        applySecondaryButtonStyle(backBtn);
        backBtn.setOnAction(e -> showUserDashboard(primaryStage, user));

        Label titleLabel = new Label("Form Setor Sampah");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#2E7D32"));

        header.getChildren().addAll(backBtn, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, titleLabel, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }});

        // Form
        VBox formContainer = new VBox();
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(50));

        VBox formCard = new VBox(20);
        formCard.setMaxWidth(400);
        applyCardStyle(formCard);

        Label formTitle = new Label("📦 Informasi Setoran");
        formTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        formTitle.setTextFill(Color.web("#2E7D32"));

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Plastik", "Organik", "Kertas");
        categoryBox.setPromptText("Pilih jenis sampah");
        categoryBox.setMaxWidth(Double.MAX_VALUE);
        categoryBox.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-padding: 12px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-border-color: #E0E0E0;" +
            "-fx-border-width: 1px;" +
            "-fx-background-color: #FAFAFA;"
        );

        TextField weightField = new TextField();
        weightField.setPromptText("Masukkan berat sampah (kg)");
        applyTextFieldStyle(weightField);

        Label pointsLabel = new Label("Poin yang akan diperoleh: -");
        pointsLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        pointsLabel.setTextFill(Color.web("#2E7D32"));
        pointsLabel.setStyle("-fx-padding: 10px; -fx-background-color: #E8F5E8; -fx-background-radius: 8px;");

        Button submitBtn = new Button("💾 Simpan Setoran");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        applyPrimaryButtonStyle(submitBtn);

        // Update points preview
        Runnable updatePoints = () -> {
            String category = categoryBox.getValue();
            String weightText = weightField.getText();
            try {
                if (category != null && !weightText.isEmpty()) {
                    double weight = Double.parseDouble(weightText);
                    int points = calculatePoints(category, weight);
                    pointsLabel.setText("Poin yang akan diperoleh: " + points + " poin");
                } else {
                    pointsLabel.setText("Poin yang akan diperoleh: -");
                }
            } catch (NumberFormatException e) {
                pointsLabel.setText("Poin yang akan diperoleh: -");
            }
        };

        weightField.textProperty().addListener((obs, oldVal, newVal) -> updatePoints.run());
        categoryBox.setOnAction(e -> updatePoints.run());

        submitBtn.setOnAction(e -> {
            String category = categoryBox.getValue();
            String weightText = weightField.getText().trim();
            
            if (category == null) {
                showAlert("Silakan pilih jenis sampah!", Alert.AlertType.WARNING);
                return;
            }
            
            if (weightText.isEmpty()) {
                showAlert("Silakan masukkan berat sampah!", Alert.AlertType.WARNING);
                return;
            }

            try {
                double weight = Double.parseDouble(weightText);
                if (weight <= 0) {
                    showAlert("Berat sampah harus lebih dari 0!", Alert.AlertType.WARNING);
                    return;
                }
                
                int points = calculatePoints(category, weight);
                int categoryId = getCategoryId(category);
                String id = UUID.randomUUID().toString();

                WasteDeposit deposit = new WasteDeposit(
                    id, user.id, categoryId, weight, points, LocalDateTime.now(), "pending"
                );

                wasteDepositDAO.insertDeposit(deposit);

                // DEBUGGING
                System.out.println("DEBUG INSERT:");
                System.out.println("ID: " + id);
                System.out.println("User ID: " + user.id);
                System.out.println("Category ID: " + categoryId);
                System.out.println("Weight: " + weight);
                System.out.println("Points: " + points);

                showAlert("Setoran berhasil disimpan!\nMenunggu verifikasi admin.", Alert.AlertType.INFORMATION);
                showUserDashboard(primaryStage, user);
                
            } catch (NumberFormatException ex) {
                showAlert("Berat harus berupa angka yang valid!", Alert.AlertType.ERROR);
            }
        });

        formCard.getChildren().addAll(
            formTitle, categoryBox, weightField, pointsLabel, submitBtn
        );

        formContainer.getChildren().add(formCard);

        root.setTop(header);
        root.setCenter(formContainer);

        Scene scene = new Scene(root, 1024, 720);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bank Sampah - Setor Sampah");
    }

    private void showDepositHistory(Stage primaryStage, User user) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background: linear-gradient(to bottom right, #E8F5E8, #C8E6C9);");

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");

        Button backBtn = new Button("← Kembali");
        applySecondaryButtonStyle(backBtn);
        backBtn.setOnAction(e -> showUserDashboard(primaryStage, user));

        Label titleLabel = new Label("Riwayat Setoran");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#2E7D32"));

        header.getChildren().addAll(backBtn, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, titleLabel, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }});

        // Table
        VBox tableContainer = new VBox();
        tableContainer.setPadding(new Insets(30));

        TableView<DepositHistoryItem> table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 12px;");

        // Columns
        TableColumn<DepositHistoryItem, String> dateCol = new TableColumn<>("Tanggal");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(150);

        TableColumn<DepositHistoryItem, String> categoryCol = new TableColumn<>("Jenis Sampah");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);

        TableColumn<DepositHistoryItem, String> weightCol = new TableColumn<>("Berat (kg)");
        weightCol.setCellValueFactory(new PropertyValueFactory<>("weight"));
        weightCol.setPrefWidth(100);

        TableColumn<DepositHistoryItem, Integer> pointsCol = new TableColumn<>("Poin");
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));
        pointsCol.setPrefWidth(80);

        TableColumn<DepositHistoryItem, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);

        // Custom cell factory for status column to add colors
        statusCol.setCellFactory(column -> new TableCell<DepositHistoryItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("verified".equals(item)) {
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    } else if ("pending".equals(item)) {
                        setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                    }
                }
            }
        });

        table.getColumns().addAll(dateCol, categoryCol, weightCol, pointsCol, statusCol);

        // Load data
        ObservableList<DepositHistoryItem> data = FXCollections.observableArrayList();
        List<WasteDeposit> deposits = wasteDepositDAO.getDepositsByUser(user.id);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (WasteDeposit deposit : deposits) {
            String categoryName = getCategoryName(deposit.categoryId);
            String formattedDate = deposit.dateDeposited.format(formatter);
            String status = deposit.status.equals("verified") ? "Terverifikasi" : 
                           deposit.status.equals("pending") ? "Menunggu" : "Ditolak";
            
            data.add(new DepositHistoryItem(
                formattedDate,
                categoryName,
                String.format("%.1f", deposit.weightKg),
                deposit.pointsEarned,
                status
            ));
        }

        table.setItems(data);

        if (data.isEmpty()) {
            Label emptyLabel = new Label("Belum ada riwayat setoran");
            emptyLabel.setFont(Font.font("System", 16));
            emptyLabel.setTextFill(Color.web("#666666"));
            tableContainer.getChildren().add(emptyLabel);
            tableContainer.setAlignment(Pos.CENTER);
        } else {
            tableContainer.getChildren().add(table);
        }

        root.setTop(header);
        root.setCenter(tableContainer);

        Scene scene = new Scene(root, 1024, 720);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bank Sampah - Riwayat Setoran");
    }
    
    private void showRedeemRewardScreen(Stage primaryStage, User user) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #E8F5E8;"); // non-gradient for safety

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");

        Button backBtn = new Button("← Kembali");
        applySecondaryButtonStyle(backBtn);
        backBtn.setOnAction(e -> showUserDashboard(primaryStage, user));

        Label titleLabel = new Label("🎁 Tukar Poin dengan Reward");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#2E7D32"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(backBtn, spacer, titleLabel);

        // Content
        VBox content = new VBox(20);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.TOP_CENTER);

        List<Reward> rewards = rewardDAO.getAllRewards();
        if (rewards.isEmpty()) {
            Label noData = new Label("Belum ada reward yang tersedia");
            noData.setFont(Font.font("Arial", 16));
            noData.setTextFill(Color.GRAY);
            content.getChildren().add(noData);
        } else {
            for (Reward reward : rewards) {
                BorderPane itemPane = new BorderPane();
                itemPane.setPadding(new Insets(20));
                itemPane.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
                itemPane.setPrefWidth(800);

                // Info panel kiri
                VBox infoBox = new VBox(5);
                infoBox.setAlignment(Pos.CENTER_LEFT);

                Label nameLabel = new Label(reward.name);
                nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                nameLabel.setTextFill(Color.web("#2E7D32"));

                Label pointLabel = new Label("Biaya: " + reward.pointsCost + " poin");
                pointLabel.setFont(Font.font("Arial", 12));
                pointLabel.setTextFill(Color.web("#555555"));

                Label stockLabel = new Label("Stok: " + reward.stock);
                stockLabel.setFont(Font.font("Arial", 12));
                stockLabel.setTextFill(Color.web("#777777"));

                infoBox.getChildren().addAll(nameLabel, pointLabel, stockLabel);
                infoBox.setPadding(new Insets(0, 20, 0, 0));

                // Tombol kanan
                Button redeemBtn = new Button("Tukar");
                applyPrimaryButtonStyle(redeemBtn);

                redeemBtn.setOnAction(e -> {
                    int userPoints = userDAO.getTotalPointsForUser(user.id);
                    if (userPoints < reward.pointsCost) {
                        showAlert("Poin kamu tidak cukup!", Alert.AlertType.WARNING);
                        return;
                    }

                    // 1. Kurangi stok reward
                    rewardDAO.reduceStock(reward.id);

                    // 2. Masukkan ke transaksi
                    pointsTransactionDAO.insertTransaction(user.id, "redeem", reward.pointsCost, "Tukar: " + reward.name);

                    // ✅ 3. Kirim notifikasi ke Discord
                    String msg = String.format( "🎉 **Penukaran Reward Berhasil!**\n👤 User: %s\n🎁 Reward: %s\n📆 Waktu: %s", user.name, reward.name, java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
                    
                    sendDiscordNotification(msg);

                    // 4. Feedback ke user
                    showAlert("Berhasil menukar reward!", Alert.AlertType.INFORMATION);
                    showUserDashboard(primaryStage, user);
                });


                itemPane.setLeft(infoBox);
                itemPane.setRight(redeemBtn);

                content.getChildren().add(itemPane);
            }
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        root.setTop(header);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1024, 720);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tukar Poin");
    }


    private void showRedeemHistoryScreen(Stage primaryStage, User user) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background: linear-gradient(to bottom right, #E8F5E8, #C8E6C9);");

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");

        Button backBtn = new Button("← Kembali");
        applySecondaryButtonStyle(backBtn);
        backBtn.setOnAction(e -> showUserDashboard(primaryStage, user));

        Label titleLabel = new Label("Riwayat Penukaran Reward");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#2E7D32"));

        header.getChildren().addAll(backBtn, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, titleLabel, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }});

        VBox content = new VBox(20);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.CENTER);

        List<PointsTransaction> transactions = pointsTransactionDAO.getRedeemTransactionsForUser(user.id);
        if (transactions.isEmpty()) {
            Label empty = new Label("Belum ada riwayat penukaran.");
            empty.setFont(Font.font("System", 16));
            empty.setTextFill(Color.GRAY);
            content.getChildren().add(empty);
        } else {
            for (PointsTransaction tx : transactions) {
                HBox item = new HBox(20);
                item.setPadding(new Insets(15));
                item.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);");

                VBox info = new VBox(5);
                Label desc = new Label(tx.description);
                desc.setFont(Font.font("System", FontWeight.BOLD, 14));
                Label date = new Label("Tanggal: " + tx.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                date.setTextFill(Color.GRAY);
                Label amount = new Label("-" + tx.amount + " poin");
                amount.setTextFill(Color.web("#FF5722"));

                info.getChildren().addAll(desc, date, amount);

                item.getChildren().add(info);
                content.getChildren().add(item);
            }
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        root.setTop(header);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1024, 720);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Riwayat Reward");
    }
    
    private void showAdminRewardManagementScreen(Stage primaryStage, User admin) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F0F4F0;");

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");

        Button backBtn = new Button("← Kembali");
        applySecondaryButtonStyle(backBtn);
        backBtn.setOnAction(e -> showAdminDashboard(primaryStage, admin));

        Label titleLabel = new Label("🎁 Kelola Reward");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#2E7D32"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(backBtn, spacer, titleLabel);

        // Table
        TableView<Reward> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Reward, String> nameCol = new TableColumn<>("Nama");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name));

        TableColumn<Reward, Integer> pointCol = new TableColumn<>("Poin");
        pointCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().pointsCost).asObject());

        TableColumn<Reward, Integer> stockCol = new TableColumn<>("Stok");
        stockCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().stock).asObject());

        table.getColumns().addAll(nameCol, pointCol, stockCol);

        refreshRewardTable(table);

        // Tombol Aksi
        Button addBtn = new Button("➕ Tambah");
        Button editBtn = new Button("✏️ Edit");
        Button deleteBtn = new Button("❌ Hapus");

        applyPrimaryButtonStyle(addBtn);
        applySecondaryButtonStyle(editBtn);
        applySecondaryButtonStyle(deleteBtn);

        HBox buttonBar = new HBox(10, addBtn, editBtn, deleteBtn);
        buttonBar.setPadding(new Insets(20));
        buttonBar.setAlignment(Pos.CENTER);

        // Event Tambah
        addBtn.setOnAction(e -> showRewardFormDialog(null, table));

        // Event Edit
        editBtn.setOnAction(e -> {
            Reward selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showRewardFormDialog(selected, table);
            } else {
                showAlert("Pilih reward yang ingin diedit!", Alert.AlertType.WARNING);
            }
        });

        // Event Hapus
        deleteBtn.setOnAction(e -> {
            Reward selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                rewardDAO.deleteReward(selected.id);
                refreshRewardTable(table);
                showAlert("Reward berhasil dihapus.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Pilih reward yang ingin dihapus!", Alert.AlertType.WARNING);
            }
        });

        VBox centerBox = new VBox(10, table, buttonBar);
        centerBox.setPadding(new Insets(30));

        root.setTop(header);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin - Kelola Reward");
    }

    private void showRewardFormDialog(Reward reward, TableView<Reward> table) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(reward == null ? "Tambah Reward" : "Edit Reward");

        // Form input
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextField pointField = new TextField();
        TextField stockField = new TextField();

        if (reward != null) {
            nameField.setText(reward.name);
            pointField.setText(String.valueOf(reward.pointsCost));
            stockField.setText(String.valueOf(reward.stock));
        }

        grid.add(new Label("Nama Reward:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Poin Dibutuhkan:"), 0, 1);
        grid.add(pointField, 1, 1);

        grid.add(new Label("Stok:"), 0, 2);
        grid.add(stockField, 1, 2);

        ButtonType saveBtnType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                try {
                    String name = nameField.getText().trim();
                    int points = Integer.parseInt(pointField.getText().trim());
                    int stock = Integer.parseInt(stockField.getText().trim());

                    if (reward == null) {
                        rewardDAO.insertReward(name, points, stock);
                        showAlert("Reward berhasil ditambahkan.", Alert.AlertType.INFORMATION);
                    } else {
                        rewardDAO.updateReward(reward.id, name, points, stock);
                        showAlert("Reward berhasil diperbarui.", Alert.AlertType.INFORMATION);
                    }
                    refreshRewardTable(table);
                } catch (Exception ex) {
                    showAlert("Input tidak valid!", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
    
    private void showAllRedeemTransactionsScreen(Stage primaryStage, User admin) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F8F8F8;");

        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.TOP_CENTER);

        // 🔽 Dropdown User
        ComboBox<User> userFilterBox = new ComboBox<>();
        userFilterBox.setPrefWidth(300);
        userFilterBox.setPromptText("🔍 Filter berdasarkan pengguna");
        
        userFilterBox.setStyle("-fx-font-size: 14px; -fx-background-color: #FAFAFA; -fx-border-color: #C8E6C9; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 10px;");
        
        userFilterBox.setOnMouseEntered(e -> {
            userFilterBox.setStyle(userFilterBox.getStyle() + "-fx-border-color: #81C784;");
        });

        userFilterBox.setOnMouseExited(e -> {
            userFilterBox.setStyle(userFilterBox.getStyle().replace("-fx-border-color: #81C784;", "-fx-border-color: #C8E6C9;"));
        });

        
        List<User> users = userDAO.getAllUsers();
        userFilterBox.getItems().add(null); // "Semua User"
        userFilterBox.getItems().addAll(users);

        userFilterBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Semua Pengguna");
                } else {
                    setText("👤 " + item.name);
                }
            }
        });

        userFilterBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("🔍 Semua Pengguna");
                } else {
                    setText("👤 " + item.name);
                }
            }
        });


        VBox transactionsBox = new VBox(15);
        transactionsBox.setAlignment(Pos.CENTER);

        // Fungsi: tampilkan transaksi
        Runnable refreshTransactionList = () -> {
            transactionsBox.getChildren().clear();
            List<PointsTransaction> transactions;

            User selectedUser = userFilterBox.getValue();
            if (selectedUser == null) {
                transactions = pointsTransactionDAO.getAllRedeemTransactionsWithUserName();
            } else {
                transactions = pointsTransactionDAO.getRedeemTransactionsForUser(selectedUser.id);
                for (PointsTransaction tx : transactions) {
                    tx.userName = selectedUser.name;
                }
            }

            if (transactions.isEmpty()) {
                Label empty = new Label("Tidak ada transaksi penukaran.");
                empty.setFont(Font.font("Arial", 14));
                empty.setTextFill(Color.GRAY);
                transactionsBox.getChildren().add(empty);
            } else {
                for (PointsTransaction tx : transactions) {
                    VBox card = new VBox(8);
                    card.setPadding(new Insets(15));
                    card.setPrefWidth(650);
                    card.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 2);");

                    Label user = new Label("👤 " + tx.userName);
                    user.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    user.setTextFill(Color.web("#1B5E20"));

                    Label desc = new Label("🔄 " + tx.description);
                    Label amount = new Label("Poin digunakan: " + tx.amount);
                    Label time = new Label("Tanggal: " + tx.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));

                    card.getChildren().addAll(user, desc, amount, time);
                    transactionsBox.getChildren().add(card);
                }
            }
        };

        userFilterBox.setOnAction(e -> refreshTransactionList.run());
        refreshTransactionList.run(); // load awal

        ScrollPane scroll = new ScrollPane(transactionsBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        Button backBtn = new Button("← Kembali");
        applySecondaryButtonStyle(backBtn);
        backBtn.setOnAction(e -> showAdminDashboard(primaryStage, admin));

        Label title = new Label("📜 Riwayat Penukaran Poin");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#2E7D32"));

        HBox header = new HBox(20, backBtn, title);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 20, 10, 20));

        container.getChildren().addAll(header, userFilterBox, scroll);

        Scene scene = new Scene(container, 1024, 720);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Filter Riwayat Penukaran");
    }

    private void showPopularRewardStats(Stage primaryStage, User admin) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F8F8F8;");

        VBox container = new VBox(30);
        container.setPadding(new Insets(40));
        container.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("📊 Statistik Reward Paling Populer");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2E7D32"));

        Button backBtn = new Button("← Kembali");
        applySecondaryButtonStyle(backBtn);
        backBtn.setOnAction(e -> showAdminDashboard(primaryStage, admin));

        List<PopularRewardStat> stats = rewardDAO.getPopularRewardsStats();

        if (stats.isEmpty()) {
            Label empty = new Label("Belum ada data penukaran reward.");
            empty.setFont(Font.font("Arial", 14));
            empty.setTextFill(Color.GRAY);
            container.getChildren().addAll(backBtn, title, empty);
        } else {
            // ---------- BAR CHART ----------
            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Reward");

            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Jumlah Penukaran");

            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Visualisasi Penukaran Reward");
            barChart.setLegendVisible(false);
            barChart.setCategoryGap(30);
            barChart.setBarGap(10);
            barChart.setPrefHeight(400);
            barChart.setPrefWidth(800);
            barChart.setStyle("-fx-background-color: transparent; -fx-padding: 20px; -fx-font-size: 13px;");

            XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();
            for (PopularRewardStat stat : stats) {
                XYChart.Data<String, Number> bar = new XYChart.Data<>(stat.rewardName, stat.totalRedeemed);
                dataSeries.getData().add(bar);
            }

            barChart.getData().add(dataSeries);

            // ---------- TABEL ----------
            VBox statsBox = new VBox(10);
            statsBox.setAlignment(Pos.CENTER);
            statsBox.setPrefWidth(800);

            for (PopularRewardStat stat : stats) {
                HBox row = new HBox(20);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(12));
                row.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 2, 0, 0, 1);");

                Label name = new Label(stat.rewardName);
                name.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                name.setPrefWidth(400);

                Label count = new Label(stat.totalRedeemed + " kali ditukar");
                count.setTextFill(Color.web("#555555"));

                row.getChildren().addAll(name, count);
                statsBox.getChildren().add(row);
            }

            ScrollPane scrollPane = new ScrollPane(statsBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent;");
            
            // Button: Export
            Button exportBtn = new Button("⬇ Ekspor ke CSV");
            applySecondaryButtonStyle(exportBtn);
            exportBtn.setOnAction(e -> exportRewardStatsToCSV(primaryStage));

            container.getChildren().addAll(backBtn, title, exportBtn, barChart, scrollPane);
        }

        root.setCenter(container);
        Scene scene = new Scene(root, 1024, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Statistik Reward Populer");
    }

    private void exportRewardStatsToCSV(Stage stage) {
        List<PopularRewardStat> stats = rewardDAO.getPopularRewardsStats();

        if (stats.isEmpty()) {
            showAlert("Tidak ada data statistik yang bisa diekspor.", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Statistik sebagai CSV");
        fileChooser.setInitialFileName("statistik_reward.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Reward,Jumlah Penukaran");
                for (PopularRewardStat stat : stats) {
                    writer.printf("\"%s\",%d%n", stat.rewardName, stat.totalRedeemed);
                }
                showAlert("Statistik berhasil diekspor ke:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Gagal menulis ke file CSV.", Alert.AlertType.ERROR);
            }
        }
    }



    public static class PopularRewardStat {
        public String rewardName;
        public int totalRedeemed;

        public PopularRewardStat(String rewardName, int totalRedeemed) {
            this.rewardName = rewardName;
            this.totalRedeemed = totalRedeemed;
        }
    }



    private void refreshRewardTable(TableView<Reward> table) {
        List<Reward> list = rewardDAO.getAllRewards();
        table.setItems(FXCollections.observableArrayList(list));
    }

    
    private int getCategoryId(String categoryName) {
        switch (categoryName.toLowerCase()) {
            case "plastik": return 1;
            case "organik": return 2;
            case "kertas":  return 3;
            default: return 0;
        }
    }

    private String getCategoryName(int categoryId) {
        switch (categoryId) {
            case 1: return "Plastik";
            case 2: return "Organik";
            case 3: return "Kertas";
            default: return "Unknown";
        }
    }
    
    private int calculatePoints(String category, double weightKg) {
        switch (category.toLowerCase()) {
            case "plastik": return (int) (weightKg * 10);
            case "organik": return (int) (weightKg * 5);
            case "kertas":  return (int) (weightKg * 8);
            default: return 0;
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Bank Sampah");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-font-size: 14px;");
        
        alert.showAndWait();
    }

    @Override
    public void stop() throws Exception {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
        super.stop();
    }
    
    public static void main(String[] args) {
        launch();
    }
}