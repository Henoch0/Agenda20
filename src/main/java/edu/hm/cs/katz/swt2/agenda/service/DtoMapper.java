package edu.hm.cs.katz.swt2.agenda.service;

import edu.hm.cs.katz.swt2.agenda.common.StatusEnum;

import edu.hm.cs.katz.swt2.agenda.persistence.Status;
import edu.hm.cs.katz.swt2.agenda.persistence.StatusRepository;
import edu.hm.cs.katz.swt2.agenda.persistence.Task;
import edu.hm.cs.katz.swt2.agenda.persistence.Topic;
import edu.hm.cs.katz.swt2.agenda.persistence.TopicRepository;
import edu.hm.cs.katz.swt2.agenda.persistence.User;
import edu.hm.cs.katz.swt2.agenda.service.dto.OwnerTaskDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.OwnerTopicDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.StatusDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.SubscriberTaskDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.SubscriberTopicDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.UserDisplayDto;

import java.util.Collection;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Hilfskomponente zum Erstellen der Transferobjekte aus den Entities. Für diese
 * Aufgabe gibt es viele Frameworks, die aber zum Teil recht schwer zu verstehen
 * sind. Da das Mapping sonst zu viel redundantem Code führt, ist die
 * Zusammenführung aber notwendig.
 * 
 * @author Bastian Katz (mailto: bastian.katz@hm.edu)
 */
@Component
public class DtoMapper {

	@Autowired
	private ModelMapper mapper;

	@Autowired
	private TopicRepository topicRepository;

	@Autowired
	private StatusRepository statusRepository;

	/**
	 * Erstellt ein {@link UserDisplayDto} aus einem {@link User}.
	 */
	public UserDisplayDto createDto(User user) {
		UserDisplayDto dto = mapper.map(user, UserDisplayDto.class);
		dto.setTopicCount(topicRepository.countByCreator(user));
		dto.setSubscriptionCount(user.getSubscriptions().size());
		return dto;
	}

	/**
	 * Erstellt ein {@link SubscriberTopicDto} aus einem {@link Topic}.
	 */
	public SubscriberTopicDto createDto(Topic topic, User user) {
		UserDisplayDto creatorDto = mapper.map(topic.getCreator(), UserDisplayDto.class);
		int openTasks = 0;
		for (Task task : topic.getTasks()) {
			Status status = statusRepository.findByUserAndTask(user, task);

			if (status == null || status.getStatus() != StatusEnum.FERTIG) {
				openTasks++;
			}
		}
		SubscriberTopicDto topicDto = new SubscriberTopicDto(topic.getUuid(), creatorDto, topic.getTitle(),
				topic.getShortDescription(), topic.getLongDescription());
		topicDto.setOpenTasks(openTasks);
		return topicDto;
	}

	/**
	 * Erstellt ein {@link StatusDto} aus einem {@link Status}.
	 */
	public StatusDto createDto(Status status) {
		StatusDto statuss = new StatusDto(status.getComment());
		statuss.setStatus(status.getStatus());
		return statuss;
	}

	/**
	 * Erstellt ein {@link SubscriberTaskDto} aus einem {@link Task} und einem
	 * {@link Status}.
	 */
	public SubscriberTaskDto createReadDto(Task task, Status status, User user) {
		Topic topic = task.getTopic();
		SubscriberTopicDto topicDto = createDto(topic, user);
		return new SubscriberTaskDto(task.getId(), task.getTitle(), task.getShortDescription(),
				task.getLongDescription(), topicDto, createDto(status), task.getDeadline(), task.getTaskType());
	}

	/**
	 * Erstellt ein {@link OwnerTopicDto} aus einem {@link Topic}.
	 */
	public OwnerTopicDto createManagedDto(Topic topic) {
		OwnerTopicDto ownerTopicDto = new OwnerTopicDto(topic.getUuid(), createDto(topic.getCreator()),
				topic.getTitle(), topic.getShortDescription(), topic.getLongDescription(),
				topic.getSubscriber().size());
		int openTasks = 0;
		for (Task task : topic.getTasks()) {
			Status status = statusRepository.findByUserAndTask(topic.getCreator(), task);
			if (status == null || status.getStatus() != StatusEnum.FERTIG) {
				openTasks++;
			}
		}
		ownerTopicDto.setOpenTasks(openTasks);
		return ownerTopicDto;
	}

	/**
	 * Erstellt ein {@link OwnerTaskDto} aus einem {@link Task}.
	 */
	public OwnerTaskDto createManagedDto(Task task) {
		Topic topic = task.getTopic();
		Collection<User> users = topic.getSubscriber();
		int finishedSubscriber = 0;
		for (User user : users) {
			Status status = statusRepository.findByUserAndTask(user, task);

			if (status != null && status.getStatus() == StatusEnum.FERTIG) {
				finishedSubscriber++;
			}
		}
		return new OwnerTaskDto(task.getId(), task.getTitle(), task.getShortDescription(), task.getLongDescription(),
				createDto(task.getTopic(), topic.getCreator()), finishedSubscriber, task.getDeadline(),
				task.getTaskType());
	}

}
