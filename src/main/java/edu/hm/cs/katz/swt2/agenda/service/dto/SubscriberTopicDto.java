package edu.hm.cs.katz.swt2.agenda.service.dto;

/**
 * Transferobjekt für einfache Anzeigeinformationen von Topics. Transferobjekte sind
 * Schnittstellenobjekte der Geschäftslogik; Sie sind nicht Teil des Modells, so dass Änderungen an
 * den Transferobjekten die Überprüfungen der Geschäftslogik nicht umgehen können.
 * 
 * @see OwnerTopicDto
 * 
 * @author Bastian Katz (mailto: bastian.katz@hm.edu)
 */
public class SubscriberTopicDto {
  private String uuid;
  private UserDisplayDto creator;
  private String title;
  private String shortDescription;
  private String longDescription;
  private int openTasks;

  /**
   * Konstruktor.
   */
  public SubscriberTopicDto(String uuid, UserDisplayDto creator, String title, String shortDescription, String longDescription) {
    this.uuid = uuid;
    this.creator = creator;
    this.title = title;
    this.shortDescription = shortDescription;
    this.longDescription = longDescription;
  }

  public String getUuid() {
    return uuid;
  }

  public UserDisplayDto getCreator() {
    return creator;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

public String getShortDescription() {
	return shortDescription;
}

public void setShortDescription(String ShortDescription) {
	this.shortDescription = ShortDescription;
}

public String getLongDescription() {
	return longDescription;
}

public void setLongDescription(String LongDescription) {
	this.longDescription = LongDescription;
}

public int getOpenTasks() {
    return openTasks;
}

public void setOpenTasks(int openTasks) {
    this.openTasks = openTasks;
}

}
