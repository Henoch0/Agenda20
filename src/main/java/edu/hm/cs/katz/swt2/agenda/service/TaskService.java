package edu.hm.cs.katz.swt2.agenda.service;

import edu.hm.cs.katz.swt2.agenda.common.TaskTypeEnum;
import edu.hm.cs.katz.swt2.agenda.service.dto.OwnerTaskDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.StatusDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.SubscriberTaskDto;
import java.util.List;

/**
 * Serviceklasse für Verarbeitung von Tasks.
 * 
 * @author Bastian Katz (mailto: bastian.katz@hm.edu)
 */
public interface TaskService {

	/**
	 * Erstellt einen neuen Task.
	 */
	Long createTask(String topicUuid, String title, String shortDescription, String longDescription, String login,
			String deadline, TaskTypeEnum taskType);

	/**
	 * Löscht einen Task
	 */
	void deleteTask(Long id, String login);

	/**
	 * Zugriff auf einen Task (priviligierte Sicht für Ersteller des Topics).
	 */
	OwnerTaskDto getManagedTask(Long id, String login);

	/**
	 * Zugriff auf alle Tasks eines eigenen Topics.
	 */
	List<OwnerTaskDto> getManagedTasks(String topicUuid, String login);

	/**
	 * Zugriff auf einen Task (Abonnentensicht).
	 */
	SubscriberTaskDto getTask(Long id, String login);

	/**
	 * Zugriff auf alle Tasks abonnierter Topics.
	 * 
	 * @param search
	 */
	List<SubscriberTaskDto> getSubscribedTasks(String login, String search);

	/**
	 * Zugriff auf alle Tasks eines abonnierten Topics.
	 */
	List<SubscriberTaskDto> getTasksForTopic(String topicUuid, String login);

	/**
	 * Markiert einen Task für einen Abonnenten als "done".
	 */
	void checkTask(Long id, String login);

	/**
	 * Setzt den Status einen Tasks zurück.
	 */
	void resetTask(Long id, String name);

	/**
	 * Aktualisert Beschreibung eines Tasks.
	 */
	void updateTask(Long id, String login, String shortDescription, String longDescription);

	/**
	 * Hinterlässt einen Kommentar beim Task.
	 */
	void comment(String login, Long taskId, String comment);

	/**
	 * Setzt den Status eines Tasks auf "In Bearbeitung"
	 */
	void startTask(Long id, String login);

	/**
	 * Zugriff auf Status eines Tasks.
	 */
	List<StatusDto> getStatusForTask(Long taskId, String login);

}
