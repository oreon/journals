package com.crossover.trial.journals.service;

import com.crossover.trial.journals.Application;
import com.crossover.trial.journals.model.Journal;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import it.ozimov.springboot.templating.mail.model.Email;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

/**
 * Created by jsingh on 2017-02-12.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Transactional
public class MailerServiceTest extends BaseServiceTest{

    @Autowired
    private MailerService mailerService;


    @Test
    public void sendEmailOnJournalCreationTest() {
        Journal journal = publishJournal(NEW_JOURNAL_NAME);

        //subscribe two users to the default category
        userService.subscribe(getUser("user1"), getCategory().getId());
        userService.subscribe(getUser("user2"), getCategory().getId());

        List<Email> sentMails = mailerService.sendEmailOnJournalCreation(journal);
        sentMails.forEach(x -> System.out.println(x));
        assertNotNull(sentMails);
        assertEquals(2, sentMails.size());
        sentMails.get(0).getSubject().equals(NEW_JOURNAL_NAME);
    }


    @Test
    public void  sendDailyDigestTest() {
        Journal journal = publishJournal(NEW_JOURNAL_NAME);
        Journal journal2 = publishJournal(SECOND_JOURNAL_NAME);

        List<Email> sentMails = mailerService.composeAndSendDailyDigest();

        sentMails.forEach(x -> System.out.println(x));
        assertNotNull(sentMails);
        assertEquals(2, sentMails.size());
        String mailBody = sentMails.get(0).getBody();
        assertTrue(mailBody.contains(NEW_JOURNAL_NAME));
        assertTrue(mailBody.contains(SECOND_JOURNAL_NAME));

    }

    @Test
    public void  sendDailyDigestTestNoNewArticles() {

        mailerService.setClock(Clock.fixed(Instant.EPOCH, ZoneId.systemDefault()));

        List<Email> sentMails = mailerService.composeAndSendDailyDigest();
        sentMails.forEach(x -> System.out.println(x));
        assertEquals(0, sentMails.size());

    }





}