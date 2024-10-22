// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.git.ui.pullrequest;

import com.microsoft.alm.plugin.idea.common.resources.TfPluginBundle;
import com.microsoft.alm.plugin.idea.common.ui.common.FilteredModel;
import com.microsoft.alm.plugin.idea.common.utils.DateHelper;
import com.microsoft.alm.plugin.operations.PullRequestLookupOperation;
import com.microsoft.alm.sourcecontrol.webapi.model.GitPullRequest;
import org.apache.commons.lang.StringUtils;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.util.ArrayList;
import java.util.List;

public class PullRequestsTreeModel extends DefaultTreeModel implements FilteredModel {
    private final PRTreeNode root;
    private final PRTreeNode requestedByMeRoot;
    private final PRTreeNode assignedToMeRoot;
    private final PRTreeNode assignedToTeamRoy;
    private final ArrayList<GitPullRequest> allAssignedToTeamRoyPullRequests;
    //private final PRTreeNode assignedToTeamRoyRoot;
    private TreeSelectionModel selectionModel;
    private final List<GitPullRequest> allRequestedByMePullRequests;
    private final List<GitPullRequest> allAssignedToMePullRequests;
    private String filter;

    public PullRequestsTreeModel() {
        super(null);

        this.root = new PRTreeNode(TfPluginBundle.message(TfPluginBundle.KEY_VCS_PR_TITLE));
        setRoot(root);
        this.requestedByMeRoot = new PRTreeNode(TfPluginBundle.message(TfPluginBundle.KEY_VCS_PR_REQUESTED_BY_ME));
        root.insert(requestedByMeRoot, 0);
        this.assignedToMeRoot = new PRTreeNode(TfPluginBundle.message(TfPluginBundle.KEY_VCS_PR_ASSIGNED_TO_ME));
        root.insert(assignedToMeRoot, 1);
        this.assignedToTeamRoy = new PRTreeNode(TfPluginBundle.message(TfPluginBundle.KEY_VCS_PR_ASSIGNED_TO_TEAM_ROY));
        root.insert(assignedToTeamRoy, 2);

        allRequestedByMePullRequests = new ArrayList<GitPullRequest>();
        allAssignedToMePullRequests = new ArrayList<GitPullRequest>();
        allAssignedToTeamRoyPullRequests = new ArrayList<GitPullRequest>();

        selectionModel = new DefaultTreeSelectionModel();
        selectionModel.setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    public PRTreeNode getRequestedByMeRoot() {
        return requestedByMeRoot;
    }

    public PRTreeNode getAssignedToMeRoot() {
        return assignedToMeRoot;
    }

    public PRTreeNode getAssignedToTeamRoy() {
        return assignedToTeamRoy;
    }

    public void appendPullRequests(final List<GitPullRequest> pullRequests, final PullRequestLookupOperation.PullRequestScope scope) {
        final PRTreeNode rootNode;

        if (scope == PullRequestLookupOperation.PullRequestScope.REQUESTED_BY_ME) {
            rootNode = requestedByMeRoot;
            allRequestedByMePullRequests.addAll(pullRequests);
        } else if (scope == PullRequestLookupOperation.PullRequestScope.ASSIGNED_TO_ME){
            rootNode = assignedToMeRoot;
            allAssignedToMePullRequests.addAll(pullRequests);
        }
        else {
            rootNode = assignedToTeamRoy;
            allAssignedToTeamRoyPullRequests.addAll(pullRequests);
        }

        // filter if there is a filter else add all PRs to root
        if (hasFilter()) {
            applyFilter();
        } else {
            for (final GitPullRequest pullRequest : pullRequests) {
                rootNode.add(new PRTreeNode(pullRequest));
            }
            reload(rootNode);
        }
    }

    public void clearPullRequests() {
        requestedByMeRoot.removeAllChildren();
        allRequestedByMePullRequests.clear();
        reload(requestedByMeRoot);

        assignedToMeRoot.removeAllChildren();
        allAssignedToMePullRequests.clear();
        reload(assignedToMeRoot);
    }

    public TreeSelectionModel getSelectionModel() {
        return selectionModel;
    }


    public GitPullRequest getSelectedPullRequest() {
        final TreePath treeModel = selectionModel.getLeadSelectionPath();
        return treeModel == null ? null : ((PRTreeNode) treeModel.getLastPathComponent()).getGitPullRequest();
    }

    public void setFilter(final String filter) {
        this.filter = filter;
        applyFilter();
    }

    private void applyFilter() {
        final boolean hasFilter = hasFilter();

        // remove all nodes so no duplicates show up
        assignedToMeRoot.removeAllChildren();
        requestedByMeRoot.removeAllChildren();

        // filter on requests by me
        for (GitPullRequest pr : allRequestedByMePullRequests) {
            if (!hasFilter || nodeContainsFilter(pr)) {
                requestedByMeRoot.add(new PRTreeNode(pr));
            }
        }

        // filter on requests assigned to me
        for (GitPullRequest pr : allAssignedToMePullRequests) {
            if (!hasFilter || nodeContainsFilter(pr)) {
                assignedToMeRoot.add(new PRTreeNode(pr));
            }
        }

        // refresh tree
        reload(requestedByMeRoot);
        reload(assignedToMeRoot);
    }

    public boolean hasFilter() {
        return StringUtils.isNotEmpty(this.filter);
    }

    private boolean nodeContainsFilter(final GitPullRequest pr) {
        if (pr == null) {
            return false;
        }

        // filter on the data shown in the node view
        if (StringUtils.containsIgnoreCase(pr.getTitle(), filter) ||
                StringUtils.containsIgnoreCase(pr.getCreatedBy().getDisplayName(), filter) ||
                StringUtils.containsIgnoreCase(String.valueOf(pr.getPullRequestId()), filter) ||
                StringUtils.containsIgnoreCase(pr.getSourceRefName().replace(PRTreeCellRenderer.GIT_REFS_HEADS, ""), filter) ||
                StringUtils.containsIgnoreCase(pr.getTargetRefName().replace(PRTreeCellRenderer.GIT_REFS_HEADS, ""), filter) ||
                StringUtils.containsIgnoreCase(pr.getMergeStatus().toString(), filter) ||
                StringUtils.containsIgnoreCase(DateHelper.getFriendlyDateTimeString(pr.getCreationDate()), filter)
                ) {
            return true;
        } else {
            return false;
        }
    }
}
