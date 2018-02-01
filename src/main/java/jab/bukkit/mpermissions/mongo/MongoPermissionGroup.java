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
 * MongoDocument designed to store and process data for PermissionGroup.
 *
 * @author Jab
 */
public class MongoPermissionGroup extends MongoUniqueNodeDocument {

  /** The parent's Unique ID for the group. */
  private UUID parentId;
  /** The String name of the group. */
  private String name;

  /**
   * MongoDB constructor.
   *
   * @param collection The MongoCollection storing the MongoDocument.
   * @param object The DBObject storing the data.
   */
  public MongoPermissionGroup(MongoCollection collection, DBObject object) {
    super(collection, object);
    onLoad(object);
  }

  /**
   * New constructor.
   *
   * @param collection The MongoCollection storing the MongoDocument.
   * @param groupName The String name of the group.
   */
  public MongoPermissionGroup(MongoCollection collection, String groupName) {
    super(collection);
    setGroupName(groupName, false);
  }

  @Override
  public void onLoad(DBObject object) {
    Object oParentId = object.get("parentId");
    if (oParentId != null) {
      setParentId(oParentId.toString());
    }
    setGroupName(object.get("name").toString(), false);
  }

  @Override
  public void onSave(DBObject object) {
    object.put("name", getGroupName());
    String parentIdAsString = null;
    UUID parentId = getParentId();
    if (parentId != null) {
      parentIdAsString = parentId.toString();
    }
    object.put("parentId", parentIdAsString);
  }

  /**
   * @return Returns the Unique ID of the Parent PermissionGroup. Returns null if the group has no
   *     parent.
   */
  public UUID getParentId() {
    return this.parentId;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the parent Unique ID for the PermissionGroup.
   *
   * @param uniqueIdAsString The String representation of the Unique ID.
   */
  private void setParentId(String uniqueIdAsString) {
    setParentId(UUID.fromString(uniqueIdAsString), false);
  }

  /**
   * Sets the parent Unique ID for the PermissionGroup.
   *
   * @param parentId The Unique ID of the parent PermissionGroup.
   * @param save Flag to save the document after changing the Unique ID.
   */
  public void setParentId(UUID parentId, boolean save) {
    this.parentId = parentId;
    if (save) {
      save();
    }
  }

  /** @return Returns the group name of the PermissionGroup. */
  public String getGroupName() {
    return this.name;
  }

  /**
   * Sets the String name of the PermissionGroup.
   *
   * @param name The String name to set for the PermissionGroup.
   * @param save Flag to save the document after changing the name.
   */
  public void setGroupName(String name, boolean save) {
    this.name = name;
    if (save) {
      save();
    }
  }
}
