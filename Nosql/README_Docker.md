# 🐳 Docker分布式图书馆系统

基于Docker的真正分布式图书馆管理系统，实现跨校图书资源共享。

## 🏗️ 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   主控制节点     │    │     Redis      │    │   GIS服务      │
│   (Flask Web)   │◄──►│     缓存        │    │   (地图服务)    │
│   localhost:5000│    │  localhost:6379 │    │                │
└─────────┬───────┘    └─────────────────┘    └─────────────────┘
          │
          │ HTTP API 调用
          │
    ┌─────┴─────┐
    │           │
    ▼           ▼
┌───────┐   ┌───────┐   ┌───────┐
│节点1   │   │节点2   │   │节点3   │
│:5001  │   │:5002  │   │:5003  │
└───┬───┘   └───┬───┘   └───┬───┘
    │           │           │
    ▼           ▼           ▼
┌───────┐   ┌───────┐   ┌───────┐
│数据库1 │   │数据库2 │   │数据库3 │
│:5432  │   │:5433  │   │:5434  │
│四川大学│   │电子科大│   │西南交大│
└───────┘   └───────┘   └───────┘
```

## 🚀 快速启动

### 1. 前置要求
- Docker
- docker-compose
- 8GB+ 内存
- 10GB+ 磁盘空间

### 2. 启动系统

```bash
# 方式1：使用启动脚本（推荐）
chmod +x start.sh
./start.sh

# 方式2：手动启动
docker-compose up --build -d
```

### 3. 访问系统

- **主应用**: http://localhost:5000
- **系统监控**: http://localhost:5000/dashboard
- **查询界面**: http://localhost:5000/query
- **GIS地图**: http://localhost:5000/gis

## 📊 服务详情

### 主要服务

| 服务名 | 端口 | 描述 |
|--------|------|------|
| app-master | 5000 | 主控制应用 |
| node-service1 | 5001 | 四川大学节点服务 |
| node-service2 | 5002 | 电子科技大学节点服务 |
| node-service3 | 5003 | 西南交通大学节点服务 |
| redis | 6379 | 缓存服务 |

### 数据库节点

| 数据库 | 端口 | 数据库名 | 描述 |
|--------|------|----------|------|
| db-node1 | 5432 | scu_library | 四川大学图书馆 |
| db-node2 | 5433 | uestc_library | 电子科技大学图书馆 |
| db-node3 | 5434 | swjtu_library | 西南交通大学图书馆 |

## 🔧 管理命令

```bash
# 查看所有服务状态
docker-compose ps

# 查看实时日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f app-master
docker-compose logs -f node-service1

# 重启服务
docker-compose restart

# 停止所有服务
docker-compose down

# 完全清理（包括数据卷）
docker-compose down -v

# 重新构建镜像
docker-compose build --no-cache

# 进入容器shell
docker-compose exec app-master bash
docker-compose exec db-node1 psql -U library_user -d scu_library
```

## 📝 示例查询

### 1. 通过Web界面

访问 http://localhost:5000/query 执行以下SQL：

```sql
-- 查询所有图书
SELECT * FROM books LIMIT 10;

-- 按分类统计图书
SELECT category, COUNT(*) as count FROM books GROUP BY category;

-- 查询可借阅图书
SELECT title, author, library_id FROM books WHERE available_copies > 0;

-- 跨节点用户统计
SELECT university, COUNT(*) as user_count FROM users GROUP BY university;
```

### 2. 通过API

```bash
# 测试节点健康状态
curl http://localhost:5001/health
curl http://localhost:5002/health
curl http://localhost:5003/health

# 直接向节点发送查询
curl -X POST http://localhost:5001/api/execute \
  -H "Content-Type: application/json" \
  -d '{"sql": "SELECT COUNT(*) FROM books"}'

# 通过主应用搜索图书
curl -X POST http://localhost:5000/api/books/search \
  -H "Content-Type: application/json" \
  -d '{"search_term": "数据库", "search_type": "title"}'
```

## 🎯 真正的分布式特性

与原版本对比，现在具备：

✅ **多机器架构模拟**: 每个服务在独立Docker容器中运行  
✅ **网络通信**: 节点间通过HTTP API通信  
✅ **独立数据库**: 每个节点使用独立的PostgreSQL实例  
✅ **分布式缓存**: Redis提供跨节点缓存  
✅ **容错机制**: 节点故障时其他节点继续工作  
✅ **负载分担**: 查询分发到多个节点并行执行  
✅ **真实网络延迟**: 容器间网络通信模拟真实环境  
✅ **独立扩展**: 可单独扩展任何服务  

## 🔍 监控和调试

### 1. 健康检查

```bash
# 检查所有服务健康状态
curl http://localhost:5000/health
curl http://localhost:5001/health
curl http://localhost:5002/health
curl http://localhost:5003/health
```

### 2. 性能监控

访问 http://localhost:5000/dashboard 查看：
- 查询执行时间统计
- 节点状态监控
- 缓存命中率
- 系统资源使用

### 3. 日志分析

```bash
# 查看分布式查询日志
docker-compose logs -f app-master | grep "分布式查询"

# 查看节点通信日志
docker-compose logs -f node-service1 | grep "执行查询"

# 查看数据库连接日志
docker-compose logs -f db-node1
```

## 🚨 故障排除

### 常见问题

1. **端口冲突**
   ```bash
   # 检查端口占用
   netstat -tlnp | grep :5000
   # 修改docker-compose.yml中的端口映射
   ```

2. **数据库连接失败**
   ```bash
   # 检查数据库容器状态
   docker-compose ps
   # 查看数据库日志
   docker-compose logs db-node1
   ```

3. **节点服务无响应**
   ```bash
   # 重启特定节点
   docker-compose restart node-service1
   # 查看节点日志
   docker-compose logs node-service1
   ```

4. **内存不足**
   ```bash
   # 检查Docker资源使用
   docker stats
   # 增加Docker内存限制或关闭其他应用
   ```

## 📈 性能优化

### 1. 数据库优化
- 已创建必要索引
- 使用连接池
- 启用查询缓存

### 2. 应用优化
- Redis缓存频繁查询
- 并行执行分布式查询
- HTTP连接复用

### 3. 容器优化
- 合理设置内存限制
- 使用多阶段构建减小镜像大小
- 启用健康检查

## 🔧 扩展和定制

### 1. 添加新节点

1. 在`docker-compose.yml`中添加新的数据库和节点服务
2. 在`config_docker.py`中添加节点配置
3. 创建相应的SQL初始化脚本

### 2. 修改数据库结构

修改`sql/init_node*.sql`文件，然后重新构建：

```bash
docker-compose down -v
docker-compose up --build
```

### 3. 自定义查询逻辑

修改`distributed_db_manager_docker.py`中的`analyze_query`方法。

## 📞 技术支持

- 查看日志: `docker-compose logs -f`
- 系统监控: http://localhost:5000/dashboard
- 健康检查: http://localhost:5000/health

这就是真正的分布式系统！🎉


