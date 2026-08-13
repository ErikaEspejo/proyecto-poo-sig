package edu.udistrital.sig.domain.repository;

import edu.udistrital.sig.domain.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    List<Category> findAll();

    Optional<Category> findById(String id);
}
