FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY src/passwordmanager /app/src/passwordmanager
COPY frontend /app/frontend
COPY data /app/data

RUN javac src/passwordmanager/*.java -d out

EXPOSE 8080

CMD ["java", "-cp", "out", "passwordmanager.Main"]