package Controller;

import Models.Report;
import Models.User;
import Services.ServiceReport;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.time.LocalDate;

public class ReportDialogController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextArea reasonArea;

    private final ServiceReport serviceReport = new ServiceReport();
    private User reporter;
    private User reported;
    private Report editing;

    public void setReporterAndReported(User reporter, User reported) {
        this.reporter = reporter;
        this.reported = reported;
        titleLabel.setText("Report " + reported.getNomUser());
        subtitleLabel.setText("Reporting as: " + reporter.getNomUser());
    }

    public void setEditingReport(Report r) {
        this.editing = r;
        if (r != null) {
            reasonArea.setText(r.getReason());
            titleLabel.setText("Edit Report");
        }
    }

    @FXML
    public void onSubmitClicked() {
        String reason = reasonArea.getText();
        if (editing == null) {
            Report r = new Report(reporter.getIdUser(), reported.getIdUser(), reason, LocalDate.now());
            serviceReport.createReport(r);
        } else {
            editing.setReason(reason);
            editing.setDateReported(LocalDate.now());
            serviceReport.updateReport(editing);
        }
        close();
    }

    @FXML
    public void onCancelClicked() { close(); }

    private void close() {
        Stage st = (Stage) reasonArea.getScene().getWindow();
        st.close();
    }
}
