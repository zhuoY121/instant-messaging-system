# instant-messaging-system

## Description

This project involves building an instant messaging (IM) system that supports multi-device login and seamless integration with other applications.  

## Notes


## Folder Description

| Folder          | Info                                          |
|-----------------|-----------------------------------------------|
| codec           | Message decoder and encoder, private protocol |
| common          | Common modules across all projects            |
| tcp             | TCP gateway                                   |
| service         | Services, such as user, group and so on       |
| message-storage | Message storage service                       |


tcp

- Netty
  - Non-blocking IO
- Feign
- Redis
- ZooKeeper
  - Service registry
- RabbitMQ
  - Message Queue


## Run

Database
- Use MAMP
  - http://localhost:8888/MAMP/

ZooKeeper
- Client: PrettyZoo
- Install
  - ```brew install zookeeper```
- Set up
  - (Optional) In ```/opt/homebrew/opt/zookeeper/share/zookeeper/examples/zoo.cfg```
    - Change dataDir: ```dataDir=<target-folder>```
- Check info:
  - ```brew info zookeeper```
- Run
  - Start the ZooKeeper service:
    - ```brew services start zookeeper```
  - Stop:
    - ```brew services stop zookeeper```

Redis
- Client
  - Another Redis Desktop Manager
- Install:
  - ```brew install redis```
- Check info:
  - ```brew info redis```
- Run:
  - start: 
    - ```brew services start redis```
  - stop: 
    - ```brew services stop redis```

RabbitMQ
- Web:
  - http://localhost:15672/
    - username: guest
    - password: guest
- Install
  - ```brew install rabbitmq```
  - Check info: 
    - ```brew info rabbitmq```
- Run:
  - start: 
    - ```brew services start rabbitmq```
  - stop: 
    - ```brew services stop rabbitmq```


### Run with the script

Start tools:
```bash
# Add the permission
chmod +x scripts/bin/start-tools.sh
# Start tools
scripts/bin/start-tools.sh
```

Stop tools:
```bash
# Add the permission
chmod +x scripts/bin/stop-tools.sh
# Stop tools
scripts/bin/stop-tools.sh
```


