package com.sport.ecommerce.modules.category.repository;

import com.sport.ecommerce.modules.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.name ASC")
    List<Category> findAllRootCategories();

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL ORDER BY c.name ASC")
    List<Category> findAllRootCategoriesWithChildren();

    List<Category> findByParentId(Long parentId);

    boolean existsByName(String name);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
