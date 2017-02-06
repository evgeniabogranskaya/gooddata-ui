package com.gooddata.cfal.restapi.exception;

import com.gooddata.cfal.restapi.dto.RequestParameters;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for {@link RequestParameters}
 */
@Component
public class RequestParametersValidator implements Validator {

    @Override
    public boolean supports(final Class<?> aClass) {
        return aClass.isAssignableFrom(RequestParameters.class);
    }

    @Override
    public void validate(final Object o, final Errors errors) {
        final RequestParameters requestParameters = (RequestParameters) o;
        if(requestParameters.getOffset() != null) {
            if (!ObjectId.isValid(requestParameters.getOffset())) {
                errors.rejectValue("offset", "requestParameters.invalid", "Invalid offset \"" + requestParameters.getOffset() + "\"");
            }
        }

        if (requestParameters.getOffset() != null && requestParameters.getFrom() != null) {
            errors.rejectValue("offset", "requestParameters.invalid", "offset and time interval param \"from\" cannot be specified at once");
        }

        if (requestParameters.getFrom() != null && requestParameters.getTo() != null) {
            if (!requestParameters.getFrom().isBefore(requestParameters.getTo())) {
                errors.rejectValue("from", "requestParameters.invalid", "\"to\" must be after \"before\"");
            }
        }
    }
}
