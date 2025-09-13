# 简易分布式缓存系统 - 实验报告

## 设计目标
实现一个简单的分布式内存缓存系统，满足作业要求：
- Key-Value 存储，保存在各节点内存中（不做持久化）。
- 使用确定性哈希策略（SHA-256(key) mod N）将 key 分配到 N 个节点（不做副本）。
- 至少启动 3 个节点，节点数量固定（不考虑动态增减）。
- 每个节点对外提供 HTTP 接口，客户端可向任意节点发起单 key 的 GET/POST/DELETE 请求。
- 当接入节点不是数据持有者时，通过内部 RPC（HTTP）向持有者请求数据并把结果返回给客户端。

## 系统结构
- 每个 cache server 运行相同的程序（`app.py`），通过环境变量区分节点名与端口：
  - `NODE_NAME`：节点名称（例：cache1）
  - `NODE_PORT`：节点对外监听端口（例：9527）
  - `ALL_NODES`：所有节点的列表，格式 `name:port,name:port,...`（容器内可通过服务名互相访问）

- 节点间通信使用 HTTP（requests 库），在 Docker Compose 内可通过服务名互连（例如 `http://cache2:9528/key`）。

## 数据分布策略
- 使用 SHA-256(key) 的整数值对节点数取模：`owner = nodes[ sha256(key) % N ]`。
- 该方法简单、确定性好（同一 key 总是映射到同一节点），便于实现与测试。

## API 约定
- Content-Type: `application/json; charset=utf-8`（对写入请求）
- 写入/更新：`POST /`，请求体为单个 KV 的 JSON 对象，例如：
  ```
  curl -XPOST -H "Content-type: application/json" http://127.0.0.1:9527/ -d '{"myname":"电子科技大学@2023"}'
  ```
  - 若接入节点为 owner：在本地存储并返回 `200` 以及存储的 KV JSON。
  - 若接入节点非 owner：转发到 owner（内部 HTTP），并将 owner 的响应返回给客户端。

- 读取：`GET /{key}`：
  - owner 节点：存在返回 `200` 与 `{"key": value}`，不存在返回 `404` 空体。
  - 非 owner：向 owner 转发请求并返回其结果。

- 删除：`DELETE /{key}`：
  - owner 会删除并返回被删除的数量（`1` 或 `0`），作为响应体（纯文本），状态 `200`。
  - 非 owner：向 owner 转发并返回其结果。

## 实现要点
- 单机节点使用 Python Flask 作为 HTTP 服务器，requests 作为内部 RPC 客户端。
- 内存存储为 `dict`，并通过线程锁 `threading.Lock()` 保护并发操作。
- 转发操作保留了 owner 返回的状态码与内容类型（用于保持行为一致性）。
- 提示：作业要求每个请求只包含一个 key，本实现会验证 POST 的 JSON 对象恰好含有一个键。

## 构建与运行
1. 构建镜像（在项目根目录包含 `Dockerfile` 与 `docker-compose.yml`）：
   ```
   docker compose build
   ```
2. 启动 3 个节点：
   ```
   docker compose up -d
   ```
3. 访问：
   - http://127.0.0.1:9527
   - http://127.0.0.1:9528
   - http://127.0.0.1:9529

## 测试示例
```
curl -XPOST -H "Content-type: application/json" http://127.0.0.1:9527/ -d '{"myname":"电子科技大学@2023"}'
curl http://127.0.0.1:9528/myname
# {"myname":"电子科技大学@2023"}

curl -XDELETE http://127.0.0.1:9529/myname
# 1

curl http://127.0.0.1:9527/myname
# 404
```

## 限制与扩展
- 当前实现未考虑副本、高可用、节点动态加入/离开或数据迁移。
- 可扩展方向：使用一致性哈希（virtual nodes）减少重映射开销；加入副本与 leader 选举保证可用性；使用更高性能的 HTTP server（gunicorn/uvicorn）等。
