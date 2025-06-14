package com.feedback.blog.service;

import com.feedback.blog.model.Article;
import com.feedback.blog.model.Author;
import com.feedback.blog.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String API_URL = "https://api.spaceflightnewsapi.net/v4/articles/";

    public void saveArticle(Article article) {
        // Verificar si ya existe un artículo con el mismo handle
        if (article.getHandle() != null && !article.getHandle().isEmpty()) {
            Optional<Article> existingArticle = articleRepository.findByHandle(article.getHandle());
            if (existingArticle.isPresent()) {
                System.out.println("Artículo con handle '" + article.getHandle() + "' ya existe. No se guardará duplicado.");
                return; // No guardar si ya existe
            }
        }
        articleRepository.save(article);
    }

    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    public Optional<Article> findById(Long id) {
        return articleRepository.findById(id);
    }

    public List<Article> fetchArticles() {
        try {
            String response = restTemplate.getForObject(API_URL, String.class);
            System.out.println("RESPUESTA" + response);
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode resultsNode = rootNode.get("results");

            List<Article> articles = new ArrayList<>();

            if (resultsNode != null && resultsNode.isArray()) {
                for (JsonNode articleNode : resultsNode) {
                    Article article = new Article();

                    String handle = getTextValue(articleNode, "id");
                    article.setHandle(handle);

                    String title = getTextValue(articleNode, "title");
                    article.setTitle(truncateText(title, 500));

                    String url = getTextValue(articleNode, "url");
                    article.setUrl(truncateText(url, 1000));

                    String imageUrl = getTextValue(articleNode, "image_url");
                    article.setImageUrl(truncateText(imageUrl, 1000));

                    String summary = getTextValue(articleNode, "summary");
                    article.setSummary(truncateText(summary, 2000));

                    String publishedAt = getTextValue(articleNode, "published_at");
                    article.setPublishedAt(publishedAt);

                    String siteName = getTextValue(articleNode, "news_site");
                    article.setSiteName(truncateText(siteName, 200));

                    articles.add(article);
                }
            }

            return articles;
        } catch (Exception e) {
            System.err.println("Error while fetching articles: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : "";
    }

    private String truncateText(String text, int maxLength) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    public List<Article> fetchAndSaveArticles() {
        List<Article> articles = fetchArticles();
        List<Article> savedArticles = new ArrayList<>();

        for (Article article : articles) {
            try {
                // Verificar si ya existe antes de intentar guardar
                if (article.getHandle() != null && !article.getHandle().isEmpty()) {
                    Optional<Article> existingArticle = articleRepository.findByHandle(article.getHandle());
                    if (!existingArticle.isPresent()) {
                        saveArticle(article);
                        savedArticles.add(article);
                        System.out.println("Artículo guardado: " + article.getTitle());
                    } else {
                        System.out.println("Artículo ya existe (handle: " + article.getHandle() + "): " + article.getTitle());
                    }
                } else {
                    // Si no tiene handle, guardarlo de todas formas (aunque esto no debería pasar)
                    saveArticle(article);
                    savedArticles.add(article);
                    System.out.println("Artículo sin handle guardado: " + article.getTitle());
                }
            } catch (Exception e) {
                System.err.println("Error al guardar artículo '" + article.getTitle() + "': " + e.getMessage());
                // Continuar con el siguiente artículo sin interrumpir el proceso
            }
        }

        System.out.println("Proceso completado. Artículos nuevos guardados: " + savedArticles.size() + " de " + articles.size() + " obtenidos.");
        return savedArticles; // Devolver solo los artículos que se guardaron realmente
    }

    public void deleteAll() {
        articleRepository.deleteAll();
        articleRepository.flush();
    }

    public long count() {
        return articleRepository.count();
    }
}