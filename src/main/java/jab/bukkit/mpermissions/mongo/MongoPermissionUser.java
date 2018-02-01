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

package jab.bukkit.mpermissions.mongo;

import com.mongodb.DBObject;

import jab.mongo.MongoCollection;
import jab.mongo.document.MongoUniqueNodeDocument;

import java.util.UUID;

/**
 * MongoDocument class to handle loading and storing data for PermissionUser.
 *
 * @author Jab
 */
public class MongoPermissionUser extends MongoUniqueNodeDocument {

  /** The Unique ID of the group the user is in. Null if no group is assigned. */
  private UUID groupId;

  /**
   * MongoDB constructor.
   *
   * @param collection The MongoCollection storing the document.
   * @param object The DBObject storing the data.
   */
  public MongoPermissionUser(MongoCollection collection, DBObject object) {
    super(collection, object);
    onLoad(object);
  }

  /**
   * New constructor.
   *
   * @param collection The MongoCollection storing the document.
   * @param playerId the Unique ID of the Player.
   */
  public MongoPermissionUser(MongoCollection collection, UUID playerId) {
    super(collection, playerId);
  }

  @Override
  public void onLoad(DBObject object) {
    Object oGroupId = object.get("groupId");
    if (oGroupId != null) {
      setGroupId(oGroupId.toString());
    }
  }

  @Override
  public void onSave(DBObject object) {
    String groupId = null;
    if (getGroupId() != null) {
      groupId = getGroupId().toString();
    }
    object.put("groupId", groupId);
  }

  /**
   * @return Returns the Unique ID for the PermissionGroup that the user is assigned to. Returns
   *     null if the user is not assigned to a group.
   */
  public UUID getGroupId() {
    return this.groupId;
  }

  /**
   * Sets the PermissionGroup Unique ID for the PermissionUser.
   *
   * @param groupId The Unique ID of the PermissionGroup being assigned to the user.
   * @param save Flag to save the document.
   */
  public void setGroupId(UUID groupId, boolean save) {
    this.groupId = groupId;
    if (save) save();
  }

  /**
   * (Private Method)
   *
   * <p>Sets the Unique ID for the PermissionUser.
   *
   * @param groupIdAsString The String version of the Unique ID to set.
   */
  private void setGroupId(String groupIdAsString) {
    setGroupId(UUID.fromString(groupIdAsString), false);
  }
}
