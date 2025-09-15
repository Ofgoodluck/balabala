# 分布式图书馆系统配置文件

# 数据库节点配置 - 模拟3个分布式站点
DATABASE_NODES = {
    'node1': {
        'name': '四川大学图书馆',
        'location': {'lat': 30.6336, 'lng': 104.0834},
        'address': '成都市武侯区一环路南一段24号',
        'host': 'localhost',
        'port': 5001,
        'database': 'scu_library.db'
    },
    'node2': {
        'name': '电子科技大学图书馆', 
        'location': {'lat': 30.7608, 'lng': 103.9348},
        'address': '成都市成华区建设北路二段四号',
        'host': 'localhost',
        'port': 5002,
        'database': 'uestc_library.db'
    },
    'node3': {
        'name': '西南交通大学图书馆',
        'location': {'lat': 30.7569, 'lng': 103.9871},
        'address': '成都市金牛区二环路北一段111号',
        'host': 'localhost', 
        'port': 5003,
        'database': 'swjtu_library.db'
    }
}

# 主控制节点配置
MASTER_NODE = {
    'host': 'localhost',
    'port': 5000,
    'name': '分布式图书馆管理中心'
}

# 数据分片配置
SHARDING_CONFIG = {
    'strategy': 'horizontal',  # horizontal, vertical, mixed
    'partition_key': 'library_id',  # 水平分片键
    'vertical_tables': {  # 垂直分片配置
        'node1': ['books', 'authors'],
        'node2': ['users', 'borrowings'],
        'node3': ['libraries', 'locations']
    }
}

# GIS 配置
GIS_CONFIG = {
    'center_lat': 30.6586,
    'center_lng': 104.0647,
    'zoom_level': 11,
    'map_style': 'OpenStreetMap'
}

# 系统配置
SYSTEM_CONFIG = {
    'debug': True,
    'log_queries': True,
    'show_optimization': True,
    'auto_refresh': 3000  # 界面自动刷新间隔（毫秒）
}

