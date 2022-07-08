package edu.hm.cs.katz.swt2.agenda.service;

/**
 * Comperator Klasse die uns ermöglicht User nach der Anzahl der abgeschlossenen Tasks zu sortieren.
 */
import java.util.Comparator;

import edu.hm.cs.katz.swt2.agenda.service.dto.UserDisplayDto;

public class CompletedTasksComperator implements Comparator<UserDisplayDto> {

	@Override
	public int compare(UserDisplayDto o1, UserDisplayDto o2) {
		// TODO Auto-generated method stub
		return o2.getcompletedTasks() - o1.getcompletedTasks();
	}

}
