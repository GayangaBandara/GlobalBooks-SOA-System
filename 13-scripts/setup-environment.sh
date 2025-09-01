#!/bin/bash
# setup-environment.sh - Environment Setup Script

set -e

echo "🚀 Setting up GlobalBooks SOA Development Environment..."

# Check prerequisites
echo "📋 Checking prerequisites..."
command -v docker >/dev/null 2>&1 || { echo "❌ Docker is required but not installed."; exit 1; }
command -v docker-compose >/dev/null 2>&1 || { echo "❌ Docker Compose is required but not installed."; exit 1; }
command -v java >/dev/null 2>&1 || { echo "❌ Java 11+ is required but not installed."; exit 1; }
command -v maven >/dev/null 2>&1 || { echo "❌ Maven is required but not installed."; exit 1; }

echo "✅ Prerequisites check passed!"

# Create project directories
echo "📁 Creating project directories..."
mkdir -p logs
mkdir -p data/{postgres,rabbitmq,redis}
mkdir -p ssl-certs
mkdir -p monitoring/{prometheus,grafana}

# Generate SSL certificates for development
echo "🔐 Generating SSL certificates..."
if [ ! -f ssl-certs/server.crt ]; then
    openssl req -x509 -newkey rsa:4096 -keyout ssl-certs/server.key \
        -out ssl-certs/server.crt -days 365 -nodes \
        -subj "/C=US/ST=CA/L=San Francisco/O=GlobalBooks/CN=localhost"
    echo "✅ SSL certificates generated"
fi

# Build all services
echo "🔨 Building services..."
services=("catalog-service" "orders-service" "payments-service" "shipping-service")

for service in "${services[@]}"; do
    if [ -d "02-$service" ] || [ -d "03-$service" ] || [ -d "04-$service" ] || [ -d "05-$service" ]; then
        echo "Building $service..."
        cd "*-$service" 2>/dev/null || cd "0*-$service"
        mvn clean package -DskipTests
        docker build -t "globalbooks/$service:latest" .
        cd ..
        echo "✅ $service built successfully"
    fi
done

# Start infrastructure services
echo "🏗️  Starting infrastructure services..."
docker-compose up -d postgres rabbitmq redis
echo "⏳ Waiting for infrastructure services to be ready..."
sleep 30

# Initialize databases
echo "💾 Initializing databases..."
docker-compose exec -T postgres psql -U postgres -c "
    CREATE DATABASE IF NOT EXISTS catalog_db;
    CREATE DATABASE IF NOT EXISTS orders_db;
    CREATE DATABASE IF NOT EXISTS payments_db;  
    CREATE DATABASE IF NOT EXISTS shipping_db;
"

# Setup RabbitMQ queues and exchanges
echo "📨 Setting up message queues..."
# Wait for RabbitMQ to be ready
until docker-compose exec rabbitmq rabbitmqctl node_health_check; do
    echo "Waiting for RabbitMQ..."
    sleep 5
done

# Configure RabbitMQ
docker-compose exec rabbitmq rabbitmqctl add_user admin password123 || true
docker-compose exec rabbitmq rabbitmqctl set_user_tags admin administrator || true
docker-compose exec rabbitmq rabbitmqctl set_permissions -p / admin ".*" ".*" ".*" || true

echo "✅ Environment setup completed!"
echo ""
echo "🎯 Next steps:"
echo "1. Run './scripts/start-services.sh' to start all services"
echo "2. Run './scripts/health-check.sh' to verify deployment"
echo "3. Access services at:"
echo "   - Catalog Service (SOAP): http://localhost:8080/catalog/soap?wsdl"
echo "   - Orders Service (REST): http://localhost:8081/api/v1/orders"
echo "   - RabbitMQ Management: http://localhost:15672 (admin/password123)"
echo "   - Grafana Dashboard: http://localhost:3000 (admin/admin123)"

#!/bin/bash
# start-services.sh - Start All Services Script

set -e

echo "🚀 Starting GlobalBooks SOA Services..."

# Start all services with dependencies
docker-compose up -d

echo "⏳ Waiting for services to be ready..."
sleep 60

# Check service health
services=("catalog-service:8080/catalog/health" 
         "orders-service:8081/api/v1/orders/health"
         "payments-service:8082/api/v1/payments/health"
         "shipping-service:8083/api/v1/shipping/health")

