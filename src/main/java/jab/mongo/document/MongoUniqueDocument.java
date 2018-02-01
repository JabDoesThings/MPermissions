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

package jab.mongo.document;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import jab.mongo.MongoCollection;

import java.util.UUID;

/**
 * A MongoDocument implementation that handles documents with a Unique ID.
 *
 * @author Jab
 */
public abstract class MongoUniqueDocument extends MongoDocument {

  /** The Unique ID for the MongoDocument. */
  private UUID uniqueId;

  /**
   * New constructor.
   *
   * @param collection The MongoCollection storing the MongoDocument.
   */
  public MongoUniqueDocument(MongoCollection collection) {
    super(collection, "id");
    setUniqueId(UUID.randomUUID(), false);
  }

  /**
   * New constructor with provided Unique ID.
   *
   * @param collection The MongoCollection storing the MongoDocument.
   * @param uniqueId The Unique ID being assigned.
   */
  public MongoUniqueDocument(MongoCollection collection, UUID uniqueId) {
    super(collection, "id");
    DBObject query = new BasicDBObject("id", uniqueId.toString());
    DBCursor cursor = collection.find(query);
    if (cursor.hasNext()) {
      cursor.close();
      throw new IllegalArgumentException(
          "New Object in collection contains ID that is already in use: \""
              + uniqueId.toString()
              + "\".");
    }
    cursor.close();
    setUniqueId(uniqueId, false);
  }

  /**
   * MongoDB constructor.
   *
   * @param collection The MongoCollection storing the MongoDocument.
   * @param object The DBObject storing the data.
   */
  public MongoUniqueDocument(MongoCollection collection, DBObject object) {
    super(collection, "id");
    // Grab the ID from the object first before loading.
    UUID uniqueId = null;
    Object oUniqueId = object.get("id");
    if (oUniqueId instanceof UUID) {
      uniqueId = (UUID) oUniqueId;
    } else if (oUniqueId instanceof String) {
      uniqueId = UUID.fromString(oUniqueId.toString());
    }
    setUniqueId(uniqueId, false);
  }

  /** @return Returns the Unique ID that represents the MongoDocument. */
  public UUID getUniqueId() {
    return this.uniqueId;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the Unique ID for the MongoDocument.
   *
   * @param uniqueId The Unique ID to set.
   * @param save The flag to save the document.
   */
  public void setUniqueId(UUID uniqueId, boolean save) {
    this.uniqueId = uniqueId;
    if (save) {
      delete();
      save();
    }
  }

  @Override
  public Object getFieldValue() {
    return getUniqueId();
  }
}
