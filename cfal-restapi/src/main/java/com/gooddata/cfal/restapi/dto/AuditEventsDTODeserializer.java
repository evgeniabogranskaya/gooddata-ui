/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import com.gooddata.collections.PageableListDeserializer;
import com.gooddata.collections.Paging;

import java.util.List;
import java.util.Map;

public class AuditEventsDTODeserializer extends PageableListDeserializer<AuditEventsDTO, AuditEventDTO>{

    protected AuditEventsDTODeserializer() {
        super(AuditEventDTO.class);
    }

    @Override
    protected AuditEventsDTO createList(final List<AuditEventDTO> items, final Paging paging, final Map<String, String> links) {
        return new AuditEventsDTO(items, paging, links);
    }
}
