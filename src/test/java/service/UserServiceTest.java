package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.hm.cs.katz.swt2.agenda.persistence.User;
import edu.hm.cs.katz.swt2.agenda.persistence.UserRepository;
import edu.hm.cs.katz.swt2.agenda.service.UserService;
import edu.hm.cs.katz.swt2.agenda.service.UserServiceImpl;

import javax.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	@Mock
	UserRepository userRepository;

	@InjectMocks
	UserService userService = new UserServiceImpl();

	@Test
	void createUserSuccess() {
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		userService.legeAn("henoch", "Henoch", "passwordpassword", false);
		Mockito.verify(userRepository).save(userCaptor.capture());
		assertEquals("henoch", userCaptor.getValue().getLogin());
	}

	@Test
	void createUserFailsForExistingLogin() {
		Mockito.when(userRepository.existsById("henoch")).thenReturn(true);
		assertThrows(ValidationException.class, () -> {
			userService.legeAn("henoch", "Henoch", "passwordpassword", false);
		});
	}

	@Test
	void createUserFailsForEmptyLogin() {
		assertThrows(ValidationException.class, () -> {
			userService.legeAn("", "Henoch", "passwordpassword", false);
		});
	}

	@Test
	void createUserFailsForLoginTooLong() {
		assertThrows(ValidationException.class, () -> {
			userService.legeAn("henochhenochhenochhenochhenoch", "Henoch", "passwordpassword", false);
		});
	}

	@Test
	void createUserFailsForPasswordTooLong() {
		assertThrows(ValidationException.class, () -> {
			userService.legeAn("henoch", "Henoch", "passwordpasswordpasswordpasswordpasswordpaassword", false);
		});
	}

}
