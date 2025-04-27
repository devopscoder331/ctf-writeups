FROM node:21
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY ./src ./src
CMD ["npm", "start"] 