/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.repository;

import com.gooddata.cfal.restapi.model.AuditEvent;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Repository for audit event management
 */
@Repository
public class AuditLogEventRepository {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public AuditLogEventRepository(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = notNull(mongoTemplate, "mongoTemplate cannot be null");
    }

    /**
     * Finds all events for domain. If <code>offset</code> is not null, than returns events younger (greater ID) than <code>offset</code>.
     * Result list (page) has size equal to <code>limit</code>
     *
     * @param domain domain to find events for
     * @param limit  result list size
     * @param offset ID of event, from which (excluding this event) list (page) starts. If null, it result list starts from the oldest event
     * @return list starting from <code>offset</code>
     */
    public List<AuditEvent> findByDomain(final String domain, final int limit, final ObjectId offset) {
        notEmpty(domain, "domain cannot be empty");

        final Query query = createQuery(limit, offset);
        return mongoTemplate.find(query, AuditEvent.class, domain);
    }

    /**
     * Finds all events for domain for given userId. If <code>offset</code> is not null, than returns events younger (greater ID) than <code>offset</code>.
     * Result list (page) has size equal to <code>limit</code>
     *
     * @param domain domain to find events for
     * @param userId user to find events for
     * @param limit  result list size
     * @param offset ID of event, from which (excluding this event) list (page) starts. If null, it result list starts from the oldest event
     * @return list starting from <code>offset</code>
     */
    public List<AuditEvent> findByDomainAndUser(final String domain, final String userId, final int limit, final ObjectId offset) {
        notEmpty(domain, "domain cannot be empty");
        notEmpty(userId, "userId cannot be empty");

        final Query query = createQuery(limit, offset).addCriteria(Criteria.where("userId").is(userId));
        return mongoTemplate.find(query, AuditEvent.class, domain);
    }

    /**
     * Persists <code>auditEvent</code>
     *
     * @param auditEvent to be persisted
     */
    public void save(final AuditEvent auditEvent) {
        notNull(auditEvent, "auditEvent cannot be null");

        mongoTemplate.save(auditEvent, auditEvent.getDomain());
    }

    /**
     * Deletes all events for domain
     *
     * @param domain to delete events for
     */
    public void deleteAllByDomain(final String domain) {
        notEmpty(domain, "domain cannot be empty");

        mongoTemplate.remove(new Query(), domain);
    }

    /**
     * Creates query, which will limit number events based on <code>limit</code> and finds events younger (greater ID) than <code>offset</code>
     *
     * @param limit  size of list (page)
     * @param offset ID to start finding events, can be null. If null, starts from oldest event.
     * @return Query, which limits result on <code>limit</code> and starts after event with ID <code>offset</code>
     */
    private Query createQuery(final int limit, final ObjectId offset) {
        final Query query = new Query();

        if (offset != null) {
            query.addCriteria(Criteria.where("id").gt(offset));
        }

        query.limit(limit);
        return query;
    }
}
