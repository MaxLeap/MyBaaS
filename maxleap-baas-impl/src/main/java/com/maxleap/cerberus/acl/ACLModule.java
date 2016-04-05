package com.maxleap.cerberus.acl;

import com.maxleap.cerberus.acl.impl.AccessControlServiceImpl;
import com.maxleap.cerberus.acl.impl.PermissionService;
import com.maxleap.cerberus.acl.spi.AccessControlService;
import com.maxleap.pandora.data.support.guice.PandoraModule;
import com.google.inject.AbstractModule;

/**
 * Created by shunlv on 16-2-3.
 */
public class ACLModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(PermissionService.class);
    bind(AccessControlService.class).to(AccessControlServiceImpl.class);
    install(new PandoraModule());
  }
}
