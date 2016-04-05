package com.maxleap.cloudcode.processors;

import com.maxleap.cloudcode.CCodeProcessor;
import com.maxleap.cloudcode.utils.DateUtils;
import com.maxleap.exception.LASException;
import com.maxleap.las.sdk.ObjectId;
import com.maxleap.las.sdk.SaveMsg;
import io.vertx.core.json.JsonObject;

/**
 * User: yuyangning
 * Date: 8/11/14
 * Time: 3:38 PM
 */
public class CCodeCreateProcessor implements CCodeProcessor<SaveMsg> {

  @Override
  public SaveMsg process(String message) {
    SaveMsg success = null;
		if (message != null){
			JsonObject saveResult = new JsonObject(message);
			long createAt = DateUtils.parseDate(saveResult.getString("createdAt")).getTime();
			String objectId = saveResult.getString("objectId");
			success = new SaveMsg(createAt, new ObjectId(objectId));
		} else
      throw new LASException(LASException.INTERNAL_SERVER_ERROR,"Bad response from save cloud code. " + message);

    return success;
  }

}
