package modeltests;

import builder.Model;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import tournament.components.Advancement;
import tournament.components.Event;
import tournament.components.Tournament;

class GetSectionalsUnitTest {

	@Test void test() {
		
		Model m = Model.getInstance();
		
		Tournament t1 = m.getTournaments().get(0);
		
		Event state = t1.getHighestEvent();
		
		ArrayList<Event> regionals = m.getDirectFeederEvents((Advancement) state);
		
		ArrayList<Event> sectionals = new ArrayList<Event>();
		
		for(int i = 0; i < regionals.size(); i++) {
			sectionals.addAll(m.getDirectFeederEvents((Advancement) regionals.get(i)));
		}
		
		
			
		
		for(int i = 0; i < sectionals.size(); i++) {
			System.out.println((i + 1) + ". " + sectionals.get(i).getHost().getName());
		}
		
		
	}

}
