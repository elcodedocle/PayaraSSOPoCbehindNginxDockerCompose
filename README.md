# PayaraSSObehindNginxNPEdockercomposePoC
Having fun learning how to break payara SSO behind nginx's proxy_pass

![image](https://user-images.githubusercontent.com/3731026/77218635-af7b1180-6b2d-11ea-86cd-2442685a4387.png)

## Motivation

https://serverfault.com/posts/1007746/

## About this project

This is a typical docker-compose setup where a Java AS ([Payara][1]) is behind a web server ([nginx][2]) using a reverse proxy via [proxy_pass](http://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_pass) directive to forward all server requests (anything but the static content served on `/appstatic`) to the AS's internal `ip:port` server socket. [docker-compose.yml](docker-compose.yml) exposes the nginx container with the following [nginx docker][2] site config:

```
server {
    listen 8080 default_server;
    listen 8181 ssl default_server;

    ssl_certificate /etc/nginx/server.pem;
    ssl_certificate_key /etc/nginx/server.key;

    root /appstatic;
    index index.html
    server_name _;

    location /appstatic {
        try_files $uri $uri/ /index.html;
    }

    location / {
        proxy_pass http://payarassodocker:8080;
    }
}
```

**It all works fine** but there is a significant performance degradation, about 20%, when working behind nginx. This may be due to the fact that Payara's single sign on is enabled and one of the things it does is [checking the request context realm][3] on every single [PwcCoyoteRequest][4] request. Somehow the context is lost when nginx is in front, so the following NPE is raised, after which the server recovers nicely serving the request anyway but leaving a nasty stacktrace on the AS error log:

```
[#|2020-03-16T11:24:02.237+0000|SEVERE|Payara 5.194|javax.enterprise.web.core|_ThreadID=48;_ThreadName=http-thread-pool::http-listener-1(5);_TimeMillis=1584357842237;_LevelValue=1000;_MessageID=AS-WEB-CORE-00037;|
An exception or error occurred in the container during the request processing
java.lang.NullPointerException
at com.sun.enterprise.security.web.GlassFishSingleSignOn.invoke(GlassFishSingleSignOn.java:327)
at org.apache.catalina.core.StandardPipeline.doInvoke(StandardPipeline.java:724)
at org.apache.catalina.core.StandardPipeline.invoke(StandardPipeline.java:575)
at org.apache.catalina.connector.CoyoteAdapter.doService(CoyoteAdapter.java:368)
at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:238)
at com.sun.enterprise.v3.services.impl.ContainerMapper$HttpHandlerCallable.call(ContainerMapper.java:520)
at com.sun.enterprise.v3.services.impl.ContainerMapper.service(ContainerMapper.java:217)
at org.glassfish.grizzly.http.server.HttpHandler.runService(HttpHandler.java:182)
at org.glassfish.grizzly.http.server.HttpHandler.doHandle(HttpHandler.java:156)
at org.glassfish.grizzly.http.server.HttpServerFilter.handleRead(HttpServerFilter.java:218)
at org.glassfish.grizzly.filterchain.ExecutorResolver$9.execute(ExecutorResolver.java:95)
at org.glassfish.grizzly.filterchain.DefaultFilterChain.executeFilter(DefaultFilterChain.java:260)
at org.glassfish.grizzly.filterchain.DefaultFilterChain.executeChainPart(DefaultFilterChain.java:177)
at org.glassfish.grizzly.filterchain.DefaultFilterChain.execute(DefaultFilterChain.java:109)
at org.glassfish.grizzly.filterchain.DefaultFilterChain.process(DefaultFilterChain.java:88)
at org.glassfish.grizzly.ProcessorExecutor.execute(ProcessorExecutor.java:53)
at org.glassfish.grizzly.nio.transport.TCPNIOTransport.fireIOEvent(TCPNIOTransport.java:524)
at org.glassfish.grizzly.strategies.AbstractIOStrategy.fireIOEvent(AbstractIOStrategy.java:89)
at org.glassfish.grizzly.strategies.WorkerThreadIOStrategy.run0(WorkerThreadIOStrategy.java:94)
at org.glassfish.grizzly.strategies.WorkerThreadIOStrategy.access$100(WorkerThreadIOStrategy.java:33)
at org.glassfish.grizzly.strategies.WorkerThreadIOStrategy$WorkerThreadRunnable.run(WorkerThreadIOStrategy.java:114)
at org.glassfish.grizzly.threadpool.AbstractThreadPool$Worker.doWork(AbstractThreadPool.java:569)
at org.glassfish.grizzly.threadpool.AbstractThreadPool$Worker.run(AbstractThreadPool.java:549)
at java.lang.Thread.run(Thread.java:748)
|#]
```
[![enter image description here][5]][5]



  [1]: https://hub.docker.com/r/payara/server-full/
  [2]: https://hub.docker.com/_/nginx
  [3]: https://github.com/javaee/glassfish/blob/f9e1f6361dcc7998cacccb574feef5b70bf84e23/appserver/web/web-glue/src/main/java/com/sun/enterprise/security/web/GlassFishSingleSignOn.java#L355
  [4]: https://github.com/javaee/glassfish/blob/master/appserver/web/web-glue/src/main/java/com/sun/enterprise/web/pwc/connector/coyote/PwcCoyoteRequest.java
  [5]: https://i.stack.imgur.com/JyfVL.png
