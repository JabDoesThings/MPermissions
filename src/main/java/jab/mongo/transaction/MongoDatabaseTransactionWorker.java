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

import java.util.ArrayList;
import java.util.List;

import jab.mongo.MongoDatabase;

/**
 * Runnable interface that handles ordered transactions for MongoDocuments.
 *
 * @author Jab
 */
public class MongoDatabaseTransactionWorker implements Runnable {

  /** The amount of time to sleep when idle. */
  private long sleepTimer = 10L;
  /** The MongoDatabase using the worker to execute transactions. */
  private MongoDatabase database;

  /**
   * Main constructor.
   *
   * @param database The MongoDatabase containing MongoDocumentTransactions.
   */
  public MongoDatabaseTransactionWorker(MongoDatabase database) {
    // Set the database using the worker.
    setMongoDatabase(database);
  }

  @Override
  public void run() {
    System.out.println("Starting MongoDatabase Transaction Worker...");
    // The database to work with.
    MongoDatabase database = getMongoDatabase();
    // The list to work with outside of the main list from the database.
    List<MongoDocumentTransaction> listToTransact = new ArrayList<>();
    // Loop through until the database is flagged to shut down. If there are
    // queued
    // transactions waiting to be processed during a shutdown, those will be
    // ran
    // first before exiting the loop.
    while (!database.isShutDown()
        || (database.isShutDown() && database.getTransactionQueueSize() > 0)) {
      // Clear the local list.
      listToTransact.clear();
      // Grab the current list of transactions.
      List<MongoDocumentTransaction> listTransactions = database.getTransactions();
      // Ensure that no transactions are added during the transfer of
      // transactions to
      // the local list.
      synchronized (listTransactions) {
        // Add every transaction currently on the database list to the
        // local list.
        listToTransact.addAll(listTransactions);
        // Clear the database list.
        listTransactions.clear();
      }
      // Make sure we have transactions to process.
      if (listToTransact.size() > 0) {
        // Go through each transaction.
        for (MongoDocumentTransaction transaction : listToTransact) {
          System.out.println(transaction);
          // Dispatch the transaction method to handle the operation.
          transaction.run();
        }
      }
      // Sleep to ensure not to start process resources.
      try {
        Thread.sleep(sleepTimer);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  /** @return Returns the MongoDatabase using the worker. */
  public MongoDatabase getMongoDatabase() {
    return this.database;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the MongoDatabase using the worker.
   *
   * @param database The MongoDatabase to set.
   */
  private void setMongoDatabase(MongoDatabase database) {
    this.database = database;
  }
}
