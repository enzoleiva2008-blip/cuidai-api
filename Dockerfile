# ---------- Etapa 1: build (compila o projeto com Maven) ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Baixa as dependencias primeiro (fica em cache se o pom.xml nao mudar)
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Copia o codigo e gera o .jar
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---------- Etapa 2: execucao (imagem leve, so com o Java) ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# O Render informa a porta pela variavel PORT; localmente usa 8080
ENV PORT=8080
EXPOSE 8080

# Fuso de Brasilia, para "data futura" e "data do agendamento" ficarem corretas
ENTRYPOINT ["java", "-Duser.timezone=America/Sao_Paulo", "-jar", "app.jar"]
