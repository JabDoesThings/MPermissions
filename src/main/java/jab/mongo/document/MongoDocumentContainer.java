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

/**
 * Simple class designed to handle simple storage of the contained document, and useful for batched
 * handling of generic containers.
 *
 * @author Jab
 * @param <M> The MongoDocument being contained.
 */
public class MongoDocumentContainer<M extends MongoDocument> {

  /** The MongoDocument being contained. */
  private M mongoDocument;

  /**
   * Main constructor.
   *
   * @param mongoDocument The MongoDocument being contained.
   */
  public MongoDocumentContainer(M mongoDocument) {
    // Set the MongoDocument.
    setMongoDocument(mongoDocument);
  }

  /** @return Returns the MongoDocument representing this Object. */
  public M getMongoDocument() {
    return this.mongoDocument;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the MongoDocument representing this Object.
   *
   * @param mongoDocument The MongoDocument to set.
   */
  private void setMongoDocument(M mongoDocument) {
    this.mongoDocument = mongoDocument;
  }

  public void save() {
    getMongoDocument().save();
  }

  public void delete() {
    getMongoDocument().delete();
  }
}
