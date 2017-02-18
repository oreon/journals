package com.crossover.trial.journals.service;

import com.crossover.trial.journals.model.Journal;
import it.ozimov.springboot.templating.mail.model.Email;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.util.List;

/**
 * Created by jsingh on 2017-02-13.
 */
public interface MailerService {
    List<Email> sendEmailOnJournalCreation(Journal journal);

    void sendDailyDigest();

    List<Email> composeAndSendDailyDigest();

    public void setClock(Clock clock);


}
