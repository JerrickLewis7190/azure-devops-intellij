// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.common.ui.workitem;

import com.microsoft.alm.common.utils.UrlHelper;
import com.microsoft.alm.plugin.idea.common.resources.TfPluginBundle;
import com.microsoft.alm.workitemtracking.webapi.models.WorkItem;
import com.microsoft.alm.workitemtracking.webapi.models.WorkItemRelation;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkItemHelper {
    public static final String FIELD_ASSIGNED_TO = "System.AssignedTo";
    public static final String FIELD_ID = "System.Id";
    public static final String FIELD_STATE = "System.State";
    public static final String FIELD_TITLE = "System.Title";
    public static final String FIELD_WORK_ITEM_TYPE = "System.WorkItemType";

    public static final String BRANCH_ATTRIBUTE_NAME = "name";
    public static final String BRANCH_ATTRIBUTE_VALUE = "branch";

    public static String getAssignedToMeQuery() {
        return "select [System.Id], [System.WorkItemType], [System.Title], [System.AssignedTo], [System.State], [System.Tags], [System.IterationPath] from WorkItems where [System.TeamProject] = 'Xbox' and [System.IterationPath] = @currentIteration('[Xbox]\\Team RoyHer <id:daa35181-9a74-4986-95f1-f32c06d64056>') and [System.AssignedTo] = 'Jerrick Lewis🎮 <jerl@microsoft.com>' and [System.WorkItemType] in ('Task', 'Bug') and [System.State] <> 'Completed' and [System.State] <> 'Closed' and [System.State] <> 'Resolved'";
    }

    public static List<String> getDefaultFields() {
        final List<String> fields = new ArrayList<String>(4);
        fields.add(FIELD_ASSIGNED_TO);
        fields.add(FIELD_ID);
        fields.add(FIELD_STATE);
        fields.add(FIELD_TITLE);
        fields.add(FIELD_WORK_ITEM_TYPE);
        return fields;
    }

    public static String getFieldValue(@NotNull final WorkItem item, @NotNull final String fieldName) {
        final HashMap<String, Object> fieldMap = item.getFields();
        if (fieldMap != null) {
            // Try a case sensitive search using the Map,
            // but if that doesn't work, loop through all the fields
            if (fieldMap.containsKey(fieldName)) {
                Object value = fieldMap.get(fieldName);
                if (value != null) {
                    return value.toString();
                }
            } else {
                for (final Map.Entry<String, Object> entry : fieldMap.entrySet()) {
                    if (fieldName.equalsIgnoreCase(entry.getKey())) {
                        if (entry.getValue() != null) {
                            return entry.getValue().toString();
                        }
                    }
                }
            }
        }
        return StringUtils.EMPTY;
    }

    public static String getRelationUrl(@NotNull final WorkItem item, @NotNull final String attributeName, @NotNull final String attributeValue) {
        final List<WorkItemRelation> relationsList = item.getRelations();
        if (relationsList != null) {
            for (WorkItemRelation relation : relationsList) {
                final Map<String, Object> attributes = relation.getAttributes();
                if (attributes != null && attributes.containsKey(attributeName) && attributeValue.equalsIgnoreCase(attributes.get(attributeName).toString())) {
                    return relation.getUrl();
                }
            }
        }
        return StringUtils.EMPTY;
    }

    public static String getLocalizedFieldName(final String wellKnownFieldName) {
        if (FIELD_ASSIGNED_TO.equalsIgnoreCase(wellKnownFieldName)) {
            return TfPluginBundle.message(TfPluginBundle.KEY_WIT_FIELD_ASSIGNED_TO);
        } else if (FIELD_ID.equalsIgnoreCase(wellKnownFieldName)) {
            return TfPluginBundle.message(TfPluginBundle.KEY_WIT_FIELD_ID);
        } else if (FIELD_STATE.equalsIgnoreCase(wellKnownFieldName)) {
            return TfPluginBundle.message(TfPluginBundle.KEY_WIT_FIELD_STATE);
        } else if (FIELD_TITLE.equalsIgnoreCase(wellKnownFieldName)) {
            return TfPluginBundle.message(TfPluginBundle.KEY_WIT_FIELD_TITLE);
        } else if (BRANCH_ATTRIBUTE_VALUE.equalsIgnoreCase(wellKnownFieldName)) {
            return TfPluginBundle.message(TfPluginBundle.KEY_WIT_FIELD_BRANCH);
        } else if (FIELD_WORK_ITEM_TYPE.equalsIgnoreCase(wellKnownFieldName)) {
            return TfPluginBundle.message(TfPluginBundle.KEY_WIT_FIELD_WORK_ITEM_TYPE);
        }

        return StringUtils.EMPTY;
    }

    public static String getWorkItemCommitMessage(final WorkItem item) {
        final String num = getFieldValue(item, FIELD_ID);
        final String type = getFieldValue(item, FIELD_WORK_ITEM_TYPE);
        final String title = getFieldValue(item, FIELD_TITLE);
        return TfPluginBundle.message(TfPluginBundle.KEY_WIT_SELECT_DIALOG_COMMIT_MESSAGE_FORMAT, type, num, title);
    }

    public static String getBranchName(final WorkItem item) {
        return UrlHelper.parseUriForBranch(getRelationUrl(item, BRANCH_ATTRIBUTE_NAME, BRANCH_ATTRIBUTE_VALUE));
    }
}
