/*
 * Copyright 2018 Joshua Edwards
 *
 * Licensed under the Apache License, Version 2.0.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jab.bukkit.mpermissions.object;

import jab.mongo.document.MongoNode;

/**
 * Class designed to handle Node data and operations for the MPermissions plug-in.
 *
 * @author Jab
 */
public class Node {

  /** The MongoDocumentEntry to store the data. */
  private MongoNode mongoNode;

  /**
   * MongoDB constructor.
   *
   * @param mongoNode The MongoDocumentEntry storing the data.
   */
  public Node(MongoNode mongoNode) {
    setMongoDocument(mongoNode);
  }

  @Override
  public boolean equals(Object other) {
    // Our flag to return.
    boolean returned = false;
    // If the other object is a Node container object.
    if (other instanceof Node) {
      // Check the node and the flag representing it.
      returned =
          ((Node) other).getNode().equals(getNode()) && ((Node) other).getFlag() == getFlag();
    }
    // Return the result.
    return returned;
  }

  @Override
  public String toString() {
    // Stores as "node:flag".
    return getNode() + ":" + (getFlag() ? "1" : "0");
  }

  /**
   * Checks if a given Node matches this Node
   *
   * @param node The Node being tested.
   * @return Returns true if the String node matches the node in this Node
   */
  public boolean isNode(Node node) {
    return isNode(node.getNode());
  }

  /**
   * Checks if a given String node is this Node.
   *
   * @param node The String node being tested.
   * @return Returns true if the String node matches the node in this Node.
   */
  public boolean isNode(String node) {
    boolean returned = false;
    node = node.toLowerCase();
    if (node.equals(getNode())) {
      returned = true;
    }
    return returned;
  }

  /**
   * Checks if a given Node is a super-node of this Node.
   *
   * @param node The Node being tested.
   * @return Returns true if the Node is a super-node of this Node.
   */
  public boolean isSuperNode(Node node) {
    return isNode(node.getNode());
  }

  /**
   * Checks if a given String node is a super-node of this Node.
   *
   * @param node The String node being tested.
   * @return Returns true if the String node is a super-node of this Node
   */
  public boolean isSuperNode(String node) {
    // Our flag to return.
    boolean returned = false;
    // Format the argument.
    node = node.toLowerCase();
    // Check if the node contains our given node string.
    // Make sure that the node does not equal the given node.
    if (!getNode().equals(node) && getNode().contains(node)) {
      // If this is true, then it is a super-node.
      returned = true;
    }
    // Return the result.
    return returned;
  }

  /**
   * Checks if a given Node is a sub-node of this Node.
   *
   * @param node The Node being tested.
   * @return Returns true if the given Node is a sub-node of this Node.
   */
  public boolean isSubNode(Node node) {
    return isSubNode(node.getNode());
  }

  /**
   * Checks if a given String node is a sub-node of the Node.
   *
   * @param node The String node being tested.
   * @return Returns true if the given String node is a sub-node of the Node.
   */
  public boolean isSubNode(String node) {
    // Our flag to return.
    boolean returned = false;
    // Format the argument.
    node = node.toLowerCase();
    // Check if our given node string contains the node.
    // Make sure that the node does not equal the given node.
    if (!getNode().equals(node) && node.contains(getNode())) {
      // If this is true, then it is a sub-node.
      returned = true;
    }
    // Return the result.
    return returned;
  }

  /** @return Returns the String format of the Node. */
  public String getNode() {
    return getMongoDocument().getNode();
  }

  /** @return Returns the explicitly-defined Boolean flag for the node. */
  public boolean getFlag() {
    return getMongoDocument().getFlag();
  }

  /**
   * Sets the explicitly-defined Boolean flag for the node.
   *
   * @param flag The Boolean flag to set.
   * @param save The flag to save the Document.
   */
  public void setFlag(boolean flag, boolean save) {
    getMongoDocument().setFlag(flag, save);
  }

  /** @return Returns the MongoNode document. */
  public MongoNode getMongoDocument() {
    return this.mongoNode;
  }

  /**
   * Sets the MongoNode document.
   *
   * @param mongoNode The MongoNode to set.
   */
  public void setMongoDocument(MongoNode mongoNode) {
    this.mongoNode = mongoNode;
  }

  /**
   * Creates a Node from a given String node. The node should be formatted as such:
   *
   * <p>1) ("bukkit.node.example") this node will automatically be set to true for the flag.
   *
   * <p>2) ("bukkit.node.example:1) this node will be set to true.
   *
   * <p>3) ("bukkit.node.example:true) this also works. (false, and other entries will be set as
   * false)
   *
   * @param node The String node being packaged into a Node object.
   * @return Returns a Node container for the given String node. (Note: The MongoDocument for the
   *     Node will not be assigned to a MongoDocument and cannot be implicitly saved or deleted. You
   *     may however use 'onSave(DBObject)' explicitly to the MongoDocument.
   */
  public static Node fromString(String node) {
    // If the node provided is null, return null.
    if (node == null) {
      return null;
    }
    // Loaded nodes without arguments are defaulted to true.
    boolean flag = true;
    // If an argument is provided, then we grab the result. It must either be 'true'
    // or '1'.
    if (node.contains(":")) {
      // Split the node into arguments.
      String[] argSplit = node.split(":");
      // Re-assign the node variable to only the node itself.
      node = argSplit[0];
      // Set the flag if it equals 1 or true. All other entries will be flagged false.
      flag = argSplit[1].equals("1") || argSplit[1].equalsIgnoreCase("true");
    }
    // Create a temporary MongoDocument for the node.
    MongoNode mongoNode = new MongoNode(null, node, flag);
    // Create the Node object.
    return new Node(mongoNode);
  }
}
