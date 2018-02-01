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

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import jab.mongo.transaction.MongoDocumentTransactionDelete;
import jab.mongo.transaction.MongoDocumentTransactionUpsert;

/**
 * TODO: Document
 *
 * @author Jab
 */
public class MongoCollection {

  /** The MongoDatabase to call to */
  private MongoDatabase database;
  /** The actual DBCollection in the MongoDB API. */
  private DBCollection collection;

  /**
   * Main constructor.
   *
   * @param database The MongoDatabase storing the DBCollection.
   * @param collection The DBCollection that is the actual collection.
   */
  public MongoCollection(MongoDatabase database, DBCollection collection) {
    setMongoDatabase(database);
    setCollection(collection);
  }

  public void upsert(DBObject object, String field, Object lock) {
    MongoDocumentTransactionUpsert upsert =
        new MongoDocumentTransactionUpsert(this, object, field, lock);
    getDatabase().addTransaction(upsert);
  }

  public void delete(String field, Object value) {
    MongoDocumentTransactionDelete delete = new MongoDocumentTransactionDelete(this, field, value);
    getDatabase().addTransaction(delete);
  }

  public DBCursor find() {
    return getDBCollection().find();
  }

  public DBCursor find(DBObject query) {
    return getDBCollection().find(query);
  }

  public void rename(String newName) {
    getDBCollection().rename(newName);
  }

  public DBCollection getDBCollection() {
    return this.collection;
  }

  private void setCollection(DBCollection collection) {
    this.collection = collection;
  }

  public MongoDatabase getDatabase() {
    return this.database;
  }

  private void setMongoDatabase(MongoDatabase database) {
    this.database = database;
  }
}
