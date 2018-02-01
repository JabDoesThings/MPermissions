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
import jab.mongo.MongoCollection;
import jab.mongo.MongoDatabase;

public class MongoDocumentTransactionDelete extends MongoDocumentTransaction {

  private String field;
  private Object value;

  public MongoDocumentTransactionDelete(MongoCollection collection, String field, Object value) {
    super(collection);
    setField(field);
    setValue(value);
  }

  @Override
  public void run() {
    MongoCollection collection = getMongoCollection();
    DBCollection dbCollection = collection.getDBCollection();
    String field = getField();
    Object value = getValue();
    if (MongoDatabase.DEBUG) {
      System.out.println(
          "("
              + dbCollection.getName()
              + "): Deleting Document (Field:"
              + field
              + " Value:"
              + value
              + ")");
    }
    dbCollection.remove(new BasicDBObject(field, value));
  }

  public String getField() {
    return this.field;
  }

  private void setField(String field) {
    this.field = field;
  }

  public Object getValue() {
    return this.value;
  }

  private void setValue(Object value) {
    this.value = value;
  }
}
