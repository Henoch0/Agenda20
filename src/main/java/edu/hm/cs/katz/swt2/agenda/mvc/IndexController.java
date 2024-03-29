package edu.hm.cs.katz.swt2.agenda.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.hm.cs.katz.swt2.agenda.service.TopicService;

/**
 * Controller-Klasse für die Landing-Page. Controller reagieren auf Aufrufe von
 * URLs. Sie benennen ein View-Template (Thymeleaf-Vorlage) und stellen Daten
 * zusammen, die darin dargestellt werden. Dafür verwenden Sie Methoden der
 * Service-Schicht.
 * 
 * @author Bastian Katz (mailto: bastian.katz@hm.edu)
 */

@Controller
public class IndexController extends AbstractController {

	@Autowired
	TopicService topicService;

	/**
	 * Erstellt die Landing-Page.
	 */
	@GetMapping("/")
	public String getIndexView(Model model, Authentication auth) {
		model.addAttribute("registration", new Registration());
		return "index";
	}

	/**
	 * Erstellt die Login-Page.
	 */
	@GetMapping("/login")
	public String getLoginView() {
		return "login";
	}

	/**
	 * Verarbeitet die Registrierung eines Keys zum abonnieren eines Topics.
	 */
	@PostMapping("/register")
	public String handleRegistrationKey(@ModelAttribute("registration") Registration registration,
			RedirectAttributes redirectAttributes) {
		String uuid = "";
		String key = registration.getKey();
		try {
			uuid = topicService.getTopicUuid(key);
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/";
		}
		return "redirect:/topics/" + uuid + "/register";
	}
}
