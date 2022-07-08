package edu.hm.cs.katz.swt2.agenda.service;

import static edu.hm.cs.katz.swt2.agenda.common.SecurityHelper.ADMIN_ROLES;
import static edu.hm.cs.katz.swt2.agenda.common.SecurityHelper.STANDARD_ROLES;

import edu.hm.cs.katz.swt2.agenda.persistence.Topic;
import edu.hm.cs.katz.swt2.agenda.persistence.User;
import edu.hm.cs.katz.swt2.agenda.persistence.UserRepository;
import edu.hm.cs.katz.swt2.agenda.service.dto.UserDisplayDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.ValidationException;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service-Klasse zur Verwaltung von Anwendern. Wird auch genutzt, um Logins zu
 * validieren. Servicemethoden sind transaktional und rollen alle Änderungen
 * zurück, wenn eine Exception auftritt. Service-Methoden sollten
 * <ul>
 * <li>keine Modell-Objekte herausreichen, um Veränderungen des Modells
 * außerhalb des transaktionalen Kontextes zu verhindern - Schnittstellenobjekte
 * sind die DTOs (Data Transfer Objects).
 * <li>die Berechtigungen überprüfen, d.h. sich nicht darauf verlassen, dass die
 * Zugriffen über die Webcontroller zulässig sind.</li>
 * </ul>
 * 
 * @author Bastian Katz (mailto: bastian.katz@hm.edu)
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserDetailsService, UserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private UserRepository anwenderRepository;

	@Autowired
	private DtoMapper mapper;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		LOG.info("Gesuchten Anwender anzeigen");
		LOG.debug("Anwender mit Usernamen {} anzeigen", username);
		Optional<User> findeMitspieler = anwenderRepository.findById(username);
		if (findeMitspieler.isPresent()) {
			User user = findeMitspieler.get();
			return new org.springframework.security.core.userdetails.User(user.getLogin(), user.getPassword(),
					user.isAdministrator() ? ADMIN_ROLES : STANDARD_ROLES);
		} else {
			LOG.debug("Anwender {} konnte nicht gefunden werden");
			throw new UsernameNotFoundException("Anwender konnte nicht gefunden werden.");
		}
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<UserDisplayDto> getAllUsers() {
		LOG.info("Alle Anwender auswählen");
		List<UserDisplayDto> result = new ArrayList<>();
		for (User anwender : anwenderRepository.findAllByOrderByLoginAsc()) {
			result.add(mapper.createDto(anwender));
		}
		return result;
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<UserDisplayDto> findeAdmins() {
		LOG.info("Admins finden");

		// Das Mapping auf DTOs geht eleganter, ist dann aber schwer verständlich.
		List<UserDisplayDto> result = new ArrayList<>();
		for (User anwender : anwenderRepository.findByAdministrator(true)) {
			result.add(mapper.createDto(anwender));
		}
		return result;
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public UserDisplayDto getUserInfo(String login) {
		LOG.info("Lese Daten von Anwender");
		LOG.debug("Lese Daten für Anwender {}.", login);
		User anwender = anwenderRepository.getOne(login);
		return mapper.createDto(anwender);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void legeAn(String login, String name, String password, boolean isAdministrator) {
		LOG.info("Anwender wird angelegt");
		LOG.debug("Erstelle Anwender {}.", login);

		if (anwenderRepository.existsById(login)) {
			LOG.debug("Benutzername existiert bereits");
			throw new ValidationException(
					"Dieser Benutzername existiert bereits! Bitte wählen Sie einen anderen Namen.");
		}

		boolean hasUpperCase = false;
		for (char Char : login.toCharArray()) {
			if (Character.isUpperCase(Char)) {
				hasUpperCase = true;
			}
		}

		if (login.length() < 4) {
			LOG.debug("Benutzername ist zu kurz (<4)");
			throw new ValidationException(
					"Ihr Benutzername muss mindestens aus 4 Zeichen bestehen! Bitte wählen Sie einen längeren Namen.");
		} else if (login.length() > 20) {
			LOG.debug("Benutzername ist zu lang (>20)");
			throw new ValidationException(
					"Ihr Benutzername darf nicht länger als 20 Zeichen sein! Bitte wählen Sie einen kürzeren Namen.");
		} else if (hasUpperCase == true) {
			LOG.debug("Benutzername ist nicht in Kleinbuchstaben");
			throw new ValidationException(
					"Ihr Benutzername darf nur aus Kleinbuchstaben bestehen! Bitte wählen Sie einen Neuen aus.");
		}

		boolean hasWhitespace = false;
		for (char Char : password.toCharArray()) {
			if (Character.isWhitespace(Char)) {
				hasWhitespace = true;
			}
		}

		if (password.length() < 8) {
			LOG.debug("Passwort ist zu kurz (<8)");
			throw new ValidationException(
					"Ihr Passwort muss mindestens aus 8 Zeichen bestehen! Bitte wählen Sie ein Neues aus.");
		} else if (password.length() > 20) {
			LOG.debug("Passwort ist zu lang (<20)");
			throw new ValidationException(
					"Ihr Passwort darf nicht länger als 20 Zeichen sein! Bitte wählen Sie ein Neues aus.");
		} else if (hasWhitespace == true) {
			LOG.debug("Passwort enthält Leerzeichen");
			throw new ValidationException(
					"Ihr Passwort darf kein Leerzeichen beinhalten! Bitte wählen Sie ein Neues aus.");
		}

		// Passwörter müssen Hashverfahren benennen.
		// Wir hashen nicht (noop), d.h. wir haben die
		// Passwörter im Klartext in der Datenbank (böse)

		User anwender = new User(login, name, "{noop}" + password, isAdministrator);
		anwenderRepository.save(anwender);
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public void changeUserData(String login, String name, String password) {
		LOG.info("Anwenderdaten werden geändert");
		LOG.debug("Neuer Name{} und Passwort{} für {}.", name, password, login);

		User anwender = anwenderRepository.getOne(login);
		anwender.setName(name);
		anwender.setPassword(password);

		boolean hasWhitespace = false;
		for (char Char : password.toCharArray()) {
			if (Character.isWhitespace(Char)) {
				hasWhitespace = true;
			}
		}

		if (password.length() < 8) {
			LOG.debug("Passwort ist zu kurz (<8)");
			throw new ValidationException(
					"Ihr Passwort muss mindestens aus 8 Zeichen bestehen! Bitte wählen Sie ein Neues aus.");
		} else if (password.length() > 20) {
			LOG.debug("Passwort ist zu lang (<20)");
			throw new ValidationException(
					"Ihr Passwort darf nicht länger als 20 Zeichen sein! Bitte wählen Sie ein Neues aus.");
		} else if (hasWhitespace == true) {
			LOG.debug("Passwort enthält Leerzeichen");
			throw new ValidationException(
					"Ihr Passwort darf kein Leerzeichen beinhalten! Bitte wählen Sie ein Neues aus.");
		}
	}

}
