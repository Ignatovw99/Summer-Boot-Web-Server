package controllers;

import broccolina.solet.HttpSolet;
import broccolina.solet.HttpSoletRequest;
import entities.Article;
import entities.Role;
import entities.User;
import models.binding.ArticleCreateBindingModel;
import repositories.ArticleRepository;
import repositories.UserRepository;
import server.javache.http.HttpSession;
import summer.api.*;

import java.net.http.HttpRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class ArticleController {

    private ArticleRepository articleRepository;

    private UserRepository userRepository;

    public ArticleController() {
        this.articleRepository = new ArticleRepository();
        this.userRepository = new UserRepository();
    }

    @GetMapping(route = "/articles/create")
    public String createArticle(HttpSoletRequest request) {
//        if (request.getSession() == null) {
//            return "redirect:/login";
//        }

        return "template:article-create";
    }

    @PostMapping(route = "/articles/create")
    public String confirmCreateArticle(HttpSoletRequest request, ArticleCreateBindingModel articleCreateBindingModel) {
//        if (request.getSession() == null) {
//            return "redirect:/login";
//        }

        User currentUser=null;
        if (request.getSession() != null) {
            String userId = request.getSession().getAttributes().get("user-id").toString();
            currentUser = this.userRepository.findById(userId);
        }

        Article article = new Article();
        article.setTitle(articleCreateBindingModel.getTitle());
        article.setContent(articleCreateBindingModel.getContent());
        article.setPublishedOn(LocalDateTime.now());
        if (request.getSession() != null) {
            article.setAuthor(currentUser);
        } else {
        }

        System.out.println(article.getTitle());
        this.articleRepository.save(article);
        this.articleRepository.findAll().forEach(article1 -> System.out.println("Persisted: " + article1));

        return "redirect:/home";
    }

    @GetMapping(route = "/articles/all")
    public String allArticles(HttpSoletRequest request, Model model) {
//        if (request.getSession() == null) {
//            return "redirect:/login";
//        }

        List<Article> articles = this.articleRepository.findAll();

        StringBuilder reusltHtml = new StringBuilder();

        int currentIndex = 0;

        for (Article article : articles) {
            reusltHtml
                    .append("<tr class=\"row\">")
                    .append("<th class=\"col-md-1\" scope=\"row\">".concat(String.valueOf(++currentIndex)).concat("</th>"))
                    .append("<td class=\"col-md-2\" scope=\"row\">" + article.getTitle() + "</td>")
                    .append("<td class=\"col-md-3\" scope=\"row\">" + (article.getContent().length() > 30 ? article.getContent().substring(0, 27) : article.getContent()) + "</td>")
                    .append("<td class=\"col-md-2\" scope=\"row\">" + article.getPublishedOn().format(DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy")) + "</td>")
                    .append("<td class=\"col-md-2\" scope=\"row\">" + article.getAuthor().getUsername() +"</td>")
                    .append("<td class=\"col-md-2\" scope=\"row\"><a class=\"btn btn-secondary\" href=\"/articles/details/" + article.getId() + "\">Details</a></td>")
                    .append("</tr>");
        }

        model.addAttribute("articles", reusltHtml.toString());

        return "template:article-all";
    }

    @GetMapping(route = "/articles/details/{id}")
    public String articleDetails(@PathVariable(name = "id") String id, Model model, HttpSoletRequest request) {
        if (request.getSession() == null) {
            return "redirect:/login";
        }

        System.out.println(id);
        Article article = this.articleRepository.findById(id);

        if (article == null) {
            return "redirect:/articles/all";
        }

        System.out.println(article.getTitle());

        model.addAttribute("title", article.getTitle());
        model.addAttribute("content", article.getContent());
        model.addAttribute("publishedOn", article.getPublishedOn().format(DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy")));
        model.addAttribute("author", article.getAuthor().getUsername());

        String articleAuthorId = article.getAuthor().getId();
        String currentUserId = request.getSession().getAttributes().get("user-id").toString();
        String currentUserRole = request.getSession().getAttributes().get("role").toString();

        String actionsBlock = "";

        if (articleAuthorId.equals(currentUserId) || currentUserRole.equals(Role.ADMIN.toString())) {
            actionsBlock = "<hr class=\"bg-secondary half-width\"/>" +
                    "<div class=\"actions-block mx-auto width-25 d-flex justify-content-between\">" +
                    "<a href=\"/articles/edit/" + article.getId() +"\" class=\"btn btn-secondary\">Edit</a>" +
                    "<a href=\"/articles/delete/" + article.getId() + "\" class=\"btn btn-secondary\">Delete</a>" +
                    "</div>";
        }

        model.addAttribute("actionsBlock", actionsBlock);

        return "template:article-details";
    }

    @GetMapping(route = "/articles/edit/{id}")
    public String edit(@PathVariable(name = "id") String id, Model model, HttpSoletRequest request) {
        if (request.getSession() == null) {
            return "redirect:/login";
        }

        Article article = this.articleRepository.findById(id);

        if (article == null
                || !article.getAuthor().getId().equals(request.getSession().getAttributes().get("user-id").toString())) {
            return "redirect:/articles/all";
        }

        model.addAttribute("id", article.getId());
        model.addAttribute("title", article.getTitle());
        model.addAttribute("content", article.getContent());

        return "template:article-edit";
    }

    @PostMapping(route = "/articles/edit/{id}")
    public String editConfirm(@PathVariable(name = "id") String id, HttpSoletRequest request, ArticleCreateBindingModel bindingModel) {
        if (request.getSession() == null) {
            return "redirect:/login";
        }

        Article article = this.articleRepository.findById(id);

        if (article == null
                || !article.getAuthor().getId().equals(request.getSession().getAttributes().get("user-id").toString())) {
            return "redirect:/articles/all";
        }

        article.setTitle(bindingModel.getTitle());
        article.setContent(bindingModel.getContent());

        this.articleRepository.update(article);

        return "redirect:/articles/details/".concat(article.getId());
    }

    @GetMapping(route = "/articles/delete/{id}")
    public String delete(@PathVariable(name = "id") String id, HttpSoletRequest request, Model model) {
        if (request.getSession() == null) {
            return "redirect:/login";
        }

        Article article = this.articleRepository.findById(id);

        if (article == null
                || !article.getAuthor().getId().equals(request.getSession().getAttributes().get("user-id").toString())) {
            return "redirect:/articles/all";
        }

        model.addAttribute("id", article.getId());
        model.addAttribute("title", article.getTitle());
        model.addAttribute("content", article.getContent());
        model.addAttribute("author", article.getAuthor().getUsername());
        model.addAttribute("publishedOn", article.getPublishedOn());

        return "template:article-delete";
    }

    @PostMapping(route = "/articles/delete/{id}")
    public String deleteConfim(@PathVariable(name = "id") String id, HttpSoletRequest request) {
        if (request.getSession() == null) {
            return "redirect:/login";
        }

        Article article = this.articleRepository.findById(id);

        if (article == null
                || !article.getAuthor().getId().equals(request.getSession().getAttributes().get("user-id").toString())) {
            return "redirect:/articles/all";
        }

        this.articleRepository.delete(article);

        return "redirect:/articles/all";
    }
}
