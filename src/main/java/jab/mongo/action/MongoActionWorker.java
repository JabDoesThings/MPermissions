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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

import jab.mongo.MongoCollection;

public abstract class MongoActionWorker<A extends MongoAction> extends BukkitRunnable
    implements Runnable {

  private int expireSeconds = 20;

  private static final long DELAY_TICKS = 200L;

  private List<UUID> listAuthoredActions;

  private MongoCollection collection;

  private long timeLast = -1L;

  public MongoActionWorker(MongoCollection collection) {
    setMongoCollection(collection);

    listAuthoredActions = new ArrayList<>();
  }

  private void assertIndex() {
    DBObject key = new BasicDBObject("timestamp", 1);
    DBObject val = new BasicDBObject("expireAfterSeconds", expireSeconds);
    DBCollection dbCollection = getMongoCollection().getDBCollection();
    dbCollection.dropIndexes();
    dbCollection.createIndex(key, val);
  }

  private DBObject createQuery() {
    Date date = new Date(timeLast);
    QueryBuilder builder = QueryBuilder.start();
    builder.put("timestamp").greaterThanEquals(date).get();
    return builder.get();
  }

  @Override
  public void run() {
    long timeNow = System.currentTimeMillis();
    DBCursor cursor = collection.find(createQuery());
    while (cursor.hasNext()) {
      A action = null;
      try {
        action = createActionDocument(getMongoCollection(), cursor.next());
      } catch (Exception e) {
        System.err.println("Failed to create Action (Constructor):");
        e.printStackTrace();
        continue;
      }
      // If the UUID of the MongoAction is present in this list, then skip
      // over it. MongoActions are only executed on concurrent services.
      if (listAuthoredActions.remove(action.getUniqueId())) {
        continue;
      }
      try {
        action.run();
      } catch (Exception e) {
        System.err.println("Failed to run Action:");
        e.printStackTrace();
        continue;
      }
    }
    cursor.close();
    timeLast = timeNow;
  }

  /**
   * Starts a BukkitTask, running the worker.
   *
   * @param plugin
   * @return Returns the assigned BukkitTask instance of the worker.
   */
  public void startBukkit(Plugin plugin) {
    assertIndex();
    timeLast = System.currentTimeMillis();
    runTaskTimer(plugin, 0L, DELAY_TICKS);
  }

  /** Stops the BukkitTask running, if one is running. */
  public void stopBukkit() {
    cancel();
    listAuthoredActions.clear();
    timeLast = -1L;
  }

  public MongoCollection getMongoCollection() {
    return this.collection;
  }

  private void setMongoCollection(MongoCollection collection) {
    this.collection = collection;
  }

  /**
   * Dispatches a MongoAction to the MongoCollection so that it can be executed on other services.
   *
   * @param action The MongoAction to dispatch.
   */
  public void dispatch(MongoAction action) {
    listAuthoredActions.add(action.getUniqueId());
    action.save();
  }

  public abstract A createActionDocument(MongoCollection collection, DBObject object);
}
