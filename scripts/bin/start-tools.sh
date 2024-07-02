#!/bin/zsh

# Start ZooKeeper
echo "Starting ZooKeeper..."
brew services start zookeeper

# Start Redis
echo "Starting Redis..."
brew services start redis

# Start RabbitMQ
echo "Starting RabbitMQ..."
brew services start rabbitmq

