package edu.hm.cs.katz.swt2.agenda.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.transaction.Transactional;
import javax.validation.ValidationException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;

import edu.hm.cs.katz.swt2.agenda.service.UserService;

@SpringBootTest
@ActiveProfiles("none")
@Transactional
public class UserCreationIt {
	@Autowired
	UserService userService;

	@Test
	@WithUserDetails("admin")
	public void createdUserContainsAllInformation() {
		userService.legeAn("henoch", "Henoch", "abcabcabc", false);
		var createdUser = userService.getUserInfo("henoch");

		assertEquals("henoch", createdUser.getLogin());
		assertEquals("Henoch", createdUser.getName());
		assertEquals("Henoch", createdUser.getDisplayName());
	}

	@Test
	@WithUserDetails("admin")
	public void cannotCreateExistingUser() {
		userService.legeAn("henoch", "Henoch", "abcabcabc", false);

		assertThrows(ValidationException.class, () -> {
			userService.legeAn("henoch", "Henoch", "abcabcabc", false);

		});
	}

}
