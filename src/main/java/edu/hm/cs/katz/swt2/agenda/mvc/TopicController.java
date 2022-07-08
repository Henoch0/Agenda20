package edu.hm.cs.katz.swt2.agenda.mvc;

import edu.hm.cs.katz.swt2.agenda.service.TaskService;
import edu.hm.cs.katz.swt2.agenda.service.TopicService;
import edu.hm.cs.katz.swt2.agenda.service.dto.OwnerTopicDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.SubscriberTopicDto;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller-Klasse für alle Interaktionen, die die Anzeige und Verwaltung von
 * Topics betrifft. Controller reagieren auf Aufrufe von URLs. Sie benennen ein
 * View-Template (Thymeleaf-Vorlage) und stellen Daten zusammen, die darin
 * dargestellt werden. Dafür verwenden Sie Methoden der Service-Schicht.
 * 
 * @author Bastian Katz (mailto: bastian.katz@hm.edu)
 */
@Controller
public class TopicController extends AbstractController {

	@Autowired
	private TopicService topicService;

	@Autowired
	private TaskService taskService;

	/**
	 * Erstellt die Übersicht über alle Topics des Anwenders, d.h. selbst erzeugte
	 * und abonnierte.
	 */
	@GetMapping("/topics")
	public String getTopicListView(Model model, Authentication auth,
			@RequestParam(name = "search", required = false, defaultValue = "") String search) {
		List<SubscriberTopicDto> topics = topicService.getSubscriptions(auth.getName(), search);
		model.addAttribute("search", new Search());
		model.addAttribute("managedTopics", topicService.getManagedTopics(auth.getName(), search));
		model.addAttribute("topics", topics);
		return "topic-listview";
	}

	/**
	 * Erstellt die Anischt über alle Subscriber.
	 */
	@GetMapping("/subscribers/{uuid}")
	public String getSubscriberListView(Model model, Authentication auth, @PathVariable("uuid") String id) {
		model.addAttribute("subscribers", topicService.getAllSubscriber(id));
		return "subscriber-listview";
	}

	/**
	 * Erstellt das Formular zum Erstellen eines Topics.
	 */
	@GetMapping("/topics/create")
	public String getTopicCreationView(Model model, Authentication auth) {
		model.addAttribute("newTopic", new SubscriberTopicDto(null, null, "", null, null));
		return "topic-creation";
	}

	/**
	 * Nimmt den Formularinhalt vom Formular zum Erstellen eines Topics entgegen und
	 * legt einen entsprechendes Topic an. Kommt es dabei zu einer Exception, wird
	 * das Erzeugungsformular wieder angezeigt und eine Fehlermeldung eingeblendet.
	 * Andernfalls wird auf die Übersicht der Topics weitergeleitet und das Anlegen
	 * in einer Einblendung bestätigt.
	 */
	@PostMapping("/topics")
	public String handleTopicCreation(Model model, Authentication auth,
			@ModelAttribute("newTopic") SubscriberTopicDto topic, RedirectAttributes redirectAttributes) {
		try {
			topicService.createTopic(topic.getTitle(), topic.getShortDescription(), topic.getLongDescription(),
					auth.getName());
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/topics/create";
		}
		redirectAttributes.addFlashAttribute("success", "Topic " + topic.getTitle() + " angelegt.");
		return "redirect:/topics";
	}

	/**
	 * Verarbeitert die Löschung eines Topics.
	 */
	@PostMapping("/topics/{uuid}/delete")
	public String handleTopicsDeletion(Authentication auth, @PathVariable("uuid") String id,
			RedirectAttributes redirectAttributes) {
		topicService.deleteTopic(id, auth.getName());
		redirectAttributes.addFlashAttribute("success", "Topic gelöscht");
		return "redirect:/topics/";

	}

	/**
	 * Erzeugt Anzeige eines Topics mit Informationen für den Ersteller.
	 */
	@GetMapping("/topics/{uuid}/manage")
	public String createTopicManagementView(Model model, Authentication auth, @PathVariable("uuid") String uuid) {
		OwnerTopicDto topic = topicService.getManagedTopic(uuid, auth.getName());
		model.addAttribute("topic", topic);
		model.addAttribute("tasks", taskService.getManagedTasks(uuid, auth.getName()));
		return "topic-management";
	}
	/**
	 * Verarbeitet die Änderung eines Topics.
	 */
	@PostMapping("topics/{uuid}/manage")
	public String handleTopicUpdate(@ModelAttribute("topic") OwnerTopicDto topic, Authentication auth,
			@PathVariable("uuid") String uuid, @RequestHeader(value = "referer", required = true) String referer) {
		topicService.updateTopic(uuid, auth.getName(), topic.getShortDescription(), topic.getLongDescription());
		return "redirect:" + referer;

	}

	/**
	 * Erzeugt Anzeige für die Nachfrage beim Abonnieren eines Topics.
	 */
	@GetMapping("/topics/{uuid}/register")
	public String getTopicRegistrationView(Model model, Authentication auth, @PathVariable("uuid") String uuid) {
		SubscriberTopicDto topic = topicService.getTopic(uuid, auth.getName());
		model.addAttribute("topic", topic);
		return "topic-registration";
	}

	/**
	 * Nimmt das Abonnement (d.h. die Bestätigung auf die Nachfrage) entgegen und
	 * erstellt ein Abonnement.
	 */
	@PostMapping("/topics/{uuid}/register")
	public String handleTopicRegistration(Model model, Authentication auth, @PathVariable("uuid") String uuid,
			RedirectAttributes redirectAttributes, @RequestHeader(value = "referer", required = true) String referer) {
		try {
			topicService.subscribe(uuid, auth.getName());
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Topic kann nicht abonniert werden");
			return "redirect:" + referer;
		}
		return "redirect:/topics/" + uuid;
	}

	/**
	 * Erstellt Übersicht eines Topics für einen Abonennten.
	 */
	@GetMapping("/topics/{uuid}")
	public String createTopicView(Model model, Authentication auth, @PathVariable("uuid") String uuid) {
		SubscriberTopicDto topic = topicService.getTopic(uuid, auth.getName());
		model.addAttribute("topic", topic);
		model.addAttribute("tasks", taskService.getTasksForTopic(uuid, auth.getName()));
		return "topic";
	}

	/**
	 * Erzeugt Anzeige für die Nachfrage beim De-Abonnieren eines Topics.
	 */
	@GetMapping("/topics/{uuid}/unregister")
	public String getTopicUnsubscriptionView(Model model, Authentication auth, @PathVariable("uuid") String uuid) {
		SubscriberTopicDto topic = topicService.getTopic(uuid, auth.getName());
		model.addAttribute("topic", topic);
		return "topic-unsubscribe";
	}

	/**
	 * Nimmt das Abonnement (d.h. die Bestätigung auf die Nachfrage) entgegen und
	 * löscht ein Abonnement.
	 */
	@PostMapping("/topics/{uuid}/unregister")
	public String handleTopicUnsubscription(Model model, Authentication auth, @PathVariable("uuid") String uuid,
			RedirectAttributes redirectAttributes, @RequestHeader(value = "referer", required = true) String referer,
			String login) {
		try {
			topicService.unsubscribe(uuid, auth.getName());

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Topic bereits de-abonniert!");
			return "redirect:" + referer;
		}
		return "redirect:/topics";
	}

}
