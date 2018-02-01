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
import com.mongodb.DBObject;
import jab.mongo.MongoCollection;

import java.util.HashMap;
import java.util.Map;

/**
 * A class designed to handle common operations of Mongo DBObjects that act as documents in a
 * DBCollection.
 *
 * @author Jab
 */
public abstract class MongoDocument {

  /** The Map that stores elements. */
  private Map<String, MongoDocumentElement> mapDocumentElements;

  /** The DBCollection storing the document. */
  private MongoCollection collection;

  /** The String identifier for the document. */
  private String fieldId;

  /**
   * Main constructor.
   *
   * @param collection The MongoCollection storing the document.
   * @param fieldId The String identifier for the document.
   */
  public MongoDocument(MongoCollection collection, String fieldId) {
    setCollection(collection);
    setFieldId(fieldId);
    mapDocumentElements = new HashMap<>();
  }

  /**
   * Adds a MongoDocumentElement to the document. This is not automatically handled when the document
   * is loaded because of the concept of sub-classing document elements. This should be handled when
   * loading the document. The elements, however will save if they are added when the document is
   * loaded.
   *
   * @param element The MongoDocumentElement being added to the document.
   */
  public void addElement(MongoDocumentElement element) {
    mapDocumentElements.put(element.getName(), element);
  }

  /**
   * Removes a given MongoDocumentElement from the document.
   *
   * @param element The MongoDocumentElement to remove.
   * @return Returns true if the document exists and is removed.
   */
  public boolean removeElement(MongoDocumentElement element) {
    return removeElement(element.getName());
  }

  /**
   * Removes a MongoDocumentElement from the document with a given String name. This also saves the
   * document.
   *
   * @param elementName The String name of the document being removed.
   * @return Returns true if the document exists and is removed.
   */
  public boolean removeElement(String elementName) {
    boolean result = this.mapDocumentElements.remove(elementName) != null;
    // If the element is contained, then process a save.
    if (result) {
      // Create a new DBObject with the document's identifier.
      DBObject object = new BasicDBObject(getFieldId(), getFieldValue());
      // Populate the main document.
      onSave(object);
      // Go through each element.
      for (String key : mapDocumentElements.keySet()) {
        // Grab the next element with the provided key.
        MongoDocumentElement element = mapDocumentElements.get(key);
        // If the element is the one we are removing.
        if (element.getName().equals(elementName)) {
          // Set the field explicitly to null.
          object.put(elementName, null);
        }
        // Else, Save it as normal.
        else {
          // Create a new DBObject to populate with the element data.
          DBObject objectElement = new BasicDBObject();
          // Populate the DBObject.
          element.onSave(objectElement);
          // Set the DBObject with the key into the main object.
          object.put(key, objectElement);
        }
      }
      // Upsert the document.
      getCollection().upsert(object, getFieldId(), this);
    }
    // If the element is not in the document at the time of attempting to remove it,
    // then this is an illegal situation. Throw the error.
    else {
      throw new IllegalArgumentException(
          "Element does not exist for instance of \""
              + getClass().getName()
              + "\": \""
              + elementName
              + "\".");
    }
    return true;
  }

  /**
   * @return Returns the String ID of the document to store. This is assigned in the constructor,
   *     and is used for the 'upsert' methodology.
   */
  public String getFieldId() {
    return this.fieldId;
  }

  /**
   * (Private method)
   *
   * <p>Sets the String ID of the document to store. This is called from the constructor, and is
   * used for the 'upsert' methodology.
   *
   * @param fieldId The String ID to set.
   */
  private void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the DBCollection storing the document.
   *
   * @param collection The DBCollection to set.
   */
  private void setCollection(MongoCollection collection) {
    this.collection = collection;
  }

  /** @return Returns the DBCollection that stores the document if saved. */
  public MongoCollection getCollection() {
    return this.collection;
  }

  /** Saves the MongoDocument with a given field to identify the document, if it already exists. */
  public void save() {
    // Create a new DBObject with the document's identifier.
    DBObject object = new BasicDBObject(getFieldId(), getFieldValue());
    // Populate the main document.
    onSave(object);
    // Save the elements.
    saveElements(object);
    // Upsert the document.
    getCollection().upsert(object, getFieldId(), this);
  }

  public void saveElements(DBObject object) {
    // Go through each element.
    for (String key : mapDocumentElements.keySet()) {
      // Grab the next element with the provided key.
      MongoDocumentElement element = mapDocumentElements.get(key);
      // Create a new DBObject to populate with the element data.
      DBObject objectElement = new BasicDBObject();
      // Populate the DBObject.
      element.onSave(objectElement);
      // Set the DBObject with the key into the main object.
      object.put(key, objectElement);
    }
  }

  /** Deletes the document from the assigned DBCollection. */
  public void delete() {
    getCollection().delete(getFieldId(), getFieldValue());
  }

  /**
   * Implemented method that loads the data for the document.
   *
   * @param object The DBObject to pass that contains the data to handle.
   */
  public abstract void onLoad(DBObject object);

  /**
   * Implemented method that saves the data from the document to the given DBObject.
   *
   * @param object The DBObject to save the data to.
   */
  public abstract void onSave(DBObject object);

  /** @return Returns the identification value for the 'upsert' methodology. */
  public abstract Object getFieldValue();
}
