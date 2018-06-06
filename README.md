# Spike: Spring Cloud Sleuth

## Description

In this spike, we are going to see how to transparently propagate information from one microservice to another and how to include such information in our log traces.

Scenario:
* we have three microservices calling each other (i.e. `service-a` -> `service-b` -> `service-c`);
* we assume that clients will call the edge microservice (i.e `service-a`) by passing a custom HTTP header (i.e. `X-Tenant-Id`) representing the tenant identifier.

Goals:
1. we want the tenant identifier to be propagated from the edge microservice (i.e `service-a`) to the downstream microservices (i.e. `service-b` and `service-c`);
2. we want the tenant identifier to be logged as part of the log traces enhanced by Spring Sleuth.

## Showtime

Let's run the three microservices:

```bash
# from terminal 1
cd microservices/service-a && ./gradlew bootRun 
./gradlew bootRun

# from terminal 2
cd microservices/service-b && ./gradlew bootRun 
./gradlew bootRun

# from terminal 3
cd microservices/service-c && ./gradlew bootRun 
./gradlew bootRun
```

Let's send a request to the edge microservice:

```bash
curl -H "X-Tenant-Id: FC" http://localhost:8080/test
```

The output of the previous command should look like this:

```
Hi, I'm service-a and this is my baggage:
{x-tenant-id=FC}

Hi, I'm service-b and this is my baggage:
{x-tenant-id=FC}

Hi, I'm service-c and this is my baggage:
{x-tenant-id=FC}
```

So, what happens under the hood?
* `service-a` receives a HTTP request with the custom HTTP header `X-Tenant-Id`, extracts its value and adds such value to the current span baggage (i.e. a set of key-value pairs stored in the span context that travels together with the trace and is attached to every span);
* `service-a` sends a request to `service-b` propagating its baggage in the form of HTTP headers prefixed by `baggage-` (in this case `baggage-x-tenant-id=[FC]`);
* `service-b` receives a HTTP request from `service-a`, which includes the tenant identifier as part of `service-a`'s baggage headers;
* `service-b` sends a request to `service-c` propagating its baggage in the form of HTTP headers prefixed by `baggage-` (in this case `baggage-x-tenant-id=[FC]`);
* `service-c` receives a HTTP request from `service-b`, which includes the tenant identifier as part of `service-b`'s baggage headers;
* `service-c` replies with the content of its baggage;
* `servuce-b` replies with the content of its baggage and `service-c`'s response;
* `servuce-a` replies with the content of its baggage and `service-b`'s response.

From the terminal running the `service-a` you should also be able to spot the tenant identifier in a log trace similar to the following:

```
2018-06-06 09:34:17.372  INFO [-,51e62dfa6fc68dd9,51e62dfa6fc68dd9,true,FC] 9727 --- [nio-8080-exec-1] c.github.fabriziocucci.spike.Controller  : logging from service-a
```

So, how did we do this?
* we have extended the default `Slf4jSpanLogger` in order to add to and remove from the mapped diagnostic context the tenant identifier when needed;
* we have customized the default `logging.pattern.level` to include the tenant identifier coming from the mapped diagnostic context;

Ultimately, we demonstrated that adding a key-value pair to the baggage of the root span in the the edge microservice is sufficient to _automagically_ propagate such information to the downstream microservices. Surprisingly, to log something traveling in the baggage we need to manually manage add and removal from the mapped diagnostic context.

## Notes

* The current spike is based on the latest service release of Spring Cloud (i.e. `Edgware.SR3`) which includes the version `1.3.3.RELEASE` of `spring-cloud-sleuth`. The next major release (i.e. `2.0.0.RC2`) seems to be substantially different but a similar approach for logging the baggage content can potentially be implemented.  

* In this case we are only propagating and logging the tenant identifier so it probably makes sense to only add such information to the baggage of the span context. Although, if Zipkin is actually used as distributed tracing system, we may need to also add that information as tag of the root span, as stated in [this](http://cloud.spring.io/spring-cloud-static/spring-cloud-sleuth/1.3.3.RELEASE/single/spring-cloud-sleuth.html#_baggage_vs_span_tags) paragraph of the official Spring Cloud Sleuth documentation:

> Baggage travels with the trace (i.e. every child span contains the baggage of its parent). Zipkin has no knowledge of baggage and will not even receive that information.
> Tags are attached to a specific span - they are presented for that particular span only. However you can search by tag to find the trace, where there exists a span having the searched tag value.
> If you want to be able to lookup a span based on baggage, you should add corresponding entry as a tag in the root span.

## Reference

* [Spring Cloud](http://projects.spring.io/spring-cloud/)
* [Spring Cloud Sleuth](http://cloud.spring.io/spring-cloud-static/spring-cloud-sleuth/1.3.3.RELEASE/single/spring-cloud-sleuth.html)