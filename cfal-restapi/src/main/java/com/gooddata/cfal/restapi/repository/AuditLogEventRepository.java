/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.repository;

import com.codahale.metrics.Timer;
import com.gooddata.auditevent.AuditEventPageRequest;
import com.gooddata.cfal.restapi.dto.UserInfo;
import com.gooddata.cfal.restapi.model.AuditEventEntity;
import com.gooddata.commons.monitoring.metrics.Measure;
import com.gooddata.commons.monitoring.metrics.Monitored;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Repository for audit event management
 */
@Repository
@Monitored("cfal.AuditLogEventRepository")
public class AuditLogEventRepository {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogEventRepository.class);

    private final long recordTtlDays;
    private final String mongoCollectionPrefix;

    private final MongoTemplate mongoTemplate;

    private final Timer findByDomainTimer = new Timer();

    private final Timer findByUserTimer = new Timer();

    public AuditLogEventRepository(final MongoTemplate mongoTemplate,
                                   @Value("${gdc.cfal.mongo.collection.prefix}") final String mongoCollectionPrefix,
                                   @Value("${gdc.cfal.mongo.record.ttl.days}") final long recordTtlDays) {
        isTrue(recordTtlDays > 0, "recordTtlDays must be greater than 0");

        this.mongoTemplate = notNull(mongoTemplate, "mongoTemplate cannot be null");
        this.recordTtlDays = recordTtlDays;
        this.mongoCollectionPrefix = mongoCollectionPrefix == null ? "" : mongoCollectionPrefix;
    }

    /**
     * Finds all events for domain in given time interval. If <code>offset</code> is not null, than returns events younger (greater ID) than <code>offset</code>.
     * Result list (page) has size equal to <code>limit</code>.
     *
     * @param domain            domain to find events for
     * @param requestParameters parameters for filtering events
     * @return list starting from <code>offset</code>
     */
    public List<AuditEventEntity> findByDomain(final String domain,
                                               final AuditEventPageRequest requestParameters) {
        notEmpty(domain, "domain cannot be empty");
        notNull(requestParameters, "requestParameters cannot be null");

        final Timer.Context time = findByDomainTimer.time();
        try {
            final Query query = createQuery(requestParameters);
            return mongoTemplate.find(query, AuditEventEntity.class, getMongoCollectionName(domain));
        } finally {
            time.stop();
        }
    }

    /**
     * Finds all events for given user and in given time interval. If <code>offset</code> is not null, than returns events younger (greater ID) than <code>offset</code>.
     * Result list (page) has size equal to <code>limit</code>.
     *
     * @param userInfo          identifies user
     * @param requestParameters parameters for filtering events
     * @return list starting from <code>offset</code> and limited on given time range
     */
    public List<AuditEventEntity> findByUser(final UserInfo userInfo,
                                             final AuditEventPageRequest requestParameters) {
        notNull(userInfo, "userInfo cannot be empty");
        notNull(requestParameters, "requestParameters cannot be null");

        final Timer.Context time = findByUserTimer.time();
        try {
            final Query query = createQuery(requestParameters).addCriteria(Criteria.where("userLogin").is(userInfo.getUserLogin()));
            return mongoTemplate.find(query, AuditEventEntity.class, getMongoCollectionName(userInfo.getDomainId()));
        } finally {
            time.stop();
        }
    }

    /**
     * Persists <code>auditEvent</code>
     *
     * @param auditEvent to be persisted
     */
    public void save(final AuditEventEntity auditEvent) {
        notNull(auditEvent, "auditEvent cannot be null");

        mongoTemplate.save(auditEvent, getMongoCollectionName(auditEvent.getDomainId()));
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
     * Creates TTL-indexes from all CFAL-related collections
     * <p>
     * {@see #createTtlIndex}
     */
    public void createTtlIndexes() {
        mongoTemplate
                .getCollectionNames()
                .stream()
                .filter(n -> n.startsWith(mongoCollectionPrefix))
                .forEach(this::createTtlIndex);
    }

    /**
     * Creates User-login-indexes from all CFAL-related collections
     * <p>
     * {@see #createUserLoginIndex}
     */
    public void createUserLoginIndexes() {
        mongoTemplate
                .getCollectionNames()
                .stream()
                .filter(n -> n.startsWith(mongoCollectionPrefix))
                .forEach(this::createUserLoginIndex);
    }

    /**
     * Return number of days all records are supposed to be deleted.
     * As the index is constructed on "eventdate" field, one extra day is added.
     *
     * @return number of days
     */
    private long getRecordTtlDays() {
        return recordTtlDays + 1;
    }

    /**
     * Creates a TTL-index on given collection. Index is set to delete all records older than {@link #getRecordTtlDays()} days.
     *
     * @param collectionName collection for which you want to create TTL-index
     */
    private void createTtlIndex(final String collectionName) {
        final Index index = new Index()
                .named("ttl")
                .background()
                .expire(getRecordTtlDays(), TimeUnit.DAYS)
                .on("eventdate", Sort.Direction.ASC);

        try {
            mongoTemplate.indexOps(collectionName).ensureIndex(index);
        } catch (Exception e) {
            logger.warn("Unable to create index for a collection=" + collectionName, e);
        }
    }

    @Measure("find.by.domain.time")
    public Timer getFindByDomainTimer() {
        return findByDomainTimer;
    }

    @Measure("find.by.user.time")
    public Timer getFindByUserTimer() {
        return findByUserTimer;
    }

    /**
     * Creates a User-login-index on given collection. Index is defined on top of 'userLogin' attribute.
     *
     * @param collectionName collection for which you want to create User-login-index
     */
    private void createUserLoginIndex(final String collectionName) {
        final Index index = new Index()
                .background()
                .on("userLogin", Sort.Direction.ASC);

        try {
            mongoTemplate.indexOps(collectionName).ensureIndex(index);
        } catch (Exception e) {
            logger.warn("Unable to create index for a collection=" + collectionName, e);
        }
    }

    /**
     * Create query based on <code>requestParameters</code>
     */
    private Query createQuery(final AuditEventPageRequest requestParameters) {

        final Query query = new Query();

        final Criteria idCriteria = createCriteriaForId(requestParameters);

        if (idCriteria != null) {
            query.addCriteria(idCriteria);
        }

        if (requestParameters.getType() != null) {
            query.addCriteria(Criteria.where("type").is(requestParameters.getType()));
        }

        query.with(new Sort(Sort.Direction.ASC, "id"));
        query.limit(requestParameters.getSanitizedLimit());

        return query;
    }

    private Criteria createCriteriaForId(final AuditEventPageRequest requestParameters) {
        Criteria idCriteria = null;

        if (getOffsetAsObjectId(requestParameters) != null) {
            idCriteria = nullSafeIdCriteria(idCriteria);
            idCriteria.gt(getOffsetAsObjectId(requestParameters));
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
        return (idCriteria == null) ? Criteria.where("id") : idCriteria;
    }

    /**
     * Get mongo collection name from domain and mongo collection prefix
     */
    String getMongoCollectionName(final String domain) {
        return mongoCollectionPrefix + domain;
    }

    private static ObjectId getOffsetAsObjectId(final AuditEventPageRequest params) {
        return params == null || params.getOffset() == null ? null : new ObjectId(params.getOffset());
    }
}
