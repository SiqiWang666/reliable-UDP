FROM alpine

# install GUN make and java jdk:11
RUN apk add --update make; \
    apk --no-cache add openjdk11

ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk
ENV PATH=$JAVA_HOME/bin:$PATH

WORKDIR /app

COPY Makefile .
COPY src ./src

CMD ["sh"]