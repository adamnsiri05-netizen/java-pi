package Repository;
import Models.Conversation;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConversationRepository {
    private final Connection connection;

    public ConversationRepository() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    public void save(Conversation conversation) {
        String sql = "INSERT INTO conversation (date_creation, statut_conversation, archiver_conversation) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(conversation.getDateCreation()));
            ps.setString(2, conversation.getStatutConversation());
            ps.setBoolean(3, conversation.isArchiverConversation());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                conversation.setIdConversation(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Conversation findById(int id) {
        String sql = "SELECT * FROM conversation WHERE id_conversation = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToConversation(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Conversation> findAll() {
        String sql = "SELECT * FROM conversation";
        List<Conversation> conversations = new ArrayList<>();
        try (Statement st = connection.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                conversations.add(mapResultSetToConversation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }

    public void update(Conversation conversation) {
        String sql = "UPDATE conversation SET date_creation=?, statut_conversation=?, archiver_conversation=? WHERE id_conversation=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(conversation.getDateCreation()));
            ps.setString(2, conversation.getStatutConversation());
            ps.setBoolean(3, conversation.isArchiverConversation());
            ps.setInt(4, conversation.getIdConversation());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM conversation WHERE id_conversation = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Conversation> findConversationsByUserPair(int userId1, int userId2) {
        String sql = "SELECT DISTINCT c.* FROM conversation c " +
                "INNER JOIN conversation_participants cp1 ON c.id_conversation = cp1.id_conversation " +
                "INNER JOIN conversation_participants cp2 ON c.id_conversation = cp2.id_conversation " +
                "WHERE (cp1.id_user = ? AND cp2.id_user = ?) " +
                "OR (cp1.id_user = ? AND cp2.id_user = ?)";
        List<Conversation> conversations = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId1);
            ps.setInt(2, userId2);
            ps.setInt(3, userId2);
            ps.setInt(4, userId1);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                conversations.add(mapResultSetToConversation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }

    public void addParticipant(int conversationId, int userId) {
        String sql = "INSERT IGNORE INTO conversation_participants (id_conversation, id_user) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteParticipants(int conversationId) {
        String sql = "DELETE FROM conversation_participants WHERE id_conversation = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Conversation mapResultSetToConversation(ResultSet rs) throws SQLException {
        Conversation conv = new Conversation();
        conv.setIdConversation(rs.getInt("id_conversation"));
        conv.setDateCreation(rs.getDate("date_creation").toLocalDate());
        conv.setStatutConversation(rs.getString("statut_conversation"));
        conv.setArchiverConversation(rs.getBoolean("archiver_conversation"));
        return conv;
    }
}