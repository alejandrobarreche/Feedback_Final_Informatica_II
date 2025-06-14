package com.feedback.blog.repository;

import com.feedback.blog.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article,Long> {
    /**
     * Busca un artículo por su handle único
     * @param handle el identificador único del artículo
     * @return Optional con el artículo si existe, vacío si no existe
     */
    Optional<Article> findByHandle(String handle);

    /**
     * Verifica si existe un artículo con el handle dado
     * @param handle el identificador único del artículo
     * @return true si existe, false si no existe
     */
    boolean existsByHandle(String handle);

}
