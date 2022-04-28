REDIS_HOSTNAME=$(kubectl exec -i -t redis-primary-0 -- hostname -i)

itemstorage_redis_host=`cat "itemstorage-service_deploy.yml" | sed "s/{{REDIS_HOSTNAME}}/$REDIS_HOSTNAME/g"`
saleorders_redis_host=`cat "saleorders-service.yml" | sed "s/{{REDIS_HOSTNAME}}/$REDIS_HOSTNAME/g"`

kubectl apply -f ./

echo "$itemstorage_redis_host" | kubectl apply -f -
echo "$saleorders_redis_host" | kubectl apply -f -
