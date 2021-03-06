package com.thrift4j.client.etcd;


import com.google.common.util.concurrent.ThreadFactoryBuilder;

import com.netflix.loadbalancer.ServerListUpdater;
import com.thrift4j.etcd.watcher.EtcdListener;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.responses.EtcdKeysResponse;

@Slf4j
public class EtcdNotificationUpdate implements ServerListUpdater {

  private static class LazyHolder {
    private static final ExecutorService DEFAULT_SERVER_LIST_UPDATE_EXECUTOR = Executors
        .newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("EtcdNotificationUpdate-%d").setDaemon(true)
                .build());

    private static final Thread SHUTDOWN_THREAD = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          DEFAULT_SERVER_LIST_UPDATE_EXECUTOR.shutdown();
          Runtime.getRuntime().removeShutdownHook(SHUTDOWN_THREAD);
        } catch (Exception e) {
        	log.error("EtcdNotificationUpdate.shutdown_thread.run", e);
        }
      }
    });

    static {
      Runtime.getRuntime().addShutdownHook(SHUTDOWN_THREAD);
    }
  }

  public EtcdNotificationUpdate(EtcdClient etcdClient, String listenPath) {
    this(etcdClient, listenPath, getDefaultRefreshExecutor());
  }

  public EtcdNotificationUpdate(EtcdClient etcdClient, String listenPath, ExecutorService refreshExecutor) {
    this.etcdClient = etcdClient;
    this.listenPath = listenPath;
    this.refreshExecutor = refreshExecutor;
  }

  public static ExecutorService getDefaultRefreshExecutor() {
    return LazyHolder.DEFAULT_SERVER_LIST_UPDATE_EXECUTOR;
  }

  private final AtomicBoolean isActive = new AtomicBoolean(false);
  private final AtomicLong lastUpdated = new AtomicLong(System.currentTimeMillis());
  private final ExecutorService refreshExecutor;
  private final EtcdClient etcdClient;
  private final String listenPath;

  @Override
  public void start(UpdateAction updateAction) {
    if (isActive.compareAndSet(false, true)) {
      try {
        EtcdResponsePromise<EtcdKeysResponse> responsePromise = etcdClient.get(listenPath).recursive().waitForChange().send();
        responsePromise.addListener(new EtcdListener(etcdClient, listenPath) {

          @Override
          protected void changeEvent() {
            refreshExecutor.submit(() -> {
              try {
                updateAction.doUpdate();
                lastUpdated.set(System.currentTimeMillis());
              } catch (Exception e) {
            	  log.error("EtcdNotificationUpdate.start.changeEvent", e);
              }
            });
          }
        });
      } catch (IOException e) {
    	  log.error("EtcdNotificationUpdate.start.outer", e);
      }
    }
  }

  @Override
  public void stop() {
    if (isActive.compareAndSet(true, false)) {
    }
  }

  @Override
  public String getLastUpdate() {
    return new Date(lastUpdated.get()).toString();
  }

  @Override
  public long getDurationSinceLastUpdateMs() {
    return System.currentTimeMillis() - lastUpdated.get();
  }

  @Override
  public int getNumberMissedCycles() {
    return 0;
  }

  @Override
  public int getCoreThreads() {
    if (isActive.get()) {
      if (refreshExecutor != null && refreshExecutor instanceof ThreadPoolExecutor) {
        return ((ThreadPoolExecutor) refreshExecutor).getCorePoolSize();
      }
    }
    return 0;
  }
}
