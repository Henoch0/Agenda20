package edu.hm.cs.katz.swt2.agenda.service.dto;

import edu.hm.cs.katz.swt2.agenda.service.TopicServiceImpl;

/**
 * Transferobjekt für einfache Anzeigeinformationen von Anwendern.
 * Transferobjekte sind Schnittstellenobjekte der Geschäftslogik; Sie sind nicht
 * Teil des Modells, so dass Änderungen an den Transferobjekten die
 * Überprüfungen der Geschäftslogik nicht umgehen können.
 * 
 * @author Bastian Katz (mailto: bastian.katz@hm.edu)
 */
public class UserDisplayDto {

	private String login = "";

	private String name;

	private int topicCount;

	private int subscriptionCount;

	private int abonnementCount;

	public int completedTasks;

	private String password = "";

	public String getDisplayName() {
		return name;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTopicCount() {
		return topicCount;
	}

	public int getSubscriptionCount() {
		return subscriptionCount;
	}

	public void setTopicCount(int topicCount) {
		this.topicCount = topicCount;
	}

	public void setSubscriptionCount(int subscriptionCount) {
		this.subscriptionCount = subscriptionCount;
	}

	public int getcompletedTasks() {
		return completedTasks;
	}

	public void setcompletedTasks(int completedTasks) {
		this.completedTasks = completedTasks;
	}

	public int getAbonnementCount() {
		return abonnementCount;
	}

	public void setAbonnementCount(int abonnementCount) {
		this.abonnementCount = abonnementCount;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
