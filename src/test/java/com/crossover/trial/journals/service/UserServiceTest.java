package com.crossover.trial.journals.service;

import com.crossover.trial.journals.Application;
import com.crossover.trial.journals.model.Publisher;
import com.crossover.trial.journals.model.User;
import com.crossover.trial.journals.repository.CategoryRepository;
import com.fasterxml.jackson.databind.deser.Deserializers;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * Created by jsingh on 2017-02-12.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Transactional
public class UserServiceTest extends BaseServiceTest{



    @Autowired
    private JournalService journalService;

    @Autowired
    CategoryRepository categoryRepository;

    @Test
    public void testSubscribe() {
        User user = getUser("user2");

        int originalSize = categoryRepository.findSubscriptions(getCategory()).size();

        userService.subscribe(user, getCategory().getId());

        int newSize = categoryRepository.findSubscriptions(getCategory()).size();

        assertEquals(newSize , originalSize + 1);

    }


}
