package com.bics.jira.mail.handler;

import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.service.util.handler.MessageHandler;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
import com.atlassian.jira.service.util.handler.MessageHandlerExecutionMonitor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.mail.MailUtils;
import com.bics.jira.mail.IssueHelper;
import com.bics.jira.mail.IssueLookupHelper;
import com.bics.jira.mail.ModelValidator;
import com.bics.jira.mail.UserHelper;
import com.bics.jira.mail.model.mail.MessageAdapter;
import com.bics.jira.mail.model.service.ServiceDeskModel;
import org.apache.commons.lang.StringUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * JavaDoc here
 *
 * @author Victor Polischuk
 * @since 03.02.13 12:29
 */
public abstract class ServiceDeskMessageHandler<M extends ServiceDeskModel> implements MessageHandler {
    protected final JiraAuthenticationContext jiraAuthenticationContext;
    protected final AttachmentManager attachmentManager;
    protected final ModelValidator<M> modelValidator;
    protected final IssueHelper issueHelper;
    protected final UserHelper userHelper;
    protected final IssueLookupHelper issueLookupHelper;
    protected final M model;

    private final Map<String, String> params = new HashMap<String, String>();
    private boolean valid;

    public ServiceDeskMessageHandler(JiraAuthenticationContext jiraAuthenticationContext, AttachmentManager attachmentManager, ModelValidator<M> modelValidator, IssueHelper issueHelper, UserHelper userHelper, IssueLookupHelper issueLookupHelper) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.attachmentManager = attachmentManager;
        this.modelValidator = modelValidator;
        this.issueHelper = issueHelper;
        this.userHelper = userHelper;
        this.issueLookupHelper = issueLookupHelper;

        this.model = createModel();
    }

    protected abstract M createModel();

    protected abstract Predicate<ApplicationUser> searchPredicate(MessageAdapter adapter, MessageHandlerErrorCollector monitor);

    protected abstract MutableIssue findIssue(MessageAdapter adapter, MessageHandlerErrorCollector monitor);

    protected abstract ApplicationUser chooseAssignee(Collection<ApplicationUser> users, String subject);

    protected abstract MutableIssue create(ApplicationUser author, ApplicationUser assignee, MessageAdapter adapter, Collection<ApplicationUser> watchers, MessageHandlerErrorCollector monitor) throws PermissionException, MessagingException, CreateException, AttachmentException;

    @Override
    public void init(Map<String, String> params, MessageHandlerErrorCollector monitor) {
        this.params.clear();
        this.params.putAll(params);

        validateModel(monitor);
    }

    @Override
    public boolean handleMessage(Message message, MessageHandlerContext context) throws MessagingException {
        MessageHandlerExecutionMonitor monitor = context.getMonitor();

        if (!valid && !validateModel(monitor)) {
            monitor.warning("ServiceDeskWebModel did not pass the validation. Emergency exit.");
            return false;
        }

        MessageAdapter adapter = new MessageAdapter(message);

        InternetAddress[] addresses = adapter.getFrom();

        Collection<ApplicationUser> authors = userHelper.ensure(addresses, model.isCreateUsers(), model.isNotifyUsers(), monitor);

        ApplicationUser author = CollectionUtil.findFirstMatch(authors, searchPredicate(adapter, monitor));

        if (author == null && model.getReporterUser() != null) {
            monitor.warning("Message sender(s) '" + StringUtils.join(MailUtils.getSenders(message), ",")
                    + "' do not have permission to create an issue. Default reporter fallback.");

            author = model.getReporterUser();
        } else if (author == null) {
            monitor.error("Message sender(s) '" + StringUtils.join(MailUtils.getSenders(message), ",")
                    + "' do not have corresponding users in JIRA. Message will be ignored");
            return false;
        }

        ApplicationUser original = jiraAuthenticationContext.getLoggedInUser();
        jiraAuthenticationContext.setLoggedInUser(author);

        try {
            MutableIssue issue = findIssue(adapter, monitor);
            Collection<ApplicationUser> users = userHelper.ensure(adapter.getAllRecipients(), model.isCreateUsers(), model.isNotifyUsers(), monitor);

            ApplicationUser assignee = chooseAssignee(users, adapter.getSubject());

            if (issue == null) {
                create(author, assignee, adapter, users, monitor);
            } else {
                if (!userHelper.canCommentIssue(author, issue)) {
                    throw new PermissionException("User " + author.getName() + " cannot comment issue " + issue.getKey() + ".");
                }

                issueHelper.comment(issue, model.getTransitions(), adapter, users, model.isStripQuotes(), monitor);
            }
        } catch (CreateException | AttachmentException | PermissionException e) {
            monitor.error(e.getMessage());
            return false;
        } finally {
            jiraAuthenticationContext.setLoggedInUser(original);
        }

        return true;
    }

    private boolean validateModel(MessageHandlerErrorCollector monitor) {
        return (this.valid = modelValidator.populateHandlerModel(model, params, monitor));
    }
}