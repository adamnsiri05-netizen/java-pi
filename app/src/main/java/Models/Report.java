package Models;

import java.time.LocalDate;

public class Report {
    private int idReport;
    private int reporterId;
    private int reportedUserId;
    private String reason;
    private LocalDate dateReported;

    public Report() {}

    public Report(int reporterId, int reportedUserId, String reason, LocalDate dateReported) {
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.reason = reason;
        this.dateReported = dateReported;
    }

    public int getIdReport() { return idReport; }
    public void setIdReport(int idReport) { this.idReport = idReport; }

    public int getReporterId() { return reporterId; }
    public void setReporterId(int reporterId) { this.reporterId = reporterId; }

    public int getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(int reportedUserId) { this.reportedUserId = reportedUserId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDate getDateReported() { return dateReported; }
    public void setDateReported(LocalDate dateReported) { this.dateReported = dateReported; }
}
