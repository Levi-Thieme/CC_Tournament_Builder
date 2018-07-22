package fx.controller;

import builder.Model;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tournament.components.Tournament;

public class TableController implements Initializable {
	
	@FXML private VBox binSectional = null;
	
	@FXML private VBox binRegional = null;
	
	@FXML private VBox binSemi = null;
	
	@FXML private VBox binState = null;
		
	@FXML private MenuButton levelList;
	
	@FXML private MenuButton hostList;
	
	private Model model;

	public TableController() {
		this.model = Model.getInstance();
	}

	@Override public void initialize(URL arg0, ResourceBundle arg1) {
		model.loadInitialTournament();
	}
	
	/*
	 * Switch from TableView to MapView
	 */
	public void switchView(ActionEvent event) throws IOException {
		model.setMapVisible(true);
	}
	
	/*
	 * Add a Tournament
	 */
	public void addTourny(ActionEvent event) throws IOException{
		
		TextInputDialog dialog = new TextInputDialog("New Name");
		dialog.setTitle("Specify New Name");
		dialog.setHeaderText("Name the Tournament Copy");
		dialog.setContentText("Tournament Name:");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			model.saveCopyOfTournamentUnderName(result.get(), model.getCurrentTournament());
		}
	}
	
	public void loadTourny(ActionEvent event) throws IOException{
		Tournament t = showList("Select which tournament to load.");
		
		if(t != null) {
			model.setCurrentTournament(t);
		}
	}
	
	public void removeTourny(ActionEvent event) throws IOException{
		
		Alert alert;
		Tournament t = showList("Select which tournament to remove.");
		
		if(t != null) {
			if(t.getId() == model.getCurrentTournament().getId()) {
				alert = new Alert(AlertType.ERROR);
				alert.setTitle("Cannot Remove");
				alert.setHeaderText("Failed to Remove Selected Tournament");
				alert.setContentText("You cannot delete the currently selected Tournament!");
				alert.showAndWait();
			} else {
				model.removeTournament(t);
			}
		}
	}
	
	private Tournament showList(String instructions) {
		
		ArrayList<Tournament> choices = new ArrayList<Tournament>();
		Tournament defTourny = null;
		choices.addAll(model.getTournaments());
		
		if(choices.size() > 0) {
			defTourny = choices.get(0);
		}
		
		ChoiceDialog<Tournament> dialog = new ChoiceDialog<>(defTourny, choices);
		dialog.setTitle("Choose Tournament");
		dialog.setHeaderText("Select a Tournament");
		dialog.setContentText(instructions);

		Optional<Tournament> result = dialog.showAndWait();
		if (result.isPresent()){
		    return result.get();
		} else {
			return null;
		}
	}
	
	public void quit(ActionEvent event) throws IOException{
		
	    Stage stage = (Stage) binSectional.getScene().getWindow();
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Unsaved Data");
		alert.setHeaderText("You may have unsaved data!");
		alert.setContentText(" Would you like to save before closing?");

		ButtonType yesButton = new ButtonType("Yes");
		ButtonType noButton = new ButtonType("No");
		ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == yesButton){
			model.saveTournament();
		    stage.close();
		} else if (result.get() == noButton) {
			stage.close();
		}
	}
	
	public void saveTourny(ActionEvent event) throws InterruptedException, IOException {
		model.saveTournament();
	}
	
	public void manageHost(ActionEvent e){
		model.setManageHostVisible(true);
	}
	
	public void addHost() {
		model.showEligibleHosts();
	}
	
	public void removeHost() {
		model.showAllHosts();
	}
	
	/*
	 * Getters
	 */
	public VBox getBinSectional() {
		return binSectional;
	}

	public VBox getBinRegional() {
		return binRegional;
	}

	public VBox getBinSemi() {
		return binSemi;
	}

	public VBox getBinState() {
		return binState;
	}

	public MenuButton getLevelList() {
		return levelList;
	}

	public MenuButton getHostList() {
		return hostList;
	}

}
