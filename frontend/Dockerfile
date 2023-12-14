# syntax=docker/dockerfile:1
FROM node:21

ARG GATEWAY_HOST
ENV GATEWAY_HOST=${GATEWAY_HOST}

WORKDIR /app

COPY . .

RUN npm install && npm run build

CMD npm run start
