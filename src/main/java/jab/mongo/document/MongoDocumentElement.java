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

import com.mongodb.DBObject;

/**
 * Class designed to handle partial MongoDocument information in a more organized fashion.
 *
 * @author Jab
 */
public abstract class MongoDocumentElement {

  /** The String name of the element. */
  private String name;

  /** The MongoDocument using the element. */
  private MongoDocument mongoDocument;

  /**
   * Main constructor.
   *
   * @param mongoDocument The MongoDocument parent.
   * @param name The name of the element.
   */
  public MongoDocumentElement(MongoDocument mongoDocument, String name) {
    setName(name);
    setMongoDocument(mongoDocument);
  }

  /** @return Returns the MongoDocument storing the element. */
  public MongoDocument getMongoDocument() {
    return this.mongoDocument;
  }

  /**
   * Sets the MongoDocument storing the element.
   *
   * @param mongoDocument The MongoDocument to set.
   */
  public void setMongoDocument(MongoDocument mongoDocument) {
    this.mongoDocument = mongoDocument;
  }

  /** @return Returns the String name of the element. */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the String name of the element.
   *
   * @param name The String name to set.
   */
  public void setName(String name) {
    this.name = name;
  }

  /** @return Returns whether or not the MongoDocumentElement has an assigned MongoDocument. */
  public boolean hasMongoDocument() {
    return getMongoDocument() != null;
  }

  /**
   * Saves the MongoDocument that contains the element.
   *
   * <p>(Note: If the MongoDocumentElement does not have an assigned MongoDocument, an
   * IllegalStateException is thrown.)
   */
  public void save() {
    // Validate that the entry has an assigned MongoDocument.
    if (!hasMongoDocument()) {
      throw new IllegalStateException(
          "Attempting to save a document entry that does not have an assigned document."
              + " This can be due to a temporary object being called to save."
              + " use 'hasMongoDocument()' before calling this method.");
    }
    // Save the document with the entry.
    getMongoDocument().save();
  }

  /**
   * Deletes the MongoDocumentElement from the assigned MongoDocument, and saves the MongoDocument.
   *
   * <p>(Note: If the MongoDocumentElement does not have an assigned MongoDocument, an
   * IllegalStateException is thrown.)
   */
  public void delete() {
    // Validate that the entry has an assigned MongoDocument.
    if (!hasMongoDocument()) {
      throw new IllegalStateException(
          "Attempting to delete a document entry that does not have an assigned document."
              + " This can be due to a temporary object being called to save."
              + " use 'hasMongoDocument()' before calling this method.");
    }
    // Remove the element, saving the document in the process.
    getMongoDocument().removeElement(this);
  }

  /**
   * Loads the MongoDocumentElement, handing the DBObject representation to the method.
   *
   * @param object The DBObject representing the MongoDocumentElement.
   */
  public abstract void onLoad(DBObject object);

  /**
   * Stores the MongoDocumentElement into a given DBObject that is created for this purpose.
   *
   * @param object The DBObject to store into.
   */
  public abstract void onSave(DBObject object);
}
