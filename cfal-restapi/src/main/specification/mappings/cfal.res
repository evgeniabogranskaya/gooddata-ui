# Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
############################################################################################################
#
#  Section: CFAL REST API
#
############################################################################################################
# Group: Description
#        CFAL REST API
#
############################################################################################################
# Group: Resource(s)
#
# header: /gdc/account/profile/<userId>/auditEvents
#
#   parameters:
#       offset - STRING (optional)          % The identifier of the first record (for paging).
#       limit  - INT    (optional)          % The maximum number of records per page.
#       from   - DATETIMEISO (optional)     % specify min timestamp of audit events to be returned
#       to     - DATETIMEISO (optional)     % specify max timestamp of audit events to be returned
#
#   GET
#    - (200 OK) <AuditEvents>
#    - (400 Bad Request) <ERROR>   % Invalid request
#    - (401 Unauthorized) <ERROR>   % User not permitted to see audit events for user
#
# header: /gdc/domains/<domainId>/auditEvents
#
#   parameters:
#       offset - STRING (optional)          % The identifier of the first record (for paging).
#       limit  - INT    (optional)          % The maximum number of records per page.
#       from   - DATETIMEISO (optional)     % specify min timestamp of audit events to be returned
#       to     - DATETIMEISO (optional)     % specify max timestamp of audit events to be returned
#
#   GET
#    - (200 OK) <AuditEvents>
#    - (400 Bad Request) <ERROR>   % Invalid request
#    - (401 Unauthorized) <ERROR>   % User not admin of domain
#    - (404 Not found) <ERROR>     % Domain doesn't exist
#
#############################################################################################################
# Group: Data Structures
#
#  header: auditEvent
#  (start code)
#  AuditEvent = <event: {
#       id : STRING,
#       userLogin: STRING,
#       occurred: DATETIMEISO,
#       recorded: DATETIMEISO,
#       userIp: STRING,
#       success: BOOLEAN,
#       type: STRING,
#       (params: {
#           (STRING:STRING)*
#       })?,
#       (links: {
#           (STRING:STRING)*
#       })?
#  }>
#  (end)
#
#  header: auditEvents
#  (start code)
#  AuditEvents = <events : {
#       items: [AuditEvent],
#       paging: {
#           (next: URISTRING)?
#       },
#       links: {
#           self: URISTRING
#       }
#  }>
#  (end)
#############################################################################################################