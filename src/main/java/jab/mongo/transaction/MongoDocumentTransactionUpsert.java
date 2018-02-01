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

package jab.mongo.transaction;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import jab.mongo.MongoCollection;
import jab.mongo.MongoDatabase;

public class MongoDocumentTransactionUpsert extends MongoDocumentTransaction {

  private DBObject object;
  private String field;
  private Object lock;

  public MongoDocumentTransactionUpsert(
      MongoCollection collection, DBObject object, String field, Object lock) {
    super(collection);
    setObject(object);
    setField(field);
    setLock(lock);
  }

  @Override
  public void run() {
    MongoCollection collection = getMongoCollection();
    DBCollection dbCollection = collection.getDBCollection();
    String field = getField();
    DBObject object = getObject();
    BasicDBObject append = new BasicDBObject();
    append.append("$set", object);
    Object id = object.get(field);
    if (MongoDatabase.DEBUG) {
      System.out.println(
          "("
              + dbCollection.getName()
              + "): Upserting document: (field:"
              + field
              + " id:"
              + id
              + ")");
    }
    dbCollection.update(new BasicDBObject(field, object.get(field)), append, true, false);
  }

  public Object getLock() {
    return this.lock;
  }

  private void setLock(Object lock) {
    this.lock = lock;
  }

  public DBObject getObject() {
    return this.object;
  }

  private void setObject(DBObject object) {
    this.object = object;
  }

  public String getField() {
    return this.field;
  }

  private void setField(String field) {
    this.field = field;
  }
}
