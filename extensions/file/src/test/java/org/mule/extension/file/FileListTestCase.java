/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.file;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mule.extension.file.common.api.exceptions.FileErrors.ACCESS_DENIED;
import static org.mule.extension.file.common.api.exceptions.FileErrors.ILLEGAL_PATH;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.TreeNode;
import org.mule.extension.file.common.api.exceptions.FileAccessDeniedException;
import org.mule.extension.file.common.api.exceptions.IllegalPathException;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class FileListTestCase extends FileConnectorTestCase {

  @Override
  protected String getConfigFile() {
    return "file-list-config.xml";
  }

  @Override
  protected void doSetUp() throws Exception {
    super.doSetUp();
    createTestFiles();
  }

  @Test
  public void listNotRecursive() throws Exception {
    TreeNode node = doList(".", false);
    List<TreeNode> childs = node.getChilds();

    assertThat(childs, hasSize(6));
    assertThat(assertListedFiles(childs), is(true));
  }

  @Test
  public void listRecursive() throws Exception {
    TreeNode node = doList(".", true);
    assertRecursiveTreeNode(node);
  }

  @Test
  public void listWithoutReadPermission() throws Exception {
    expectedError.expectError(NAMESPACE, ACCESS_DENIED.getType(), FileAccessDeniedException.class,
                              "access was denied by the operating system");

    temporaryFolder.newFolder("forbiddenDirectory").setReadable(false);
    doList(".", true);
  }

  private void assertRecursiveTreeNode(TreeNode node) throws Exception {
    List<TreeNode> childs = node.getChilds();

    assertThat(childs, hasSize(6));
    assertThat(assertListedFiles(childs), is(true));

    List<TreeNode> subDirectories =
        childs.stream().filter(child -> child.getAttributes().isDirectory()).collect(Collectors.toList());

    assertThat(subDirectories, hasSize(1));
    TreeNode subDirectory = subDirectories.get(0);
    assertThat(subDirectory.getChilds(), hasSize(2));
    assertThat(assertListedFiles(subDirectory.getChilds()), is(false));
  }

  @Test
  public void notDirectory() throws Exception {
    expectedError.expectError(NAMESPACE, ILLEGAL_PATH.getType(), IllegalPathException.class, "Only directories can be listed");
    doList(String.format(TEST_FILE_PATTERN, 0), false);
  }

  @Test
  public void notExistingPath() throws Exception {
    expectedError.expectError(NAMESPACE, ILLEGAL_PATH.getType(), IllegalPathException.class, "doesn't exists");
    doList(String.format("whatever", 0), false);
  }

  @Test
  public void listWithEmbeddedMatcher() throws Exception {
    TreeNode node = doList("listWithEmbeddedPredicate", ".", false);
    List<TreeNode> childs = node.getChilds();

    assertThat(childs, hasSize(2));
    assertThat(assertListedFiles(childs), is(false));
  }

  @Test
  public void listWithGlobalMatcher() throws Exception {
    TreeNode node = doList("listWithGlobalMatcher", ".", true);
    List<TreeNode> childs = node.getChilds();

    assertThat(childs, hasSize(1));

    FileAttributes file = childs.get(0).getAttributes();
    assertThat(file.isDirectory(), is(true));
    assertThat(file.getName(), equalTo(SUB_DIRECTORY_NAME));
  }

  @Test
  public void listWithoutPath() throws Exception {
    TreeNode node = (TreeNode) flowRunner("listWithoutPath").run().getMessage().getPayload().getValue();

    assertThat(node.getAttributes().getPath(), is(equalTo(temporaryFolder.getRoot().getAbsolutePath())));
    assertThat(node.getChilds(), hasSize(6));
  }

  private boolean assertListedFiles(List<TreeNode> nodes) throws Exception {
    boolean directoryWasFound = false;

    for (TreeNode node : nodes) {
      FileAttributes attributes = node.getAttributes();
      if (attributes.isDirectory()) {
        assertThat("two directories found", directoryWasFound, is(false));
        directoryWasFound = true;
        assertThat(attributes.getName(), equalTo(SUB_DIRECTORY_NAME));
      } else {
        assertThat(attributes.getName(), endsWith(".html"));
        assertThat(IOUtils.toString(node.getContent()), equalTo(CONTENT));
        assertThat(attributes.getSize(), is(new Long(CONTENT.length())));
      }
    }

    return directoryWasFound;
  }

  private TreeNode doList(String path, boolean recursive) throws Exception {
    return doList("list", path, recursive);
  }

  private TreeNode doList(String flowName, String path, boolean recursive) throws Exception {
    TreeNode node = (TreeNode) flowRunner(flowName).withVariable("path", path).withVariable("recursive", recursive).run()
        .getMessage().getPayload().getValue();

    assertThat(node, is(notNullValue()));
    assertThat(node.getContent(), is(nullValue()));

    FileAttributes attributes = node.getAttributes();
    assertThat(attributes.isDirectory(), is(true));
    assertThat(attributes.getPath(), equalTo(Paths.get(temporaryFolder.getRoot().toURI()).resolve(path).toString()));

    return node;
  }
}
