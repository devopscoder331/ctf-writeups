FROM nginx:latest

WORKDIR /etc/nginx/conf.d

COPY server/task/static/ /var/www/static/

COPY nginx/default.conf /etc/nginx/conf.d/default.conf

RUN chown -R nginx:nginx /var/www/static

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
