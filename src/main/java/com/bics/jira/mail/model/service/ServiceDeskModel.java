package com.bics.jira.mail.model.service;

import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Map;

/**
 * JavaDoc here
 *
 * @author Victor Polischuk
 * @since 03.02.13 22:29
 */
public class ServiceDeskModel {
    private ApplicationUser reporterUser;
    private long resolvedBefore;
    private Map<Status, String> transitions;
    private boolean createUsers;
    private boolean notifyUsers;
    private boolean ccWatcher;
    private boolean ccAssignee;
    private boolean stripQuotes;

    public ApplicationUser getReporterUser() {
        return reporterUser;
    }

    public void setReporterUser(ApplicationUser reporterUser) {
        this.reporterUser = reporterUser;
    }

    public long getResolvedBefore() {
        return resolvedBefore;
    }

    public void setResolvedBefore(long resolvedBefore) {
        this.resolvedBefore = resolvedBefore;
    }

    public Map<Status, String> getTransitions() {
        return transitions;
    }

    public void setTransitions(Map<Status, String> transitions) {
        this.transitions = transitions;
    }

    public boolean isCreateUsers() {
        return createUsers;
    }

    public void setCreateUsers(boolean createUsers) {
        this.createUsers = createUsers;
    }

    public boolean isNotifyUsers() {
        return notifyUsers;
    }

    public void setNotifyUsers(boolean notifyUsers) {
        this.notifyUsers = notifyUsers;
    }

    public boolean isCcWatcher() {
        return ccWatcher;
    }

    public void setCcWatcher(boolean ccWatcher) {
        this.ccWatcher = ccWatcher;
    }

    public boolean isCcAssignee() {
        return ccAssignee;
    }

    public void setCcAssignee(boolean ccAssignee) {
        this.ccAssignee = ccAssignee;
    }

    public boolean isStripQuotes() {
        return stripQuotes;
    }

    public void setStripQuotes(boolean stripQuotes) {
        this.stripQuotes = stripQuotes;
    }
}
