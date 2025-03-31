FROM node:23

WORKDIR /app

RUN apt-get update && apt-get install -y \
    netcat-openbsd

COPY package.json ./

RUN npm install

COPY . .

RUN chmod +x entrypoint.sh

EXPOSE 80

CMD ["./entrypoint.sh"]
