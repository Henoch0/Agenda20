package edu.hm.cs.katz.swt2.agenda.service.dto;

import edu.hm.cs.katz.swt2.agenda.common.TaskTypeEnum;

/**
 * Transferobjekt für Tasks mit Metadaten, die nur für Verwalter eines Tasks (d.h. Eigentümer des
 * Topics) sichtbar sind. Transferobjekte sind Schnittstellenobjekte der Geschäftslogik; Sie sind
 * nicht Teil des Modells, so dass Änderungen an den Transferobjekten die Überprüfungen der
 * Geschäftslogik nicht umgehen können.
 * 
 * @see TaskDto
 * 
 * @author Bastian Katz (mailto: bastian.katz@hm.edu)
 */
public class OwnerTaskDto extends TaskDto {
    private int finishedSubscriber;

  public OwnerTaskDto(Long id, String title, String shortDescription, String longDescription, SubscriberTopicDto topicDto, int finishedSubscriber, String deadline, TaskTypeEnum taskType) {
    super(id, title, shortDescription, longDescription, topicDto, deadline, taskType);
    this.finishedSubscriber = finishedSubscriber;
  }
  
  public int getFinishedSubscriber() {
      return finishedSubscriber;
    }

}
