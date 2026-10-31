@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem 한글 출력용 UTF-8 로 전환하고, 종료 시 원래 코드페이지로 되돌림
rem (chcp 는 표준입력을 먹어버리므로 항상 <nul 을 붙임)
for /f "tokens=2 delims=:" %%c in ('chcp ^<nul') do set "OLD_CP=%%c"
set "OLD_CP=!OLD_CP: =!"
set "OLD_CP=!OLD_CP:.=!"
chcp 65001 <nul >nul

rem ================================================================
rem  machugi 통합 테스트 실행기
rem
rem    machugi.bat               서비스 선택 후 실행 (대화형)
rem    machugi.bat down          전체 종료 (DB 데이터는 유지)
rem    machugi.bat ps            실행 중인 컨테이너 보기
rem    machugi.bat logs [이름]   로그 보기 (예: machugi.bat logs user)
rem
rem  실행 순서: 서비스 선택 -> 빌드 선택 -> descriptor 생성 -> pull -> 빌드 -> 실행
rem ================================================================

rem 프로젝트 루트 = 이 파일이 있는 test\ 의 상위 폴더
pushd "%~dp0.." || exit /b 1
set "ROOT=%CD%"

set "BASE_FILE=docker-compose.yml"
set "BUILD_DIR=test\compose"
set "GEN_DIR=test\.generated"
set "LAST_FILE=test\.last-selection"
set "BUF_IMAGE=bufbuild/buf:1.73.0"

rem 선택 가능한 서비스 목록
rem   compose 서비스명 = <이름>-service
rem   로컬 빌드 설정   = test\compose\build-<이름>.yml
rem   서비스를 추가하면 SVC_COUNT 와 SVC_n 만 늘리고 build-<이름>.yml 을 만들면 됨
set "SVC_COUNT=8"
set "SVC_1=user"
set "SVC_2=board"
set "SVC_3=quiz"
set "SVC_4=room"
set "SVC_5=game"
set "SVC_6=chat-node"
set "SVC_7=chat-dotnet"
set "SVC_8=chat-spring"

rem 항상 같이 실행 (redis, postgres 는 depends_on 으로 필요할 때만 자동 실행)
set "ALWAYS=envoy test-web quiz-test-service"

if /i "%~1"=="down" goto :cmd_down
if /i "%~1"=="ps"   goto :cmd_ps
if /i "%~1"=="logs" goto :cmd_logs
if not "%~1"=="" goto :usage

call :check_env || goto :end_fail

echo.
echo  ============ machugi 통합 테스트 환경 ============

rem ---------------------------------------------------------------
rem  이전 선택 재사용
rem ---------------------------------------------------------------
set "RUN_NAMES="
set "BUILD_NAMES="
set "LAST_RUN="
set "LAST_BUILD="
set "ASK_COUNT=0"
if exist "%LAST_FILE%" for /f "usebackq tokens=1,* delims==" %%a in ("%LAST_FILE%") do set "%%a=%%b"
if not defined LAST_RUN goto :ask_run

echo.
echo  이전 선택
echo    실행 : !LAST_RUN!
if defined LAST_BUILD (echo    빌드 : !LAST_BUILD!) else echo    빌드 : 없음
echo.
call :read_input ANS " 이전 선택 그대로 실행할까요? [Y/n]: "
if /i "!ANS!"=="n" goto :ask_run
set "RUN_NAMES=!LAST_RUN!"
set "BUILD_NAMES=!LAST_BUILD!"
goto :save_selection

rem ---------------------------------------------------------------
rem  선택 1. 실행할 서비스
rem ---------------------------------------------------------------
:ask_run
set /a ASK_COUNT+=1
if !ASK_COUNT! gtr 5 (
    echo  [오류] 입력이 없어 종료합니다.
    goto :end_fail
)
echo.
echo  [선택 1/2] 실행할 서비스  - envoy, test-web 은 항상 실행됨
call :print_services ALL
echo.
call :read_input INPUT " 번호를 띄어쓰기로 입력 [예: 1 3 / 전체: a]: "
if not defined INPUT goto :ask_run

