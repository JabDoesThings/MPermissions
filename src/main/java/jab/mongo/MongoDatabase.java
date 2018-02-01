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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import jab.mongo.transaction.MongoDatabaseTransactionWorker;
import jab.mongo.transaction.MongoDocumentTransaction;

public abstract class MongoDatabase {

  public static boolean DEBUG = false;

  private volatile boolean shutdown = false;

  private DB db;
  private MongoClient client = null;
  private List<MongoDocumentTransaction> listTransactions;

  private MongoDatabaseTransactionWorker worker;

  public MongoDatabase() {
    listTransactions = new ArrayList<>();
    worker = new MongoDatabaseTransactionWorker(this);
  }

  /**
   * Pre-connection constructor. Automatically fires 'onConnection()'.
   *
   * @param client The pre-connected MongoClient connection.
   */
  public MongoDatabase(MongoClient client) {
    listTransactions = new ArrayList<>();
    worker = new MongoDatabaseTransactionWorker(this);
    connect(client);
  }

  /**
   * @param url The URL of the MongoDB.
   * @param username (Optional) The account username.
   * @param password (Optional) The account password.
   * @param database (Optional) The account database.
   */
  public void connect(
      @Nonnull String url,
      @Nullable String username,
      @Nullable String password,
      @Nullable String database) {
    String accountEntry = "";
    String databaseEntry = "";
    if (username != null) {
      accountEntry = username;
      if (password != null) {
        accountEntry += ":" + password;
      }
      databaseEntry = "admin";
      if (database != null) {
        databaseEntry = database;
      }
      accountEntry += "@";
      databaseEntry = "/" + databaseEntry;
    }
    String mongoURL = "mongodb://" + accountEntry + url + databaseEntry;
    connect(mongoURL);
  }

  public void connect(String url) {
    if (client != null) {
      client.close();
    }
    client = new MongoClient(new MongoClientURI(url));
    onConnection(client);
    // Start the worker thread.
    (new Thread(worker)).start();
  }

  public void connect(MongoClient client) {
    setClient(client);
    onConnection(client);
    // Start the worker thread.
    (new Thread(worker)).start();
  }

  public MongoClient getClient() {
    return this.client;
  }

  private void setClient(MongoClient client) {
    this.client = client;
  }

  public void addTransaction(MongoDocumentTransaction transaction) {
    // Run this operation outside of the main thread.
    (new Thread(
            new Runnable() {
              @Override
              public void run() {
                synchronized (listTransactions) {
                  listTransactions.add(transaction);
                }
              }
            }))
        .start();
  }

  public int getTransactionQueueSize() {
    synchronized (listTransactions) {
      return listTransactions.size();
    }
  }

  public MongoCollection createMongoCollection(String name) {
    return new MongoCollection(this, getDatabase().getCollection(name));
  }

  public void shutDown() {
    onShutDown();
    setShutDown(true);
  }

  public boolean isShutDown() {
    return this.shutdown;
  }

  private void setShutDown(boolean flag) {
    this.shutdown = flag;
  }

  public List<MongoDocumentTransaction> getTransactions() {
    return this.listTransactions;
  }

  public DB getDatabase() {
    return this.db;
  }

  public void setDatabase(DB db) {
    this.db = db;
  }

  public abstract void reset();

  public abstract void onConnection(MongoClient client);

  public abstract void onShutDown();

  public static void setMongoDBLogger(Level level) {
    Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
    mongoLogger.setLevel(level);
  }
}
