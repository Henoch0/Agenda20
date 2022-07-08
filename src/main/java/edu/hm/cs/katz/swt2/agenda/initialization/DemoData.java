package edu.hm.cs.katz.swt2.agenda.initialization;

import edu.hm.cs.katz.swt2.agenda.common.SecurityHelper;
import edu.hm.cs.katz.swt2.agenda.common.TaskTypeEnum;
import edu.hm.cs.katz.swt2.agenda.service.TaskService;
import edu.hm.cs.katz.swt2.agenda.service.TopicService;
import edu.hm.cs.katz.swt2.agenda.service.UserService;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Initialisierung von Demo-Daten. Diese Komponente erstellt beim Systemstart
 * Anwender, Topics, Abonnements usw., damit man die Anwendung mit allen
 * Features vorführen kann.
 * 
 * @author Bastian Katz (mailto: bastian.katz@hm.edu)
 */
@Component
@Profile("demo")
public class DemoData {

	private static final String LOGIN_FINE = "fine";

	private static final String LOGIN_ERNIE = "ernie";

	private static final String LOGIN_BERT = "bert";

	private static final Logger LOG = LoggerFactory.getLogger(DemoData.class);

	@Autowired
	UserService anwenderService;

	@Autowired
	TopicService topicService;

	@Autowired
	TaskService taskService;

	/**
	 * Erstellt die Demo-Daten.
	 */
	@PostConstruct
	@SuppressWarnings("unused")
	public void addData() {
		SecurityHelper.escalate(); // admin rights
		LOG.debug("Erzeuge Demo-Daten.");

		anwenderService.legeAn(LOGIN_FINE, "Fine", "doppelapfel", false);
		anwenderService.legeAn(LOGIN_ERNIE, "Ernie", "traubeminze", false);
		anwenderService.legeAn(LOGIN_BERT, "Bert", "mangotango", false);

		String htmlKursUuid = topicService.createTopic("HTML für Anfänger",
				"Beginner-Kurs die sich mit HTML probieren wollen",
				"Ernie zeigt euch die Basics um mit HTML richtig durchstarten zu können ", LOGIN_FINE);
		topicService.subscribe(htmlKursUuid, LOGIN_ERNIE);
		topicService.subscribe(htmlKursUuid, LOGIN_BERT);
		Long linkErstellenTask = taskService.createTask(htmlKursUuid, "Link erstellen",
				"Bei dieser Aufgabe handelt es sich um die Erstellung eines Links",
				"Um einen Link herzustellen brauchen wir den Tag <a> mit der class=href", LOGIN_FINE, "\u221e",
				TaskTypeEnum.NOTYPE);
		taskService.checkTask(linkErstellenTask, LOGIN_ERNIE);
		taskService.createTask(htmlKursUuid, "Leeres HTML-Template erstellen",
				"Bei dieser Aufgabe handelt es sich um die Erstellung eines leeren HTML-Templates",
				"Für die Erstellung eines HTML-Templates muss lediglich eine Datei erstellt werden deren Namen mit .html endet",
				LOGIN_FINE, "\u221e", TaskTypeEnum.NOTYPE);

		String cssKursUuid = topicService.createTopic("CSS für Fortgeschrittene",
				"In diesem Kurs könnt ihr fortgeschrittene CSS Techniken lernen",
				"Fine zeigt euch CSS Techniken um eure Website noch schöner und reibungsloser zu gestalten ",
				LOGIN_FINE);
		String erniesKursUuid = topicService.createTopic("Ernies Backkurs",
				"In diesem Kurs zeigt euch Ernie wie man backt",
				"Ernie bringt euch bei verschiedene Gebäcke perfekt zu backen", LOGIN_ERNIE);
		taskService.createTask(erniesKursUuid, "Googlehupf backen", "Bei dieser Aufgabe backt man einen Googlehupf",
				"Für den Googlehupf wird Butter, Zucker, Mehl, Eier, Vanillezucker Backpulver und Milch benötigt.",
				LOGIN_ERNIE, "\u221e", TaskTypeEnum.NOTYPE);

		Long affenMuffinTask = taskService.createTask(erniesKursUuid, "Affenmuffins backen",
				"Bei dieser Aufgabe backt man Affenmuffins",
				"Für Affenmuffins werden Mehl, Backpulver, Kakao, Butter, Zucker, Eier und Milch benötigt.",
				LOGIN_ERNIE, "\u221e", TaskTypeEnum.NOTYPE);
		topicService.subscribe(erniesKursUuid, LOGIN_BERT);
		taskService.checkTask(affenMuffinTask, LOGIN_BERT);

	}

}
