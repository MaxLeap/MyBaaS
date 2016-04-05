package com.maxleap.cerberus.acl;

import com.maxleap.domain.auth.PermissionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Permission {
  String permisson() default "";

  PermissionType[] type() default PermissionType.SYS_ADMIN;

  String name();
}