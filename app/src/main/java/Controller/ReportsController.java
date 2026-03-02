package Controller;

import Models.Report;
import Models.User;
import Services.ServiceReport;
import Services.ServiceUser;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ReportsController {

    @FXML private ListView<Report> reportsListView;

    private final ServiceReport serviceReport = new ServiceReport();
    private final ServiceUser serviceUser = new ServiceUser();
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadReports();
    }

    private void loadReports() {
        List<Report> reports = serviceReport.getReportsByReporter(currentUser.getIdUser());
        reportsListView.setItems(FXCollections.observableArrayList(reports));
        reportsListView.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Report item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    var reported = serviceUser.getUserById(item.getReportedUserId());
                    String rName = reported != null ? reported.getNomUser() : "(deleted)";
                    setText("To: " + rName + " — " + item.getReason() + " (" + item.getDateReported() + ")");
                }
            }
        });
    }

    @FXML
    public void onEditClicked() {
        Report sel = reportsListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        openReportDialog(sel);
        loadReports();
    }

    @FXML
    public void onDeleteClicked() {
        Report sel = reportsListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Delete this report?", ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                serviceReport.deleteReport(sel.getIdReport());
                loadReports();
            }
        });
    }

    @FXML
    public void onCloseClicked() {
        Stage st = (Stage) reportsListView.getScene().getWindow();
        st.close();
    }

    private void openReportDialog(Report existing) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org.example/report_dialog.fxml"));
            Parent root = loader.load();
            ReportDialogController ctrl = loader.getController();
            ctrl.setReporterAndReported(currentUser, serviceUser.getUserById(existing.getReportedUserId()));
            ctrl.setEditingReport(existing);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
