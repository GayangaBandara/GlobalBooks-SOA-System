# Service Interaction Diagrams

## 1. Order Processing Workflow (Complete Flow)

```mermaid
sequenceDiagram
    participant Client as Client Application
    participant Gateway as API Gateway
    participant Keycloak as Keycloak OAuth2
    participant Camel as Camel Orchestrator
    participant Catalog as Catalog Service (SOAP)
    participant Orders as Orders Service (REST)
    participant RabbitMQ as RabbitMQ ESB
    participant Payments as Payments Service
    participant Shipping as Shipping Service

    Note over Client,Shipping: Complete Order Processing Flow

    %% Authentication
    Client->>Keycloak: 1. Authenticate & Get JWT Token
    Keycloak-->>Client: 2. JWT Access Token

    %% Order Initiation
    Client->>Gateway: 3. Place Order Request (JWT Bearer)
    Gateway->>Keycloak: 4. Validate JWT Token
    Keycloak-->>Gateway: 5. Token Valid
    Gateway->>Camel: 6. Forward SOAP Request

    %% Price Calculation Phase
    Note over Camel,Catalog: Price Lookup for Each Item
    loop For Each Order Item
        Camel->>Catalog: 7. Get Book Price (SOAP)
        Catalog-->>Camel: 8. Price Response
        Note over Camel: Calculate Running Total
    end

    %% Order Creation Phase
    Camel->>Orders: 9. Create Order (REST + JWT)
    Orders->>Orders: 10. Persist Order to H2 DB
    Orders-->>Camel: 11. Order Created (Order ID)

    %% Event Publishing
    Orders->>RabbitMQ: 12. Publish Order Created Event
    Note over RabbitMQ: Route to Payment & Shipping Queues

    %% Payment Processing
    Camel->>Payments: 13. Process Payment (REST)
    RabbitMQ->>Payments: 14. Payment Event (Async)
    Payments->>Payments: 15. Process Payment Logic
    Payments-->>Camel: 16. Payment Response (Success/Failure)

    %% Conditional Shipping
    alt Payment Successful
        Camel->>Shipping: 17. Create Shipment (REST)
        RabbitMQ->>Shipping: 18. Shipping Event (Async)
        Shipping->>Shipping: 19. Create Shipment Record
        Shipping-->>Camel: 20. Shipment Created (Tracking Number)
    else Payment Failed
        Note over Camel: Skip Shipping Creation
    end

    %% Response Generation
    Camel-->>Gateway: 21. SOAP Response (Order Details)
    Gateway-->>Client: 22. Final Order Response

    %% Background Processing
    Note over Payments,Shipping: Async Event Processing Continues
    Payments->>RabbitMQ: 23. Payment Status Update
    Shipping->>RabbitMQ: 24. Shipping Status Update
```

## 2. Service Discovery and Registration

```mermaid
graph TB
    subgraph "Service Registry (UDDI)"
        UDDI[UDDI Registry<br/>Service Metadata]
    end

    subgraph "Service Providers"
        CATALOG[Catalog Service<br/>SOAP Provider]
        ORDERS[Orders Service<br/>REST Provider]
        PAYMENTS[Payments Service<br/>REST Provider]
        SHIPPING[Shipping Service<br/>REST Provider]
    end

    subgraph "Service Consumers"
        CAMEL[Camel Orchestrator]
        BPEL[BPEL Process Engine]
        CLIENT[External Clients]
    end

    %% Registration Process
    CATALOG -->|1. Register WSDL| UDDI
    ORDERS -->|2. Register OpenAPI| UDDI
    PAYMENTS -->|3. Register OpenAPI| UDDI
    SHIPPING -->|4. Register OpenAPI| UDDI

    %% Discovery Process
    CAMEL -->|5. Discover Services| UDDI
    BPEL -->|6. Lookup Endpoints| UDDI
    CLIENT -->|7. Find Services| UDDI

    %% Service Invocation
    UDDI -.->|8. Return Endpoints| CAMEL
    UDDI -.->|9. Return Endpoints| BPEL
    UDDI -.->|10. Return Endpoints| CLIENT

    classDef registryClass fill:#fff3e0,stroke:#e65100
    classDef providerClass fill:#e1f5fe,stroke:#01579b
    classDef consumerClass fill:#e8f5e8,stroke:#2e7d32

    class UDDI registryClass
    class CATALOG,ORDERS,PAYMENTS,SHIPPING providerClass
    class CAMEL,BPEL,CLIENT consumerClass
```

