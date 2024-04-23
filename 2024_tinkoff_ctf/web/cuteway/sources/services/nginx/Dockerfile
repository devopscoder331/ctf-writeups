FROM nginx:alpine

RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d

COPY frontend /var/www/frontend/

RUN sed -i 's/worker_processes  auto/worker_processes 2/' /etc/nginx/nginx.conf
