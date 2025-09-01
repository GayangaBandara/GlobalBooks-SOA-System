# GlobalBooks SOA Governance Policy

## 1. Service Versioning Strategy

### 1.1 URL Versioning Convention
All REST services MUST implement URL-based versioning following this pattern:
```
https://api.globalbooks.com/{service-name}/v{major-version}/{resource}
```

**Examples:**
- `https://api.globalbooks.com/orders/v1/orders`
- `https://api.globalbooks.com/payments/v2/transactions`
- `https://api.globalbooks.com/shipping/v1/shipments`

### 1.2 SOAP Namespace Versioning
SOAP services MUST use namespace versioning with the following convention:
```
http://{service-name}.globalbooks.com/v{major-version}/
```

**Examples:**
- `http://catalog.globalbooks.com/v1/` (Current version)
- `http://catalog.globalbooks.com/v2/` (Future version)

### 1.3 Semantic Versioning Rules

#### Major Version Changes (v1 → v2)
- Breaking changes to existing APIs
- Removal of operations or parameters
- Changes to data types that are not backward compatible
- Changes in authentication/authorization mechanisms

#### Minor Version Changes (v1.1 → v1.2)
- Addition of new operations
- Addition of optional parameters
- Addition of new response fields
- Performance improvements

#### Patch Version Changes (v1.1.1 → v1.1.2)
- Bug fixes
- Security patches
- Documentation updates
- Internal refactoring without API changes

### 1.4 Version Support Policy
- **Current Version (N)**: Full support and active development
- **Previous Version (N-1)**: Maintenance mode - critical bugs and security fixes only
- **Legacy Version (N-2)**: Deprecated - no updates, scheduled for sunset

## 2. Service Level Agreements (SLAs)

### 2.1 Availability Targets

#### Production Services
| Service | Availability | Monthly Downtime |
|---------|--------------|-------------------|
| CatalogService | 99.9% | ≤ 43 minutes |
| OrdersService | 99.95% | ≤ 22 minutes |
| PaymentsService | 99.99% | ≤ 4 minutes |
| ShippingService | 99.5% | ≤ 3.6 hours |

#### Development/Staging Services
- Availability: 99.0% (Best effort)
- Scheduled maintenance windows: Weekends 02:00-06:00 UTC

### 2.2 Response Time Targets

#### REST Services
| Operation Type | Target Response Time | 95th Percentile |
|----------------|---------------------|------------------|
| GET (single resource) | < 100ms | < 200ms |
| GET (collection) | < 200ms | < 500ms |
| POST/PUT | < 300ms | < 1000ms |
| DELETE | < 100ms | < 200ms |

#### SOAP Services
| Operation Type | Target Response Time | 95th Percentile |
|----------------|---------------------|------------------|
| Simple queries | < 200ms | < 500ms |
| Complex operations | < 1000ms | < 2000ms |
| Batch operations | < 5000ms | < 10000ms |

#### BPEL Process Orchestration
| Process Type | Target Response Time | 95th Percentile |
|-------------|---------------------|------------------|
| PlaceOrder (Success path) | < 2000ms | < 5000ms |
| PlaceOrder (With retries) | < 10000ms | < 30000ms |

### 2.3 Throughput Requirements
- **CatalogService**: 1000 requests/second sustained
- **OrdersService**: 500 requests/second sustained
- **PaymentsService**: 200 requests/second sustained
- **ShippingService**: 100 requests/second sustained

### 2.4 Error Rate Targets
- **4xx Client Errors**: < 5% of total requests
- **5xx Server Errors**: < 0.1% of total requests
- **Timeout Errors**: < 0.05% of total requests

## 3. Service Deprecation and Sunset Process

### 3.1 Deprecation Timeline

#### Phase 1: Deprecation Announcement (T-12 months)
- **Action**: Official announcement of deprecation
- **Notice Period**: 12 months for major versions, 6 months for minor versions
- **Communication**: 
  - Email notification to all registered API consumers
  - Updates to API documentation and developer portal
  - Addition of deprecation headers to API responses

