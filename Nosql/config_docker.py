# Docker环境分布式图书馆系统配置文件
import os

# 数据库节点配置 - 真正的分布式配置
DATABASE_NODES = {
    'node1': {
        'name': '四川大学图书馆',
        'location': {'lat': 30.6336, 'lng': 104.0834},
        'address': '成都市武侯区一环路南一段24号',
        'host': 'node-service1' if os.getenv('NODE_TYPE') == 'master' else 'db-node1',
        'port': 5000 if os.getenv('NODE_TYPE') == 'master' else 5432,
        'database': 'scu_library',
        'user': 'library_user',
        'password': 'library_pass'
    },
    'node2': {
        'name': '电子科技大学图书馆',
        'location': {'lat': 30.7608, 'lng': 103.9348},
        'address': '成都市成华区建设北路二段四号',
        'host': 'node-service2' if os.getenv('NODE_TYPE') == 'master' else 'db-node2',
        'port': 5000 if os.getenv('NODE_TYPE') == 'master' else 5432,
        'database': 'uestc_library',
        'user': 'library_user',
        'password': 'library_pass'
    },
    'node3': {
        'name': '西南交通大学图书馆',
        'location': {'lat': 30.7569, 'lng': 103.9871},
        'address': '成都市金牛区二环路北一段111号',
        'host': 'node-service3' if os.getenv('NODE_TYPE') == 'master' else 'db-node3',
        'port': 5000 if os.getenv('NODE_TYPE') == 'master' else 5432,
        'database': 'swjtu_library',
        'user': 'library_user',
        'password': 'library_pass'
    }
}

# Redis配置
REDIS_CONFIG = {
    'host': 'redis' if 'redis' in os.getenv('REDIS_URL', '') else 'localhost',
    'port': 6379,
    'db': 0,
    'decode_responses': True
}

# 主控制节点配置
MASTER_NODE = {
    'host': '0.0.0.0',  # Docker容器内监听所有接口
    'port': 5000
}

# 系统配置
SYSTEM_CONFIG = {
    'debug': os.getenv('DEBUG', 'False').lower() == 'true',
    'environment': 'docker',
    'node_type': os.getenv('NODE_TYPE', 'master'),
    'node_id': os.getenv('NODE_ID', 'master'),
    'max_connections': 100,
    'query_timeout': 30,
    'enable_caching': True
}

# 数据库连接池配置
DB_POOL_CONFIG = {
    'minconn': 1,
    'maxconn': 10
}

# 日志配置
LOGGING_CONFIG = {
    'level': 'INFO' if not SYSTEM_CONFIG['debug'] else 'DEBUG',
    'format': '%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    'file': '/app/logs/app.log'
}


