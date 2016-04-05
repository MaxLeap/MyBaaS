package com.maxleap.domain;


/**
 * User: yuyangning
 * Date: 6/13/14
 * Time: 5:55 PM
 */
public class LASAppEmailConfig {

  private boolean verifyEmail;
  private String pwdResetPage;
  private String pwdSucessPage;
  private String verifySucessPage;
  private String verifyPage;
  private String invalidLinkPage;


  public String getInvalidLinkPage() {
    return invalidLinkPage;
  }

  public void setInvalidLinkPage(String invalidLinkPage) {
    this.invalidLinkPage = invalidLinkPage;
  }

  public String getPwdResetPage() {
    return pwdResetPage;
  }

  public void setPwdResetPage(String pwdResetPage) {
    this.pwdResetPage = pwdResetPage;
  }

  public String getPwdSucessPage() {
    return pwdSucessPage;
  }

  public void setPwdSucessPage(String pwdSucessPage) {
    this.pwdSucessPage = pwdSucessPage;
  }

  public boolean isVerifyEmail() {
    return verifyEmail;
  }

  public void setVerifyEmail(boolean verifyEmail) {
    this.verifyEmail = verifyEmail;
  }

  public String getVerifySucessPage() {
    return verifySucessPage;
  }

  public void setVerifySucessPage(String verifySucessPage) {
    this.verifySucessPage = verifySucessPage;
  }

  public String getVerifyPage() {
    return verifyPage;
  }

  public void setVerifyPage(String verifyPage) {
    this.verifyPage = verifyPage;
  }

}
