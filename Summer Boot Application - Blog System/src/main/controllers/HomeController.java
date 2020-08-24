package controllers;

import server.javache.http.HttpSession;
import summer.api.Controller;
import summer.api.GetMapping;
import summer.api.Model;

@Controller
public class HomeController {

    @GetMapping(route = "/")
    public String index() {
        return "template:index";
    }

    @GetMapping(route = "/home")
    public String home(HttpSession session, Model model) {
        if (session == null) {
            return "redirect:/login";
        }

        String username = session.getAttributes().get("username").toString();
        model.addAttribute("username", username);

        return "template:home";
    }
}
