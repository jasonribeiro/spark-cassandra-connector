package com.datastax.spark.connector.util

import scala.collection.concurrent.TrieMap

import org.apache.spark.Logging

private[connector] object SerialShutdownHooks extends Logging {

  private val hooks = new TrieMap[String, () => Unit]

  def add(name: String)(body: () => Unit): Unit = {
    hooks.put(name, body)
  }

  Runtime.getRuntime.addShutdownHook(new Thread("Serial shutdown hooks thread") {
    override def run(): Unit = {
      for ((name, task) <- hooks) {
        try {
          logDebug(s"Running shutdown hook: $name")
          task()
          logInfo(s"Successfully executed shutdown hook: $name")
        } catch {
          case exc: Throwable =>
            logError(s"Shutdown hook ($name) failed", exc)
        }
      }
    }
  })
}
