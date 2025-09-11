# CCS3341 SOA & Microservices Project Structure

```
GlobalBooks-SOA-Project/
│
├── README.md
├── PROJECT_CHECKLIST.md
│
├── 01-design-artifacts/
│   ├── soa-design-document.md
│   ├── architecture-diagrams/
│   │   ├── overall-architecture.png
│   │   ├── service-decomposition.png
│   │   └── bpel-workflow.png
│   └── governance-policy.md
│
├── 02-catalog-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/globalbooks/catalog/
│   │       │       ├── CatalogService.java
│   │       │       ├── CatalogServiceImpl.java
│   │       │       ├── Book.java
│   │       │       └── CatalogEndpoint.java
│   │       ├── resources/
│   │       │   └── META-INF/
│   │       │       └── services/
│   │       └── webapp/
│   │           └── WEB-INF/
│   │               ├── web.xml
│   │               └── sun-jaxws.xml
│   ├── wsdl/
│   │   └── CatalogService.wsdl
│   ├── uddi/
│   │   └── catalog-service-registry.xml
│   ├── pom.xml
│   └── Dockerfile
│
├── 03-orders-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/globalbooks/orders/
│   │       │       ├── OrdersApplication.java
│   │       │       ├── controller/
│   │       │       │   └── OrderController.java
│   │       │       ├── model/
│   │       │       │   ├── Order.java
│   │       │       │   └── OrderItem.java
│   │       │       ├── service/
│   │       │       │   └── OrderService.java
│   │       │       └── repository/
│   │       │           └── OrderRepository.java
│   │       └── resources/
│   │           ├── application.yml
│   │           └── schema/
│   │               └── order-schema.json
│   ├── pom.xml
│   └── Dockerfile
│
├── 04-payments-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/globalbooks/payments/
│   │       │       ├── PaymentsApplication.java
│   │       │       ├── controller/
│   │       │       ├── model/
│   │       │       └── service/
│   │       └── resources/
│   │           └── application.yml
│   ├── pom.xml
│   └── Dockerfile
│
├── 05-shipping-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/globalbooks/shipping/
│   │       │       ├── ShippingApplication.java
│   │       │       ├── controller/
│   │       │       ├── model/
│   │       │       └── service/
│   │       └── resources/
│   │           └── application.yml
│   ├── pom.xml
│   └── Dockerfile
│
├── 06-bpel-orchestration/
│   ├── processes/
│   │   └── PlaceOrderProcess.bpel
│   ├── deployment/
│   │   ├── deploy.xml
│   │   └── process-deployment-descriptor.xml
│   └── wsdl/
│       └── PlaceOrderService.wsdl
│
├── 07-integration/
│   ├── rabbitmq/
│   │   ├── rabbitmq-config.yaml
│   │   ├── queue-definitions.json
│   │   └── docker-compose-rabbitmq.yml
│   ├── producers/
│   │   ├── OrderEventProducer.java
│   │   └── PaymentEventProducer.java
│   └── consumers/
│       ├── PaymentEventConsumer.java
│       └── ShippingEventConsumer.java
│
├── 08-security/
│   ├── ws-security/
│   │   ├── security-policy.xml
│   │   ├── keystore.jks
│   │   └── ws-security-config.xml
│   ├── oauth2/
│   │   ├── oauth2-config.yml
│   │   ├── jwt-config.properties
│   │   └── security-config.java
│   └── certificates/
│       ├── ca-cert.pem
│       └── server-cert.pem
│
├── 09-testing/
│   ├── soap-ui/
│   │   ├── CatalogService-TestSuite.xml
│   │   └── test-results/
│   ├── postman/
│   │   ├── OrdersService-Collection.json
│   │   └── Environment-Variables.json
│   ├── integration-tests/
│   │   ├── bpel-test-cases.xml
│   │   └── queue-status-screenshots/
│   └── performance/
│       ├── jmeter-test-plan.jmx
│       └── load-test-results/
│
├── 10-deployment/
│   ├── docker/
│   │   ├── docker-compose.yml
│   │   └── docker-compose-prod.yml
│   ├── kubernetes/
│   │   ├── namespace.yaml
│   │   ├── catalog-service-deployment.yaml
│   │   ├── orders-service-deployment.yaml
│   │   ├── payments-service-deployment.yaml
│   │   ├── shipping-service-deployment.yaml
│   │   └── ingress.yaml
│   ├── cloud/
│   │   ├── aws-deployment.yml
│   │   ├── azure-deployment.yml
│   │   └── gcp-deployment.yml
│   └── scripts/
│       ├── deploy.sh
│       ├── health-check.sh
│       └── rollback.sh
│
├── 11-documentation/
│   ├── api-docs/
│   │   ├── catalog-service-api.md
│   │   ├── orders-service-api.md
│   │   ├── payments-service-api.md
│   │   └── shipping-service-api.md
│   ├── deployment-guide.md
│   ├── testing-guide.md
│   └── troubleshooting-guide.md
│
├── 12-reports/
│   ├── reflective-report.md
│   ├── trade-off-analysis.md
│   └── viva-presentation/
│       ├── demo-script.md
│       ├── slides.pptx
│       └── demo-scenarios.md
│
└── 13-scripts/
    ├── setup-environment.sh
    ├── start-services.sh
    ├── stop-services.sh
    └── reset-data.sh
```

