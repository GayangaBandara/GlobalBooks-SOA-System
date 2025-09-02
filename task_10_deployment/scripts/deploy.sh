#!/bin/bash

# GlobalBooks SOA Deployment Script
echo "Starting GlobalBooks SOA Deployment..."

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

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Navigate to deployment directory
cd "$(dirname "$0")/../docker"

print_status "Building and starting services..."

# Start services with Docker Compose
docker-compose up -d --build

if [ $? -eq 0 ]; then
    print_status "Services started successfully!"
    print_status "Waiting for services to be healthy..."

    # Wait for services to be ready
    sleep 30

    print_status "Checking service health..."

    # Check if services are responding
    if curl -f http://localhost:8080/soap/catalog?wsdl &> /dev/null; then
        print_status "Catalog Service (SOAP) is running on port 8080"
    else
        print_warning "Catalog Service health check failed"
    fi

    if curl -f http://localhost:8081/api/v1/orders/health &> /dev/null; then
        print_status "Orders Service (REST) is running on port 8081"
    else
        print_warning "Orders Service health check failed"
    fi

    if curl -f http://localhost:8082/api/v1/payments/health &> /dev/null; then
        print_status "Payments Service is running on port 8082"
    else
        print_warning "Payments Service health check failed"
    fi

    if curl -f http://localhost:8083/api/v1/shippings/health &> /dev/null; then
        print_status "Shipping Service is running on port 8083"
    else
        print_warning "Shipping Service health check failed"
    fi

    print_status "RabbitMQ Management UI available at: http://localhost:15672"
    print_status "Use credentials: admin / password123"

    print_status "Deployment completed successfully!"
    print_status "Use 'docker-compose logs -f' to monitor service logs"
    print_status "Use 'docker-compose down' to stop all services"

else
    print_error "Failed to start services"
    exit 1
fi