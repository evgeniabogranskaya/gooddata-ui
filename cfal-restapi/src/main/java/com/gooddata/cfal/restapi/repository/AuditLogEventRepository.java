/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.repository;

import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.cfal.restapi.model.AuditEvent;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
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

    private final String mongoCollectionPrefix;

    private final MongoTemplate mongoTemplate;

    public AuditLogEventRepository(final MongoTemplate mongoTemplate,
                                   @Value("${gdc.cfal.mongo.collection.prefix}") final String mongoCollectionPrefix) {
        this.mongoTemplate = notNull(mongoTemplate, "mongoTemplate cannot be null");
        this.mongoCollectionPrefix = mongoCollectionPrefix == null ? "" : mongoCollectionPrefix;
    }

    /**
     * Finds all events for domain in given time interval. If <code>offset</code> is not null, than returns events younger (greater ID) than <code>offset</code>.
     * Result list (page) has size equal to <code>limit</code>.
     *
     * @param domain domain to find events for
     * @param requestParameters parameters for filtering events
     * @return list starting from <code>offset</code>
     */
    public List<AuditEvent> findByDomain(final String domain,
                                         final RequestParameters requestParameters) {
        notEmpty(domain, "domain cannot be empty");
        notNull(requestParameters, "requestParameters cannot be null");

        final Query query = createQuery(requestParameters);
        return mongoTemplate.find(query, AuditEvent.class, getMongoCollectionName(domain));
    }

    /**
     * Finds all events for domain for given userId and in given time interval. If <code>offset</code> is not null, than returns events younger (greater ID) than <code>offset</code>.
     * Result list (page) has size equal to <code>limit</code>.
     *
     * @param domain domain to find events for
     * @param userId user to find events for
     * @param requestParameters parameters for filtering events
     * @return list starting from <code>offset</code> and limited on given time range
     */
    public List<AuditEvent> findByDomainAndUser(final String domain,
                                                final String userId,
                                                final RequestParameters requestParameters) {
        notEmpty(domain, "domain cannot be empty");
        notEmpty(userId, "userId cannot be empty");
        notNull(requestParameters, "requestParameters cannot be null");

        final Query query = createQuery(requestParameters).addCriteria(Criteria.where("userId").is(userId));
        return mongoTemplate.find(query, AuditEvent.class, getMongoCollectionName(domain));
    }

    /**
     * Persists <code>auditEvent</code>
     *
     * @param auditEvent to be persisted
     */
    public void save(final AuditEvent auditEvent) {
        notNull(auditEvent, "auditEvent cannot be null");

        mongoTemplate.save(auditEvent, getMongoCollectionName(auditEvent.getDomain()));
    }

    /**
     * Deletes all events for domain
     *
     * @param domain to delete events for
     */
    public void deleteAllByDomain(final String domain) {
        notEmpty(domain, "domain cannot be empty");

        mongoTemplate.remove(new Query(), getMongoCollectionName(domain));
    }

    /**
     * Create query based on <code>requestParameters</code>
     */
    private Query createQuery(final RequestParameters requestParameters) {

        final Query query = new Query();

        final Criteria idCriteria = createCriteriaForId(requestParameters);

        if(idCriteria != null) {
            query.addCriteria(idCriteria);
        }

        query.with(new Sort(Sort.Direction.ASC, "id"));
        query.limit(requestParameters.getSanitizedLimit());

        return query;
    }

    private Criteria createCriteriaForId(final RequestParameters requestParameters) {
        Criteria idCriteria = null;

        if (requestParameters.getOffsetAsObjectId() != null) {
            idCriteria = nullSafeIdCriteria(idCriteria);
            idCriteria.gt(requestParameters.getOffsetAsObjectId());
        }

        if (requestParameters.getFrom() != null) {
            idCriteria = nullSafeIdCriteria(idCriteria);
            //use constructor ObjectId(Date, int, short, int, int) in order to get ObjectID with value xxxxxxxx0000000000000000
            idCriteria.gte(new ObjectId(requestParameters.getFrom().toDate(), 0, (short) 0, 0));
        }

        if (requestParameters.getTo() != null) {
            idCriteria = nullSafeIdCriteria(idCriteria);
            //use constructor ObjectId(Date, int, short, int, int) in order to get ObjectID with value xxxxxxxx0000000000000000
            idCriteria.lte(new ObjectId(requestParameters.getTo().toDate(), 0, (short) 0, 0));
        }

        return idCriteria;
    }

    private Criteria nullSafeIdCriteria(final Criteria idCriteria) {
        return (idCriteria == null)? Criteria.where("id") : idCriteria;
    }

    String getMongoCollectionPrefix() {
        return mongoCollectionPrefix;
    }

    /**
     * Get mongo collection name from domain and mongo collection prefix
     */
    private String getMongoCollectionName(final String domain) {
        return mongoCollectionPrefix + domain;
    }
}
