
FROM nginx:alpine

RUN rm -rf /usr/share/nginx/html/*

COPY Frontend/ /usr/share/nginx/html/

COPY infra/docker/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
