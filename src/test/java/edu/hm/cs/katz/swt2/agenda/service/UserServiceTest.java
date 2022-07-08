package edu.hm.cs.katz.swt2.agenda.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.validation.ValidationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.hm.cs.katz.swt2.agenda.persistence.User;
import edu.hm.cs.katz.swt2.agenda.persistence.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	@Mock
	UserRepository userRepository;

	@InjectMocks
	UserService userService = new UserServiceImpl();

	@Test
	public void createUserSuccess() {
		Mockito.when(userRepository.existsById("henoch")).thenReturn(false);

		userService.legeAn("henoch", "Henoch", "abcabcabc", false);

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		Mockito.verify(userRepository).save(userCaptor.capture());
		assertEquals("henoch", userCaptor.getValue().getLogin());

	}

	@Test
	public void shouldFailIfUserExsists() {
		Mockito.when(userRepository.existsById("henoch")).thenReturn(true);
		assertThrows(ValidationException.class, () -> {
			userService.legeAn("henoch", "Henoch", "abcabcabc", false);
		});
	}

	@Test
	public void createUserFailsLoginTooLong() {
		assertThrows(ValidationException.class, () -> {
			userService.legeAn("henochhenochneochhenochhneoch", "Henoch", "abcabcabc", false);
		});
	}

}
