package com.maxleap.domain;

import com.maxleap.exception.LASException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by.
 * User: ben
 * Date: 1/9/15
 * Time: 9:58 AM
 * Email:benkris1@gmail.com
 *
 * @ilengend
 */
public enum LASSegmentStatus {
  created(0),inprogress(1),finish(2),error(3);

  private int code;
  LASSegmentStatus(int code) {
    this.code = code;
  }
  public int getCode(){
    return this.code;
  }

  @JsonCreator
  public static LASSegmentStatus forValue(String value) throws RuntimeException {
    try{
      int t = Integer.parseInt(value);
      for(LASSegmentStatus status: LASSegmentStatus.values()){
        if(status.getCode() == t){
          return status;
        }
      }
      throw new LASException(LASException.INVALID_PARAMETER,"there is no status type named:"+value);
    }catch (NumberFormatException e){
      throw new LASException(LASException.INVALID_PARAMETER,"there is no status type named:"+value);
    }

  }
  public static LASSegmentStatus fromInt(int t) {
    for(LASSegmentStatus status: LASSegmentStatus.values()){
      if(status.getCode() == t){
        return status;
      }
    }
    throw new LASException(LASException.INVALID_PARAMETER,"there is no status type named:"+t);
  }
  @JsonValue
  public String toString() {
    return String.valueOf(this.code);
  }

}
