package distancetests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import builder.Model;
import org.junit.Before;
import org.junit.jupiter.api.Test;

class TestGetDistanceMethod {
	
	Model model;
	
	@Before void setUp() {
		model = Model.getInstance();
	}

	@Test void test_A() {
		assertEquals(160.0, model.getSchoolDistance(2, 1));
	}
	
	@Test void test_B() {
		assertEquals(155, model.getSchoolDistance(95, 31));
	}
	
	@Test void test_C() {
		assertEquals(47.8, model.getSchoolDistance(260, 141));
	}
	
	@Test void test_D() {
		assertEquals(20.7, model.getSchoolDistance(355, 224));
	}
	
	@Test void test_F() {
		assertEquals(192, model.getSchoolDistance(407, 406));
	}

}