## 3. Error Handling and Fault Tolerance

```mermaid
graph TD
    subgraph "Client Request"
        CLIENT[Client Request]
    end

    subgraph "Circuit Breaker Pattern"
        CB[Circuit Breaker<br/>State: CLOSED/OPEN/HALF-OPEN]
    end

    subgraph "Service Invocation"
        SERVICE[Target Service]
    end

    subgraph "Retry Mechanism"
        RETRY[Retry Logic<br/>Max: 3 attempts<br/>Backoff: Exponential]
    end

    subgraph "Fallback Handling"
        FALLBACK[Fallback Response<br/>Cached Data/Default Values]
    end

    subgraph "Dead Letter Queue"
        DLQ[Dead Letter Queue<br/>Failed Messages]
    end

    CLIENT --> CB
    CB -->|State: CLOSED| SERVICE
    CB -->|State: OPEN| FALLBACK
    CB -->|State: HALF-OPEN| SERVICE

    SERVICE -->|Success| CLIENT
    SERVICE -->|Failure| RETRY
    RETRY -->|Retry Exhausted| CB
    RETRY -->|Retry Success| CLIENT
    CB -->|Failure Threshold| FALLBACK
    SERVICE -->|Persistent Failure| DLQ

    classDef clientClass fill:#e3f2fd,stroke:#1976d2
    classDef circuitClass fill:#fff3e0,stroke:#f57c00
    classDef serviceClass fill:#e1f5fe,stroke:#01579b
    classDef retryClass fill:#f3e5f5,stroke:#7b1fa2
    classDef fallbackClass fill:#ffebee,stroke:#c62828
    classDef dlqClass fill:#fafafa,stroke:#424242

    class CLIENT clientClass
    class CB circuitClass
    class SERVICE serviceClass
    class RETRY retryClass
    class FALLBACK fallbackClass
    class DLQ dlqClass
```

## 4. Data Consistency and Transaction Management

```mermaid
sequenceDiagram
    participant Orchestrator as Camel/BPEL
    participant Orders as Orders Service
    participant Payments as Payments Service
    participant Shipping as Shipping Service
    participant Compensation as Compensation Handler

    Note over Orchestrator,Compensation: Saga Pattern Implementation

    %% Happy Path
    Orchestrator->>Orders: 1. Create Order
    Orders-->>Orchestrator: 2. Order Created (Tx1)
    
    Orchestrator->>Payments: 3. Process Payment
    Payments-->>Orchestrator: 4. Payment Success (Tx2)
    
    Orchestrator->>Shipping: 5. Create Shipment
    Shipping-->>Orchestrator: 6. Shipment Created (Tx3)

    Note over Orchestrator: All transactions successful

    %% Failure Scenario
    rect rgb(255, 240, 240)
        Note over Orchestrator,Compensation: Failure Scenario - Compensation Required
        
        Orchestrator->>Orders: 7. Create Order
        Orders-->>Orchestrator: 8. Order Created (Tx1)
        
        Orchestrator->>Payments: 9. Process Payment
        Payments-->>Orchestrator: 10. Payment Failed (Tx2 Failed)
        
        Note over Orchestrator: Trigger Compensation
        
        Orchestrator->>Compensation: 11. Compensate Order
        Compensation->>Orders: 12. Cancel Order (Compensating Tx1)
        Orders-->>Compensation: 13. Order Cancelled
        Compensation-->>Orchestrator: 14. Compensation Complete
    end
```

## 5. Security Flow Diagram

