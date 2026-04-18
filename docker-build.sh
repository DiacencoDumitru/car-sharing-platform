docker build -t api-gateway -f services/api-gateway/Dockerfile .

docker build -t eureka-server -f services/eureka-server/Dockerfile .

docker build -t booking-service -f services/booking-service/Dockerfile .

docker build -t car-service -f services/car-service/Dockerfile .

docker build -t dispute-service -f services/dispute-service/Dockerfile .

docker build -t user-service -f services/user-service/Dockerfile .

docker build -t notification-service -f services/notification-service/Dockerfile .
