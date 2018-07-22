package modeltests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import builder.Model;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tournament.components.Base;
import tournament.components.Event;
import tournament.components.Event.Level;
import tournament.components.Tournament;


class CopyTournament {

	static Model model = null;
	static Tournament t;
	static Tournament newTournament;
	
	@BeforeAll static void setUpBeforeClass() throws Exception {
		model = Model.getInstance();
		model.loadInitialTournament();
		t = model.getCurrentTournament();
		model.saveCopyOfTournamentUnderName(t.getName(), t);
		for(int i = 0; i < model.getTournaments().size(); i++) {
			if(model.getTournaments().get(i).getName().compareTo(t.getName() + "(1)") == 0) {
				newTournament = model.getTournaments().get(i);
			}
		}
	}

	@AfterAll static void tearDownAfterClass() throws Exception {
		model.removeTournament(newTournament);
		model.cleanUp();
	}

	@Test void test_A() {
		
		if(newTournament == null) {
			fail("The tournament was not added");
		}
		
	}
	
	@Test void test_B() {
		assertEquals(model.getEventTier(Level.Sec, t).size(), model.getEventTier(Level.Sec, newTournament).size());
	}
	
	@Test void test_C() {
		assertEquals(t.getHighestEvent().getHost().getId(), newTournament.getHighestEvent().getHost().getId());
	}
	
	@Test void test_D() {
		assertEquals(newTournament.getId(), newTournament.getHighestEvent().getTournament().getId());
	}
	
	@Test void test_E() {
		
		ArrayList<Event> newEvents = model.getDirectFeederEvents(newTournament.getHighestEvent());
		ArrayList<Event> events = model.getDirectFeederEvents(t.getHighestEvent());
		ArrayList<Event> newFeeders = new ArrayList<Event>();
		ArrayList<Event> eventFeeders = new ArrayList<Event>();
		ArrayList<Event> newSectionals = new ArrayList<Event>();
		ArrayList<Event> oldSectionals = new ArrayList<Event>();
		Base oldBase;
		Base newBase;
		
		assertEquals(events.size(), newEvents.size());
		
		for(int i = 0; i < newEvents.size(); i++) {
			assertEquals(events.get(i).getHost().getId(), newEvents.get(i).getHost().getId());
			assertEquals(events.get(i).getLevel(), newEvents.get(i).getLevel());
			
			newFeeders = model.getDirectFeederEvents(newEvents.get(i));
			eventFeeders = model.getDirectFeederEvents(events.get(i));
			
			assertEquals(eventFeeders.size(), newFeeders.size());
			
			for(int j = 0; j < eventFeeders.size(); j++) {
				assertEquals(eventFeeders.get(j).getHost().getId(), newFeeders.get(j).getHost().getId());
				assertEquals(eventFeeders.get(j).getLevel(), newFeeders.get(j).getLevel());
				
				newSectionals = model.getDirectFeederEvents(newFeeders.get(j));
				oldSectionals = model.getDirectFeederEvents(eventFeeders.get(j));
				
				assertEquals(oldSectionals.size(), newSectionals.size());
				
				for(int w = 0; w < oldSectionals.size(); w++) {
					assertEquals(oldSectionals.get(w).getHost().getId(), newSectionals.get(w).getHost().getId());
					assertEquals(oldSectionals.get(w).getLevel(), newSectionals.get(w).getLevel());
					
					newBase = (Base) newSectionals.get(w);
					oldBase = (Base) oldSectionals.get(w);
					
					assertEquals(oldBase.getSchools().size(), newBase.getSchools().size());
					
					for(int r = 0; r < newBase.getSchools().size(); r++) {
						
						assertEquals(oldBase.getSchools().get(r).getId(), newBase.getSchools().get(r).getId());
						
					}
					
				}
				
			}
			
		}
	}

}