#### Phase 2: Deprecation Warning (T-6 months)
- **Action**: Enhanced deprecation warnings
- **Activities**:
  - HTTP `Deprecation` headers added to all responses
  - Warning messages in SOAP fault details
  - Dashboard notifications for active consumers
  - Migration guide published

#### Phase 3: Sunset Warning (T-3 months)
- **Action**: Final warning before shutdown
- **Activities**:
  - `Sunset` HTTP headers with exact shutdown date
  - Direct outreach to remaining consumers
  - Migration assistance offered
  - Monitoring of consumer adoption of new versions

#### Phase 4: Service Sunset (T-0)
- **Action**: Service shutdown
- **Process**:
  - HTTP 410 Gone responses for REST endpoints
  - SOAP fault responses for deprecated operations
  - Redirect responses to new service versions where applicable
  - Final consumer notification

### 3.2 Deprecation Headers

#### REST Services
```http
Deprecation: Sat, 01 Sep 2025 00:00:00 GMT
Sunset: Sat, 01 Mar 2026 00:00:00 GMT
Link: <https://api.globalbooks.com/orders/v2/orders>; rel="successor-version"
```

#### SOAP Services
```xml
<soap:Header>
    <dep:DeprecationNotice xmlns:dep="http://globalbooks.com/deprecation">
        <dep:DeprecatedSince>2025-09-01T00:00:00Z</dep:DeprecatedSince>
        <dep:SunsetDate>2026-03-01T00:00:00Z</dep:SunsetDate>
        <dep:SuccessorVersion>http://catalog.globalbooks.com/v2/</dep:SuccessorVersion>
    </dep:DeprecationNotice>
</soap:Header>
```

## 4. Quality of Service (QoS) Mechanisms

### 4.1 Circuit Breaker Configuration
```yaml
circuit-breaker:
  failure-threshold: 5
  success-threshold: 3
  timeout: 10000ms
  reset-timeout: 30000ms
  monitoring-period: 60000ms
```

### 4.2 Retry Policies
```yaml
retry-policy:
  max-attempts: 3
  initial-delay: 1000ms
  max-delay: 10000ms
  backoff-multiplier: 2.0
  retryable-exceptions:
    - TimeoutException
    - ConnectException
    - ServiceUnavailableException
```

### 4.3 Rate Limiting
| Consumer Type | Rate Limit | Burst Limit |
|---------------|------------|-------------|
| Internal Services | 10000/hour | 100/minute |
| Premium Partners | 5000/hour | 50/minute |
| Standard Partners | 1000/hour | 20/minute |
| Free Tier | 100/hour | 10/minute |

### 4.4 Load Balancing Strategy
- **Algorithm**: Weighted Round Robin
- **Health Checks**: HTTP /health endpoint every 30 seconds
- **Failover**: Automatic removal of unhealthy instances
- **Scaling**: Auto-scaling based on CPU (>80%) and response time (>500ms avg)

## 5. Security Governance

### 5.1 Authentication Requirements
- **SOAP Services**: WS-Security UsernameToken or X.509 certificates
- **REST Services**: OAuth 2.0 Bearer tokens (JWT format)
- **Token Expiration**: 
  - Access tokens: 1 hour
  - Refresh tokens: 30 days
  - Service-to-service tokens: 24 hours

### 5.2 Authorization Model
```
Roles:
- service.catalog.read
- service.catalog.write
- service.orders.read  
- service.orders.write
- service.orders.admin
- service.payments.process
- service.shipping.create

Scopes:
- catalog:read
- orders:read orders:write orders:admin
- payments:process
- shipping:create shipping:track
```

### 5.3 Data Classification
| Classification | Examples | Protection Level |
|---------------|----------|------------------|
| Public | Product catalog, pricing | Basic encryption in transit |
| Internal | Order status, inventory | Encryption in transit + at rest |
| Confidential | Payment details, PII | Encryption + access logging + DLP |
| Restricted | Security keys, secrets | HSM storage + strict access controls |

## 6. Monitoring and Compliance

