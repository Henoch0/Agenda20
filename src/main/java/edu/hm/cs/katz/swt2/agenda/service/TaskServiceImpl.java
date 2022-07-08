package edu.hm.cs.katz.swt2.agenda.service;

import edu.hm.cs.katz.swt2.agenda.common.TaskTypeEnum;
import edu.hm.cs.katz.swt2.agenda.common.StatusEnum;
import edu.hm.cs.katz.swt2.agenda.persistence.Status;
import edu.hm.cs.katz.swt2.agenda.persistence.StatusRepository;
import edu.hm.cs.katz.swt2.agenda.persistence.Task;
import edu.hm.cs.katz.swt2.agenda.persistence.TaskRepository;
import edu.hm.cs.katz.swt2.agenda.persistence.Topic;
import edu.hm.cs.katz.swt2.agenda.persistence.TopicRepository;
import edu.hm.cs.katz.swt2.agenda.persistence.User;
import edu.hm.cs.katz.swt2.agenda.persistence.UserRepository;
import edu.hm.cs.katz.swt2.agenda.service.dto.OwnerTaskDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.StatusDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.SubscriberTaskDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.TaskDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.apache.commons.collections4.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(rollbackFor = Exception.class)
public class TaskServiceImpl implements TaskService {

	private static final Logger LOG = LoggerFactory.getLogger(TaskServiceImpl.class);

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private TopicRepository topicRepository;

	@Autowired
	private UserRepository anwenderRepository;

	@Autowired
	private StatusRepository statusRepository;

