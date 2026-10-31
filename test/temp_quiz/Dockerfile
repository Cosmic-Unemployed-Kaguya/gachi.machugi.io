FROM node:20-alpine

WORKDIR /usr/src/app

COPY package*.json ./
RUN npm install --omit=dev

COPY index.js ./
# 다른 서비스와 동일하게 build-contexts(proto=./proto) 로 proto 를 받음
COPY --from=proto . /usr/src/proto

ENV PROTO_ROOT=/usr/src/proto
ENV GRPC_SERVER_ADDRESS=0.0.0.0:9090

EXPOSE 9090

CMD ["node", "index.js"]
