package com.crossover.trial.journals.service;

import com.crossover.trial.journals.model.Journal;
import com.crossover.trial.journals.model.Role;
import com.crossover.trial.journals.model.User;
import com.crossover.trial.journals.repository.CategoryRepository;
import com.crossover.trial.journals.repository.JournalRepository;
import com.crossover.trial.journals.repository.SubscriptionRepository;
import com.crossover.trial.journals.repository.UserRepository;
import com.google.common.collect.Lists;
import it.ozimov.springboot.templating.mail.model.Email;
import it.ozimov.springboot.templating.mail.model.defaultimpl.DefaultEmail;
import it.ozimov.springboot.templating.mail.service.EmailService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.nio.charset.Charset;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by jsingh on 2017-02-12.
 */
@Service
public class MailerServiceImpl implements MailerService {

    @Autowired
    JournalRepository journalRepository;

    @Autowired
    CategoryRepository categoryRepository;

    private final static Logger log = Logger.getLogger(JournalServiceImpl.class);

    private InternetAddress from = null;

    @Value("${mail.from}")
    private String fromAddress;

    @Value("${dailyMailsubject}")
    private String dailyMailsubject;

    private Clock clock = Clock.systemDefaultZone();

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;


    private Date fromLdt(LocalDateTime ldt) {
        ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
        GregorianCalendar cal = GregorianCalendar.from(zdt);
        return cal.getTime();
    }

    MailerServiceImpl() {
        try {
            from = new InternetAddress("joe@noname.com");  //hardcoded for the sake of time
        } catch (AddressException ae) {
            log.error("failed to initialize MailerService due to from Address not being correct ", ae);
        }

    }

    /**
     * Whenever a new Journal is created, send an email to the subscribers of the category the journal belongs to
     * @param journal
     * @return  list of mails
     */
    @Override
    public List<Email> sendEmailOnJournalCreation(final Journal journal) {
        List<Email> successes = new ArrayList<Email>();

        categoryRepository.findSubscriptions(journal.getCategory())
            .stream()
            .filter(s -> s.getUser().getEnabled())
            .forEach(s -> {
                        Optional<Email> newMail = composeNewJournalMail(s.getUser().getLoginName(), journal);
                        newMail.ifPresent(x -> {
                            sendMail(x) ;
                            successes.add(x);
                        });
                    }
            );

        return successes;
    }


    @Override
    @Scheduled(cron = "0 0 23 * * *")
    public void sendDailyDigest() {
        composeAndSendDailyDigest();
    }

    /**
     *
     * @return List of successfully sent emails, Collections.emptyList() if no new article was added
     */
    @Override
    public List<Email> composeAndSendDailyDigest(){

        Map<String, List<Journal>> journalsByCategory = fetchLast24HrJournals();

        //if no new articles added return
        if (journalsByCategory.keySet().isEmpty()) {
            log.info("No new journals added today");
            return Collections.emptyList();
        }


        StringBuilder stringBuilder = new StringBuilder();

        //Send journals grouped by Category - for sake of time in line composition
        // - a templating engine should be used in a real app
        journalsByCategory.keySet().forEach(
                x -> {
                    stringBuilder.append("-----" + x + "---------\r\n");
                    journalsByCategory.get(x).forEach(
                            j -> stringBuilder.append(j.getName() + "-" + j.getPublishDate() +
                                    "-" + j.getPublisher().getName() + "\r\n")
                    );
                }
        );


        return sendDailyEmails(stringBuilder);

    }

    /**
     * Send yesterday's newly added journals to all users who are not publishers
     * @param stringBuilder
     * @return
     */
    private List<Email> sendDailyEmails(StringBuilder stringBuilder) {
        List<Email> successes = new ArrayList<Email>();

        userRepository
            .findSubscribers()
            .forEach(x -> {
                Optional<Email> newMail = createMail(x.getLoginName(),
                        dailyMailsubject, stringBuilder.toString());

                newMail.ifPresent(mail -> {
                    sendMail(mail);
                    successes.add(mail);
                });
            });

        return successes;
    }

    private Map<String, List<Journal>> fetchLast24HrJournals() {
        //find the journals created in last 24 hours
        LocalDateTime now = LocalDateTime.now(getClock());
        LocalDateTime yesterday = now.minusHours(24);

        Stream<Journal> newJournals = journalRepository.findJournalsInWindow(fromLdt(yesterday),
                fromLdt(now));

        //Group them by category
        return newJournals.collect(
                Collectors.groupingBy(x -> x.getCategory().getName())
        );
    }

    private Optional<Email> composeNewJournalMail(final String userMail, final Journal journal) {
        final String subject = String.format("New Journal published - %s", journal.getName());
        final String body = String.format(" A new journal - %s in category %s has been published ",
                journal.getName(), journal.getCategory().getName());

        return createMail(userMail, subject, body);
    }

    private Optional<Email> createMail(final String userMail, final String subject, final String body) {

        try {

            log.info("Sending notification to " + userMail);
            return Optional.of(DefaultEmail.builder()
                    .from(from)
                    .to(Lists.newArrayList(new InternetAddress(userMail)))
                    .subject(subject)
                    .body(body)
                    .build()
            );


        } catch (Exception e) {
            log.error("failed composing email to " + userMail, e);
        }

        return Optional.empty();
    }

    private void sendMail(final Email email) {
        try {
            emailService.send(email);
        } catch (Exception e) {
            log.error("failed sending email to " + email.getTo().iterator().next(), e);
        }
    }

    public Clock getClock() {
        return clock;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }


}
