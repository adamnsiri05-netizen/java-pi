package utils;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseLoader {

    public static void initializeDatabase() {
        Connection connection = MyDatabase.getInstance().getConnection();
        try (Statement statement = connection.createStatement()) {

            // Create user table
            String createUserTable = "CREATE TABLE IF NOT EXISTS user (" +
                    "id_user INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nom_user VARCHAR(100) NOT NULL, " +
                    "sexe_user VARCHAR(10), " +
                    "dn_user DATE, " +
                    "adresse_user VARCHAR(255), " +
                    "mail_user VARCHAR(100) UNIQUE, " +
                    "tel_user VARCHAR(20)" +
                    ")";
            statement.executeUpdate(createUserTable);
            System.out.println("✓ User table created/verified");

            // Create conversation table
            String createConversationTable = "CREATE TABLE IF NOT EXISTS conversation (" +
                    "id_conversation INT AUTO_INCREMENT PRIMARY KEY, " +
                    "date_creation DATE NOT NULL, " +
                    "statut_conversation VARCHAR(50), " +
                    "archiver_conversation BOOLEAN DEFAULT FALSE" +
                    ")";
            statement.executeUpdate(createConversationTable);
            System.out.println("✓ Conversation table created/verified");

                // Create reports table
                String createReportsTable = "CREATE TABLE IF NOT EXISTS report (" +
                    "id_report INT AUTO_INCREMENT PRIMARY KEY, " +
                    "reporter_id INT NOT NULL, " +
                    "reported_user_id INT NOT NULL, " +
                    "reason TEXT NOT NULL, " +
                    "date_reported DATE NOT NULL, " +
                    "FOREIGN KEY (reporter_id) REFERENCES user(id_user), " +
                    "FOREIGN KEY (reported_user_id) REFERENCES user(id_user)" +
                    ")";
                statement.executeUpdate(createReportsTable);
                System.out.println("✓ Reports table created/verified");

            // Create conversation_participants table to track who is in each conversation
            String createParticipantsTable = "CREATE TABLE IF NOT EXISTS conversation_participants (" +
                    "id_participant INT AUTO_INCREMENT PRIMARY KEY, " +
                    "id_conversation INT NOT NULL, " +
                    "id_user INT NOT NULL, " +
                    "FOREIGN KEY (id_conversation) REFERENCES conversation(id_conversation), " +
                    "FOREIGN KEY (id_user) REFERENCES user(id_user), " +
                    "UNIQUE KEY (id_conversation, id_user)" +
                    ")";
            statement.executeUpdate(createParticipantsTable);
            System.out.println("✓ Conversation Participants table created/verified");

            // Create message table
            String createMessageTable = "CREATE TABLE IF NOT EXISTS message (" +
                    "id_message INT AUTO_INCREMENT PRIMARY KEY, " +
                    "contenu_message TEXT NOT NULL, " +
                    "date_message DATE NOT NULL, " +
                    "est_lu BOOLEAN DEFAULT FALSE, " +
                    "id_expediteur INT NOT NULL, " +
                    "id_destinataire INT NOT NULL, " +
                    "id_conversation INT NOT NULL, " +
                    "FOREIGN KEY (id_expediteur) REFERENCES user(id_user), " +
                    "FOREIGN KEY (id_destinataire) REFERENCES user(id_user), " +
                    "FOREIGN KEY (id_conversation) REFERENCES conversation(id_conversation)" +
                    ")";
            statement.executeUpdate(createMessageTable);
            System.out.println("✓ Message table created/verified");

            System.out.println("Database schema initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void loadSampleData() {
        Connection connection = MyDatabase.getInstance().getConnection();
        try (Statement statement = connection.createStatement()) {

            // Check if sample data already exists
            String checkUsers = "SELECT COUNT(*) as count FROM user";
            var resultSet = statement.executeQuery(checkUsers);
            resultSet.next();
            int userCount = resultSet.getInt("count");

            if (userCount > 0) {
                System.out.println("Sample data already exists");
                return;
            }

            // Insert sample users
            String insertUsers = "INSERT INTO user (nom_user, sexe_user, dn_user, adresse_user, mail_user, tel_user) " +
                    "VALUES " +
                    "('Ahmed Ahmed', 'M', '1990-05-15', '123 Rue de Paris', 'ahmed@email.com', '0123456789'), " +
                    "('Fatima Ben', 'F', '1992-08-22', '456 Avenue Lyon', 'fatima@email.com', '0987654321'), " +
                    "('Mohamed Ali', 'M', '1988-03-10', '789 Boulevard Marseille', 'ali@email.com', '0555555555'), " +
                    "('Leila Saïd', 'F', '1995-11-30', '321 Chemin Toulouse', 'leila@email.com', '0666666666')";
            statement.executeUpdate(insertUsers);
            System.out.println("✓ Sample users loaded");

            // Insert sample conversations
            String insertConversations = "INSERT INTO conversation (date_creation, statut_conversation, archiver_conversation) " +
                    "VALUES " +
                    "(CURDATE(), 'active', FALSE), " +
                    "(CURDATE(), 'active', FALSE), " +
                    "(CURDATE(), 'archived', TRUE)";
            statement.executeUpdate(insertConversations);
            System.out.println("✓ Sample conversations loaded");

            // Insert sample messages
            String insertMessages = "INSERT INTO message (contenu_message, date_message, est_lu, id_expediteur, id_destinataire, id_conversation) " +
                    "VALUES " +
                    "('Bonjour, comment vas-tu?', CURDATE(), TRUE, 1, 2, 1), " +
                    "('Très bien, merci!', CURDATE(), TRUE, 2, 1, 1), " +
                    "('Au revoir!', CURDATE(), FALSE, 1, 2, 1), " +
                    "('Salut, cela te dit une réunion?', CURDATE(), TRUE, 3, 4, 2), " +
                    "('Oui, quand?', CURDATE(), TRUE, 4, 3, 2)";
            statement.executeUpdate(insertMessages);
            System.out.println("✓ Sample messages loaded");

            System.out.println("Sample data loaded successfully");
        } catch (SQLException e) {
            System.err.println("Error loading sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
