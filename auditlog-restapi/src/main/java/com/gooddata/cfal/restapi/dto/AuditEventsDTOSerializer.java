/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import com.gooddata.collections.PageableListSerializer;

public class AuditEventsDTOSerializer extends PageableListSerializer {

    public AuditEventsDTOSerializer() {
        super(AuditEventsDTO.ROOT_NODE);
    }
}
