
#  Build 

FROM node:20-alpine AS builder

WORKDIR /usr/src/app

# 패키지 정보만 먼저 복사해서 npm install (캐시 최적화)
COPY package*.json .npmrc ./

RUN --mount=type=secret,id=npm_token \
    export NPM_TOKEN=$(cat /run/secrets/npm_token) && \
    npm ci --omit=dev

RUN rm -f .npmrc

RUN npm install

# 나머지 모든 소스 코드 복사 후 빌드 (ts -> js)
COPY . .
RUN npm run build

#  Production

FROM node:20-alpine

WORKDIR /usr/src/app

# 프로덕션 환경 변수 설정
ENV NODE_ENV=production

# 패키지 정보 복사, 운영에 필요한 패키지만 설치
COPY package*.json .npmrc ./

RUN --mount=type=secret,id=npm_token \
    export NPM_TOKEN=$(cat /run/secrets/npm_token) && \
    npm ci --omit=dev

# 컴파일된 dist 폴더만
COPY --from=builder /usr/src/app/dist ./dist
RUN rm -f .npmrc

# Express 서버 포트 
EXPOSE 3000

CMD ["npm", "start"]