```mermaid
sequenceDiagram
    participant Client as Client Application
    participant Gateway as API Gateway
    participant Keycloak as Keycloak Server
    participant JWT as JWT Validator
    participant SOAP as SOAP Service (WS-Security)
    participant REST as REST Service (OAuth2)

    Note over Client,REST: Multi-Protocol Security Implementation

    %% OAuth2 Flow for REST Services
    rect rgb(240, 248, 255)
        Note over Client,REST: OAuth2 Flow for REST Services
        Client->>Keycloak: 1. Authentication Request
        Keycloak-->>Client: 2. JWT Access Token
        Client->>Gateway: 3. API Request + Bearer Token
        Gateway->>JWT: 4. Validate JWT
        JWT-->>Gateway: 5. Token Valid + Claims
        Gateway->>REST: 6. Forward Request + User Context
        REST-->>Gateway: 7. Response
        Gateway-->>Client: 8. Final Response
    end

    %% WS-Security Flow for SOAP Services
    rect rgb(248, 255, 240)
        Note over Client,SOAP: WS-Security Flow for SOAP Services
        Client->>Gateway: 9. SOAP Request + WS-Security Header
        Gateway->>SOAP: 10. Forward SOAP Request
        SOAP->>SOAP: 11. Validate UsernameToken
        SOAP-->>Gateway: 12. SOAP Response
        Gateway-->>Client: 13. Final SOAP Response
    end

    %% Service-to-Service Authentication
    rect rgb(255, 248, 240)
        Note over Gateway,REST: Service-to-Service Authentication
        Gateway->>Keycloak: 14. Get Service Token (Client Credentials)
        Keycloak-->>Gateway: 15. Service JWT Token
        Gateway->>REST: 16. Internal Request + Service Token
        REST->>JWT: 17. Validate Service Token
        JWT-->>REST: 18. Token Valid
        REST-->>Gateway: 19. Internal Response
    end
```

## 6. Performance and Scalability Architecture

```mermaid
graph TB
    subgraph "Load Balancer Layer"
        LB[Load Balancer<br/>nginx/HAProxy<br/>Round Robin]
    end

    subgraph "Service Instances"
        subgraph "Catalog Service Cluster"
            CAT1[Catalog Instance 1<br/>Port: 8080]
            CAT2[Catalog Instance 2<br/>Port: 8081]
            CAT3[Catalog Instance 3<br/>Port: 8082]
        end

        subgraph "Orders Service Cluster"
            ORD1[Orders Instance 1<br/>Port: 8090]
            ORD2[Orders Instance 2<br/>Port: 8091]
        end

        subgraph "Payments Service Cluster"
            PAY1[Payments Instance 1<br/>Port: 8100]
            PAY2[Payments Instance 2<br/>Port: 8101]
        end
    end

    subgraph "Caching Layer"
        REDIS[Redis Cache<br/>Session & Data Caching]
    end

    subgraph "Message Broker Cluster"
        RMQ1[RabbitMQ Node 1]
        RMQ2[RabbitMQ Node 2]
        RMQ3[RabbitMQ Node 3]
    end

    subgraph "Database Layer"
        DB_MASTER[(Primary Database)]
        DB_REPLICA1[(Read Replica 1)]
        DB_REPLICA2[(Read Replica 2)]
    end

    %% Load balancing
    LB --> CAT1
    LB --> CAT2
    LB --> CAT3
    LB --> ORD1
    LB --> ORD2
    LB --> PAY1
    LB --> PAY2

    %% Caching
    CAT1 --> REDIS
    CAT2 --> REDIS
    CAT3 --> REDIS
    ORD1 --> REDIS
    ORD2 --> REDIS

    %% Message broker clustering
    RMQ1 -.-> RMQ2
    RMQ2 -.-> RMQ3
    RMQ3 -.-> RMQ1

    %% Database replication
    DB_MASTER --> DB_REPLICA1
    DB_MASTER --> DB_REPLICA2

    %% Service to database connections
    CAT1 --> DB_MASTER
    CAT2 --> DB_REPLICA1
    CAT3 --> DB_REPLICA2
    ORD1 --> DB_MASTER
    ORD2 --> DB_REPLICA1

    classDef lbClass fill:#e3f2fd,stroke:#1976d2
    classDef serviceClass fill:#e1f5fe,stroke:#01579b
    classDef cacheClass fill:#fff3e0,stroke:#f57c00
    classDef brokerClass fill:#f3e5f5,stroke:#7b1fa2
    classDef dbClass fill:#e8f5e8,stroke:#2e7d32

    class LB lbClass
    class CAT1,CAT2,CAT3,ORD1,ORD2,PAY1,PAY2 serviceClass
    class REDIS cacheClass
    class RMQ1,RMQ2,RMQ3 brokerClass
    class DB_MASTER,DB_REPLICA1,DB_REPLICA2 dbClass
```

