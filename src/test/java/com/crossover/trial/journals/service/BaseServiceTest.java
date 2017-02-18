package com.crossover.trial.journals.service;

import com.crossover.trial.journals.model.Category;
import com.crossover.trial.journals.model.Journal;
import com.crossover.trial.journals.model.Publisher;
import com.crossover.trial.journals.model.User;
import com.crossover.trial.journals.repository.CategoryRepository;
import com.crossover.trial.journals.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.fail;

/**
 * Created by jsingh on 2017-02-12.
 */
public abstract class BaseServiceTest {

    protected final static String NEW_JOURNAL_NAME = "New Journal";

    protected final static String SECOND_JOURNAL_NAME = "New Journal 2";


    @Autowired
    protected UserService userService;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected JournalService journalService;


    @Autowired
    private PublisherRepository publisherRepository;


    protected User getUser(String name) {
        Optional<User> user = userService.getUserByLoginName(name);
        if (!user.isPresent()) {
            fail("user1 doesn't exist");
        }
        return user.get();
    }

    protected Category getCategory(){
        return categoryRepository.findOne(1L);
    }

    protected Journal publishJournal(String journalName){
        User user = getUser("publisher2");
        Optional<Publisher> p = publisherRepository.findByUser(user);

        Journal journal = new Journal();
        journal.setName(journalName);
        journal.setUuid(UUID.randomUUID().toString());

        try{
            return journalService.publish(p.get(), journal, getCategory().getId());
        } catch (ServiceException e) {
            fail(e.getMessage());
        }

        return null;
    }
}
