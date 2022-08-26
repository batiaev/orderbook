# Orderbook

test implementation of order book based on ws feed from coinbase

- CoinbaseClient on start open ws connection
- on connect send message to subscribe to specified pair
- on text message from ws coinbase client will put new event to disruptor
- disruptor has couple processors
  - to process order book to maintain current state
  - to log updates of order book to console
  - to cleanup events in righ buffer

## PoC limitations
- websocket connectivity management not implemented
  - currency only happy path logic of reconnection
- ping pong with server to validate healthcheck not implemented
- monitoring not added
  - can be added as additional handler
- order book has two implementations
  - sorted map - easy to use and understand solution
  - long arrays works better with caches but more complicated and current logic of orderbook updates is not efficient
- order book size
  - at map based stored full order book from exchange
  - list based limit it to min (100, requiredDepth)

## Build

```groovy
./gradlew clean build
```

## Run

```
OrderBookApp idea configuration
```

## Author

Anton Batiaev
