package edu.hm.cs.katz.swt2.agenda.service.dto;

import edu.hm.cs.katz.swt2.agenda.common.StatusEnum;

/**
 * Transferobjekt für Statusinformationen zu Tasks, die spezifisch für
 * Abonnenten des Topics sind. Transferobjekte sind Schnittstellenobjekte der
 * Geschäftslogik; Sie sind nicht Teil des Modells, so dass Änderungen an den
 * Transferobjekten die Überprüfungen der Geschäftslogik nicht umgehen können.
 * 
 * @see TaskDto
 * 
 * @author Bastian Katz (mailto: bastian.katz@hm.edu)
 */
public class StatusDto implements Comparable<StatusDto> {

	private StatusEnum status = StatusEnum.NEU;
	private String comment;

	public StatusDto(String comment) {
		this.comment = comment;
	} 

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public int compareTo(StatusDto o) {
		return this.getStatus().compareTo(o.getStatus());
	}
}
