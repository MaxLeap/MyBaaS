package com.maxleap.code.impl;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by stream.
 */
class CloudCodeMetrics {

  private final MetricRegistry metricRegistry = new MetricRegistry();
  private final Counter rejectedTask = metricRegistry.counter(MetricRegistry.name("CloudCodeRejectedTask"));
  private final ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry).build();
  private final Queue<Runnable> queue;

  public CloudCodeMetrics(final BlockingQueue<Runnable> queue) {
    this.queue = queue;
    metricRegistry.register(MetricRegistry.name("CloudCodeTaskQueueSize"), (Gauge<Integer>) () -> queue.size());
  }

  int queueSize() {
    return queue.size();
  }

  Counter getRejectedTaskCount() {
    return rejectedTask;
  }

  void start() {
    reporter.start(1, TimeUnit.MINUTES);
  }

  void stop() {
    reporter.stop();
  }


}