### 6.1 Required Metrics
All services MUST expose the following metrics:
- Request count by operation and status code
- Response time percentiles (50th, 95th, 99th)
- Error rates by error type
- Circuit breaker state
- Queue depth (for async operations)
- Database connection pool utilization

### 6.2 Health Check Requirements
Each service MUST provide:
```http
GET /health
{
  "status": "UP|DOWN|DEGRADED",
  "timestamp": "2025-08-18T10:30:00Z",
  "version": "1.2.3",
  "dependencies": {
    "database": "UP",
    "external-service": "UP",
    "message-queue": "UP"
  },
  "metrics": {
    "responseTime": "45ms",
    "errorRate": "0.1%"
  }
}
```

### 6.3 Audit Requirements
- All service invocations logged with correlation IDs
- Security events (auth failures, access violations) logged
- Performance metrics collected every 60 seconds
- Log retention: 90 days for operational, 7 years for audit
- Compliance reporting: SOX, PCI-DSS, GDPR

## 7. Change Management Process

### 7.1 Service Modification Approval Matrix
| Change Type | Developer | Tech Lead | Architect | Change Board |
|-------------|-----------|-----------|-----------|--------------|
| Bug fixes | ✓ | ✓ | - | - |
| New features | ✓ | ✓ | ✓ | - |
| API changes | ✓ | ✓ | ✓ | ✓ |
| Security changes | ✓ | ✓ | ✓ | ✓ |
| Infrastructure | ✓ | ✓ | ✓ | ✓ |

### 7.2 Testing Requirements by Environment
| Environment | Unit Tests | Integration Tests | Contract Tests | Load Tests |
|-------------|------------|-------------------|----------------|------------|
| Development | ✓ (>80% coverage) | ✓ | - | - |
| Test | ✓ | ✓ | ✓ | - |
| Staging | ✓ | ✓ | ✓ | ✓ |
| Production | ✓ | ✓ | ✓ | ✓ |

### 7.3 Deployment Gates
1. **Code Quality Gate**: SonarQube quality gate passed
2. **Security Gate**: Security scan with no HIGH/CRITICAL vulnerabilities
3. **Performance Gate**: Load tests meet SLA requirements
4. **Contract Gate**: Consumer contract tests pass
5. **Approval Gate**: Required approvals obtained

## 8. Service Registry and Discovery

### 8.1 UDDI Registry Requirements
All SOAP services MUST register in UDDI with:
- Complete service description and capabilities
- Technical contact information
- SLA commitments
- Security requirements
- Consumer onboarding process

### 8.2 API Gateway Integration
All REST services MUST register with API Gateway providing:
- OpenAPI 3.0 specification
- Rate limiting requirements  
- Authentication/authorization schemes
- Monitoring and alerting configuration

## 9. Consumer Onboarding Process

### 9.1 Service Consumer Registration
1. **Application**: Submit service consumer application
2. **Review**: Technical and business review (5 business days)
3. **Approval**: Service level and rate limit approval
4. **Credentials**: API key/certificate provisioning
5. **Testing**: Sandbox environment access for integration testing
6. **Production**: Production access after successful testing

### 9.2 Support Levels
| Support Level | Response Time | Channels | Cost |
|---------------|---------------|----------|------|
| Premium | 1 hour | Phone, Email, Slack | Paid |
| Standard | 4 hours | Email, Portal | Free |
| Community | Best effort | Forums, Documentation | Free |

## 10. Compliance and Audit Trail

### 10.1 Regulatory Compliance
- **SOX**: Financial transaction audit trails
- **PCI-DSS**: Payment card data protection
- **GDPR**: Personal data processing compliance
- **CCPA**: California consumer privacy compliance

### 10.2 Audit Evidence Requirements
- Service access logs with user attribution
- Data modification trails with before/after states
- Security event logs with threat analysis
- Performance metrics with SLA compliance evidence
- Change management records with approval chains

---

**Document Control**
- Version: 1.0
- Effective Date: August 18, 2025
- Review Cycle: Quarterly
- Owner: SOA Governance Board
- Approved By: CTO, Security Officer, Compliance Officer