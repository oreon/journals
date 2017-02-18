package com.crossover.trial.journals.repository;

import com.crossover.trial.journals.model.Category;
import com.crossover.trial.journals.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Subscription> findSubscriptions(Category c);
}