## 7. Monitoring and Observability

```mermaid
graph TB
    subgraph "Application Services"
        CATALOG[Catalog Service<br/>Spring Actuator]
        ORDERS[Orders Service<br/>Spring Actuator]
        PAYMENTS[Payments Service<br/>Spring Actuator]
        SHIPPING[Shipping Service<br/>Spring Actuator]
    end

    subgraph "Metrics Collection"
        PROMETHEUS[Prometheus<br/>Metrics Server]
        MICROMETER[Micrometer<br/>Metrics Registry]
    end

    subgraph "Logging"
        LOGBACK[Logback<br/>Structured Logging]
        ELK[ELK Stack<br/>Elasticsearch, Logstash, Kibana]
    end

    subgraph "Tracing"
        ZIPKIN[Zipkin<br/>Distributed Tracing]
        JAEGER[Jaeger<br/>Request Tracing]
    end

    subgraph "Visualization"
        GRAFANA[Grafana<br/>Dashboards & Alerts]
        KIBANA[Kibana<br/>Log Analysis]
    end

    subgraph "Health Monitoring"
        HEALTH[Health Check Endpoints<br/>/actuator/health]
        READINESS[Readiness Probes<br/>Kubernetes]
        LIVENESS[Liveness Probes<br/>Kubernetes]
    end

    %% Metrics flow
    CATALOG --> MICROMETER
    ORDERS --> MICROMETER
    PAYMENTS --> MICROMETER
    SHIPPING --> MICROMETER
    MICROMETER --> PROMETHEUS
    PROMETHEUS --> GRAFANA

    %% Logging flow
    CATALOG --> LOGBACK
    ORDERS --> LOGBACK
    PAYMENTS --> LOGBACK
    SHIPPING --> LOGBACK
    LOGBACK --> ELK
    ELK --> KIBANA

    %% Tracing flow
    CATALOG --> ZIPKIN
    ORDERS --> ZIPKIN
    PAYMENTS --> ZIPKIN
    SHIPPING --> ZIPKIN
    ZIPKIN --> JAEGER

    %% Health monitoring
    CATALOG --> HEALTH
    ORDERS --> HEALTH
    PAYMENTS --> HEALTH
    SHIPPING --> HEALTH
    HEALTH --> READINESS
    HEALTH --> LIVENESS

    classDef serviceClass fill:#e1f5fe,stroke:#01579b
    classDef metricsClass fill:#e8f5e8,stroke:#2e7d32
    classDef loggingClass fill:#fff3e0,stroke:#f57c00
    classDef tracingClass fill:#f3e5f5,stroke:#7b1fa2
    classDef visualClass fill:#e3f2fd,stroke:#1976d2
    classDef healthClass fill:#ffebee,stroke:#c62828

    class CATALOG,ORDERS,PAYMENTS,SHIPPING serviceClass
    class PROMETHEUS,MICROMETER metricsClass
    class LOGBACK,ELK loggingClass
    class ZIPKIN,JAEGER tracingClass
    class GRAFANA,KIBANA visualClass
    class HEALTH,READINESS,LIVENESS healthClass