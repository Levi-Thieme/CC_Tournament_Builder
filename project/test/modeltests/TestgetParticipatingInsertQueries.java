package modeltests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import builder.Model;
import tournament.components.Base;
import tournament.components.Event.Level;
import tournament.components.School;

class TestgetParticipatingInsertQueries {

	@Test
	void test() {
		List<School> schools = Arrays.asList(new School(0, null, null, null, null, false, false, 0, 0, 0), new School(1, null, null, null, null, false, false, 0, 0, 0));
		String sql = "INSERT INTO `participating`(`school_id`, `sectional_id`) "
				+ "VALUES(";
		sql += String.join("", schools.stream()
						.map(school -> "("+school.getId() + ", 21),")
						.collect(Collectors.toList()));
		
						
		sql = sql.substring(0, sql.length()-1);
		sql += ");";
		System.out.println(sql);
	}

}
