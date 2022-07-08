package edu.hm.cs.katz.swt2.agenda.service;

import edu.hm.cs.katz.swt2.agenda.service.dto.OwnerTopicDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.SubscriberTopicDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.UserDisplayDto;

import java.util.List;

/**
 * Serviceklasse für Verarbeitung von Topics.
 * 
 * @author Bastian Katz (mailto: bastian.katz@hm.edu)
 */
public interface TopicService {

	/**
	 * Erstellt ein neues Topic.
	 */
	String createTopic(String title, String shortDescription, String longDescription, String login);

	/**
	 * Löscht ein Topic
	 */
	void deleteTopic(String id, String login);

	/**
	 * Zugriff auf ein eigenes Topic.
	 */
	OwnerTopicDto getManagedTopic(String topicUuid, String login);

	/**
	 * Zugriff auf alle eigenen Topics.
	 */
	List<OwnerTopicDto> getManagedTopics(String login, String search);

	/**
	 * Zugriff auf ein abonniertes Topic.
	 */
	SubscriberTopicDto getTopic(String topicUuid, String login);

	/**
	 * Zugriff auf alle abonnierten Topics.
	 */
	List<SubscriberTopicDto> getSubscriptions(String login, String search);

	/**
	 * Abonnieren eines Topics.
	 */
	void subscribe(String topicUuid, String login);

	/**
	 * Aktualiserung der Beschreibung eines Topics.
	 */
	void updateTopic(String id, String login, String shortDescription, String longDescription);

	/**
	 * Zugriff auf Topic Uuid.
	 */
	String getTopicUuid(String key);

	/**
	 * De-Abonnieren eines Topics.
	 */
	void unsubscribe(String topicUuid, String login);

	/**
	 * Zugriff auf alle Subscriber eines Topics.
	 */
	List<UserDisplayDto> getAllSubscriber(String topicUuid);

}