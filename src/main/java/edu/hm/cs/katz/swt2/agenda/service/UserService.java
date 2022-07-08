package edu.hm.cs.katz.swt2.agenda.service;

import edu.hm.cs.katz.swt2.agenda.service.dto.UserDisplayDto;
import java.util.List;

public interface UserService {
	/**
	 * Zugriff auf alle Nutzer.
	 */
	List<UserDisplayDto> getAllUsers();

	/**
	 * Zugriff auf alle Admins.
	 */
	List<UserDisplayDto> findeAdmins();

	/**
	 * Zugriff auf alle Nutzerdaten.
	 */
	UserDisplayDto getUserInfo(String login);

	/**
	 * Legt einen neuen Nutzer an.
	 */

	void legeAn(String login, String name, String password, boolean b);

	/**
	 * Ändert die Nutzerdaten eines Users
	 */

	void changeUserData(String login, String name, String password);

}
