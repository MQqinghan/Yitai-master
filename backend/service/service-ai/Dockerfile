# Yitai 后端服务通用运行镜像
# 采用「宿主机 mvn package 打 fat jar -> 复制进精简 JRE 镜像」两步法：
#   优点：docker build 不需要联网拉 Maven 依赖，构建快、可复现；构建与运行镜像职责清晰。
#   对应 docker-compose 的 build.context 指向各服务模块目录（那里有 target/*.jar）。
FROM eclipse-temurin:17-jre

WORKDIR /app

# 该服务模块执行 mvn package 后，target 下会生成一个可执行 fat jar，用通配符匹配它
COPY target/*.jar app.jar

# 可在 docker-compose 里用 JAVA_OPTS 覆盖（例如 -Xms256m -Xmx512m）
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
