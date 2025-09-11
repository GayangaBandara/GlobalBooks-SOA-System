# GlobalBooks SOA System Architecture Diagrams

## 1. Overall System Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        WEB[Web Client]
        MOBILE[Mobile App]
        API[API Client]
    end

    subgraph "Security Layer"
        KEYCLOAK[Keycloak OAuth2 Server<br/>Port: 9000]
        GATEWAY[API Gateway<br/>Load Balancer]
    end

    subgraph "Orchestration Layer"
        CAMEL[Apache Camel<br/>Orchestration Service<br/>Port: 8085]
        BPEL[BPEL Process Engine<br/>PlaceOrderProcess]
    end

    subgraph "Core Services Layer"
        CATALOG[Catalog Service<br/>SOAP/JAX-WS<br/>Port: 8080]
        ORDERS[Orders Service<br/>REST/Spring Boot<br/>Port: 8081]
        PAYMENTS[Payments Service<br/>REST/Spring Boot<br/>Port: 8083]
        SHIPPING[Shipping Service<br/>REST/Spring Boot<br/>Port: 8084]
    end

    subgraph "Integration Layer"
        RABBITMQ[RabbitMQ ESB<br/>Port: 5672<br/>Management: 15672]
        UDDI[UDDI Registry<br/>Service Discovery]
    end

    subgraph "Data Layer"
        CATALOG_DB[(Catalog DB<br/>H2)]
        ORDERS_DB[(Orders DB<br/>H2)]
        PAYMENTS_DB[(Payments DB<br/>H2)]
        SHIPPING_DB[(Shipping DB<br/>H2)]
        KEYCLOAK_DB[(Keycloak DB<br/>PostgreSQL)]
    end

    %% Client connections
    WEB --> GATEWAY
    MOBILE --> GATEWAY
    API --> GATEWAY

    %% Security flow
    GATEWAY --> KEYCLOAK
    GATEWAY --> CAMEL
    GATEWAY --> CATALOG
    GATEWAY --> ORDERS

    %% Orchestration connections
    CAMEL --> CATALOG
    CAMEL --> ORDERS
    CAMEL --> PAYMENTS
    CAMEL --> SHIPPING
    BPEL --> CATALOG
    BPEL --> ORDERS
    BPEL --> PAYMENTS
    BPEL --> SHIPPING

    %% Service to database connections
    CATALOG --> CATALOG_DB
    ORDERS --> ORDERS_DB
    PAYMENTS --> PAYMENTS_DB
    SHIPPING --> SHIPPING_DB
    KEYCLOAK --> KEYCLOAK_DB

    %% Message broker connections
    ORDERS --> RABBITMQ
    RABBITMQ --> PAYMENTS
    RABBITMQ --> SHIPPING

    %% Service discovery
    CATALOG --> UDDI
    ORDERS --> UDDI
    PAYMENTS --> UDDI
    SHIPPING --> UDDI

    %% Styling
    classDef serviceClass fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    classDef orchestrationClass fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef dataClass fill:#e8f5e8,stroke:#1b5e20,stroke-width:2px
    classDef integrationClass fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef securityClass fill:#ffebee,stroke:#b71c1c,stroke-width:2px

    class CATALOG,ORDERS,PAYMENTS,SHIPPING serviceClass
    class CAMEL,BPEL orchestrationClass
    class CATALOG_DB,ORDERS_DB,PAYMENTS_DB,SHIPPING_DB,KEYCLOAK_DB dataClass
    class RABBITMQ,UDDI integrationClass
    class KEYCLOAK,GATEWAY securityClass
```

## 2. Service Communication Flow

```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant Camel
    participant Catalog
    participant Orders
    participant RabbitMQ
    participant Payments
    participant Shipping

    Client->>Gateway: Place Order Request
    Gateway->>Camel: Forward Request (SOAP)
    
    Note over Camel: Order Processing Orchestration
    
    loop For each item
        Camel->>Catalog: Get Book Price (SOAP)
        Catalog-->>Camel: Price Response
    end
    
    Camel->>Orders: Create Order (REST)
    Orders-->>Camel: Order Created
    
    Orders->>RabbitMQ: Publish Order Event
    
    Camel->>Payments: Process Payment (REST)
    Payments-->>Camel: Payment Response
    
    RabbitMQ->>Payments: Payment Event
    RabbitMQ->>Shipping: Shipping Event
    
    alt Payment Successful
        Camel->>Shipping: Create Shipment (REST)
        Shipping-->>Camel: Shipment Created
    end
    
    Camel-->>Gateway: Order Response (SOAP)
    Gateway-->>Client: Final Response
