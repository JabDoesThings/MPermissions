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

import jab.bukkit.mpermissions.mongo.MongoPermissionGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PermissionObject designed to handle permission-group data and operations for the Permissions
 * Module.
 *
 * @author Jab
 */
public class PermissionGroup extends PermissionObject<MongoPermissionGroup> {

  /** The List of PermissionUser members assigned to the group. */
  private List<PermissionUser> listPermissionUsers;
  /** The PermissionGroup parent that the group inherits permissions from. */
  private PermissionGroup parent;

  private PermissionGroup parentTemporary;

  /**
   * Load constructor.
   *
   * @param mongoDocument The MongoDocument to set.
   */
  public PermissionGroup(MongoPermissionGroup mongoDocument) {
    super(mongoDocument, "PermissionGroup");
    loadNodes(mongoDocument);
    listPermissionUsers = new ArrayList<>();
  }

  @Override
  public boolean hasPermission(String node) {
    // The flag to return. (If no Nodes are defined from the parent and the group,
    // the permission is denied)
    boolean returned = false;
    // The node to get from the parent, if the group has a parent.
    Node parentNodeSpecific = null;
    // The node to get directly from the group, if the node is defined.
    Node nodeSpecific;
    // If the group has a parent, grab this one first.
    if (hasParent()) {
      // Grab the most specific node that exists from the parent.
      parentNodeSpecific = getParent().getPermissionNode(node);
      // If the parent has a node that implicitly defines the node being tested, or
      // contains an explicit definition, use this first.
      if (parentNodeSpecific != null) {
        // Set the value to the flag defined by the node.
        returned = parentNodeSpecific.getFlag();
      }
    }
    // Next, grab the definition for the group.
    nodeSpecific = getPermissionNode(node);
    if (nodeSpecific != null) {
      // If the parent also defines this permission.
      if (parentNodeSpecific != null) {
        // If both the group node and the parent node are the same, then use the group
        // as this is a more explicit definition to override the group definition.
        // | OR |
        // If the group node is a super-node of the group node, this means that the node
        // for the group overrides the super-node, and also uses the group definition.
        if (parentNodeSpecific.equals(nodeSpecific)
            || parentNodeSpecific.isSuperNode(nodeSpecific)) {
          // Set the group flag as the returned variable.
          returned = nodeSpecific.getFlag();
        }
        // The group node will only be subjected to the parents definition at this point
        // because the group node is a super-node to the parent, which is more
        // explicitly defined.
        else {
          returned = parentNodeSpecific.getFlag();
        }
      }
      // If the parent does not define it, but the group defines it.
      else {
        // Assign the value directly.
        returned = nodeSpecific.getFlag();
      }
    } else {
      List<Node> listAllPermissionNodes = getAllPermissionNodes();
      if (listAllPermissionNodes.size() > 0) {
        Node superNode = null;
        for (Node permissionNodeNext : listAllPermissionNodes) {
          if (permissionNodeNext.isSubNode(node)) {
            if (superNode == null || superNode.isSubNode(permissionNodeNext)) {
              superNode = permissionNodeNext;
            }
          }
        }
        if (superNode != null) {
          returned = superNode.getFlag();
        }
      }
    }
    // Return the result.
    return returned;
  }

  /**
   * @return Returns a List of PermissionNodes from the parent PermissionGroup (If the
   *     PermissionGroup has a parent), along with the Permission Nodes provided in the Permission
   *     Group.
   */
  public List<Node> getAllPermissionNodes() {
    // The List to return.
    List<Node> listNodes = new ArrayList<>();
    PermissionGroup parent = getParent();
    // If the parent has definitions, grab them first to override them.
    if (parent != null) {
      listNodes.addAll(parent.getAllPermissionNodes());
    }
    boolean hasParentNodes = listNodes.size() > 0;
    if (hasParentNodes) {
      // The list contains parent Nodes. Go through each Node, and check for matches.
      for (Node node : getPermissionNodes()) {
        // Go through each parent node.
        for (Node nodeParent : listNodes) {
          // If the nodes match, then override it by removing the parent Node definition and adding
          // the child
          // Node definition.
          if (nodeParent.getNode().equalsIgnoreCase(node.getNode())) {
            listNodes.remove(nodeParent);
          }
          listNodes.add(node);
        }
      }
    } else {
      // No parent Nodes are present. Add all available nodes from the Permission Group.
      listNodes.addAll(getPermissionNodes());
    }
    // Return the result Nodes.
    return listNodes;
  }

