package edu.hm.cs.katz.swt2.agenda.service;

import edu.hm.cs.katz.swt2.agenda.common.StatusEnum;
import edu.hm.cs.katz.swt2.agenda.common.UuidProviderImpl;
import edu.hm.cs.katz.swt2.agenda.persistence.Status;
import edu.hm.cs.katz.swt2.agenda.persistence.Task;
import edu.hm.cs.katz.swt2.agenda.persistence.Topic;
import edu.hm.cs.katz.swt2.agenda.persistence.TopicRepository;
import edu.hm.cs.katz.swt2.agenda.persistence.User;
import edu.hm.cs.katz.swt2.agenda.persistence.UserRepository;
import edu.hm.cs.katz.swt2.agenda.service.dto.OwnerTopicDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.SubscriberTaskDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.SubscriberTopicDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.UserDisplayDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(rollbackFor = Exception.class)
public class TopicServiceImpl implements TopicService {

	private static final Logger LOG = LoggerFactory.getLogger(TopicServiceImpl.class);

	@Autowired
	private UuidProviderImpl uuidProvider;

	@Autowired
	private UserRepository anwenderRepository;

	@Autowired
	private TopicRepository topicRepository;

	@Autowired
	private DtoMapper mapper;

	@Override
	@PreAuthorize("#login==authentication.name OR hasRole('ROLE_ADMIN')")
	public String createTopic(String title, String shortDescription, String longDescription, String login) {
		LOG.info("Topic von {} wird erstellt", login);
		LOG.debug("Erstelle Topic {}.", title);

		if (title.length() < 10) {
			LOG.debug("Topic-Titel ist zu kurz");
			throw new ValidationException(
					"Ihr Topic-Titel muss mindestens aus 10 Zeichen bestehen! Bitte wählen Sie einen längeren Titel aus.");
		} else if (title.length() > 60) {
			LOG.debug("Topic-Titel ist zu lang");
			throw new ValidationException(
					"Ihr Topic-Titel darf nicht mehr als 60 Zeichen beinhalten! Bitte wählen Sie einen kürzeren Titel aus.");
		}

		String uuid = uuidProvider.getRandomUuid();
		User creator = anwenderRepository.findById(login).get();
		Topic topic = new Topic(uuid, title, shortDescription, longDescription, creator);
		topicRepository.save(topic);
		return uuid;
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public void deleteTopic(String id, String login) {
		LOG.info("Löschen eines Topics von {}", login);
		LOG.debug("Löschen eines Topics mit ID und Titel {}", id, login);
		Topic topic = topicRepository.getOne(id);
		User user = anwenderRepository.getOne(login);
		// Überprüfung ob Zugriff erlaubt ist
		if (user.equals(topic.getCreator())) {
			topicRepository.delete(topic);

		} else {
			LOG.warn("Löschen des Topic verweigert!");
			throw new AccessDeniedException("Zugriff verweigert.");

		}
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public void updateTopic(String id, String login, String shortDescription, String longDescription) {
		LOG.info("Ändern eines Topics von {}", login);
		LOG.debug("Ändern eines Topics mit ID und Titel {}", id, login);
		Topic topic = topicRepository.getOne(id);
		User user = anwenderRepository.getOne(login);
		// Überprüfung ob Zugriff erlaubt ist
		if (user.equals(topic.getCreator())) {
			topic.setShortDescription(shortDescription);
			topic.setLongDescription(longDescription);
		} else {
			LOG.warn("Zugriff auf Änderung der Beschreibung verweigert!");
			throw new AccessDeniedException("Zugriff verweigert.");
		}
	}

	@Override
	@PreAuthorize("#login==authentication.name")
	public List<OwnerTopicDto> getManagedTopics(String login, String search) {
		LOG.info("Zugriff auf Topics von {}", login);
		User creator = anwenderRepository.findById(login).get();
		List<Topic> managedTopics = topicRepository.findByCreatorOrderByTitleAsc(creator);
		List<OwnerTopicDto> result = new ArrayList<>();
		for (Topic topic : managedTopics) {
			result.add(mapper.createManagedDto(topic));
		}

		// nach search filtern!
		for (Iterator<OwnerTopicDto> it = result.iterator(); it.hasNext();) {
			SubscriberTopicDto next = it.next();
			if (!next.getTitle().toLowerCase().contains(search.toLowerCase())) {
				it.remove();
			}
		}
		return result;
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public OwnerTopicDto getManagedTopic(String topicUuid, String login) {
		LOG.info("Zugriff auf Topic von {} ", login);
		LOG.debug("Zugriff auf Topic mit id {}", topicUuid);
		Topic topic = topicRepository.getOne(topicUuid);
		return mapper.createManagedDto(topic);
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public SubscriberTopicDto getTopic(String uuid, String login) {
		LOG.info("Zugriff auf Topic von {} abonniert", login);
		LOG.debug("Abonniertes Topic mit id {} wird zugegriffen", uuid);
		Topic topic = topicRepository.getOne(uuid);
		return mapper.createDto(topic, anwenderRepository.getOne(login));
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public void subscribe(String topicUuid, String login) {
		LOG.info("Abonnieren eines Topics");
		LOG.debug("Topic mit id {} wird abonniert", topicUuid);
		Topic topic = topicRepository.getOne(topicUuid);
		User anwender = anwenderRepository.getOne(login);
		if (anwender.getSubscriptions().contains(topic)) {
			throw new ValidationException("Topic bereits abonniert!");
		}

		if (anwender.equals(topic.getCreator())) {
			throw new ValidationException("Eigenes Topic kann nicht abonniert werden");
		}

		topic.register(anwender);
	}

	@Override
	public void unsubscribe(String topicUuid, String login) {
		LOG.info("De-Abonnieren eines Topics");
		Topic topic = topicRepository.getOne(topicUuid);
		User anwender = anwenderRepository.getOne(login);
		LOG.debug("Topic mit id {} wird deabonniert");
		topic.unregister(anwender);
	}

	@Override
	@PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
	public List<SubscriberTopicDto> getSubscriptions(String login, String search) {
		LOG.info("Zugriff auf alle abbonierten Topics von {}", login);
		User creator = anwenderRepository.findById(login).get();
		// Collection<Topic> subscriptions = creator.getSubscriptions();
		List<SubscriberTopicDto> result = new ArrayList<>();
		List<Topic> topicSubscriptions = topicRepository.findBySubscriberOrderByTitleAsc(creator);
		for (Topic topic : topicSubscriptions) {
			result.add(mapper.createDto(topic, anwenderRepository.getOne(login)));
		}
		// nach search filtern!
		for (Iterator<SubscriberTopicDto> it = result.iterator(); it.hasNext();) {
			SubscriberTopicDto next = it.next();
			if (!next.getTitle().toLowerCase().contains(search.toLowerCase())) {
				it.remove();
			}
		}
		return result;
	}

	@Override
	public String getTopicUuid(String key) {
		LOG.info("Uuod auflösen für Key {}.", key);
		if (key.length() < 8) {
			throw new ValidationException("Key ist zu kurz!");
		}

		Topic topic = topicRepository.findByUuidEndingWith(key);
		if (topic == null) {
			throw new ValidationException("Kein Topic mit diesem Schlüssel vorhanden!");
		}

		return topic.getUuid();
	}

	@Override
	public List<UserDisplayDto> getAllSubscriber(String topicUuid) {
		LOG.info("Alle Subscriber finden");
		Topic topic = topicRepository.getOne(topicUuid);
		List<UserDisplayDto> result = new ArrayList<>();
		Collection<User> list = topic.getSubscriber();

		for (User anwender : list) {
			int counter = 0;

			for (Status status : anwender.getStatus()) {
				if (status.getStatus() == StatusEnum.FERTIG && status.getTask().getTopic().equals(topic)) {
					counter++;
				}
			}
			UserDisplayDto userDto = mapper.createDto(anwender);
			userDto.setcompletedTasks(counter);
			result.add(userDto);
		}
		Collections.sort(result, new CompletedTasksComperator());
		return result;
	}

}
