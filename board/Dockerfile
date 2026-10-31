
#  Build 

FROM node:20-alpine AS builder

WORKDIR /usr/src/app

# 패키지 정보만 먼저 복사해서 npm install (캐시 최적화)
COPY package*.json ./

RUN npm ci

RUN npm install

# 나머지 모든 소스 코드 복사 후 빌드 (ts -> js)
COPY . .
# 서비스 경로에 넣는것이 아닌, 기존 위치와 동일하게 루트에 복사
COPY --from=proto . /usr/src/proto
# proto 빌드 우선
RUN npm run build:proto
# TS 빌드
RUN npm run build

#  Production

FROM node:20-alpine

WORKDIR /usr/src/app

# 프로덕션 환경 변수 설정
ENV NODE_ENV=production

# 패키지 정보 복사, 운영에 필요한 패키지만 설치
COPY package*.json ./

RUN npm ci --omit=dev

# 컴파일된 dist 폴더만
COPY --from=builder /usr/src/app/dist ./dist

# Express 서버 포트 
EXPOSE 3000

CMD ["npm", "start"]