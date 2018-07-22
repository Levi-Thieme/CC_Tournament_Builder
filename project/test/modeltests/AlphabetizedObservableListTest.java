package modeltests;

import builder.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import tournament.components.School;

class AlphabetizedObservableListTest {

	@Test void test() {
		
		Model m = Model.getInstance();
		
		List<String> list = new ArrayList<>();
		
		ArrayList<School> schools = m.getAllSchools();
		
		for(int i = 0; i < schools.size(); i++) {
			list.add(schools.get(i).getName());
		}
		
		
		
		ObservableList<String> l = FXCollections.observableList(list);
		
		l.add("A school");
		l.add("a school");
		
		Collections.sort(l);
		
		String[] expected = {"A school", "a school"};
		
		int lastIndex = l.size();

		String[] actual = {l.get(1), l.get(lastIndex - 1)};


		
		Assert.assertArrayEquals(expected, actual);
	
		
		
	}

}
