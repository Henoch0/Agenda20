package edu.hm.cs.katz.swt2.agenda.service.dto;

/**
 * Transferobjekt für Topics mit Metadaten, die nur für Verwalter eines Topics
 * (d.h. Eigentümer des Topics) sichtbar sind. Transferobjekte sind
 * Schnittstellenobjekte der Geschäftslogik; Sie sind nicht Teil des Modells, so
 * dass Änderungen an den Transferobjekten die Überprüfungen der Geschäftslogik
 * nicht umgehen können.
 * 
 * @see SubscriberTopicDto
 * 
 * @author Bastian Katz (mailto: bastian.katz@hm.edu)
 */
public class OwnerTopicDto extends SubscriberTopicDto {
    
    private Integer countSubscriber;

	public OwnerTopicDto(String uuid, UserDisplayDto user, String title, String shortDescription,
			String longDescription, Integer countSubscriber) {
		super(uuid, user, title, shortDescription, longDescription);
		this.countSubscriber = countSubscriber;
	}
	
	public Integer getCountSubscriber() {
	    return countSubscriber;
	}

	public String getKey() {
		return getUuid().substring(28);
	}
}
