#!/bin/zsh

# Stop ZooKeeper
echo "Stopping ZooKeeper..."
brew services stop zookeeper

# Stop Redis
echo "Stopping Redis..."
brew services stop redis

# Stop RabbitMQ
echo "Stopping RabbitMQ..."
brew services stop rabbitmq

