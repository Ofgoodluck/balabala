#!/bin/bash

# 分布式图书馆系统Docker启动脚本

echo "🚀 启动分布式图书馆系统..."

# 检查Docker和docker-compose是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ Docker未安装，请先安装Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose未安装，请先安装docker-compose"
    exit 1
fi

# 创建必要的目录
echo "📁 创建必要的目录..."
mkdir -p logs
mkdir -p sql

# 停止并清理之前的容器（如果存在）
echo "🧹 清理之前的容器..."
docker-compose down -v

# 构建并启动所有服务
echo "🔨 构建Docker镜像..."
docker-compose build

echo "🚀 启动所有服务..."
docker-compose up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo "🔍 检查服务状态..."
docker-compose ps

# 检查数据库连接
echo "🔗 检查数据库连接..."
for i in {1..3}; do
    echo "检查数据库节点 $i..."
    docker-compose exec -T db-node$i pg_isready -U library_user -d $([ $i -eq 1 ] && echo "scu_library" || [ $i -eq 2 ] && echo "uestc_library" || echo "swjtu_library") || echo "节点 $i 未就绪"
done

# 检查节点服务
echo "🖥️ 检查节点服务..."
for i in {1..3}; do
    echo "检查节点服务 $i..."
    curl -s http://localhost:500$i/health | jq '.status' 2>/dev/null || echo "节点服务 $i 未就绪"
done

# 检查主应用
echo "🌐 检查主应用..."
curl -s http://localhost:5000/health | jq '.status' 2>/dev/null || echo "主应用未就绪"

echo ""
echo "✅ 分布式图书馆系统启动完成！"
echo ""
echo "🌐 访问地址："
echo "   主应用:     http://localhost:5000"
echo "   节点服务1:  http://localhost:5001/health"
echo "   节点服务2:  http://localhost:5002/health"
echo "   节点服务3:  http://localhost:5003/health"
echo ""
echo "📊 数据库连接："
echo "   节点1 (四川大学):     localhost:5432"
echo "   节点2 (电子科技大学): localhost:5433"
echo "   节点3 (西南交通大学): localhost:5434"
echo ""
echo "🔧 管理命令："
echo "   查看日志: docker-compose logs -f"
echo "   停止系统: docker-compose down"
echo "   重启系统: docker-compose restart"
echo ""


