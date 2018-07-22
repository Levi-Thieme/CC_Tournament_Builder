package fx.controller;

import builder.Model;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import tournament.components.Event.Level;
import tournament.components.School;


public class MapController implements Initializable {
	
	
	@FXML private MenuButton levelList;
	
	@FXML private MenuButton hostList;
	
	@FXML private AnchorPane googleMapViewParent;
	
	@FXML private Button addSectionalSchools;

	@FXML private Button cancelChanges;
	
	@FXML private Button confirmChanges;
	
	private GoogleMapView mapView;

    private GoogleMap map;

	private Model model;
	
	public MapController() {
		
		this.model = Model.getInstance();
	}

	@Override public void initialize(URL location, ResourceBundle resources) {
		// "AIzaSyA8HYKDAeJFtuJ8q3fpbpLN9dJ5pqCnq40"
		mapView = new GoogleMapView();	
		mapView.addMapInitializedListener(() -> configureMap());
	}
	
	protected void configureMap() {
	        MapOptions mapOptions = new MapOptions();

	        mapOptions.center(new LatLong(39.768403,-86.158068))
	                .mapType(MapTypeIdEnum.ROADMAP)
	                .zoom(8)
	                .clickableIcons(true);
	        
	        map = mapView.createMap(mapOptions, false);
	        
	        googleMapViewParent.getChildren().addAll(mapView);
	 }
	
	
	 
	/*
	 * Switch from MapView to TableView
	 */
	public void switchView(ActionEvent event) throws IOException {
		
		model.setMapVisible(false);
		
	}
	
	/*
	 * Switch from Event Level to Event Level
	 */
	public void eventSwitch(ActionEvent event) throws IOException {
		
		MenuItem m;
		String text;
		
		if( event.getSource() instanceof MenuItem) {
			m = (MenuItem) event.getSource();
			
			text = m.getText();

			// No event is selected since the level has been changed
			model.setSelectedEvent(null);
			
			if(text.compareTo("Sectional") == 0) {
				model.setSelectedLevel(Level.Sec);
			} else if(text.compareTo("Regional") == 0) {
				model.setSelectedLevel(Level.Reg);
			} else if(text.compareTo("Semi-State") == 0) {
				model.setSelectedLevel(Level.Semi);
			} else if(text.compareTo("State") == 0) {
				model.setSelectedLevel(Level.State);
			}
			
		}
		
	}

	/*
	 * Add a Tournament
	 */
	public void addTourny(ActionEvent event) throws IOException{
		
		TextInputDialog dialog = new TextInputDialog("New Tournament's Name");
		dialog.setTitle("Specify New Name");
		dialog.setHeaderText("Name the Tournament Copy");
		dialog.setContentText("Tournament Name:");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			model.saveCopyOfTournamentUnderName(result.get(), model.getCurrentTournament());
		}
		
	}
	
	/*
	 * Add a host school
	 */
	public void addHost(ActionEvent e) throws IOException {
		//Call Model method "showHosts"
		model.showHosts();
	}
	
	public void removeHost(ActionEvent e) {
		model.showRemovalHosts();
	}
	
	/*
	 * Create pop-up window
	 */
	public void createWindow(String fileName) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fileName));
		//System.out.println("/fx.application/"+fileName);
		Parent root = loader.load();
		Stage stage = new Stage();
		stage.setScene(new Scene(root));
		stage.show();
	}
	
	public MenuButton getLevelList() {
		return levelList;
	}
	
	public MenuButton getHostList() {
		return hostList;
	}

	public GoogleMap getMap() {
		return map;
	}
	
	public Button getAddSectionalSchools() {
		return addSectionalSchools;
	}

	public Button getCancelChanges() {
		return cancelChanges;
	}

	public Button getConfirmChanges() {
		return confirmChanges;
	}
	
	public void setUserIsTransferringTrue() {
		model.setUserIsTransferring(true);
	}
	
	public void setUserIsTransferringFalse() {
		model.setUserIsTransferring(true);
	}
	
	public void possibleTransferClicked(School school) {
		model.addTransfer(school);
	}
	
	public void removeSchoolFromTransferList(School school) {
		model.removeTransfer(school);
	}
	
	public void cancelTransferProcess() {
		model.setUserIsTransferring(false);
		model.clearTransferList();
		model.setSelectedEvent(null);
	}
	
	public void confirmTransferProcess() {
		model.commitTransfers();
	}
	
	public void startHostChangeProcess() {
		model.startHostChangeProcess();
	}
	
	public void confirmHostChange(int newHostId) {
		model.updateEventHost(model.getSelectedEvent(), newHostId);
	}

}
















