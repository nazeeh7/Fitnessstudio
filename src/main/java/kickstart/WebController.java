package kickstart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import kickstart.verkaufsbereich.controller.NachbestellungController;

@Controller
public class WebController {
	@Autowired
	private NachbestellungController nachbestellungController;

    @GetMapping("/")
    public String home(Model model) {
        // Hier können Sie Attribute hinzufügen, die in der Startseite verwendet werden sollen.
        // Beispiel: model.addAttribute("products", productService.findAll());
        return "index"; // Name der HTML-Datei für die Startseite (ohne .html)
    }
    @GetMapping("/index")
    public String homeIndex(Model model) {
        // Hier können Sie Attribute hinzufügen, die in der Startseite verwendet werden sollen.
        // Beispiel: model.addAttribute("products", productService.findAll());
        return "index"; // Name der HTML-Datei für die Startseite (ohne .html)
    }
    @GetMapping("/about")
    public String about(Model model) {
        // Hier können Sie Attribute hinzufügen, die auf der About-Seite verwendet werden sollen.
        return "about"; // Name der HTML-Datei für die About-Seite (ohne .html)
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        // Hier können Sie Attribute hinzufügen, die auf der Kontaktseite verwendet werden sollen.
        return "contact"; // Name der HTML-Datei für die Kontaktseite (ohne .html)
    }
	@GetMapping("/dashboard")
	public String dashboard() {
		return "dashboard";
	}
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/welcome")
	public String welcome(Model model) {
		if(nachbestellungController.isReorderNeeded()) 
		    model.addAttribute("hasNotifications","Es gibt Artikels, die nachbestellt werden müssen! Sehen Sie die Nachbestellungsliste Bitte!");
		    return "welcome";
	}
	@GetMapping("/login")
	public String signin() {
		return "login";
	}
	  
}
