1. 레파지토리 다운로드
2. 도커 실행 > docker run --name spring-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=spring_db -d mysql:8.0
3. 해당 레파지토리의 build.gradle 프로젝트로 열기, JDK 23 버전 확인
4. docker 실행되어있는 상태에서 애플리케이션 run
5. 스웨거 접속 후 확인 > http://localhost:8080/swagger-ui/index.html
