package fx.application;

import builder.Model;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.InfoWindow;
import com.lynden.gmapsfx.javascript.object.InfoWindowOptions;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;
import fx.controller.MapController;
import fx.controller.TableController;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import netscape.javascript.JSObject;
import tournament.components.Base;
import tournament.components.Event;
import tournament.components.Event.Level;
import tournament.components.School;

public class Main extends Application implements Observer{

	private static final String HOST_ICON = "https://maps.google.com/mapfiles/kml/pal4/icon21.png";
	private static final String SCHOOL_ICON = "http://maps.google.com/mapfiles/kml/pal2/icon2.png";
	private static final String POSSIBLE_TRANSFER_ICON = "http://maps.google.com/mapfiles/kml/pal5/icon11.png";
	private static final String UNCONFIRMED_TRANSFER_ICON = "http://maps.google.com/mapfiles/kml/pal3/icon38.png";
	private static final String POSSIBLE_HOST_ICON = "http://maps.google.com/mapfiles/kml/pal4/icon20.png";
	private static final String ELIGIBLE_HOST_ICON = "http://maps.google.com/mapfiles/kml/pal2/icon13.png";
	private static final String REMOVE_ELIGIBLE_HOST_ICON = "http://maps.google.com/mapfiles/kml/pal4/icon20.png";
	
	private enum EventHandlerType {LEVEL_HOST_SCHOOL, HOST_SCHOOL, FEEDER_SCHOOL, POSSIBLE_TRANSFER, UNCONFIRMED_TRANSFER, POSSIBLE_HOST, ELIGIBLE_HOST, REMOVE_ELIGIBLE_HOST};
	
	private static Model model;
	private static TableController tableController;
	private static MapController mapController;
	
	private static Stage window;
	private static Scene mapView;
	private static Scene tableView;

