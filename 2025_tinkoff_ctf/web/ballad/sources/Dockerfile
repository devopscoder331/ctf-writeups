FROM golang:1.21.1 AS builder

WORKDIR /app

COPY go.mod go.sum ./

RUN go mod download 

COPY cmd /app/cmd
COPY internal /app/internal
COPY pkg /app/pkg
RUN CGO_ENABLED=0 GOOS=linux go build -a -o syllabus cmd/syllabus/main.go


FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteravtive
RUN apt-get update
RUN apt-get install -y ca-certificates

ARG UID=2000
ARG GID=2000
ARG USER=app
ARG DIR=/home/${USER}


RUN addgroup --gid ${GID} ${USER} && adduser --gecos "" --disabled-password --gid ${GID} --uid ${UID} ${USER}

WORKDIR ${DIR}

COPY --from=builder /app/syllabus .

RUN chown ${USER}.${USER} syllabus
USER ${USER}

EXPOSE 8080

CMD ["./syllabus"]