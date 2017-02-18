package com.crossover.trial.journals.repository;

import com.crossover.trial.journals.model.Category;
import com.crossover.trial.journals.model.Subscription;
import com.crossover.trial.journals.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by jsingh on 2017-02-12.
 */
public interface  SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Category> findByCategory(Category category);

}
