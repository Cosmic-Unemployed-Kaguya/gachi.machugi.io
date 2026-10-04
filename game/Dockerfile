# --- 빌드 ---
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

# 의존성 복원(테스트 제외)
COPY ["src/main/game.csproj", "src/main/"]
RUN dotnet restore "src/main/game.csproj"

# 나머지 소스코드 전체 복사 및 빌드/퍼블리시
COPY . .
WORKDIR "/src/src/main"
RUN dotnet publish "game.csproj" -c Release -o /app/publish /p:UseAppHost=false

# --- 런타임 --- 
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS final
WORKDIR /app

EXPOSE 8080

# 빌드 된 결과물 만 복사
COPY --from=build /app/publish .

# 컨테이너 실행 시 앱 가동
ENTRYPOINT ["dotnet", "game.dll"]