	private static Marker hostMarker;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override public void start(Stage primaryStage) throws Exception {
		
		FXMLLoader loader;
		Parent tableView;
		Parent mapView;
		
		// Fetch Model
		model = Model.getInstance();
		model.addObserver(this);
		
        
	    // Load the table view
	    loader = new FXMLLoader(getClass().getResource("TableView.fxml"));
	    tableView = loader.load();
	    
	    // Save a reference to the controller
	    tableController = (TableController) loader.getController();
	    
		Main.tableView = new Scene(tableView);
		
		// Load the map View the same way
		loader = new FXMLLoader(getClass().getResource("MapView.fxml"));
		mapView = loader.load();
				
		mapController = ((MapController) loader.getController());
		
		Main.mapView = new Scene(mapView);
		
		// Save the stage reference
		window = primaryStage;
		window.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override public void handle(WindowEvent arg0) {
				model.cleanUp();
				Platform.exit();
		        System.exit(0);
			}
		});
		window.setScene(Main.tableView);
		window.show();
		
		model.loadInitialTournament();
		
	}

	@Override public void update(Observable o, Object arg) {

		Object[] successAndType = (Object[]) arg;

		if((Boolean)successAndType[0] == false) {
			return;
		}

		if((Boolean) successAndType[0]) {
			
			switch( (Model.ActionPerformed) successAndType[1]) {
			case UPDATED_SCHOOL:
				break;
			case ADDED_SCHOOL:
				break;
			case REMOVED_SCHOOL:
				break;
			case TOURNAMENT_REFRESHED:
				refreshTournament();
				break;
			case TOURNAMENT_CHANGED:
				refreshTournament();
				model.setSelectedLevel(Level.Sec);
				break;
			case ADDED_TOURNAMENT:
				break;
			case SCHOOL_SELECTED:
				
				Event selectedEvent = model.getSelectedEvent();
				
				if(selectedEvent != null) {

					showFeeders(selectedEvent);
					
					mapController.getHostList().setText(selectedEvent.getHost().getName());
					
				} else {
					// Clear the host Menu Button
					mapController.getHostList().setText("Choose Host");
				}
				break;
			case EVENT_LEVEL_SELECTED:
				
				if(model.getSelectedLevel() == Level.State){
					mapController.getLevelList().setText("State");
					disableSectionalControls();
				} else if(model.getSelectedLevel() == Level.Semi){
					mapController.getLevelList().setText("Semistate");
					disableSectionalControls();
				} else if(model.getSelectedLevel() == Level.Reg){
					mapController.getLevelList().setText("Regional");
					disableSectionalControls();
				} else {
					mapController.getLevelList().setText("Sectional");
					mapController.getAddSectionalSchools().setDisable(false);
					mapController.getCancelChanges().setDisable(false);
					mapController.getConfirmChanges().setDisable(false);
				}
				
				showLevel(model.getSelectedLevel());
				
				break;
			case VIEW_SWITCHED:
				toggle();
				break;
			case TRANSFER_PROCESS_STARTED:
				
				String[] choices = {"25 miles", "50 miles", "75 miles", "100 miles", "125 miles", "150 miles", "All"};
				
				ChoiceDialog<String> radiusDialog = new ChoiceDialog<String>("25 miles", choices);
				
				radiusDialog.setTitle("Transfer Radius");
				radiusDialog.setHeaderText("Select a distance for displaying schools to transfer here.");
				radiusDialog.setContentText("Select a distance:");
				
				Optional<String> result = radiusDialog.showAndWait();
				
				if(result.isPresent()) {
					if(result.get().compareTo("All") == 0) {
						showPossibleTransfers(400);
					} else {
						showPossibleTransfers(Integer.parseInt(result.get().substring(0, result.get().indexOf(' '))));
					}
				} else {
					radiusDialog.close();
				}
				
				break;
				
			case TRANSFER_ADDED_TO_TLIST:
				School transferSchool = (School) successAndType[2];
				
				addUnconfirmedTransferMarker(transferSchool);
				updateTransferHostMarkerInfo();
				
				
				break;
				
			case TRANSFER_REMOVED_FROM_TLIST:
				School removedSchool = (School) successAndType[2];
				
				Marker marker = displaySchoolMarker(removedSchool, POSSIBLE_TRANSFER_ICON);
				addMarkerEventHandlers(marker, mapController.getMap(), removedSchool, EventHandlerType.POSSIBLE_TRANSFER);

				break;
			
			case TRANSFER_PROCESS_CANCELED:
				hostMarker = null;
				mapController.getLevelList().setText("Sectional");
				showLevel(Level.Sec);
				
				break;
			case SECTIONAL_UPDATED:
				
				refreshTournament();
				
				showFeeders(model.getSelectedEvent());
				
				break;
			case CHANGE_HOST_PROCESS_STARTED:
				ArrayList<School> feeders = model.getFeederSchools(model.getSelectedEvent());
				
				
				mapController.getMap().clearMarkers();
				
				for(int i = 0; i < feeders.size(); i++) {
					
					if(feeders.get(i).equals(model.getSelectedEvent().getHost())) {
						continue;
					}
					Marker hostMarker = displaySchoolMarker(feeders.get(i), POSSIBLE_HOST_ICON);
					addMarkerEventHandlers(hostMarker, mapController.getMap(), feeders.get(i), EventHandlerType.POSSIBLE_HOST);
				}
				
				break;
			case SHOW_ELIGIBLE_HOSTS:
				mapController.getMap().clearMarkers();
				
				ArrayList<School> eligibleHostList = model.getNonHosts();
				ArrayList<String> schools = new ArrayList<>();

				for(int i=0; i<eligibleHostList.size(); i++) {
					schools.add(eligibleHostList.get(i).getName());
				}
				
				for(School possibleHostAdd : eligibleHostList) {
					Marker hostMarker = displaySchoolMarker(possibleHostAdd, ELIGIBLE_HOST_ICON);
					addMarkerEventHandlers(hostMarker, mapController.getMap(), possibleHostAdd, EventHandlerType.ELIGIBLE_HOST);
				}
				
				showAddOrRemoveHostDialog(eligibleHostList, schools, "Add", "map");
				
				break;
			case SHOW_REMOVE_ELIGIBLE_HOSTS:
				mapController.getMap().clearMarkers();
				ArrayList<School> removeEligibleHostList = model.getHostList();
				ArrayList<String> removeSchools = new ArrayList<>();
				for(int i=0; i<removeEligibleHostList.size(); i++) {
					removeSchools.add(removeEligibleHostList.get(i).getName());
				}
				//Display markers
				for(School possibleHostRemove : removeEligibleHostList) {
					Marker hostMarker = displaySchoolMarker(possibleHostRemove, REMOVE_ELIGIBLE_HOST_ICON);
					addMarkerEventHandlers(hostMarker, mapController.getMap(), possibleHostRemove, EventHandlerType.REMOVE_ELIGIBLE_HOST);
				}
				showAddOrRemoveHostDialog(removeEligibleHostList, removeSchools, "Remove", "map");
				break;
			case SHOW_ALL_ELIGIBLE_HOSTS:				
				ArrayList<School> allHosts = model.getNonHosts();
				ArrayList<String> listSchools = new ArrayList<>();
				for(int i=0; i<allHosts.size(); i++) {
					listSchools.add(allHosts.get(i).getName());
				}
				showAddOrRemoveHostDialog(allHosts, listSchools, "Add", "table");
				break;
			case SHOW_UNSET_HOSTS:
				ArrayList<School> unsetHosts = model.getHostList();
				ArrayList<String> listUnsetSchools = new ArrayList<>();
				for(int i=0; i<unsetHosts.size(); i++) {
					listUnsetSchools.add(unsetHosts.get(i).getName());
				}
				showAddOrRemoveHostDialog(unsetHosts, listUnsetSchools, "Remove", "table");
				break;
			case EVENT_HOST_UPDATED:
				refreshTournament();
				mapController.getMap().clearMarkers();
				showLevel(model.getSelectedLevel());
				break;
			default:
				break;
			}
		}

	}
	
	/** Displays all the host schools for a selected level
	 * 
	 * @param lvl - Level to show
	 */
	private void showLevel(Event.Level lvl) {
		
		ArrayList<Event> events = model.getEventTier(lvl);
		MenuButton menu = mapController.getHostList();
		MenuItem item = null;
		School school = null;

		menu.getItems().removeAll(menu.getItems());
		
		mapController.getMap().clearMarkers();
		
		events.sort(Event.Comparators.BY_HOST_NAME);
		
		for(int i = 0; i < events.size(); i++) {
			
			school = events.get(i).getHost();
			
			final Event hostEvent = events.get(i);
			
			item = new MenuItem(school.getName());
			
			item.setOnAction(new EventHandler<ActionEvent>() {
			    @Override public void handle(ActionEvent event) {
			        Model.getInstance().setSelectedEvent(hostEvent);
			    }
			});
			
			menu.getItems().add(item);
			
			Marker marker = displaySchoolMarker(school, HOST_ICON);
			
			//This marker event handler is added here b/c it requires a reference to the Event, which is unavailable in addMarkerEventHandlers()
			mapController.getMap().addUIEventHandler(marker, UIEventType.click, (JSObject obj) -> { 
				Model.getInstance().setSelectedEvent(hostEvent); 
			});
			
			addMarkerEventHandlers(marker, mapController.getMap(), hostEvent.getHost(), EventHandlerType.LEVEL_HOST_SCHOOL);
		}
	}
	
	
	/*
	 * Shows all the schools that feed an event on the map
	 */
	public void showFeeders(Event event) {
		
		ArrayList<School> schools = model.getFeederSchools(event);
		GoogleMap map = mapController.getMap();
		School school = null;
		School host = event.getHost();
		
		map.clearMarkers();
		
		
		if(host != null) {
			final Marker marker = displaySchoolMarker(host, HOST_ICON);
			hostMarker = marker;
			final School markerSchool = host;
				
			addMarkerEventHandlers(marker, map, markerSchool, EventHandlerType.HOST_SCHOOL);
		}

		
		for(int i = 0; i < schools.size(); i++) {
			
			school = schools.get(i);
			
			// Skip over a feeder if it is also hosting the event
			if(host.getId() == school.getId()) {
				continue;
			}
			
			Marker schoolMarker = displaySchoolMarker(school, SCHOOL_ICON);
			addMarkerEventHandlers(schoolMarker, map, school, EventHandlerType.FEEDER_SCHOOL);
		}

	}
	
	/*
	 * Shows all the schools that feed an event on the map including possible transfer schools
	 */
	public void showPossibleTransfers(int transferRadius) {
		
		GoogleMap map = mapController.getMap();
		map.clearMarkers();
		
		ArrayList<School> potentialTransfers = null;

		potentialTransfers = model.getSchoolsInRadiusofSelectedEvent(transferRadius);
		
		if(potentialTransfers.isEmpty()) {
			return;
		}
		

		for(int i = 0; i < potentialTransfers.size(); i++) {
			
			School potentialTransfer = potentialTransfers.get(i);
			
			Marker marker = displaySchoolMarker(potentialTransfer, POSSIBLE_TRANSFER_ICON);
			
			addMarkerEventHandlers(marker, map, potentialTransfer, EventHandlerType.POSSIBLE_TRANSFER);
		}
		
	}
	
	
	public void addUnconfirmedTransferMarker(School transferSchool) {
		
		Marker schoolMarker = displaySchoolMarker(transferSchool, UNCONFIRMED_TRANSFER_ICON);
		
		addMarkerEventHandlers(schoolMarker, mapController.getMap(), transferSchool, EventHandlerType.UNCONFIRMED_TRANSFER);
		
	}
	
	
	public Marker displaySchoolMarker(School school, final String iconUrl) {
		GoogleMap map = mapController.getMap();
		MarkerOptions opts = new MarkerOptions();
		Marker marker = null;
		
		opts.label("");
		opts.icon(iconUrl);
		opts.position(new LatLong(school.getLatitude(), school.getLongitude()));
		
		marker = new Marker(opts);
		map.addMarker(marker);	
		
		return marker;
	}
	
	/*
	 * Adds the corresponding UIEventHandlers to map for a marker based on the specified type
	 */
	public void addMarkerEventHandlers(Marker marker, GoogleMap map, School school, EventHandlerType type) {
		
		Event hostEvent = model.getSelectedEvent();
		School host = null;
		
		if(hostEvent != null) {
			host = model.getSelectedEvent().getHost();
		}
		
		String schoolInfo = "";
		InfoWindowOptions schoolInfoOpt = new InfoWindowOptions();	
		
		
		if(type == EventHandlerType.LEVEL_HOST_SCHOOL) {
			
			schoolInfo = "<h3><strong>" + school.getName() + " (host)" + "</strong></h3>";

		} else if(type == EventHandlerType.HOST_SCHOOL) {
			
			ArrayList<Event> events = model.getEventTier(model.getSelectedLevel());
			Event feederSchool = null;
			
			for(int i = 0; i < events.size(); i++) {
				if(school.getId() == events.get(i).getHost().getId()) {
					feederSchool = events.get(i);
					break;
				}
			}
			
			double averageDistance = 0.0;
			double longestDistance = 0.0;
			
			if(model.getUserIsTransferring()) {
				averageDistance = model.getAverageDistanceWithTransfers();
				longestDistance = model.getLongestDistanceWithTransfers();
			} else {
				averageDistance = model.getAverageDistance(feederSchool);
				longestDistance = (Double) model.getLongestDistanceAndSchool(feederSchool)[0];
			}
						
			schoolInfo = "<h3><strong>" + host.getName() + " (host)" + "</strong></h3>"
					+ "<h4>Distance Info</h4>"
                    + "Average: " + String.format("%.1f", averageDistance) + " miles<br>"
                    + "Longest: " + String.format("%.1f", longestDistance) + " miles";
			
			
			schoolInfoOpt.content(schoolInfo);
		
		} else if(type == EventHandlerType.FEEDER_SCHOOL) {
		
			schoolInfo = "<h5><strong><center>" + school.getName() + "</br>" + 
					"Distance: " + model.getSchoolDistance(host.getId(), school.getId()) + " miles " + school.getCity() + " </center></strong></h5>";
		
		} else if(type == EventHandlerType.POSSIBLE_TRANSFER) {
			
		
			schoolInfo = "<h5><strong><center>" + school.getName() + "</br>" + "Distance: " + model.getSchoolDistance(host.getId(), school.getId()) + 
			" miles " + school.getCity() + " </center></strong></h5>";
	
		
			
			
			map.addUIEventHandler(marker, UIEventType.click, (JSObject obj) -> { 
				if(model.getUserIsTransferring()) {
					
					map.removeMarker(marker);
					mapController.possibleTransferClicked(school);
				}
			});
		} else if(type == EventHandlerType.UNCONFIRMED_TRANSFER) {
		
			schoolInfo = "<h5><strong><center>" + school.getName() + "</br>" + "Distance: " + model.getSchoolDistance(host.getId(), school.getId()) + 
					" miles </center></strong></h5>";
			
			schoolInfoOpt.content(schoolInfo);
						
			map.addUIEventHandler(marker, UIEventType.click, (JSObject obj) -> { 
				if(model.getUserIsTransferring()) {
					
					
					Alert confirmRemoval = new Alert(AlertType.CONFIRMATION);
					confirmRemoval.setTitle("Remove Unconfirmed Transfer");
					confirmRemoval.setHeaderText("Remove Unconfirmed Transfer?");
					confirmRemoval.setContentText("Are you sure you want to remove " + school.getName()
							+ " as a transfer school to " + model.getSelectedEvent().getHost().getName() + "'s sectional?");
					
					Optional<ButtonType> result = confirmRemoval.showAndWait();
					
					if(result.get() == ButtonType.OK) {
						map.removeMarker(marker);
						mapController.removeSchoolFromTransferList(school);
					}
				}
			});
		} else if(type == EventHandlerType.POSSIBLE_HOST) {
			
			map.addUIEventHandler(marker, UIEventType.click, (JSObject obj) -> { 
					
				if(model.getUserIsChangingHost()) {
					
					Alert confirmRemoval = new Alert(AlertType.CONFIRMATION);
					confirmRemoval.setTitle("Change Host School Confirmation?");
					confirmRemoval.setHeaderText("Set this as the new host school?");
					confirmRemoval.setContentText("Are you sure you want make " + school.getName() + " the new host school for the currently selected event?");
					
					Optional<ButtonType> result = confirmRemoval.showAndWait();
					
					if(result.get() == ButtonType.OK) {
						mapController.getHostList().setText(school.getName());
						mapController.confirmHostChange(school.getId()); 
					}
				}
			});
			
			
		}else if(type == EventHandlerType.ELIGIBLE_HOST) {
			schoolInfo = "<h3><strong>" + school.getName() + "</strong></h3>";
			
			
			schoolInfoOpt.content(schoolInfo);
			
			map.addUIEventHandler(marker, UIEventType.click, (JSObject obj) -> { 
				Alert confirmAddition = new Alert(AlertType.CONFIRMATION);
				confirmAddition.setTitle("Add School to Host List Confirmation?");
				confirmAddition.setHeaderText("Add as eligible host school?");
				confirmAddition.setContentText("Are you sure you want make " + school.getName() + " an eligible host school for the currently selected event?");
				
				Optional<ButtonType> result = confirmAddition.showAndWait();
				
				if(result.get() == ButtonType.OK) {
					model.addSchoolToHostList(school);
					map.removeMarker(marker);
					Marker hostMarker = displaySchoolMarker(school, POSSIBLE_HOST_ICON);
					addMarkerEventHandlers(hostMarker, mapController.getMap(), school, EventHandlerType.REMOVE_ELIGIBLE_HOST);
				}
			});
		} else if(type == EventHandlerType.REMOVE_ELIGIBLE_HOST) {
			ArrayList<Event> eventsWhereHost = model.getAppearances(school);
			if(eventsWhereHost.size() != 0) {
				schoolInfo = "<h3><strong>" + school.getName() + " (host)" + "</strong></h3>";
			} else {
				schoolInfo = "<h3><strong>" + school.getName() + " " + "</strong></h3>";
			}
			
			schoolInfoOpt.content(schoolInfo);
			
			map.addUIEventHandler(marker, UIEventType.click, (JSObject obj) -> { 
				if(eventsWhereHost.size() != 0) {
					showWarning();
				} else {
					Alert confirmAddition = new Alert(AlertType.CONFIRMATION);
					confirmAddition.setTitle("Remove School from Host List Confirmation?");
					confirmAddition.setHeaderText("Remove as eligible host school?");
					confirmAddition.setContentText("Are you sure you want remove " + school.getName() + " from the eligible host school list for the currently selected event?");
					
					Optional<ButtonType> result = confirmAddition.showAndWait();
					
					if(result.get() == ButtonType.OK) {
						model.deleteSchoolFromHostList(school);
						map.removeMarker(marker);
						Marker hostMarker = displaySchoolMarker(school, ELIGIBLE_HOST_ICON);
						addMarkerEventHandlers(hostMarker, mapController.getMap(), school, EventHandlerType.ELIGIBLE_HOST);
					}
				}
			});
		}
		
		schoolInfoOpt.content(schoolInfo);
		
		final InfoWindow infoWindow = new InfoWindow(schoolInfoOpt);
		
		map.addUIEventHandler(marker, UIEventType.mouseover, (JSObject obj) -> { 
			
			if(type == EventHandlerType.POSSIBLE_HOST) {
				double averageDistance = 0.0;
				double longestDistance = 0.0;
				
				averageDistance = model.getAverageDistanceForPossibleHost(school);
				longestDistance = model.getLongestDistanceForPossibleHost(school);
				
				String possibleHostInfo = "<h3><strong>" + school.getName() + "</strong></h3>"
						+ "<h4>Distance Info</h4>"
	                    + "Average: " + String.format("%.1f", averageDistance) + " miles<br>"
	                    + "Longest: " + String.format("%.1f", longestDistance) + " miles";
				
				
				infoWindow.setContent(possibleHostInfo);
			}
			
			infoWindow.open(map, marker);
		});
		map.addUIEventHandler(marker, UIEventType.mouseout, (JSObject obj) -> { 
			infoWindow.close();
		});
	}
	
	
	/*
	 * Refreshes the transfer host marker to contain updated distance info
	 */
	public void updateTransferHostMarkerInfo() {
		mapController.getMap().removeMarker(hostMarker);
		
		School hostSchool = model.getSelectedEvent().getHost();
		
		hostMarker = displaySchoolMarker(hostSchool, HOST_ICON);
		addMarkerEventHandlers(hostMarker, mapController.getMap(), hostSchool, EventHandlerType.HOST_SCHOOL);
	}
	
	
	/*
	 * Updates the TableView with the current tournament
	 */
	private void refreshTournament() {
		
		Object[] distanceAndSchool;
		double longestDistance;
		double avgDistance;
		Label[] eventLabels;
		String[] descriptions;
		ArrayList<Event> feeders = new ArrayList<Event>();
		ArrayList<Event> descendants = new ArrayList<Event>();
		ArrayList<Event> parents = new ArrayList<Event>();
		ArrayList<School> schools = new ArrayList<School>();
		DecimalFormat formatter = new DecimalFormat("#0.0");
		VBox[] bins = new VBox[4];
		
		// Skip a refresh if the Controller is null
		if(tableController == null) {
			return;
		}
		
		if(!Model.getInstance().getConnectionStatus()) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Server Status");
			alert.setHeaderText("MySQL Server Not responding");
			alert.setContentText("Unable to connect to MySQL server! Please restart...");
			alert.showAndWait();
			return;
		}

		bins[0] = tableController.getBinState();
		bins[1] = tableController.getBinSemi();
		bins[2] = tableController.getBinRegional();
		bins[3] = tableController.getBinSectional();
		
		parents.add(Model.getInstance().getCurrentTournament().getHighestEvent());
		
		for(int i = 0; i < bins.length; i++) {
			bins[i].setSpacing(10);
			
			// Clear the previous labels
			bins[i].getChildren().clear();
			descriptions = new String[parents.size()];
			
			// Evaluate parents events
			for(int j = 0; j < parents.size(); j++) {
				
				descendants.addAll(model.getDirectFeederEvents(parents.get(j)));
				feeders.addAll(descendants);
				
				distanceAndSchool = model.getLongestDistanceAndSchool(parents.get(j));
				longestDistance = (double) distanceAndSchool[0];
				avgDistance = model.getAverageDistance(parents.get(j));
				
				descriptions[j] = (j + 1) + ". " + parents.get(j).getHost().getName() + "\t\t Furthest Distance: " + formatter.format(longestDistance) + " miles  Average Distance: " + formatter.format(avgDistance) + " miles\n";
				
				// Check if this is the base case
				if(descendants.size() != 1) {
					for(int k = 0; k < descendants.size(); k++) {
						descriptions[j] += descendants.get(k).getHost().getName();
						if(k != descendants.size() - 1) {
							descriptions[j] += ", ";
						}
					}
				} else {
					
					schools.addAll(((Base) descendants.get(0)).getSchools());
					
					for(int k = 0; k < schools.size(); k++) {
						descriptions[j] += schools.get(k).getName();
						if(k != schools.size() - 1) {
							descriptions[j] += ", ";
						}
						if((k + 1) % 8 == 0) {
							descriptions[j] += "\n";
						}
					}
					
					schools.clear();
				}
				
				descendants.clear();
				
			}
			
			//Add labels
			eventLabels = new Label[descriptions.length];
			
			for(int j = 0; j < descriptions.length; j++) {
				eventLabels[j] = new Label(descriptions[j]);
				eventLabels[j].setStyle("-fx-padding: 4 0 4 0");
				eventLabels[j].setMinHeight(60);
			}

			// Add Labels
			bins[i].getChildren().addAll(eventLabels);
			
			// Swap feeders with parents
			descendants.clear();
			parents.clear();
			parents.addAll(feeders);
			feeders.clear();
			
		}
	}
	
	/*
	 * Switch between map view and table view 
	 */
	public void toggle() {
		
		window.hide();
		
		if(model.isMapVisible()) {
			if(mapController.getMap() == null) {
				mapController.initialize(null, null);
			}
			window.setScene(mapView);
			window.show();
		} else {
			
			window.setScene(tableView);
			window.show();
		}
	}

	/*
	 * Shows the dialog box to add/remove schools
	 */
	private void showAddOrRemoveHostDialog(ArrayList<School> eligibleHostList, ArrayList<String> schools, String addOrRemove, String view) {
		ChoiceDialog<String> dialog = new ChoiceDialog<>(eligibleHostList.get(0).getName(), schools);
		dialog.setTitle("Choose Host Dialog");
		dialog.setHeaderText("Choose a school to " + addOrRemove.toLowerCase() + " as a host school");
		dialog.setContentText("Choose Host School:");
		((Button)dialog.getDialogPane().lookupButton(ButtonType.OK)).setText(addOrRemove);
		if(view.equals("map")) {
			((Button)dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText(addOrRemove+" From Map");
		}
		Optional<String> chosenSchool = dialog.showAndWait();
		if (chosenSchool.isPresent()){
			School selectedSchool = new School(null, null, null, null, false, false, 0, 0, 0);
			for(int i =0; i<eligibleHostList.size(); i++) {
				if(eligibleHostList.get(i).getName().equals(chosenSchool.get())) {
					selectedSchool = eligibleHostList.get(i);
				}
			}
			ArrayList<Event> eventsWhereHost = model.getAppearances(selectedSchool);
			if(eventsWhereHost.size() != 0) {
				showWarning();
			} else {

				if(addOrRemove.equals("Add")) {
					model.addSchoolToHostList(selectedSchool);
					Marker hostMarker = displaySchoolMarker(selectedSchool, POSSIBLE_HOST_ICON);
					addMarkerEventHandlers(hostMarker, mapController.getMap(), selectedSchool, EventHandlerType.POSSIBLE_HOST);
				} else if(addOrRemove.equals("Remove")) {
					model.deleteSchoolFromHostList(selectedSchool);
					Marker hostMarker = displaySchoolMarker(selectedSchool, ELIGIBLE_HOST_ICON);
					addMarkerEventHandlers(hostMarker, mapController.getMap(), selectedSchool, EventHandlerType.ELIGIBLE_HOST);
				}
			}
		    
		}
	}
	
	private void showWarning() {
		Alert warning = new Alert(AlertType.WARNING);
		warning.setTitle("Cannot remove current host");
		warning.setHeaderText("This school is currently serving as a host in the tournament.");
		warning.setContentText("If you would like to remove this school from the host list, please first change the host school of the event and then remove.");
		warning.showAndWait();
	}
	
	private void disableSectionalControls() {
		mapController.getAddSectionalSchools().setDisable(true);
		mapController.getConfirmChanges().setDisable(true);
	}
}