```

## 3. Message Flow Architecture

```mermaid
graph LR
    subgraph "Event Publishers"
        ORDERS[Orders Service]
    end

    subgraph "Message Broker"
        EXCHANGE[Order Exchange]
        PAYMENT_Q[Payment Queue]
        SHIPPING_Q[Shipping Queue]
        DLQ[Dead Letter Queue]
    end

    subgraph "Event Consumers"
        PAYMENTS[Payments Service]
        SHIPPING[Shipping Service]
    end

    ORDERS -->|Publish Order Event| EXCHANGE
    EXCHANGE -->|Route to| PAYMENT_Q
    EXCHANGE -->|Route to| SHIPPING_Q
    PAYMENT_Q -->|Consume| PAYMENTS
    SHIPPING_Q -->|Consume| SHIPPING
    PAYMENT_Q -.->|Failed Messages| DLQ
    SHIPPING_Q -.->|Failed Messages| DLQ

    classDef publisherClass fill:#e3f2fd,stroke:#0277bd
    classDef brokerClass fill:#fff8e1,stroke:#f57f17
    classDef consumerClass fill:#e8f5e8,stroke:#388e3c

    class ORDERS publisherClass
    class EXCHANGE,PAYMENT_Q,SHIPPING_Q,DLQ brokerClass
    class PAYMENTS,SHIPPING consumerClass
```

## 4. Security Architecture

```mermaid
graph TB
    subgraph "External Clients"
        WEB_CLIENT[Web Application]
        MOBILE_CLIENT[Mobile App]
        API_CLIENT[Third-party API]
    end

    subgraph "Security Layer"
        KEYCLOAK[Keycloak Identity Provider<br/>OAuth2 Authorization Server]
        JWT_VALIDATOR[JWT Token Validator]
    end

    subgraph "API Gateway"
        GATEWAY[Load Balancer/<br/>API Gateway]
    end

    subgraph "SOAP Services"
        CATALOG[Catalog Service<br/>WS-Security]
    end

    subgraph "REST Services"
        ORDERS[Orders Service<br/>OAuth2 Resource Server]
        PAYMENTS[Payments Service<br/>OAuth2 Resource Server]
        SHIPPING[Shipping Service<br/>OAuth2 Resource Server]
    end

    %% Authentication Flow
    WEB_CLIENT -->|1. Login Request| KEYCLOAK
    KEYCLOAK -->|2. JWT Token| WEB_CLIENT
    WEB_CLIENT -->|3. API Request + JWT| GATEWAY
    GATEWAY -->|4. Validate Token| JWT_VALIDATOR
    JWT_VALIDATOR -->|5. Token Valid| GATEWAY

    %% Service Access
    GATEWAY -->|SOAP + WS-Security| CATALOG
    GATEWAY -->|REST + JWT Bearer| ORDERS
    GATEWAY -->|REST + JWT Bearer| PAYMENTS
    GATEWAY -->|REST + JWT Bearer| SHIPPING

    %% Mobile and API flows
    MOBILE_CLIENT --> KEYCLOAK
    API_CLIENT --> KEYCLOAK

    classDef clientClass fill:#e3f2fd,stroke:#1976d2
    classDef securityClass fill:#ffebee,stroke:#d32f2f
    classDef soapClass fill:#f3e5f5,stroke:#7b1fa2
    classDef restClass fill:#e8f5e8,stroke:#388e3c

    class WEB_CLIENT,MOBILE_CLIENT,API_CLIENT clientClass
    class KEYCLOAK,JWT_VALIDATOR,GATEWAY securityClass
    class CATALOG soapClass
    class ORDERS,PAYMENTS,SHIPPING restClass
```

## 5. Data Architecture

```mermaid
graph TB
    subgraph "Service Layer"
        CATALOG_SVC[Catalog Service]
        ORDERS_SVC[Orders Service]
        PAYMENTS_SVC[Payments Service]
        SHIPPING_SVC[Shipping Service]
        KEYCLOAK_SVC[Keycloak Service]
    end

    subgraph "Database Layer"
        CATALOG_DB[(Catalog Database<br/>H2 In-Memory<br/>Books, Pricing, Inventory)]
        ORDERS_DB[(Orders Database<br/>H2 In-Memory<br/>Orders, Order Items)]
        PAYMENTS_DB[(Payments Database<br/>H2 In-Memory<br/>Transactions, Payment History)]
        SHIPPING_DB[(Shipping Database<br/>H2 In-Memory<br/>Shipments, Tracking)]
        KEYCLOAK_DB[(Keycloak Database<br/>PostgreSQL<br/>Users, Roles, Tokens)]
    end

    %% Service to Database connections
    CATALOG_SVC -->|JPA/Hibernate| CATALOG_DB
    ORDERS_SVC -->|JPA/Hibernate| ORDERS_DB
    PAYMENTS_SVC -->|JPA/Hibernate| PAYMENTS_DB
    SHIPPING_SVC -->|JPA/Hibernate| SHIPPING_DB
    KEYCLOAK_SVC -->|JDBC| KEYCLOAK_DB

    %% Data isolation principle
    CATALOG_DB -.->|No Direct Access| ORDERS_SVC
    ORDERS_DB -.->|No Direct Access| PAYMENTS_SVC
    PAYMENTS_DB -.->|No Direct Access| SHIPPING_SVC

    classDef serviceClass fill:#e1f5fe,stroke:#01579b
    classDef h2Class fill:#e8f5e8,stroke:#2e7d32
    classDef postgresClass fill:#e3f2fd,stroke:#1565c0

    class CATALOG_SVC,ORDERS_SVC,PAYMENTS_SVC,SHIPPING_SVC,KEYCLOAK_SVC serviceClass
    class CATALOG_DB,ORDERS_DB,PAYMENTS_DB,SHIPPING_DB h2Class
    class KEYCLOAK_DB postgresClass