  @Override
  public List<Node> getAllSubPermissionNodes(String superNodeAsString) {
    // Format the node argument.
    superNodeAsString = superNodeAsString.trim();
    // The List to return.
    List<Node> listNodes = new ArrayList<>();
    PermissionGroup parent = getParent();
    // If the group has a parent.
    if (parent != null) {
      // Add all of the nodes form the parent's execution of this method.
      listNodes.addAll(parent.getAllSubPermissionNodes(superNodeAsString));
    }
    // Go through group all group nodes.
    for (Node nodeNext : getPermissionNodes()) {
      // If the next node is a sub-node
      if (nodeNext.isSubNode(superNodeAsString)) {
        // If the current list (from the parent), already contains the sub-node
        //
        // (Note: Refer to collections using the method 'equals(Object other)' to
        // understand how collections identifies keys, and items in collections)
        if (listNodes.contains(nodeNext)) {
          // Remove the old Object from the list.
          listNodes.remove(nodeNext);
          // Put in the overriding sub-node.
          listNodes.add(nodeNext);
        }
      }
    }
    // Return the result List.
    return listNodes;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof PermissionGroup
        && ((PermissionGroup) other).getUniqueId().equals(getUniqueId());
  }

  /**
   * @param other The PermissionGroup to test as the parent PermissionGroup.
   * @return Returns true if the PermissionGroup is a child of the PermissionGroup given.
   */
  public boolean isChildOf(PermissionGroup other) {
    PermissionGroup parent = getParent();
    return parent != null && (parent.equals(other) || parent.isChildOf(other));
  }

  /**
   * Returns whether or not if the PermissionUser specified is in the PermissionGroup.
   *
   * @param member The PermissionUser being tested.
   * @return Returns true if the PermissionUser given is in the PermissionGroup.
   */
  public boolean hasMember(PermissionUser member) {
    return listPermissionUsers.contains(member);
  }

  /**
   * Adds a PermissionUser to the PermissionGroup.
   *
   * @param permissionUser The PermissionUser to add.
   * @param save The flag to save the Document.
   */
  public void addMember(PermissionUser permissionUser, boolean save) {
    if (!hasMember(permissionUser)) {
      listPermissionUsers.add(permissionUser);
    }
    permissionUser.setPermissionGroup(this, save);
  }

  /**
   * Removes a PermissionUser from the PermissionGroup.
   *
   * @param permissionUser The PermissionUser to remove.
   * @param save The flag to save the Document.
   */
  public void removeMember(PermissionUser permissionUser, boolean save) {
    if (hasMember(permissionUser)) {
      listPermissionUsers.remove(permissionUser);
    }
    permissionUser.setPermissionGroup(null, save);
  }

  /** @return Returns the parent PermissionGroup, if one is assigned. */
  public PermissionGroup getParent() {
    return this.parent != null ? this.parent : this.parentTemporary;
  }

  /**
   * Sets the parent PermissionGroup for the PermissionGroup.
   *
   * @param group The PermissionGroup to assign as the parent.
   * @param save The flag to save the Document.
   */
  public void setParent(PermissionGroup group, boolean save) {
    this.parent = group;
    UUID parentId = group != null ? group.getUniqueId() : null;
    getMongoDocument().setParentId(parentId, save);
  }

  /**
   * Sets the parent PermissionGroup for the PermissionGroup without affecting the MongoDocument.
   *
   * @param group The PermissionGroup to assign as the parent.
   */
  public void setTemporaryParent(PermissionGroup group) {
    this.parentTemporary = group;
  }

  /** @return Returns true if the PermissionGroup has a parent PermissionGroup. */
  public boolean hasParent() {
    return this.parent != null;
  }

  /**
   * @return Returns a List of PermissionUsers of Players who are assigned to the PermissionGroup.
   */
  public List<PermissionUser> getMembers() {
    return this.listPermissionUsers;
  }

  /** @return The UUID identifier for the group. */
  public UUID getUniqueId() {
    return getMongoDocument().getUniqueId();
  }

  /** @return Returns the String name of the PermissionGroup. */
  public String getGroupName() {
    return getMongoDocument().getGroupName();
  }

  /**
   * Sets the String name of the PermissionGroup.
   *
   * @param name The String name to set.
   * @param save The flag to save the Document.
   */
  public void setGroupName(String name, boolean save) {
    getMongoDocument().setGroupName(name, save);
  }
}
