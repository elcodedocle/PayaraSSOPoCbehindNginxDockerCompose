# PayaraSSOPoCbehindNginxDockerCompose
docker-compose deploying a Payara SSO PoC behind nginx's proxy_pass for serving single page applications (e.g. Angular applications)

![image](https://user-images.githubusercontent.com/3731026/77218635-af7b1180-6b2d-11ea-86cd-2442685a4387.png)

## About this project

This is a typical docker-compose setup where a Java AS ([Payara][1]) is behind a web server ([nginx][2]) using a reverse proxy via [proxy_pass](http://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_pass) directive to forward all server requests (anything but the static content served on [nginx/appstatic](nginx/appstatic) volume) to the AS's internal `ip:port` server socket. [docker-compose.yml](docker-compose.yml) exposes the nginx container with the following [nginx docker][2] site config on [nginx/app.site](nginx/app.site).

  [1]: https://hub.docker.com/r/payara/server-full/
  [2]: https://hub.docker.com/_/nginx
