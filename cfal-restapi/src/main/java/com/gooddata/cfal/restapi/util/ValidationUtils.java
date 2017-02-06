/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.util;

import static org.apache.commons.lang3.Validate.notNull;

import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.cfal.restapi.exception.InvalidOffsetException;
import com.gooddata.cfal.restapi.exception.InvalidTimeIntervalException;
import com.gooddata.cfal.restapi.exception.OffsetAndFromSpecifiedException;
import org.bson.types.ObjectId;

public class ValidationUtils {

    /**
     * Validate if request parameters are valid
     * @param requestParameters request parameters to validate
     *
     * @throws OffsetAndFromSpecifiedException if offset and from is set at once
     * @throws InvalidTimeIntervalException if time interval is not valid
     * @throws InvalidOffsetException if offset is not valid ObjectId
     */
    public static void validate(final RequestParameters requestParameters) {
        notNull(requestParameters, "requestParameters cannot be null");


        if(requestParameters.getOffset() != null) {
            if (!ObjectId.isValid(requestParameters.getOffset())) {
                throw new InvalidOffsetException("Invalid offset " + requestParameters.getOffset());
            }
        }

        if (requestParameters.getOffset() != null && requestParameters.getFrom() != null) {
            throw new OffsetAndFromSpecifiedException("offset and time interval param \"from\" cannot be specified at once");
        }

        if (requestParameters.getFrom() != null && requestParameters.getTo() != null) {
            if (!requestParameters.getFrom().isBefore(requestParameters.getTo())) {
                throw new InvalidTimeIntervalException("\"to\" must be after \"before\"");
            }
        }
    }
}
