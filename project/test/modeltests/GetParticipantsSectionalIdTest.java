package modeltests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import builder.Model;

import org.junit.jupiter.api.Test;


class GetParticipantsSectionalIdTest {

	@Test void test() {
		int sectionalId = Model.getInstance().getParticipantsSectionalId(1);
		int sectional2Id = Model.getInstance().getParticipantsSectionalId(2);
		
		assertEquals(2, sectionalId);
		assertEquals(13, sectional2Id);
	}

}
