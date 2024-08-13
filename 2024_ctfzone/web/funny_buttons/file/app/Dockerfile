FROM node:22-alpine

ENV NODE_ENV=production

WORKDIR /app/

COPY package.json /app/

RUN npm install

COPY . /app/

ENV BIND_ADDR=0.0.0.0 PORT=3000

CMD ["/bin/sh", "entrypoint.sh"]
