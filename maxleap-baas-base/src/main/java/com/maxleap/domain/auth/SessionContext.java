package com.maxleap.domain.auth;

/**
 * Created by shunlv on 15-9-18.
 */
public class SessionContext {
  public static ThreadLocal<Principal> CURRENT_PRINCIPAL = new ThreadLocal<>();

  public static void setCurrentPrincipal(Principal principal) {
    CURRENT_PRINCIPAL.set(principal);
  }

  public static void clearCurrentPrincipal() {
    CURRENT_PRINCIPAL.remove();
  }

  public static Principal getCurrentPrincipal() {
    return CURRENT_PRINCIPAL.get();
  }

  public static LASPrincipal getCurrentLASPrincipal() {
    return (LASPrincipal) CURRENT_PRINCIPAL.get();
  }
}