echo "🏥 Checking service health..."
for service in "${services[@]}"; do
    service_name=$(echo $service | cut -d: -f1)
    health_endpoint="http://localhost:${service#*:}"
    
    echo -n "Checking $service_name... "
    for i in {1..12}; do
        if curl -s "$health_endpoint" > /dev/null 2>&1; then
            echo "✅ Healthy"
            break
        fi
        if [ $i -eq 12 ]; then
            echo "❌ Unhealthy"
        else
            sleep 5
        fi
    done
done

echo ""
echo "🎉 All services started successfully!"
echo ""
echo "📊 Service Endpoints:"
echo "  Catalog Service: http://localhost:8080/catalog/soap?wsdl"
echo "  Orders Service: http://localhost:8081/api/v1/orders"
echo "  Payments Service: http://localhost:8082/api/v1/payments"
echo "  Shipping Service: http://localhost:8083/api/v1/shipping"
echo "  BPEL Engine: http://localhost:8084/ode"
echo ""
echo "🔧 Management Interfaces:"
echo "  RabbitMQ: http://localhost:15672"
echo "  Prometheus: http://localhost:9090"
echo "  Grafana: http://localhost:3000"

#!/bin/bash  
# health-check.sh - Comprehensive Health Check Script

set -e

echo "🏥 GlobalBooks SOA Health Check"
echo "================================="

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check service health
check_service() {
    local service_name=$1
    local health_url=$2
    local expected_status=${3:-200}
    
    echo -n "Checking $service_name... "
    
    if response=$(curl -s -o /dev/null -w "%{http_code}" "$health_url" 2>/dev/null); then
        if [ "$response" -eq "$expected_status" ]; then
            echo -e "${GREEN}✅ Healthy${NC} (HTTP $response)"
        else
            echo -e "${YELLOW}⚠️  Warning${NC} (HTTP $response)"
        fi
    else
        echo -e "${RED}❌ Unreachable${NC}"
    fi
}

# Function to check database connectivity
check_database() {
    echo -n "Checking PostgreSQL... "
    if docker-compose exec -T postgres pg_isready -U postgres >/dev/null 2>&1; then
        echo -e "${GREEN}✅ Connected${NC}"
    else
        echo -e "${RED}❌ Connection Failed${NC}"
    fi
}

# Function to check message queue
check_rabbitmq() {
    echo -n "Checking RabbitMQ... "
    if docker-compose exec rabbitmq rabbitmqctl node_health_check >/dev/null 2>&1; then
        echo -e "${GREEN}✅ Running${NC}"
    else
        echo -e "${RED}❌ Not Running${NC}"
    fi
}

# Infrastructure Health Checks
echo "🏗️  Infrastructure Services:"
check_database
check_rabbitmq
check_service "Redis" "http://localhost:6379" 

# Application Health Checks
echo ""
echo "🚀 Application Services:"
check_service "Catalog Service" "http://localhost:8080/catalog/health"
check_service "Orders Service" "http://localhost:8081/api/v1/orders/health"
check_service "Payments Service" "http://localhost:8082/api/v1/payments/health"
check_service "Shipping Service" "http://localhost:8083/api/v1/shipping/health"
check_service "BPEL Engine" "http://localhost:8084/ode"

# Integration Tests
echo ""
echo "🔗 Integration Tests:"

