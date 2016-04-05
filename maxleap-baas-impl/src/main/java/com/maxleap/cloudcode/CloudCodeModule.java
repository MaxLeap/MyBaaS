package com.maxleap.cloudcode;

import com.maxleap.cloudcode.job.CloudCodeJob;
import com.maxleap.cloudcode.job.impl.CloudCodeJobImpl;
import com.maxleap.cloudcode.job.impl.TaskAcquirer;
import com.maxleap.cloudcode.job.impl.TaskDeliver;
import com.maxleap.cloudcode.service.ZCloudCodeService;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;

/**
 * User：David Young
 * Date：16/2/1
 */
public class CloudCodeModule implements Module {

  private String cloudCodeAddr;

  public CloudCodeModule(String cloudCodeAddr) {
    this.cloudCodeAddr = cloudCodeAddr;
  }

  @Override
  public void configure(Binder binder) {
    binder.bind(String.class).annotatedWith(Names.named("cloudCodeAddr")).toInstance(cloudCodeAddr);
    binder.bind(CloudCodeRestClient.class);
    binder.bind(CCodeExecutor.class);
    binder.bind(ZCloudCodeService.class);
    binder.bind(TaskDeliver.class);
    binder.bind(TaskAcquirer.class);
    binder.bind(CloudCodeJob.class).to(CloudCodeJobImpl.class);
  }
}
