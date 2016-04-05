package com.maxleap.exception;

import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Map;

public class LASException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Internal server error. No information available.
   */
  public static final int INTERNAL_SERVER_ERROR = 1;

  /**
   * Third party error. No information available.
   */
  public static final int THIRD_PARTY_ERROR = 2;

  public static final int CUSTOM_INVALID_PARAMETER = 10;

  /**
   * The connection to the ZCloud servers failed
   */
  public static final int CONNECTION_FAILED = 100;

  /**
   * Object doesn't exist, or has an incorrect password
   */
  public static final int OBJECT_NOT_FOUND = 101;

  /**
   * Query syntax error
   */
  public static final int INVALID_QUERY = 102;

  /**
   * Missing or invalid classname. Classnames are case-sensitive. They must start with a letter, and a-zA-Z0-9_ are the only valid characters
   */
  public static final int INVALID_CLASS_NAME = 103;

  /**
   * Missing ObjectId, usually the objectId no introduction in the query time, or objectId is illegal. ObjectId string can only letters, numbers.
   */
  public static final int MISSING_OBJECT_ID = 104;

  /**
   * <p>Key is reserved. objectId, createdAt, updatedAt.</p>
   * <p>Invalid key name. Keys are case-sensitive. They must start with a letter, and a-zA-Z0-9_ are the only valid characters</p>
   */
  public static final int INVALID_KEY_NAME = 105;

  /**
   * <p>invalid format. Date, Pointer, Relation...</p>
   */
  public static final int INVALID_TYPE = 106;

  /**
   * Json invalid
   */
  public static final int INVALID_JSON = 107;

  /**
   * Tried to access a feature only available internally
   */
  public static final int COMMAND_UNAVAILABLE = 108;

  public static final int NOT_INITIALIZED = 109;

  /**
   * Update syntax error
   */
  public static final int INVALID_UPDATE = 110;

  /**
   * <p>Type mismatch<p/>
   */
  public static final int INCORRECT_TYPE = 111;

  public static final int INVALID_CHANNEL_NAME = 112;


  /**
   * BindTo class not found.
   */
  public static final int BIND_TO_CLASS_NOT_FOUND = 113;

  public static final int PUSH_MISCONFIGURED = 115;

  /**
   * The object is too large.
   */
  public static final int OBJECT_TOO_LARGE = 116;

  /**
   * The parameters is invalid.
   */
  public static final int INVALID_PARAMETER = 117;

  /**
   * <p>Invalid object id.</p>
   */
  public static final int INVALID_OBJECT_ID = 118;

  public static final int OPERATION_FORBIDDEN = 119;
  public static final int CACHE_MISS = 120;
  public static final int INVALID_NESTED_KEY = 121;
  public static final int INVALID_FILE_NAME = 122;
  public static final int INVALID_ACL = 123;

  /**
   * Maybe:
   * <p/>
   * <P>1.The database operation is interrupted</P>
   * <P>2.The database operation timeout</P>
   */
  public static final int TIMEOUT = 124;

  /**
   * The email address was invalid.
   */
  public static final int INVALID_EMAIL_ADDRESS = 125;
  public static final int ROLE_NOT_CHANGENAME = 136;

  /**
   * The object duplicate.
   */
  public static final int DUPLICATE_VALUE = 137;

  public static final int INVALID_ROLE_NAME = 139;
  public static final int EXCEEDED_QUOTA = 140;
  public static final int CLOUD_ERROR = 141;
  public static final int ROLE_NOT_FOUND = 142;
  public static final int CLOUD_CODE_NOT_DEPLOYED = 143;
  public static final int INVALID_TOKEN = 160;

  public static final int USERNAME_MISSING = 200;
  public static final int PASSWORD_MISSING = 201;

  public static final int USERNAME_TAKEN = 202;
  public static final int EMAIL_TAKEN = 203;
  public static final int EMAIL_MISSING = 204;
  public static final int EMAIL_NOT_FOUND = 205;
  public static final int SESSION_MISSING = 206;
  public static final int MUST_CREATE_USER_THROUGH_SIGNUP = 207;
  public static final int ACCOUNT_ALREADY_LINKED = 208;
  public static final int PASSWORD_MISMATCH = 210;
  public static final int NOT_FIND_USER = 211;
  public static final int NAME_TAKEN = 212;
  public static final int ID_TAKEN = 213;
  public static final int SUB_OBJECT_NOT_FOUND = 214;
  public static final int EMAIL_NOT_VERIFIED = 215;
  public static final int INVALID_RECEIPT = 216;
  public static final int COUNT_LIMIT = 217;
  public static final int MOBILEPHONE_MISSING = 238;
  public static final int SMSCODE_MISSING = 239;
  public static final int MOBILEPHONE_TAKEN = 240;
  public static final int SMSCODE_ERROR = 241;
  public static final int MOBILEPHONE_NOT_FOUND = 242;
  public static final int SMS_INVALID_PARAMETER = 243;

  public static final int LINKED_ID_MISSING = 250;
  public static final int INVALID_LINKED_SESSION = 251;

  /**
   * no supported account linking service found.
   */
  public static final int UNSUPPORTED_SERVICE = 252;

  /**
   * The authData must be Hash type, not null.
   */
  public static final int AUTHDATA_INVALID = 253;


  public static final int CAPTCHA_ERROR = 301;


  /**
   * Unauthorized access, no App ID, or App ID and App key verification failed.
   */
  public static final int UNAUTHORIZED = 401;

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public abstract class Unauthorized {
    public static final int SESSION_TOKEN_INVALID = 90100;
    public static final int SESSION_TOKEN_EXPIRED = 90101;
    public static final int APPID_AND_KEY_NOT_MATCHED = 90102;
    public static final int APPID_AND_SESSION_NOT_MATCHED = 90103;
    public static final int NO_PERMISSION = 90000;
  }

  /**
   * push error code 220-240
   */
  public abstract class PushModule {

    public static final int DEVICE_TYPE_ERROR = 220;

    public static final int TASK_STATUS_ERROR = 221;

    public static final int TASK_FORMATTER_ERROR = 222;
    public static final int IOS_CER_ERROR = 223;
    public static final int ANDROID_API_KEY_ERROR = 224;

    public static final int INNER_SERVER_ERROR = 225;
    public static final int PUSH_IS_OFF = 226;
  }

  public abstract class BillModule {

    public static final int INVALID_REQUEST_ERROR = 601;
    public static final int REMOTE_BILL_SERVER_ERROR = 602;
    public static final int CARD_ERROR = 603;
    public static final int AUTH_ERROR = 604;

  }

  /**
   * Rate limit exceeded
   */
  public static final int RATE_LIMIT = 503;

  public int code;
  private String locale;

  /**
   * @param code
   * @param msg   the detail message
   * @param cause the root cause
   */
  public LASException(int code, String msg, Throwable cause) {
    super(msg, cause);
    this.code = code;
  }

  public LASException(int theCode, String theMessage) {
    super(theMessage);
    this.code = theCode;
  }

  public LASException(int theCode, String theMessage, String locale) {
    super(theMessage);
    this.code = theCode;
    this.locale = locale;
  }

  public int getCode() {
    return this.code;
  }

  public static void parameterNotEmpty(String k, Object v) {
    parameterNotEmpty(v, INVALID_PARAMETER, "The " + k + " is empty.", null);
  }

  public static void parameterNotNull(String k, Object v) {
    parameterNotNull(v, INVALID_PARAMETER, "The " + k + " is null.", null);
  }

  public static void parameterNotNull(Object v, int code, String message, String locale) {
    if (v == null) {
      throw new LASException(code, message, locale);
    }
  }

  public static void parameterNotEmpty(Object v, int code, String message, String locale) {
    parameterNotNull(v, code, message, locale);
    if (v instanceof String) {
      if (StringUtils.isEmpty((String) v)) {
        throw new LASException(code, message, locale);
      }
    } else if (ObjectUtils.isArray(v)) {
      if (ObjectUtils.isEmpty((Object[]) v)) {
        throw new LASException(code, message, locale);
      }
    } else if (v instanceof Collection) {
      if (CollectionUtils.isEmpty((Collection) v)) {
        throw new LASException(code, message, locale);
      }
    } else if (v instanceof Map) {
      if (CollectionUtils.isEmpty((Map) v)) {
        throw new LASException(code, message, locale);
      }
    }
  }

  @Override
  public String toString() {
    return "ZCloud Exception [code=" + code + ", error=" + getMessage() + "]";
  }

  public int getErrorCode() {
    return this.code;
  }
}