call :clear_picks
set "RUN_NAMES="
if /i "!INPUT!"=="a" (
    for /l %%i in (1,1,%SVC_COUNT%) do (
        set "PICK_!SVC_%%i!=1"
        set "RUN_NAMES=!RUN_NAMES! !SVC_%%i!"
    )
) else (
    for %%n in (!INPUT!) do (
        if defined SVC_%%n (
            if not defined PICK_!SVC_%%n! (
                set "PICK_!SVC_%%n!=1"
                set "RUN_NAMES=!RUN_NAMES! !SVC_%%n!"
            )
        ) else (
            echo  [무시] 없는 번호: %%n
        )
    )
)
if not defined RUN_NAMES goto :ask_run
set "RUN_NAMES=!RUN_NAMES:~1!"

rem ---------------------------------------------------------------
rem  선택 2. 로컬 소스로 빌드할 서비스 (선택 1 중에서)
rem ---------------------------------------------------------------
echo.
echo  [선택 2/2] 로컬 소스로 빌드할 서비스  - 나머지는 Docker Hub 의 :dev 이미지 사용
call :print_services PICKED
echo.
call :read_input INPUT " 번호를 띄어쓰기로 입력 [없으면 엔터 / 전부: a]: "

set "BUILD_NAMES="
if not defined INPUT goto :save_selection
if /i "!INPUT!"=="a" (
    set "BUILD_NAMES=!RUN_NAMES!"
    goto :save_selection
)
for %%n in (!INPUT!) do (
    set "OK="
    if defined SVC_%%n if defined PICK_!SVC_%%n! set "OK=1"
    if defined OK (
        if not defined BPICK_!SVC_%%n! (
            set "BPICK_!SVC_%%n!=1"
            set "BUILD_NAMES=!BUILD_NAMES! !SVC_%%n!"
        )
    ) else (
        echo  [무시] 실행 목록에 없는 번호: %%n
    )
)
if defined BUILD_NAMES set "BUILD_NAMES=!BUILD_NAMES:~1!"

:save_selection
> "%LAST_FILE%" (
    echo LAST_RUN=!RUN_NAMES!
    echo LAST_BUILD=!BUILD_NAMES!
)

rem ---------------------------------------------------------------
rem  선택 결과 -> compose 인자 만들기
rem ---------------------------------------------------------------
set "FILES=-f %BASE_FILE%"
set "UP_SVCS=%ALWAYS%"
set "PULL_SVCS="
set "BUILD_SVCS="
for %%s in (!RUN_NAMES!) do (
    set "UP_SVCS=!UP_SVCS! %%s-service"
    set "IS_BUILD="
    for %%b in (!BUILD_NAMES!) do if /i "%%b"=="%%s" set "IS_BUILD=1"
    if defined IS_BUILD (
        if not exist "%BUILD_DIR%\build-%%s.yml" (
            echo  [오류] 빌드 설정 파일이 없습니다: %BUILD_DIR%\build-%%s.yml
            goto :end_fail
        )
        set "FILES=!FILES! -f %BUILD_DIR%\build-%%s.yml"
        set "BUILD_SVCS=!BUILD_SVCS! %%s-service"
    ) else (
        set "PULL_SVCS=!PULL_SVCS! %%s-service"
    )
)

echo.
echo  --------------------------------------------------
echo   실행     : !RUN_NAMES!  + %ALWAYS%
if defined BUILD_NAMES (echo   로컬빌드 : !BUILD_NAMES!) else echo   로컬빌드 : 없음
echo  --------------------------------------------------

rem ---------------------------------------------------------------
rem  1. proto -> envoy descriptor
rem ---------------------------------------------------------------
echo.
echo  [1/4] proto 로 envoy descriptor 생성
if not exist "%GEN_DIR%" mkdir "%GEN_DIR%"
docker run --rm -v "%ROOT%\proto:/workspace:ro" -v "%ROOT%\%GEN_DIR%:/out" -w /workspace %BUF_IMAGE% build -o /out/descriptor.pb
if errorlevel 1 (
    echo  [경고] descriptor 생성 실패 - 기존 infra\envoy\descriptor.pb 로 계속 진행합니다.
    set "ENVOY_DESCRIPTOR=./infra/envoy/descriptor.pb"
) else (
    echo  - %GEN_DIR%\descriptor.pb 생성 완료
    set "ENVOY_DESCRIPTOR=./test/.generated/descriptor.pb"
)

rem ---------------------------------------------------------------
rem  2. Docker Hub 최신 :dev 이미지 받기 (로컬 빌드 안 하는 서비스만)
rem ---------------------------------------------------------------
echo.
echo  [2/4] Docker Hub 에서 최신 :dev 이미지 받기
if defined PULL_SVCS (
    docker compose -f %BASE_FILE% pull !PULL_SVCS!
    if errorlevel 1 echo  [경고] pull 실패 - 로컬에 남아있는 이미지로 계속 진행합니다.
) else (
    echo  - 받을 이미지 없음
)

