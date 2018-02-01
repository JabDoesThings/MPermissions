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

import jab.bukkit.mpermissions.mongo.MongoPermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * PermissionObject to handle permission-user data and operations for the MPermissions plug-in.
 *
 * @author Jab
 */
public class PermissionUser extends PermissionObject<MongoPermissionUser> {

  /**
   * The PermissionGroup that the user is assigned to, if any. If not, the reference will be null.
   */
  private PermissionGroup permissionGroup;

  private PermissionGroup permissionGroupTemporary;

  /**
   * Main constructor.
   *
   * @param mongoDocument The MongoDocument storing the data.
   */
  public PermissionUser(MongoPermissionUser mongoDocument) {
    super(mongoDocument, "PermissionUser");
    loadNodes(mongoDocument);
  }

  @Override
  public boolean hasPermission(String node) {
    // Our returning flag result.
    boolean returned = false;
    // This will be the group's returned node for the one requested, if one is
    // defined.
    Node nodeGroup = null;
    // This will be the user's returned node for the one requested, if one is
    // defined.
    Node nodeUser;
    // Get the PermissionGroup associated with the user.
    PermissionGroup group = getPermissionGroup();
    // Check and see if the group exists.
    if (group != null) {
      // If so, then Grab the flag from the group.
      nodeGroup = group.getClosestPermissionNode(node);
    }
    // After the group comes any user-specific settings. This means that if a group
    // has a true flag for the node in question, and the user has a false flag, then
    // this means that the user overrides the group flag.
    //
    // Grab the closest permission for the user, if one exists.
    nodeUser = getClosestPermissionNode(node);
    // If the user has a Node definition affecting the one given
    if (nodeUser != null) {
      // If the group also has a definition for this node.
      if (nodeGroup != null) {
        // If they are equal, the user has authority over the group definition.
        // If the user is a sub-node of the most specific node defined for the group,
        // The user definition also has authority.
        if (nodeGroup.equals(nodeUser) || nodeUser.isSubNode(nodeGroup)) {
          // Set the user node's flag as the returned value.
          returned = nodeUser.getFlag();
        }
        // In this situation, the group definition is the most specific, being the
        // sub-node to the user, so the group node definition has the authority.
        else {
          // Set the group node's flag as the returned value.
          returned = nodeGroup.getFlag();
        }
      } else {
        // Set the user node's flag as the returned value.
        returned = nodeUser.getFlag();
      }
    }
    // In this situation, the user has no node defined for the one requested.
    else {
      // If the group has a definition for the node, while the user does not.
      // This should be the most common result, as user-specific permissions
      // are not the best practice for permissions, although it is reserved for
      // special instances or rare occasions.
      if (nodeGroup != null) {
        // Set the group node's flag as the returned value.
        returned = nodeGroup.getFlag();
      }
    }
    // Return the result flag.
    return returned;
  }

  /**
   * @return Returns true if the Player being represented by the PermissionUser is an administrator.
   */
  public boolean isAdministrator() {
    // Default to false.
    boolean returned = false;
    // Attempt to grab the Player if online.
    OfflinePlayer player = Bukkit.getPlayer(getUniqueId());
    // If the Player is not online.
    if (player == null) {
      // Grab the offline version.
      player = Bukkit.getOfflinePlayer(getUniqueId());
    }
    // If the Player is null at this point, the Player does not exist.
    //
    // Check if the Player is an administrator.
    if (player != null && player.isOp()) {
      // If so, set returned to true.
      returned = true;
    }
    // Return the result.
    return returned;
  }

  /**
   * @return Returns the PermissionGroup the user is assigned to, if any. If the user is not
   *     assigned to a group, null is returned.
   */
  public PermissionGroup getPermissionGroup() {
    return this.permissionGroup != null ? this.permissionGroup : this.permissionGroupTemporary;
  }

  /**
   * Sets the PermissionGroup that the PermissionUser is assigned to. To set the PermissionUser to
   * not have an assigned PermissionGroup, use null.
   *
   * <p>(Note: If the PermissionUser does not have a PermissionGroup assigned, the PermissionUser
   * will refer to the default PermissionGroup in the Permissions Module.
   *
   * @param permissionGroup The PermissionGroup to set.
   * @param save The flag to save the document.
   */
  public void setPermissionGroup(PermissionGroup permissionGroup, boolean save) {
    this.permissionGroup = permissionGroup;
    UUID groupId = null;
    if (permissionGroup != null) {
      groupId = permissionGroup.getUniqueId();
    }
    getMongoDocument().setGroupId(groupId, save);
  }

  public void setTemporaryPermissionGroup(PermissionGroup permissionGroup) {
    this.permissionGroupTemporary = permissionGroup;
  }

  /** @return Returns the Unique ID identifier associated with the PermissionUsers Player. */
  public UUID getUniqueId() {
    return getMongoDocument().getUniqueId();
  }

  /**
   * @return Returns the Unique ID identifier associated with the PermissionUsers PermissionGroup.
   */
  public UUID getGroupId() {
    return getMongoDocument().getGroupId();
  }

  /** @return Returns true if the PermissionUser is assigned to a PermissionGroup. */
  public boolean hasPermissionGroup() {
    return this.permissionGroup != null;
  }
}
