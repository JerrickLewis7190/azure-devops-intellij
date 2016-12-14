// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.external.commands;

import com.microsoft.alm.plugin.external.ToolRunner;
import org.junit.Assert;
import org.junit.Test;

public class DownloadCommandTest extends AbstractCommandTest {
    @Test
    public void testConstructor_nullContext() {
        final DownloadCommand cmd = new DownloadCommand(null, "/path/localfile.txt", 12, "/path2/newfile;12.txt", false);
    }

    @Test
    public void testConstructor_withContext() {
        final DownloadCommand cmd = new DownloadCommand(context, "/path/localfile.txt", 12, "/path2/newfile;12.txt", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullArgs() {
        final DownloadCommand cmd = new DownloadCommand(null, null, 0, null, false);
    }

    @Test
    public void testGetArgumentBuilder_withContext() {
        final DownloadCommand cmd = new DownloadCommand(context, "/path/localfile.txt", 12, "/path2/newfile;12.txt", false);
        final ToolRunner.ArgumentBuilder builder = cmd.getArgumentBuilder();
        Assert.assertEquals("print -noprompt -collection:http://server:8080/tfs/defaultcollection ******** /path/localfile.txt -version:12", builder.toString());
    }

    @Test
    public void testGetArgumentBuilder_nullContext() {
        final DownloadCommand cmd = new DownloadCommand(null, "/path/localfile.txt", 12, "/path2/newfile;12.txt", false);
        final ToolRunner.ArgumentBuilder builder = cmd.getArgumentBuilder();
        Assert.assertEquals("print -noprompt /path/localfile.txt -version:12", builder.toString());
    }

    // TODO Need to mock the File usage in the parseOutput method so we can add more Unit tests
}
