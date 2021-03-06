package com.bics.jira.mail.model.mail;

import org.junit.Assert;
import org.junit.Test;

import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;

/**
 * JavaDoc here
 *
 * @author Victor Polischuk
 * @since 06.02.13 22:36
 */
public class MessageAdapterTest extends Assert {
    private static final String FILE_MULTIPART_SIMPLE = "MultipartSimple.txt";
    private static final String FILE_MULTIPART_REGULAR = "MultipartRegular.txt";
    private static final String FILE_TORTURE_TEST = "InfamousTortureTest.txt";
    private static final String FILE_UTF8_SUBJECT = "UTF8Subject.txt";

    @Test
    public void testGetSubject_MultipartSimple() throws MessagingException {
        MessageAdapter adapter = message(FILE_MULTIPART_SIMPLE);

        assertEquals("[MAILINATOR] Not important", adapter.getSubject());
    }

    @Test
    public void testGetPlainTextBody_MultipartSimple() throws MessagingException {
        MessageAdapter adapter = message(FILE_MULTIPART_SIMPLE);

        assertEquals("Hello World", trim(adapter.getPlainTextBody()));
    }

    @Test
    public void testGetHtmlTextBody_MultipartSimple() throws MessagingException {
        MessageAdapter adapter = message(FILE_MULTIPART_SIMPLE);

        assertEquals("<div dir=\"ltr\"><br><div>Hello World</div><div><br></div></div>", trim(adapter.getHtmlTextBody()));
    }

    @Test
    public void testGetSubject_MultipartRegular() throws MessagingException {
        MessageAdapter adapter = message(FILE_MULTIPART_REGULAR);

        assertEquals("[MAILINATOR] Fw: Confirmation of Thalys Ticketless booking - 837245850", adapter.getSubject());
    }

    @Test
    public void testGetPlainTextBody_MultipartRegular() throws MessagingException {
        MessageAdapter adapter = message(FILE_MULTIPART_REGULAR);

        assertEquals("If this email is not correctly displayed, please view the online" +
                "version.To visualise your barcode ID, you need to allow images to be displayed. " +
                "If images remain blocked, use the link above or print the barcode in the attachment.", trim(adapter.getPlainTextBody()));
    }

    @Test
    public void testGetHtmlTextBody_MultipartRegular() throws MessagingException {
        MessageAdapter adapter = message(FILE_MULTIPART_REGULAR);

        assertEquals("<html><body>" +
                "<span class=\"xfm_1631483529\"><span><br />" +
                "<br /></span></span><blockquote class=\"xfmc0\" style=\"border-left: 1px solid rgb(204, 204, 204); margin: 0px 0px 0px 0.8ex; padding-left: 1ex;\">" +
                "<span class=\"xfm_1631483529\"><span class=\"xfmc1\"> " +
                "</span></span><img alt=\"\" height=\"0\" src=\"http://rt2-t.campaigns.thalys.com/r/?id=hb3f74b1,458c6a22,1\" width=\"0\" /> " +
                "</blockquote></body></html>", trim(adapter.getHtmlTextBody()));
    }

    @Test
    public void testGetAttachments_MultipartRegular() throws MessagingException {
        MessageAdapter adapter = message(FILE_MULTIPART_REGULAR);

        assertEquals(1, adapter.getAttachments().size());

        Attachment first = adapter.getAttachments().iterator().next();

        assertEquals("barcode.jpg", first.getFileName());
        assertTrue(new ContentType("image/jpeg").match(first.getContentType()));
    }

    @Test
    public void testGetSubject_TortureTest() throws MessagingException {
        MessageAdapter adapter = message(FILE_TORTURE_TEST);

        assertEquals("Multi-media mail demonstration", adapter.getSubject());
    }

    @Test
    public void testGetPlainTextBody_TortureTest() throws MessagingException {
        MessageAdapter adapter = message(FILE_TORTURE_TEST);

        assertEquals("This is a demonstration of multi-part mail with encapsulated messages.  This" +
                "is a very complex message whose purpose it is to exercise software using the" +
                "new multi-part message standard.", trim(adapter.getPlainTextBody()));
    }

    @Test
    public void testGetHtmlTextBody_TortureTest() throws MessagingException {
        MessageAdapter adapter = message(FILE_TORTURE_TEST);

        assertEquals("", adapter.getHtmlTextBody());
    }

    @Test
    public void testGetAttachments_TortureTest() throws MessagingException {
        MessageAdapter adapter = message(FILE_TORTURE_TEST);

        assertEquals(11, adapter.getAttachments().size());
    }

    @Test
    public void testGetSubject_UTF8() throws MessagingException {
        MessageAdapter adapter = message(FILE_UTF8_SUBJECT);

        assertEquals("Instant Roaming Pricing Tool Unknown values are present in <BMB_BICS_HUB_ROAMING_ACCUMULATED_DATA_20130302100907.csv> traffic file in PROD", adapter.getSubject());
    }

    private static String trim(String str) {
        return str.replace("\r", "").replace("\n", "").replace("\t", "");
    }

    private static MessageAdapter message(String fileName) {
        InputStream stream = MessageAdapterTest.class.getClassLoader().getResourceAsStream("mail/" + fileName);

        try {
            return new MessageAdapter(new MimeMessage(null, stream));
        } catch (MessagingException e) {
            throw new AssertionError("Cannot parse given resource: " + fileName + ". " + e.getMessage());
        }
    }
}
