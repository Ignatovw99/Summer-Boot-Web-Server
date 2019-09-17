package app.controllers;

import app.ChuckBindingModel;
import broccolina.solet.HttpSoletRequest;
import broccolina.solet.HttpSoletResponse;
import summer.api.*;

@Controller
public class HomeController {

    @GetMapping(route = "/")
    public String index(HttpSoletResponse response, Model model, ChuckBindingModel bindingModel) {
        model.addAttribute("username", "Prakash");
        return "template:index";
    }

    @GetMapping(route = "/testme/{id}/comments/{commentId}")
    public String testme(HttpSoletRequest request, @PathVariable(name = "id") Integer id, Model model, ChuckBindingModel bindingModel, @PathVariable(name = "commentId") Integer commentId) {
        model.addAttribute("url", request.getRequestUrl());
        model.addAttribute("id", id);
        model.addAttribute("commentId", commentId);
        model.addAttribute("username", "Prakash");

        return "template:testme";
    }

    @GetMapping(route = "/chuck")
    public String chuck() {
        return "template:chuck";
    }

    @PostMapping(route = "/chuck")
    public String chuckConfirm(ChuckBindingModel model) {
        return "redirect:/";
    }
}