	@Autowired
	private DtoMapper mapper;

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public Long createTask(String uuid, String titel, String shortDescription, String longDescription, String login,
			String deadline, TaskTypeEnum taskType) {
		LOG.info("Erstellung eines Tasks von {}", login);
		LOG.debug("Erstellung eines Tasks mit ID und Titel {}", uuid, login);

		User creator = anwenderRepository.findById(login).get();
		Topic t = topicRepository.findById(uuid).get();
		Task task = new Task(t, titel, shortDescription, longDescription, creator, deadline, taskType);

		if (titel.length() < 8) {
			LOG.debug("Task-Titel zu kurz");
			throw new ValidationException(
					"Ihr Task-Titel muss mindestens aus 8 Zeichen bestehen! Bitte wählen Sie einen längeren Titel aus.");
		} else if (titel.length() > 32) {
			LOG.debug("Task-Titel zu lang");
			throw new ValidationException(
					"Ihr Task-Titel darf nicht mehr als 32 Zeichen beinhalten! Bitte wählen Sie einen kürzeren Titel aus.");
		}

		taskRepository.save(task);
		return task.getId();
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public void deleteTask(Long id, String login) {
		LOG.info("Löschen eines Tasks von {}", login);
		LOG.debug("Löschen eines Tasks mit ID und Titel {}", id, login);
		Task task = taskRepository.getOne(id);
		User user = anwenderRepository.getOne(login);
		// Überprüfung ob Zugriff erlaubt ist
		if (user.equals(task.getTopic().getCreator())) {
			taskRepository.delete(task);
		} else {
			LOG.warn("Löschen des Tasks verweigert!");
			throw new AccessDeniedException("Zugriff verweigert.");

		}
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public void updateTask(Long id, String login, String shortDescription, String longDescription) {
		LOG.info("Ändern eines Tasks von {}", login);
		LOG.debug("Ändern eines Tasks mit ID und Titel {}", id, login);
		Task task = taskRepository.getOne(id);
		User user = anwenderRepository.getOne(login);
		// Überprüfung ob Zugriff erlaubt ist
		if (user.equals(task.getTopic().getCreator())) {
			task.setShortDescription(shortDescription);
			task.setLongDescription(longDescription);
		} else {
			LOG.warn("Löschen des Tasks verweigert!");
			throw new AccessDeniedException("Zugriff verweigert.");
		}
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public SubscriberTaskDto getTask(Long id, String login) {
		LOG.info("Zugriff auf abonnierten Task");
		LOG.debug("Zugriff auf Task mit ID {}", id);
		Task task = taskRepository.getOne(id);
		Topic topic = task.getTopic();
		User user = anwenderRepository.getOne(login);
		if (!(topic.getCreator().equals(user) || topic.getSubscriber().contains(user))) {
			LOG.warn("Zugrif auf Task verweigert");
			throw new AccessDeniedException("Zugriff verweigert.");
		}
		Status status = getOrCreateStatus(id, login);
		return mapper.createReadDto(task, status, anwenderRepository.getOne(login));
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public OwnerTaskDto getManagedTask(Long id, String login) {
		LOG.info("Zugriff auf Task von {}", login);
		LOG.debug("Zugriff auf Task mit ID {}", id);
		Task task = taskRepository.getOne(id);
		Topic topic = task.getTopic();
		User createdBy = topic.getCreator();
		if (!login.equals(createdBy.getLogin())) {
			LOG.warn("Zugrif auf Task verweigert");
			throw new AccessDeniedException("Zugriff verweigert.");
		}
		return mapper.createManagedDto(task);
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public List<SubscriberTaskDto> getSubscribedTasks(String login, String search) {
		LOG.info("Zugriff auf alle abonnierten Tasks");
		User user = anwenderRepository.getOne(login);
		Collection<Topic> topics = user.getSubscriptions();
		List<SubscriberTaskDto> result = extracted(user, topics);
		// nach search filtern!
		for (Iterator<SubscriberTaskDto> it = result.iterator(); it.hasNext();) {
			SubscriberTaskDto next = it.next();
			if (!next.getTitle().toLowerCase().contains(search.toLowerCase())) {
				it.remove();
			}
		}
		return result;
	}

	private List<SubscriberTaskDto> extracted(User user, Collection<Topic> topics) {
		Collection<Status> status = user.getStatus();
		Map<Task, Status> statusForTask = new HashMap<>();
		for (Status currentStatus : status) {
			statusForTask.put(currentStatus.getTask(), currentStatus);
		}

		List<SubscriberTaskDto> result = new ArrayList<>();

		for (Topic t : topics) {
			for (Task task : t.getTasks()) {
				if (statusForTask.get(task) == null) {
					Status createdStatus = getOrCreateStatus(task.getId(), user.getLogin());
					statusForTask.put(task, createdStatus);
				}
				result.add(mapper.createReadDto(task, statusForTask.get(task), user));
			}
		}

		Comparator<SubscriberTaskDto> compareByStatusAndTitle = new Comparator<SubscriberTaskDto>() {

			public int compare(SubscriberTaskDto t1, SubscriberTaskDto t2) {

				int value = t1.getStatus().compareTo(t2.getStatus());

				if (value == 0) {
					return t1.getTitle().toLowerCase().compareTo(t2.getTitle().toLowerCase());
				} else {
					return value;
				}
			}
		};

		Collections.sort(result, compareByStatusAndTitle);
		return result;
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public List<SubscriberTaskDto> getTasksForTopic(String uuid, String login) {
		LOG.info("Zugriff auf alle Tasks eines abonnierten Topics");
		LOG.debug("Zugriff auf alle Tasks mit ID {} eines abbonierten Topics", uuid);
		User user = anwenderRepository.getOne(login);
		Topic topic = topicRepository.getOne(uuid);

		return extracted(user, SetUtils.hashSet(topic));
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public void checkTask(Long id, String login) {
		LOG.info("Martkiert einen Task als erledigt");
		LOG.debug("Markiert den Task {} als erledigt");
		Status status = getOrCreateStatus(id, login);
		status.setStatus(StatusEnum.FERTIG);
		LOG.debug("Status von Task {} und Anwender {} gesetzt auf {}", status.getTask(), status.getUser(),
				status.getStatus());
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public void startTask(Long id, String login) {
		LOG.info("Martkiert einen Task als in Bearbeitung");
		LOG.debug("Markiert den Task {} als in Bearbeitung");
		Status status = getOrCreateStatus(id, login);
		status.setStatus(StatusEnum.BEARBEITUNG);
		LOG.debug("Status von Task {} und Anwender {} gesetzt auf {}", status.getTask(), status.getUser(),
				status.getStatus());
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public void resetTask(Long id, String login) {
		LOG.info("Setzt den Status eines Tasks zurück");
		LOG.debug("Setzt den Status des Task {} Zurück");
		Status status = getOrCreateStatus(id, login);
		status.setStatus(StatusEnum.OFFEN);
		LOG.debug("Status von Task {} und Anwender {} gesetzt auf {}", status.getTask(), status.getUser(),
				status.getStatus());
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public List<OwnerTaskDto> getManagedTasks(String uuid, String login) {
		LOG.info("Zugriff auf alle eigenen Tasks");
		LOG.debug("Zugriff auf alle eigenen Tasks mit ID, uuid");
		User user = anwenderRepository.getOne(login);
		List<OwnerTaskDto> result = new ArrayList<>();
		Topic topic = topicRepository.getOne(uuid);
		if (!user.equals(topic.getCreator())) {
			LOG.warn("{} ist nicht berechtigt auf {} zugreifen", login, topic);
			throw new AccessDeniedException("Zugriff auf Topic verwährt!");
		}
		for (Task task : topic.getTasks()) {
			result.add(mapper.createManagedDto(task));
		}
		Comparator<OwnerTaskDto> compareByTitle = new Comparator<OwnerTaskDto>() {
			public int compare(OwnerTaskDto o1, OwnerTaskDto o2) {
				return o1.getTitle().compareTo(o2.getTitle());
			}
		};
		Collections.sort(result, compareByTitle);
		return result;
	}

	private Status getOrCreateStatus(Long id, String login) {
		LOG.info("Erstellung oder Ändern eine Status {}", login);
		LOG.debug("Erstellen oder Ändern des Tasks {}", id, login);
		User user = anwenderRepository.getOne(login);
		Task task = taskRepository.getOne(id);
		Status status = statusRepository.findByUserAndTask(user, task);
		if (status == null) {
			status = new Status(task, user, "");
			statusRepository.save(status);
			LOG.debug("Neuer Status wure erstellt {}", id, login);
		}
		return status;
	}

	@Override
	public void comment(String login, Long taskId, String comment) {
		LOG.info("Kommentar wird angelegt");
		LOG.debug("Kommentar wird mit folgenden Parameter angelegt: {}, {}, {}.", login, taskId, comment);
		User user = anwenderRepository.getOne(login);
		Task task = taskRepository.getOne(taskId);
		Status status = statusRepository.findByUserAndTask(user, task);
		status.setComment(comment);
		statusRepository.save(status);

	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public List<StatusDto> getStatusForTask(Long taskId, String login) {
		LOG.info("Zugriff auf Status eines Tasks");
		LOG.debug("Zugriff auf Status des Tasks {} von {}", taskId, login);
		Task task = taskRepository.getOne(taskId);
		List<Status> status = statusRepository.findByTask(task);
		if (status.isEmpty()) {
			LOG.warn("Kein Status zu dem Task: {}!", task.getTitle());
			return null;
		}

		List<StatusDto> result = new ArrayList<StatusDto>();
		for (Status statuse : status) {
			result.add(mapper.createDto(statuse));
		}

		return result;
	}

}
