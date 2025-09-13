FROM ubuntu:20.04
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y python3 python3-pip && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY . /app
RUN pip3 install --no-cache-dir -r requirements.txt
# default env placeholders (overridable in docker-compose)
ENV NODE_NAME=cache1
ENV NODE_PORT=9527
ENV ALL_NODES=cache1:9527,cache2:9528,cache3:9529
EXPOSE 9527
CMD ["python3", "app.py"]
