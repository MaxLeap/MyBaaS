package com.maxleap.cloudcode.processors;

import com.maxleap.cloudcode.CCodeProcessor;
import com.maxleap.cloudcode.utils.DateUtils;
import com.maxleap.exception.LASException;
import com.maxleap.las.sdk.UpdateMsg;
import io.vertx.core.json.JsonObject;

import java.util.Map;

/**
 * User: yuyangning
 * Date: 8/11/14
 * Time: 3:38 PM
 */
public class CCodeUpdateProcessor implements CCodeProcessor<UpdateMsg> {

	@Override
	public UpdateMsg process(String message) {
		UpdateMsg updateMsg = null;
		if (message != null) {
			JsonObject updateResult = new JsonObject(message);

			String updateAtString = updateResult.getString("updateAt");
			long updateAt;
			Map result = null;
			int num = 0;
			if (updateAtString != null) {
				updateAt = DateUtils.parseDate(updateAtString).getTime();
			} else updateAt = System.currentTimeMillis();

			Integer number = updateResult.getInteger("number");
			if (number != null) num = number;

			JsonObject resultObject = updateResult.getJsonObject("result");
			if (resultObject != null) result = resultObject.getMap();

			updateMsg = new UpdateMsg(num, updateAt, result);
		} else
			throw new LASException(LASException.INTERNAL_SERVER_ERROR,"Bad response from update cloud code. " + message);

		return updateMsg;

	}

}
