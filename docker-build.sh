docker build -t api-gateway -f api-gateway/Dockerfile .

docker build -t eureka-server -f eureka-server/Dockerfile .

docker build -t booking-service -f booking-service/Dockerfile .

docker build -t car-service -f car-service/Dockerfile .

docker build -t dispute-service -f dispute-service/Dockerfile .

docker build -t user-service -f user-service/Dockerfile .

docker build -t notification-service -f notification-service/Dockerfile .