rem ---------------------------------------------------------------
rem  3. 로컬 소스 빌드 (실패하면 기존 환경은 건드리지 않고 중단)
rem ---------------------------------------------------------------
echo.
echo  [3/4] 로컬 소스 빌드
if defined BUILD_SVCS (
    docker compose !FILES! build !BUILD_SVCS!
    if errorlevel 1 (
        echo  [오류] 빌드 실패 - 기존에 실행 중인 환경은 그대로 둡니다.
        goto :end_fail
    )
) else (
    echo  - 빌드할 서비스 없음
)

rem ---------------------------------------------------------------
rem  4. 기존 컨테이너 정리 후 선택한 것만 실행
rem ---------------------------------------------------------------
echo.
echo  [4/4] 컨테이너 실행
docker compose !FILES! down --remove-orphans
docker compose !FILES! up -d --no-build !UP_SVCS!
if errorlevel 1 goto :end_fail

echo.
docker compose !FILES! ps
echo.
echo  ==================================================
echo   게이트웨이   http://localhost:8080
echo   Envoy 관리   http://localhost:9901
echo   로그 보기    test\machugi.bat logs user
echo   전체 종료    test\machugi.bat down
echo  ==================================================
goto :end_ok


rem ===============================================================
rem  부가 명령
rem ===============================================================
:cmd_down
docker compose -f %BASE_FILE% down --remove-orphans
goto :end_quiet

:cmd_ps
docker compose -f %BASE_FILE% ps
goto :end_quiet

:cmd_logs
set "TARGET=%~2"
if not defined TARGET (
    docker compose -f %BASE_FILE% logs -f --tail 100
    goto :end_quiet
)
rem user -> user-service 처럼 짧은 이름도 받음 (envoy, redis 등은 그대로)
for /l %%i in (1,1,%SVC_COUNT%) do if /i "!SVC_%%i!"=="%~2" set "TARGET=%~2-service"
docker compose -f %BASE_FILE% logs -f --tail 100 !TARGET!
goto :end_quiet

:usage
echo.
echo  사용법
echo    test\machugi.bat               서비스 선택 후 실행
echo    test\machugi.bat down          전체 종료 - DB 데이터는 유지
echo    test\machugi.bat ps            실행 중인 컨테이너 보기
echo    test\machugi.bat logs [이름]   로그 보기, 예: logs user
goto :end_quiet


rem ===============================================================
rem  서브루틴
rem ===============================================================
:check_env
docker info <nul >nul 2>&1
if errorlevel 1 (
    echo  [오류] Docker Desktop 이 꺼져 있습니다. 실행 후 다시 시도하세요.
    exit /b 1
)
if not exist ".env" (
    echo  [오류] 루트에 .env 파일이 없습니다: %ROOT%\.env
    exit /b 1
)
exit /b 0

rem %1 = 결과 변수명, %2 = 안내 문구 (엔터만 치면 변수는 비어있음)
:read_input
set "%~1="
set /p "%~1=%~2"
exit /b 0

rem %1 = ALL: 전체 목록 / PICKED: 선택 1에서 고른 것만
:print_services
for /l %%i in (1,1,%SVC_COUNT%) do (
    set "NAME=!SVC_%%i!"
    if /i "%~1"=="ALL" (
        echo     [%%i] !NAME!
    ) else if defined PICK_!NAME! (
        echo     [%%i] !NAME!
    )
)
exit /b 0

:clear_picks
for /l %%i in (1,1,%SVC_COUNT%) do (
    set "PICK_!SVC_%%i!="
    set "BPICK_!SVC_%%i!="
)
exit /b 0


rem ===============================================================
rem  종료
rem ===============================================================
:end_ok
popd
echo.
pause
if defined OLD_CP chcp %OLD_CP% <nul >nul
exit /b 0

:end_fail
popd
echo.
echo  중단되었습니다. 위 메시지를 확인하세요.
pause
if defined OLD_CP chcp %OLD_CP% <nul >nul
exit /b 1

:end_quiet
set "RC=%ERRORLEVEL%"
popd
if defined OLD_CP chcp %OLD_CP% <nul >nul
exit /b %RC%