```

## 6. Deployment Architecture

```mermaid
graph TB
    subgraph "Docker Network: globalbooks-network"
        subgraph "Infrastructure Services"
            RABBITMQ_C[RabbitMQ Container<br/>Port: 5672, 15672]
            POSTGRES_C[PostgreSQL Container<br/>Port: 5432]
            KEYCLOAK_C[Keycloak Container<br/>Port: 9000]
        end

        subgraph "Application Services"
            CATALOG_C[Catalog Service Container<br/>Port: 8080]
            ORDERS_C[Orders Service Container<br/>Port: 8081]
            PAYMENTS_C[Payments Service Container<br/>Port: 8083]
            SHIPPING_C[Shipping Service Container<br/>Port: 8084]
            CAMEL_C[Camel Orchestration Container<br/>Port: 8085]
        end
    end

    subgraph "External Access"
        LB[Load Balancer<br/>nginx/HAProxy]
        CLIENT[External Clients]
    end

    subgraph "Persistent Storage"
        RABBITMQ_VOL[RabbitMQ Volume]
        POSTGRES_VOL[PostgreSQL Volume]
    end

    %% External connections
    CLIENT --> LB
    LB --> CATALOG_C
    LB --> ORDERS_C
    LB --> CAMEL_C

    %% Service dependencies
    KEYCLOAK_C --> POSTGRES_C
    ORDERS_C --> RABBITMQ_C
    PAYMENTS_C --> RABBITMQ_C
    SHIPPING_C --> RABBITMQ_C
    CAMEL_C --> CATALOG_C
    CAMEL_C --> ORDERS_C
    CAMEL_C --> PAYMENTS_C
    CAMEL_C --> SHIPPING_C

    %% Volume mounts
    RABBITMQ_C --> RABBITMQ_VOL
    POSTGRES_C --> POSTGRES_VOL

    classDef infraClass fill:#fff3e0,stroke:#e65100
    classDef appClass fill:#e1f5fe,stroke:#01579b
    classDef storageClass fill:#f3e5f5,stroke:#7b1fa2
    classDef externalClass fill:#e8f5e8,stroke:#2e7d32

    class RABBITMQ_C,POSTGRES_C,KEYCLOAK_C infraClass
    class CATALOG_C,ORDERS_C,PAYMENTS_C,SHIPPING_C,CAMEL_C appClass
    class RABBITMQ_VOL,POSTGRES_VOL storageClass
    class LB,CLIENT externalClass