## Key Template Files to Create

### 1. PROJECT_CHECKLIST.md
```markdown
# CCS3341 SOA Project Checklist

## Task Completion Tracker

### Design & Architecture (15 marks)
- [ ] Task 1: SOA design principles explanation (10 marks)
- [ ] Task 2: Benefits and challenges discussion (5 marks)

### SOAP Services (25 marks)
- [ ] Task 3: WSDL excerpt for CatalogService (6 marks)
- [ ] Task 4: UDDI registry entry metadata (4 marks)
- [ ] Task 5: Java SOAP endpoint implementation (10 marks)
- [ ] Task 6: SOAP UI testing explanation (5 marks)

### REST Services (10 marks)
- [ ] Task 7: OrdersService REST API design (10 marks)

### Orchestration (15 marks)
- [ ] Task 8: BPEL PlaceOrder process design (10 marks)
- [ ] Task 9: BPEL deployment and testing (5 marks)

### Integration (10 marks)
- [ ] Task 10: PaymentsService & ShippingService integration (7 marks)
- [ ] Task 11: Error handling and dead-letter routing (3 marks)

### Security (8 marks)
- [ ] Task 12: WS-Security configuration (4 marks)
- [ ] Task 13: OAuth2 setup (4 marks)

### Quality & Governance (12 marks)
- [ ] Task 14: QoS mechanism configuration (2 marks)
- [ ] Task 15: Governance policy (10 marks)

### Deployment (5 marks)
- [ ] Task 16: Cloud platform deployment (5 marks)

### Final Deliverables
- [ ] All source code completed and tested
- [ ] Configuration files ready
- [ ] Test suites prepared
- [ ] Reflective report written
- [ ] Viva presentation prepared
- [ ] Demo scenarios tested

**Total: 100 marks**
```

### 2. README.md
```markdown
# GlobalBooks SOA Migration Project

## Project Overview
Migration of GlobalBooks Inc.'s monolithic order-processing system to Service-Oriented Architecture (SOA) with four autonomous services: Catalog, Orders, Payments, and Shipping.

## Architecture Components
- **Services**: Java SOAP (Catalog) + Spring Boot REST (Orders, Payments, Shipping)
- **Registry**: UDDI-based service discovery
- **Integration**: RabbitMQ ESB for async messaging
- **Orchestration**: BPEL engine for PlaceOrder workflow
- **Security**: WS-Security (SOAP) + OAuth2 (REST)

## Quick Start
1. `./scripts/setup-environment.sh` - Setup dependencies
2. `./scripts/start-services.sh` - Start all services
3. `./scripts/health-check.sh` - Verify deployment

## Service Endpoints
- **CatalogService (SOAP)**: http://localhost:8080/catalog/soap
- **OrdersService (REST)**: http://localhost:8081/api/v1/orders
- **PaymentsService (REST)**: http://localhost:8082/api/v1/payments
- **ShippingService (REST)**: http://localhost:8083/api/v1/shipping

## Testing
- SOAP UI project: `09-testing/soap-ui/CatalogService-TestSuite.xml`
- Postman collection: `09-testing/postman/OrdersService-Collection.json`

## Documentation
See `11-documentation/` for detailed API docs and guides.
```

### 3. SOA Design Document Template
```markdown
# SOA Design Document - GlobalBooks Migration

## 1. SOA Design Principles Applied

### Service Autonomy
- Each service (Catalog, Orders, Payments, Shipping) owns its data and business logic
- Independent deployment and scaling capabilities
- Isolated failure domains

### Service Discoverability
- UDDI registry for service discovery
- Well-defined service contracts via WSDL and OpenAPI

### Service Composability
- Services designed for orchestration via BPEL
- Standard interfaces enabling reuse

### Service Abstraction
- Implementation details hidden behind service contracts
- Platform-agnostic interfaces

## 2. Service Decomposition Strategy

### Domain-Driven Design
- Services aligned with business capabilities
- Clear bounded contexts for each service

### Data Separation
- Each service maintains its own database
- No shared data stores between services

## 3. Benefits and Challenges

### Key Benefit: Scalability
- Independent scaling based on service-specific load
- Horizontal scaling during peak events

### Primary Challenge: Distributed System Complexity
- Network latency and failure handling
- Data consistency across services
- Increased operational overhead
```

Would you like me to create specific template files for any particular component? I can generate:

1. **Complete Java SOAP service** with WSDL, implementation, and configuration
2. **Spring Boot REST service** with controllers, models, and security
3. **BPEL process definition** for the PlaceOrder workflow
4. **RabbitMQ integration** configuration and messaging code
5. **Security configurations** for both WS-Security and OAuth2
6. **Testing templates** for SOAP UI and Postman
7. **Cloud deployment** configurations for AWS/Azure/GCP

Which specific files would you like me to start with?