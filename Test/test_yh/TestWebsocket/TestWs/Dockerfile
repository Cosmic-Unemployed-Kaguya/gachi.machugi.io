# ==========================================
# Stage 1: Build (무거운 컴파일 환경)
# ==========================================
FROM node:18-alpine AS builder

WORKDIR /usr/src/app

# 패키지 정보만 먼저 복사해서 npm install (캐시 최적화)
COPY package*.json ./
RUN npm install

# 나머지 모든 소스 코드 복사 후 빌드 (ts -> js)
COPY . .
RUN npm run build

# ==========================================
# Stage 2: Production (가볍고 안전한 실행 환경)
# ==========================================
FROM node:18-alpine

WORKDIR /usr/src/app

# 프로덕션 환경 변수 설정
ENV NODE_ENV=production

# 패키지 정보 복사 후, '운영'에 필요한 패키지만 설치 (devDependency 제외)
COPY package*.json ./
RUN npm ci --only=production

# Stage 1(builder)에서 컴파일된 dist 폴더만 쏙 빼옴
COPY --from=builder /usr/src/app/dist ./dist

# Express 서버 포트 열어주기 (본인이 사용하는 포트 번호)
EXPOSE 3012

# ts-node가 아닌 순수 node로 가볍게 실행!
CMD ["npm", "start"]