```

## 7. BPEL Process Flow

```mermaid
graph TD
    START([Start: Receive Order]) --> INIT[Initialize Variables<br/>totalAmount = 0<br/>currentItem = 0]
    INIT --> LOOP{For Each Item<br/>currentItem < itemCount}
    
    LOOP -->|Yes| GET_PRICE[Get Book Price<br/>from Catalog Service]
    GET_PRICE --> CALC[Calculate Subtotal<br/>Add to Total Amount]
    CALC --> INCREMENT[Increment currentItem]
    INCREMENT --> LOOP
    
    LOOP -->|No| CREATE_ORDER[Create Order<br/>via Orders Service]
    CREATE_ORDER --> PROCESS_PAYMENT[Process Payment<br/>via Payments Service]
    
    PROCESS_PAYMENT --> PAYMENT_CHECK{Payment<br/>Successful?}
    PAYMENT_CHECK -->|Yes| CREATE_SHIPMENT[Create Shipment<br/>via Shipping Service]
    PAYMENT_CHECK -->|No| PREPARE_RESPONSE[Prepare Response<br/>Status: FAILED]
    
    CREATE_SHIPMENT --> PREPARE_SUCCESS[Prepare Response<br/>Status: SUCCESS<br/>Include Tracking Number]
    PREPARE_SUCCESS --> REPLY[Reply to Client]
    PREPARE_RESPONSE --> REPLY
    REPLY --> END([End])

    %% Error handling
    GET_PRICE -.->|Catalog Fault| CATALOG_ERROR[Catalog Error Handler]
    PROCESS_PAYMENT -.->|Payment Fault| PAYMENT_ERROR[Payment Error Handler]
    CREATE_SHIPMENT -.->|Shipping Fault| SHIPPING_ERROR[Shipping Error Handler]
    
    CATALOG_ERROR --> ERROR_REPLY[Reply with Error]
    PAYMENT_ERROR --> ERROR_REPLY
    SHIPPING_ERROR --> ERROR_REPLY
    ERROR_REPLY --> END

    classDef startEndClass fill:#c8e6c9,stroke:#2e7d32
    classDef processClass fill:#e1f5fe,stroke:#01579b
    classDef decisionClass fill:#fff3e0,stroke:#f57c00
    classDef errorClass fill:#ffebee,stroke:#c62828

    class START,END startEndClass
    class INIT,GET_PRICE,CALC,INCREMENT,CREATE_ORDER,PROCESS_PAYMENT,CREATE_SHIPMENT,PREPARE_SUCCESS,PREPARE_RESPONSE,REPLY processClass
    class LOOP,PAYMENT_CHECK decisionClass
    class CATALOG_ERROR,PAYMENT_ERROR,SHIPPING_ERROR,ERROR_REPLY errorClass
```

## 8. Technology Stack Layers

```mermaid
graph TB
    subgraph "Presentation Layer"
        WEB[Web UI<br/>HTML/CSS/JavaScript]
        MOBILE[Mobile Apps<br/>iOS/Android]
        API_DOCS[API Documentation<br/>Swagger/OpenAPI]
    end

    subgraph "API Gateway Layer"
        GATEWAY[Load Balancer<br/>nginx/HAProxy]
        RATE_LIMIT[Rate Limiting]
        AUTH[Authentication]
    end

    subgraph "Service Layer"
        SOAP_SVC[SOAP Services<br/>JAX-WS/Spring WS]
        REST_SVC[REST Services<br/>Spring Boot/Spring MVC]
        ORCHESTRATION[Orchestration<br/>Apache Camel/BPEL]
    end

    subgraph "Integration Layer"
        ESB[Enterprise Service Bus<br/>RabbitMQ/AMQP]
        REGISTRY[Service Registry<br/>UDDI]
    end

    subgraph "Security Layer"
        OAUTH[OAuth2/JWT<br/>Keycloak]
        WS_SEC[WS-Security<br/>UsernameToken]
    end

    subgraph "Data Layer"
        ORM[Object-Relational Mapping<br/>JPA/Hibernate]
        H2_DB[In-Memory Database<br/>H2]
        POSTGRES[Relational Database<br/>PostgreSQL]
    end

    subgraph "Infrastructure Layer"
        DOCKER[Containerization<br/>Docker/Docker Compose]
        MONITORING[Monitoring<br/>Spring Actuator]
        LOGGING[Logging<br/>SLF4J/Logback]
    end

    %% Layer connections
    WEB --> GATEWAY
    MOBILE --> GATEWAY
    GATEWAY --> SOAP_SVC
    GATEWAY --> REST_SVC
    SOAP_SVC --> ESB
    REST_SVC --> ESB
    ORCHESTRATION --> SOAP_SVC
    ORCHESTRATION --> REST_SVC
    SOAP_SVC --> WS_SEC
    REST_SVC --> OAUTH
    SOAP_SVC --> ORM
    REST_SVC --> ORM
    ORM --> H2_DB
    ORM --> POSTGRES
    ESB --> DOCKER
    OAUTH --> POSTGRES

    classDef presentationClass fill:#e8eaf6,stroke:#3f51b5
    classDef gatewayClass fill:#f3e5f5,stroke:#9c27b0
    classDef serviceClass fill:#e1f5fe,stroke:#03a9f4
    classDef integrationClass fill:#fff3e0,stroke:#ff9800
    classDef securityClass fill:#ffebee,stroke:#f44336
    classDef dataClass fill:#e8f5e8,stroke:#4caf50
    classDef infraClass fill:#fafafa,stroke:#607d8b

    class WEB,MOBILE,API_DOCS presentationClass
    class GATEWAY,RATE_LIMIT,AUTH gatewayClass
    class SOAP_SVC,REST_SVC,ORCHESTRATION serviceClass
    class ESB,REGISTRY integrationClass
    class OAUTH,WS_SEC securityClass
    class ORM,H2_DB,POSTGRES dataClass
    class DOCKER,MONITORING,LOGGING infraClass