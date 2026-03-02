package Repository;

import Models.Report;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportRepository {
    private final Connection connection;

    public ReportRepository() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    public void save(Report report) {
        String sql = "INSERT INTO report (reporter_id, reported_user_id, reason, date_reported) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, report.getReporterId());
            ps.setInt(2, report.getReportedUserId());
            ps.setString(3, report.getReason());
            ps.setDate(4, Date.valueOf(report.getDateReported()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) report.setIdReport(rs.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public Report findById(int id) {
        String sql = "SELECT * FROM report WHERE id_report = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Report> findByReporterId(int reporterId) {
        String sql = "SELECT * FROM report WHERE reporter_id = ? ORDER BY date_reported DESC";
        List<Report> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reporterId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void update(Report report) {
        String sql = "UPDATE report SET reason=?, date_reported=? WHERE id_report=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, report.getReason());
            ps.setDate(2, Date.valueOf(report.getDateReported()));
            ps.setInt(3, report.getIdReport());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        String sql = "DELETE FROM report WHERE id_report = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Report map(ResultSet rs) throws SQLException {
        Report r = new Report();
        r.setIdReport(rs.getInt("id_report"));
        r.setReporterId(rs.getInt("reporter_id"));
        r.setReportedUserId(rs.getInt("reported_user_id"));
        r.setReason(rs.getString("reason"));
        r.setDateReported(rs.getDate("date_reported").toLocalDate());
        return r;
    }
}
