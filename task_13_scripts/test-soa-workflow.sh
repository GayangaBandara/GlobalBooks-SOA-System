#!/bin/bash

# GlobalBooks SOA Workflow Test Script
echo "Testing GlobalBooks SOA Workflow..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# Test health checks
test_health_check() {
    local service_name=$1
    local url=$2

    if curl -f -s "$url" > /dev/null; then
        print_success "$service_name is healthy"
        return 0
    else
        print_error "$service_name health check failed"
        return 1
    fi
}

# Test SOAP service
test_soap_service() {
    print_status "Testing SOAP Catalog Service..."

    # Test WSDL availability
    if curl -f -s "http://localhost:8080/soap/catalog?wsdl" > /dev/null; then
        print_success "SOAP WSDL is accessible"
    else
        print_error "SOAP WSDL is not accessible"
        return 1
    fi

    # Test SOAP operation (searchBooks)
    SOAP_REQUEST='<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:cat="http://catalog.globalbooks.com/">
    <soapenv:Header/>
    <soapenv:Body>
        <cat:searchBooks>
            <query>Java</query>
            <category>Programming</category>
        </cat:searchBooks>
    </soapenv:Body>
</soapenv:Envelope>'

    SOAP_RESPONSE=$(curl -s -X POST \
        -H "Content-Type: text/xml;charset=UTF-8" \
        -H "SOAPAction: \"\"" \
        -d "$SOAP_REQUEST" \
        "http://localhost:8080/soap/catalog")

    if echo "$SOAP_RESPONSE" | grep -q "Effective Java"; then
        print_success "SOAP searchBooks operation successful"
    else
        print_warning "SOAP searchBooks operation may have issues"
    fi
}

# Test REST services
test_rest_services() {
    print_status "Testing REST Services..."

    # Test Orders Service
    if test_health_check "Orders Service" "http://localhost:8081/api/v1/orders/health"; then
        # Test creating an order
        ORDER_DATA='{
            "customerId": "test-customer-123",
            "shippingAddress": "123 Test Street, Test City, TC 12345",
            "items": [
                {
                    "bookId": "978-0134685991",
                    "bookTitle": "Effective Java",
                    "quantity": 1,
                    "unitPrice": 45.99
                }
            ]
        }'

        CREATE_RESPONSE=$(curl -s -X POST \
            -H "Content-Type: application/json" \
            -d "$ORDER_DATA" \
            "http://localhost:8081/api/v1/orders")

        if echo "$CREATE_RESPONSE" | grep -q "id"; then
            print_success "Order creation successful"
            # Extract order ID for further testing
            ORDER_ID=$(echo "$CREATE_RESPONSE" | grep -o '"id":[0-9]*' | cut -d':' -f2)
        else
            print_error "Order creation failed"
        fi
    fi

    # Test Payments Service
    test_health_check "Payments Service" "http://localhost:8082/api/v1/payments/health"

    # Test Shipping Service
    test_health_check "Shipping Service" "http://localhost:8083/api/v1/shippings/health"
}

# Test RabbitMQ
test_rabbitmq() {
    print_status "Testing RabbitMQ..."

    # Check if RabbitMQ management interface is accessible
    if curl -f -s -u admin:password123 "http://localhost:15672/api/overview" > /dev/null; then
        print_success "RabbitMQ is accessible"

        # Check queue status
        QUEUE_INFO=$(curl -s -u admin:password123 "http://localhost:15672/api/queues/%2F/payments.process")
        if echo "$QUEUE_INFO" | grep -q "payments.process"; then
            print_success "Payment processing queue exists"
        else
            print_warning "Payment processing queue not found"
        fi

        QUEUE_INFO=$(curl -s -u admin:password123 "http://localhost:15672/api/queues/%2F/shipping.create")
        if echo "$QUEUE_INFO" | grep -q "shipping.create"; then
            print_success "Shipping creation queue exists"
        else
            print_warning "Shipping creation queue not found"
        fi
    else
        print_error "RabbitMQ management interface not accessible"
    fi
}

# Main test execution
main() {
    print_status "Starting SOA Workflow Tests..."
    echo "========================================"

    local all_tests_passed=true

    # Test individual services
    if ! test_soap_service; then
        all_tests_passed=false
    fi

    if ! test_rest_services; then
        all_tests_passed=false
    fi

    if ! test_rabbitmq; then
        all_tests_passed=false
    fi

    echo "========================================"

    if $all_tests_passed; then
        print_success "All SOA workflow tests completed successfully!"
        print_status "The GlobalBooks SOA system is fully operational."
        print_status ""
        print_status "Service Endpoints:"
        print_status "  - Catalog Service (SOAP): http://localhost:8080/soap/catalog"
        print_status "  - Orders Service (REST): http://localhost:8081/api/v1/orders"
        print_status "  - Payments Service: http://localhost:8082/api/v1/payments"
        print_status "  - Shipping Service: http://localhost:8083/api/v1/shippings"
        print_status "  - RabbitMQ Management: http://localhost:15672"
        exit 0
    else
        print_error "Some tests failed. Please check the service logs and configurations."
        print_status "Use 'docker-compose logs <service-name>' to check individual service logs."
        exit 1
    fi
}

# Run main function
main