# Test SOAP service
echo -n "Testing Catalog SOAP service... "
if soap_response=$(curl -s -X POST \
    -H "Content-Type: text/xml; charset=utf-8" \
    -H "SOAPAction: http://catalog.globalbooks.com/searchBooks" \
    -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:cat="http://catalog.globalbooks.com/">
          <soapenv:Body>
            <cat:searchBooksRequest>
              <cat:query>Java</cat:query>
            </cat:searchBooksRequest>
          </soapenv:Body>
        </soapenv:Envelope>' \
    "http://localhost:8080/catalog/soap" 2>/dev/null); then
    if echo "$soap_response" | grep -q "searchBooksResponse"; then
        echo -e "${GREEN}✅ SOAP Working${NC}"
    else
        echo -e "${YELLOW}⚠️  SOAP Response Invalid${NC}"
    fi
else
    echo -e "${RED}❌ SOAP Failed${NC}"
fi

# Test REST service
echo -n "Testing Orders REST service... "
if rest_response=$(curl -s -X GET \
    -H "Authorization: Bearer test-token" \
    "http://localhost:8081/api/v1/orders/health" 2>/dev/null); then
    if echo "$rest_response" | grep -q "UP"; then
        echo -e "${GREEN}✅ REST Working${NC}"
    else
        echo -e "${YELLOW}⚠️  REST Response Invalid${NC}"
    fi
else
    echo -e "${RED}❌ REST Failed${NC}"
fi

# Performance Metrics
echo ""
echo "📊 Performance Metrics:"
echo "Container Resource Usage:"
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" | head -10

echo ""
echo "💾 Disk Usage:"
df -h | grep -E "(Filesystem|/dev/)"

echo ""
echo "🏁 Health check completed!"

#!/bin/bash
# run-tests.sh - Execute All Test Suites

set -e

echo "🧪 Running GlobalBooks SOA Test Suites"
echo "======================================"

# Unit Tests
echo "🔬 Running Unit Tests..."
for service in 02-catalog-service 03-orders-service 04-payments-service 05-shipping-service; do
    if [ -d "$service" ]; then
        echo "Testing $service..."
        cd "$service"
        mvn test
        cd ..
        echo "✅ $service tests completed"
    fi
done

# Integration Tests
echo ""
echo "🔗 Running Integration Tests..."

# SOAP UI Tests (if SOAP UI is installed)
if command -v testrunner.sh >/dev/null 2>&1; then
    echo "Running SOAP UI tests..."
    testrunner.sh -s"CatalogService Test Suite" \
        "09-testing/soap-ui/CatalogService-TestSuite.xml"
    echo "✅ SOAP UI tests completed"
fi

# Postman/Newman Tests (if Newman is installed)
if command -v newman >/dev/null 2>&1; then
    echo "Running Postman/Newman tests..."
    newman run "09-testing/postman/OrdersService-Collection.json" \
        -e "09-testing/postman/Environment-Variables.json"
    echo "✅ Postman tests completed"
fi

# Load Tests (if JMeter is installed)
if command -v jmeter >/dev/null 2>&1; then
    echo "Running load tests..."
    jmeter -n -t "09-testing/performance/jmeter-test-plan.jmx" \
        -l "09-testing/performance/results.jtl"
    echo "✅ Load tests completed"
fi

echo ""
echo "🎉 All tests completed successfully!"

# Deployment Scripts
#!/bin/bash  
# deploy-to-cloud.sh - Cloud Deployment Script

set -e

ENVIRONMENT=${1:-staging}
CLOUD_PROVIDER=${2:-aws}

echo "☁️  Deploying GlobalBooks SOA to $CLOUD_PROVIDER ($ENVIRONMENT)"
echo "=============================================================="

case $CLOUD_PROVIDER in
    "aws")
        echo "🚀 Deploying to AWS EKS..."
        
        # Apply Kubernetes configurations
        kubectl apply -f 10-deployment/kubernetes/namespace.yaml
        kubectl apply -f 10-deployment/kubernetes/ -n globalbooks
        
        # Wait for deployment
        kubectl rollout status deployment/orders-service -n globalbooks
        kubectl rollout status deployment/catalog-service -n globalbooks
        
        echo "✅ AWS deployment completed"
        ;;
    
    "azure")
        echo "🚀 Deploying to Azure AKS..."
        az aks get-credentials --resource-group globalbooks-rg --name globalbooks-aks
        kubectl apply -f 10-deployment/azure/ -n globalbooks
        echo "✅ Azure deployment completed"
        ;;
        
    "gcp")
        echo "🚀 Deploying to Google GKE..."
        gcloud container clusters get-credentials globalbooks-gke --zone us-central1-a
        kubectl apply -f 10-deployment/gcp/ -n globalbooks
        echo "✅ GCP deployment completed"
        ;;
        
    *)
        echo "❌ Unsupported cloud provider: $CLOUD_PROVIDER"
        exit 1
        ;;
esac

# Verify deployment
echo "🏥 Verifying deployment..."
kubectl get pods -n globalbooks
kubectl get services -n globalbooks

echo ""
echo "🎉 Cloud deployment completed successfully!"
echo "📍 Access your services via the load balancer endpoints shown above"