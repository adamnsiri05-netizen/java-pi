package Controller;

import Models.User;
import Services.ServiceUser;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class UserSelectionController {

    @FXML private ListView<String> userListView;

    private final ServiceUser serviceUser = new ServiceUser();
    private List<User> users;

    @FXML
    public void initialize() {
        loadUsers();
    }

    private void loadUsers() {
        users = serviceUser.getAllUsers();
        if (users != null) {
            var userNames = FXCollections.<String>observableArrayList();
            for (User u : users) {
                userNames.add(u.getNomUser());
            }
            userListView.setItems(userNames);
        }
    }

    @FXML
    public void onSelectClicked() {
        int selectedIdx = userListView.getSelectionModel().getSelectedIndex();
        if (selectedIdx < 0) {
            return;
        }

        User selectedUser = users.get(selectedIdx);
        
        try {
            // Load the conversations view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org.example/conversations_view.fxml"));
            Parent root = loader.load();
            
            ConversationsController controller = loader.getController();
            controller.setCurrentUser(selectedUser);
            
            Scene scene = new Scene(root);
            String css = getClass().getResource("/org.example/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            Stage stage = (Stage) userListView.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
