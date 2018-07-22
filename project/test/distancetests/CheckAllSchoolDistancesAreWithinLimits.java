package distancetests;

import static org.junit.Assert.assertEquals;

import builder.Model;
import org.junit.Before;
import org.junit.Test;

public class CheckAllSchoolDistancesAreWithinLimits {

	Model model;
	static final int MAX_ROUTE_DISTANCE_IN_MILES = 400;
	
	@Before public void setUp() throws Exception {
		model = Model.getInstance();
	}

	@Test public void testGetSchoolDistance() {
		
		Double d = 0.0;
		int schools = 0;
		
		for(int id1 = 1; id1 <= 407; id1++) {
			for(int id2 = 1; id2 <= id1 - 1; id2++, schools++) {
				d += model.getSchoolDistance(id1, id2);
				
				assert(model.getSchoolDistance(id1, id2) < MAX_ROUTE_DISTANCE_IN_MILES);
			}
		}
		
		// Average of all distances is 125 +/- 25
		assertEquals(125, d/schools, 25);
	}

}
