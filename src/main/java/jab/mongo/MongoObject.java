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

package jab.mongo;

import jab.mongo.document.MongoDocument;

/**
 * Boilerplate utility Class to handle the generic assignment of MongoDocuments.
 *
 * @param <M> The MongoDocument sub-class.
 * @author Jab
 */
public abstract class MongoObject<M extends MongoDocument> {

  /** The MongoDocument storing data for the mongo object. */
  private M mongoDocument;

  /**
   * Main constructor.
   *
   * @param mongoDocument The mongo document storing data.
   * @param name The name of the mongo object.
   */
  public MongoObject(M mongoDocument, String name) {
    setMongoDocument(mongoDocument);
  }

  /** @return Returns the mongo document storing data. */
  public M getMongoDocument() {
    return this.mongoDocument;
  }

  /**
   * (Protected Method)
   * Sets the mongo document storing data.
   *
   * @param mongoDocument The MongoDocument to set.
   */
  protected void setMongoDocument(M mongoDocument) {
    this.mongoDocument = mongoDocument;
  }
}
