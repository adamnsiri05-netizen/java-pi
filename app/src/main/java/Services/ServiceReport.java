package Services;

import Models.Report;
import Repository.ReportRepository;

import java.time.LocalDate;
import java.util.List;

public class ServiceReport {
    private final ReportRepository reportRepository;

    public ServiceReport() {
        this.reportRepository = new ReportRepository();
    }

    public void createReport(Report report) {
        if (report.getReason() == null || report.getReason().trim().isEmpty()) return;
        if (report.getDateReported() == null) report.setDateReported(LocalDate.now());
        reportRepository.save(report);
    }

    public List<Report> getReportsByReporter(int reporterId) {
        return reportRepository.findByReporterId(reporterId);
    }

    public Report getReportById(int id) { return reportRepository.findById(id); }

    public void updateReport(Report report) { reportRepository.update(report); }

    public void deleteReport(int id) { reportRepository.delete(id); }
}
