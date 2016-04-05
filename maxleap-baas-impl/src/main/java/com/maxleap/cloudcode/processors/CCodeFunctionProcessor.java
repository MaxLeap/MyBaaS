package com.maxleap.cloudcode.processors;

import com.maxleap.cloudcode.CCodeProcessor;

/**
 * User: yuyangning
 * Date: 8/11/14
 * Time: 3:36 PM
 */
public class CCodeFunctionProcessor implements CCodeProcessor<Object> {

  @Override
  public Object process(String message) {
		return message;
  }
}
