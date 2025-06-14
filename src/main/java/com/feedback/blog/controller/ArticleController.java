package com.feedback.blog.controller;

import com.feedback.blog.model.Article;
import com.feedback.blog.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @GetMapping
    public String home(Model model) {
        List<Article> articles = articleService.findAll();
        model.addAttribute("articles", articles);
        return "index";
    }

    @GetMapping("/fetch")
    public String fetchArticles(RedirectAttributes redirectAttributes) {
        List<Article> newArticles = articleService.fetchAndSaveArticles();

        if (newArticles.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "No se encontraron nuevos artículos. Todos los artículos ya estaban guardados.");
            redirectAttributes.addFlashAttribute("messageType", "info");
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "¡Éxito! Se guardaron " + newArticles.size() + " nuevos artículos.");
            redirectAttributes.addFlashAttribute("messageType", "success");
        }

        return "redirect:/";
    }

    @GetMapping("/api")
    public String apiDocumentation() {
        return "api-docs";
    }

    @GetMapping("/api/articles")
    @ResponseBody
    public List<Article> getArticlesApi() {
        return articleService.findAll();
    }

    @GetMapping("/api/fetch")
    @ResponseBody
    public List<Article> fetchArticlesApi() {
        return articleService.fetchAndSaveArticles();
    }

    @PostMapping("/api/articles/delete")
    @ResponseBody
    public String deleteArticles() {
        articleService.deleteAll();
        return "{\"message\": \"Todos los artículos han sido eliminados\", \"status\": \"success\"}";
    }

    @GetMapping("/api/articles/count")
    @ResponseBody
    public String getArticleCount() {
        long count = articleService.findAll().size();
        return "{\"count\": " + count + ", \"status\": \"success\"}";
    }


    @GetMapping("/article/{id}")
    public String viewArticle(@PathVariable Long id, Model model) {
        try {
            Optional<Article> articleOptional = articleService.findById(id);

            if (!articleOptional.isPresent()) {
                model.addAttribute("error", "Artículo no encontrado con ID: " + id);
                return "article-detail";
            }

            model.addAttribute("article", articleOptional.get());
            return "article-detail";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el artículo: " + e.getMessage());
            return "article-detail";
        }
    }
}
