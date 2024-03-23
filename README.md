# cook

The most micro of the microservices frameworks.

## Introduction

Cook is a minimalistic library for running HTTP Servers, specifically aimed for simple microservices.
It has no third-party dependencies, it loads blazingly fast.

## How to compile

    mvn clean install

## Concept

### Server

A server accepts requests in a port, like this:

    try (var server = new cook.Server(8080)) {
        server.run();
    }

You can try it like this:

    curl -v http://localhost:8080/ping
    *   Trying 127.0.0.1:8080...
    * Connected to localhost (127.0.0.1) port 8080 (#0)
    > GET / HTTP/1.1
    > Host: localhost:8080
    > User-Agent: curl/7.81.0
    > Accept: */*
    > 
    * Mark bundle as not supporting multiuse
    < HTTP/1.1 404 Not found
    < Content-Type: text/plain
    * no chunk, no close, no size. Assume close to signal end
    < 
    * Closing connection 0

This example returns ```404``` because the server doesn't know how to handle any request.
Keep reading...

### Rules

The previous example runs, but the server doesn't have any __rule__ to handle requests. 
Each rule has a predicate to match requests, and a function that generates a response.
You must specify the rules in the server's constructor.

Here's he simplest possible example, without using any helper function or class:

    var pingRule = new Rule() {
        public boolean test(Request request) {
            return request.hasMethod(Methods.GET) && request.uriEqualsMatchCase("/ping");
        }

        public String handle(Request request) {
            return  "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\npong";
        }
    };
    try (var server = new cook.Server(8080, 1, new NoOpCallback(), asList(pingRule))) {
        server.run();
    }

Let's try it again...

    curl -v http://localhost:8080/ping
    *   Trying 127.0.0.1:8080...
    * Connected to localhost (127.0.0.1) port 8080 (#0)
    > GET /ping HTTP/1.1
    > Host: localhost:8080
    > User-Agent: curl/7.81.0
    > Accept: */*
    > 
    * Mark bundle as not supporting multiuse
    < HTTP/1.1 200 OK
    < Content-Type: text/plain
    < Content-length: 4
    < 
    * Connection #0 to host localhost left intact
    pong

Now we have a response.
