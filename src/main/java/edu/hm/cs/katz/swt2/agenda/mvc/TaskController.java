package edu.hm.cs.katz.swt2.agenda.mvc;

import edu.hm.cs.katz.swt2.agenda.common.TaskTypeEnum;
import edu.hm.cs.katz.swt2.agenda.service.TaskService;
import edu.hm.cs.katz.swt2.agenda.service.TopicService;
import edu.hm.cs.katz.swt2.agenda.service.dto.OwnerTaskDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.OwnerTopicDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.StatusDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.SubscriberTaskDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.TaskDto;
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

@Controller
public class TaskController extends AbstractController {

	@Autowired
	private TopicService topicService;

	@Autowired
	private TaskService taskService;

	/**
	 * Ertellt das Formular zur Erfassung eines neuen Tasks.
	 */
	@GetMapping("/topics/{uuid}/createTask")
	public String getTaskCreationView(Model model, Authentication auth, @PathVariable("uuid") String uuid) {
		OwnerTopicDto topic = topicService.getManagedTopic(uuid, auth.getName());
		model.addAttribute("topic", topic);
		TaskTypeEnum taskType = null;
		model.addAttribute("newTask", new TaskDto(null, "", "", "", topic, "", taskType));
		return "task-creation";
	}

	/**
	 * Verarbeitert die Löschung eines Tasks.
	 */
	@PostMapping("/tasks/{id}/delete")
	public String handleTaskDeletion(Authentication auth, @PathVariable("id") Long id,
			RedirectAttributes redirectAttributes) {
		TaskDto task = taskService.getManagedTask(id, auth.getName());
		taskService.deleteTask(id, auth.getName());
		redirectAttributes.addFlashAttribute("success", "Task gelöscht");
		return "redirect:/topics/" + task.getTopic().getUuid() + "/manage";
	}

	/**
	 * Verarbeitet die Erstellung eines Tasks.
	 */
	@PostMapping("/topics/{uuid}/createTask")
	public String handleTaskCreation(Model model, Authentication auth, @PathVariable("uuid") String uuid,
			@ModelAttribute("newTask") TaskDto newTask, RedirectAttributes redirectAttributes) {
		try {
			taskService.createTask(uuid, newTask.getTitle(), newTask.getShortDescription(),
					newTask.getLongDescription(), auth.getName(), newTask.getDeadline(), newTask.getTaskType());
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/topics/" + uuid + "/createTask";
		}
		redirectAttributes.addFlashAttribute("success", "Task \"" + newTask.getTitle() + "\" erstellt.");
		return "redirect:/topics/" + uuid + "/manage";
	}

	/**
	 * Erstellt die Taskansicht für Abonnenten.
	 */
	@GetMapping("tasks/{id}")
	public String getSubscriberTaskView(Model model, Authentication auth, @PathVariable("id") Long id) {
		SubscriberTaskDto task = taskService.getTask(id, auth.getName());
		model.addAttribute("status", task.getStatus());
		model.addAttribute("task", task);
		return "task";
	}

	/**
	 * Ermöglicht das Kommentieren von Tasks.
	 */
	@PostMapping("tasks/{id}")
	public String commentTask(Model model, Authentication auth, @PathVariable("id") Long id,
			@RequestHeader(value = "referer", required = true) String referer,
			@ModelAttribute("status") StatusDto status) {
		SubscriberTaskDto task = taskService.getTask(id, auth.getName());
		taskService.comment(auth.getName(), task.getId(), status.getComment());
		return "redirect:" + referer;

	}

	/**
	 * Erstellt die Taskansicht für den Verwalter/Ersteller eines Topics.
	 */
	@GetMapping("tasks/{id}/manage")
	public String getManagerTaskView(Model model, Authentication auth, @PathVariable("id") Long id) {
		OwnerTaskDto task = taskService.getManagedTask(id, auth.getName());
		List<StatusDto> status = taskService.getStatusForTask(id, auth.getName());
		model.addAttribute("task", task);
		model.addAttribute("status", status);
		return "task-management";
	}

	/**
	 * Verarbeitung die Änderung eines Tasks.
	 */
	@PostMapping("tasks/{id}/manage")
	public String handleTaskUpdate(@ModelAttribute("task") TaskDto task, Authentication auth,
			@PathVariable("id") Long id, @RequestHeader(value = "referer", required = true) String referer) {
		taskService.updateTask(id, auth.getName(), task.getShortDescription(), task.getLongDescription());
		return "redirect:" + referer;

	}

	/**
	 * Verarbeitet die Markierung eines Tasks als "Done".
	 */
	@PostMapping("tasks/{id}/check")
	public String handleTaskChecking(Authentication auth, @PathVariable("id") Long id,
			@RequestHeader(value = "referer", required = true) String referer) {
		taskService.checkTask(id, auth.getName());
		return "redirect:" + referer;
	}

	/**
	 * Verarbeitet die Markierung eines Tasks als "In Bearbeitung".
	 */

	@PostMapping("tasks/{id}/start")
	public String handleTaskProgress(Authentication auth, @PathVariable("id") Long id,
			@RequestHeader(value = "referer", required = true) String referer) {
		taskService.startTask(id, auth.getName());
		return "redirect:" + referer;
	}

	/**
	 * Verarbeitet die Markierung eines Tasks als "Zurücksetzen".
	 */
	@PostMapping("tasks/{id}/reset")
	public String handleTaskReset(Authentication auth, @PathVariable("id") Long id,
			@RequestHeader(value = "referer", required = true) String referer) {
		taskService.resetTask(id, auth.getName());
		return "redirect:" + referer;
	}

	/**
	 * Erstellt die Übersicht aller Tasks abonnierter Topics für einen Anwender.
	 */
	@GetMapping("tasks")
	public String getSubscriberTaskListView(Model model, Authentication auth,
			@RequestParam(name = "search", required = false, defaultValue = "") String search) {
		List<SubscriberTaskDto> tasks = taskService.getSubscribedTasks(auth.getName(), search);
		model.addAttribute("search", new Search());
		model.addAttribute("tasks", tasks);
		return "task-listview";
	}

}
