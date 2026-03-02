package Controller;

import Models.User;
import Models.Message;
import Models.Conversation;
import Services.ServiceUser;
import Services.ServiceMessage;
import Services.ServiceConversation;
import Services.ServiceReport;
import Services.ServiceUser;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConversationsController {

    @FXML private ListView<String> conversationListView;
    @FXML private ListView<Message> messageListView;
    @FXML private TextField inputField;
    @FXML private Label conversationTitleLabel;
    @FXML private Button deleteConvButton;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceMessage serviceMessage = new ServiceMessage();
    private final ServiceConversation serviceConversation = new ServiceConversation();
    private final ServiceReport serviceReport = new ServiceReport();

    private User currentUser;
    private User selectedUser;
    private int currentConversationId = -1;
    private List<User> allUsers;
    private List<User> usersWithConversations;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        initialize();
    }

    @FXML
    public void initialize() {
        if (currentUser == null) return;

        loadConversations();
        setupMessageDisplay();

        conversationListView.setOnMouseClicked(e -> onConversationSelected());
    }

    private void loadConversations() {
        conversationListView.getItems().clear();
        
        allUsers = serviceUser.getAllUsers();
        usersWithConversations = new ArrayList<>();

        if (allUsers != null) {
            for (User u : allUsers) {
                if (u.getIdUser() != currentUser.getIdUser()) {
                    // Check if there's any conversation between currentUser and u
                    List<Conversation> conversations = serviceConversation.getConversationsBetweenUsers(
                            currentUser.getIdUser(), u.getIdUser());
                    if (!conversations.isEmpty()) {
                        usersWithConversations.add(u);
                    }
                }
            }
        }

        var conversationNames = FXCollections.<String>observableArrayList();
        for (User u : usersWithConversations) {
            conversationNames.add(u.getNomUser());
        }
        conversationListView.setItems(conversationNames);
    }

    private void onConversationSelected() {
        int selectedIdx = conversationListView.getSelectionModel().getSelectedIndex();
        if (selectedIdx < 0) {
            messageListView.setItems(FXCollections.observableArrayList());
            conversationTitleLabel.setText("Select a conversation");
            deleteConvButton.setVisible(false);
            return;
        }

        selectedUser = usersWithConversations.get(selectedIdx);
        
        // Get the first conversation between these two users
        List<Conversation> conversations = serviceConversation.getConversationsBetweenUsers(
                currentUser.getIdUser(), selectedUser.getIdUser());
        
        if (!conversations.isEmpty()) {
            currentConversationId = conversations.get(0).getIdConversation();
            conversationTitleLabel.setText("Chat with " + selectedUser.getNomUser());
            deleteConvButton.setVisible(true);
            loadMessages();
        }
    }

    private void loadMessages() {
        if (currentConversationId <= 0) {
            messageListView.setItems(FXCollections.observableArrayList());
            return;
        }

        List<Message> messages = serviceMessage.getMessagesByConversation(currentConversationId);
        messageListView.setItems(FXCollections.observableArrayList(messages));
        messageListView.scrollTo(messages.size() - 1);
    }

    private void setupMessageDisplay() {
        messageListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Message item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label messageText = new Label(item.getContenuMessage());
                    messageText.setWrapText(true);
                    messageText.getStyleClass().add("message-text");

                    Label dateLabel = new Label(item.getDateMessage().toString());
                    dateLabel.setStyle("-fx-font-size: 0.8em; -fx-text-fill: #888888;");

                    Button optBtn = new Button("+");
                    optBtn.getStyleClass().add("plus-button");
                    setupMessageContextMenu(optBtn, item);

                    VBox textContainer = new VBox(2, messageText, dateLabel);
                    HBox bubble = new HBox(10, textContainer, optBtn);
                    bubble.getStyleClass().add("bubble");
                    bubble.setMaxWidth(350);

                    HBox alignmentWrapper = new HBox(bubble);
                    alignmentWrapper.setFillHeight(true);

                    if (item.getIdExpediteur() == currentUser.getIdUser()) {
                        alignmentWrapper.setAlignment(Pos.CENTER_RIGHT);
                        bubble.getStyleClass().add("bubble-me");
                    } else {
                        alignmentWrapper.setAlignment(Pos.CENTER_LEFT);
                        bubble.getStyleClass().add("bubble-other");
                    }

                    setGraphic(alignmentWrapper);
                }
            }
        });
    }

    private void setupMessageContextMenu(Button button, Message item) {
        button.setOnAction(e -> {
            if (item.getIdExpediteur() != currentUser.getIdUser()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Permission Denied");
                alert.setHeaderText(null);
                alert.setContentText("You can only modify your own messages.");
                alert.showAndWait();
                return;
            }

            ContextMenu cm = new ContextMenu();
            MenuItem edit = new MenuItem("Modifier");
            MenuItem del = new MenuItem("Supprimer");

            edit.setOnAction(ev -> {
                TextInputDialog dialog = new TextInputDialog(item.getContenuMessage());
                dialog.setTitle("Modifier le message");
                dialog.setHeaderText(null);
                dialog.setContentText("Nouveau message:");
                dialog.showAndWait().ifPresent(newText -> {
                    if (!newText.trim().isEmpty()) {
                        item.setContenuMessage(newText);
                        serviceMessage.updateMessage(item);
                        loadMessages();
                    }
                });
            });

            del.setOnAction(ev -> {
                Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce message ?", ButtonType.YES, ButtonType.NO);
                a.showAndWait().ifPresent(resp -> {
                    if (resp == ButtonType.YES) {
                        serviceMessage.deleteMessage(item.getIdMessage());
                        loadMessages();
                    }
                });
            });

            cm.getItems().addAll(edit, del);
            cm.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
        });
    }

    @FXML
    public void onSendClicked() {
        String text = inputField.getText();
        if (text == null || text.trim().isEmpty() || selectedUser == null || currentConversationId <= 0) {
            return;
        }

        Message m = new Message(text, LocalDate.now(), false, currentUser.getIdUser(), selectedUser.getIdUser(), currentConversationId);
        serviceMessage.createMessage(m);

        inputField.clear();
        loadMessages();
    }

    @FXML
    public void onNewConversationClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org.example/new_conversation_view.fxml"));
            Parent root = loader.load();

            NewConversationController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setParentController(this);

            Stage dialog = new Stage();
            dialog.setTitle("New Conversation");
            Scene scene = new Scene(root, 400, 400);
            String css = getClass().getResource("/org.example/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
            dialog.setScene(scene);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onReportUserClicked() {
        if (selectedUser == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org.example/report_dialog.fxml"));
            Parent root = loader.load();
            ReportDialogController controller = loader.getController();
            controller.setReporterAndReported(currentUser, selectedUser);

            Stage dialog = new Stage();
            dialog.initOwner(conversationListView.getScene().getWindow());
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML
    public void onMyReportsClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org.example/reports_view.fxml"));
            Parent root = loader.load();
            ReportsController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);

            Stage dialog = new Stage();
            dialog.initOwner(conversationListView.getScene().getWindow());
            dialog.setScene(new Scene(root, 600, 400));
            dialog.showAndWait();
            // refresh in case reports changed
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML
    public void onDeleteConversationClicked() {
        if (currentConversationId <= 0) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                "Delete this entire conversation? All messages will be deleted.", 
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                serviceConversation.deleteConversation(currentConversationId);
                currentConversationId = -1;
                loadConversations();
                messageListView.setItems(FXCollections.observableArrayList());
                conversationTitleLabel.setText("Select a conversation");
                deleteConvButton.setVisible(false);
            }
        });
    }

    @FXML
    public void onLogoutClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org.example/user_selection_view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            String css = getClass().getResource("/org.example/styles.css").toExternalForm();
            scene.getStylesheets().add(css);

            Stage stage = (Stage) conversationListView.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshConversations() {
        loadConversations();
    }
}
