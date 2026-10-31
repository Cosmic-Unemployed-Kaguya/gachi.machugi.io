# 일단 배포용
# descriptor 빌드 + envoy 이미지 

FROM bufbuild/buf:1.73.0 AS proto
WORKDIR /proto
COPY proto/ .
RUN buf build -o /descriptor.pb

FROM envoyproxy/envoy:v1.33-latest
COPY --from=proto /descriptor.pb /etc/envoy/descriptor.pb
COPY infra/envoy/envoy.yml /etc/envoy/envoy.yaml
