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

package jab.mongo.action;

import java.util.Date;

import com.mongodb.DBObject;

import jab.mongo.MongoCollection;
import jab.mongo.document.MongoUniqueDocument;

/**
 * MongoDocument class to store and transmit actions to other servers concurrently running the same
 * data.
 *
 * @author Jab
 */
public abstract class MongoAction extends MongoUniqueDocument implements Runnable {

  private String type;
  private String[] args;
  private Date date;

  public MongoAction(MongoCollection collection, String type, String[] args) {
    super(collection);
    setType(type);
    setArguments(args);
  }

  public MongoAction(MongoCollection collection, DBObject object) {
    super(collection, object);
  }

  @Override
  public void onLoad(DBObject object) {
    setType(object.get("type").toString());
    setArguments(object.get("args").toString().split(","));
    date = (Date) object.get("timestamp");
  }

  @Override
  public void onSave(DBObject object) {
    object.put("type", getType());
    object.put("args", saveArguments());
    if (date == null) {
      date = new Date();
    }
    object.put("timestamp", date);
  }

  private void setArguments(String[] args) {
    this.args = args;
    for (int index = 0; index < args.length; index++) {
      String arg = args[index];
      if (arg.equals("_null")) {
        args[index] = (String) null;
      } else if (arg.equals("_empty")) {
        args[index] = "";
      }
    }
  }

  private String saveArguments() {
    String args = "";
    for (String arg : getArguments()) {
      if (arg == null) {
        arg = "_null";
      } else if (arg.isEmpty()) {
        arg = "_empty";
      }
      args += arg + ",";
    }
    return args.substring(0, args.length() - 1);
  }

  public void print() {
    String[] args = getArguments();
    String type = getType();
    String argsAsString = "";
    for (int index = 0; index < args.length; index++) {
      argsAsString += "[" + index + "] -> \"" + args[index] + "\"\n\t";
    }
    argsAsString = argsAsString.substring(0, argsAsString.length() - 2);
    System.out.println("MongoGangAction:\n\tType: " + type + "\n\tArguments:\n\t" + argsAsString);
  }

  public String[] getArguments() {
    return this.args;
  }

  public String getType() {
    return this.type;
  }

  private void setType(String type) {
    this.type = type;
  }

  public long getTime() {
    return this.date.getTime();
  }
}
