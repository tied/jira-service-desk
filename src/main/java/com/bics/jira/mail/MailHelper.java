package com.bics.jira.mail;

import com.bics.jira.mail.model.mail.MessageAdapter;

import javax.mail.MessagingException;

/**
 * JavaDoc here
 *
 * @author Victor Polischuk
 * @since 04.02.13 21:57
 */
public interface MailHelper {
    String extract(MessageAdapter message, boolean stripQuotes) throws MessagingException;
}
