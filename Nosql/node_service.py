# 分布式节点服务
from flask import Flask, request, jsonify
import os
import psycopg2
import json
import time
import logging
from datetime import datetime
from config_docker import DATABASE_NODES, SYSTEM_CONFIG

app = Flask(__name__)

# 当前节点配置
NODE_ID = os.getenv('NODE_ID')
if NODE_ID not in DATABASE_NODES:
    raise ValueError(f"未知的节点ID: {NODE_ID}")

node_config = DATABASE_NODES[NODE_ID]

# 设置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 数据库连接池（简化版）
db_connections = []

def get_db_connection():
    """获取数据库连接"""
    try:
        conn = psycopg2.connect(
            host=os.getenv('DB_HOST'),
            port=os.getenv('DB_PORT'),
            database=os.getenv('DB_NAME'),
            user=os.getenv('DB_USER'),
            password=os.getenv('DB_PASSWORD')
        )
        return conn
    except Exception as e:
        logger.error(f"数据库连接失败: {e}")
        raise

def close_db_connection(conn):
    """关闭数据库连接"""
    if conn:
        conn.close()

@app.route('/health')
def health_check():
    """健康检查"""
    try:
        # 测试数据库连接
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("SELECT 1")
        cursor.fetchone()
        cursor.close()
        close_db_connection(conn)
        
        return jsonify({
            'status': 'healthy',
            'node_id': NODE_ID,
            'node_name': node_config['name'],
            'timestamp': datetime.now().isoformat(),
            'database_status': 'connected'
        })
    except Exception as e:
        logger.error(f"健康检查失败: {e}")
        return jsonify({
            'status': 'unhealthy',
            'node_id': NODE_ID,
            'node_name': node_config['name'],
            'timestamp': datetime.now().isoformat(),
            'error': str(e),
            'database_status': 'disconnected'
        }), 500

@app.route('/api/execute', methods=['POST'])
def execute_query():
    """执行SQL查询"""
    start_time = time.time()
    
    try:
        data = request.get_json()
        sql = data.get('sql', '').strip()
        
        if not sql:
            return jsonify({
                'success': False,
                'error': 'SQL查询不能为空',
                'node_id': NODE_ID
            }), 400
        
        logger.info(f"节点 {NODE_ID} 执行查询: {sql[:100]}...")
        
        # 获取数据库连接
        conn = get_db_connection()
        cursor = conn.cursor()
        
        # 执行查询
        cursor.execute(sql)
        
        # 获取结果
        if cursor.description:
            # SELECT查询
            columns = [desc[0] for desc in cursor.description]
            rows = cursor.fetchall()
            
            results = []
            for row in rows:
                row_dict = dict(zip(columns, row))
                # 处理日期时间类型
                for key, value in row_dict.items():
                    if isinstance(value, datetime):
                        row_dict[key] = value.isoformat()
                results.append(row_dict)
            
            affected_rows = len(results)
        else:
            # INSERT/UPDATE/DELETE查询
            affected_rows = cursor.rowcount
            results = []
            conn.commit()  # 提交事务
        
        cursor.close()
        close_db_connection(conn)
        
        execution_time = time.time() - start_time
        
        logger.info(f"节点 {NODE_ID} 查询完成: {affected_rows} 行受影响，耗时 {execution_time:.3f}s")
        
        return jsonify({
            'success': True,
            'results': results,
            'node_id': NODE_ID,
            'node_name': node_config['name'],
            'affected_rows': affected_rows,
            'execution_time': execution_time,
            'timestamp': datetime.now().isoformat()
        })
        
    except psycopg2.Error as e:
        execution_time = time.time() - start_time
        error_msg = f"数据库错误: {e}"
        logger.error(f"节点 {NODE_ID} 数据库错误: {e}")
        
        return jsonify({
            'success': False,
            'error': error_msg,
            'node_id': NODE_ID,
            'node_name': node_config['name'],
            'execution_time': execution_time,
            'timestamp': datetime.now().isoformat()
        }), 500
        
    except Exception as e:
        execution_time = time.time() - start_time
        error_msg = f"服务器错误: {e}"
        logger.error(f"节点 {NODE_ID} 服务器错误: {e}")
        
        return jsonify({
            'success': False,
            'error': error_msg,
            'node_id': NODE_ID,
            'node_name': node_config['name'],
            'execution_time': execution_time,
            'timestamp': datetime.now().isoformat()
        }), 500

@app.route('/api/status')
def node_status():
    """获取节点详细状态"""
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        
        # 获取数据库统计信息
        stats_queries = {
            'books_count': "SELECT COUNT(*) FROM books",
            'users_count': "SELECT COUNT(*) FROM users",
            'libraries_count': "SELECT COUNT(*) FROM libraries"
        }
        
        stats = {}
        for stat_name, query in stats_queries.items():
            try:
                cursor.execute(query)
                result = cursor.fetchone()
                stats[stat_name] = result[0] if result else 0
            except:
                stats[stat_name] = 0
        
        cursor.close()
        close_db_connection(conn)
        
        return jsonify({
            'node_id': NODE_ID,
            'node_name': node_config['name'],
            'location': node_config['location'],
            'address': node_config['address'],
            'status': 'healthy',
            'database_stats': stats,
            'timestamp': datetime.now().isoformat()
        })
        
    except Exception as e:
        logger.error(f"获取节点状态失败: {e}")
        return jsonify({
            'node_id': NODE_ID,
            'node_name': node_config['name'],
            'status': 'error',
            'error': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@app.route('/api/tables')
def list_tables():
    """列出数据库中的表"""
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        
        cursor.execute("""
            SELECT table_name 
            FROM information_schema.tables 
            WHERE table_schema = 'public'
        """)
        
        tables = [row[0] for row in cursor.fetchall()]
        
        cursor.close()
        close_db_connection(conn)
        
        return jsonify({
            'success': True,
            'tables': tables,
            'node_id': NODE_ID
        })
        
    except Exception as e:
        logger.error(f"获取表列表失败: {e}")
        return jsonify({
            'success': False,
            'error': str(e),
            'node_id': NODE_ID
        }), 500

@app.errorhandler(404)
def not_found(error):
    return jsonify({
        'error': 'API端点未找到',
        'node_id': NODE_ID,
        'available_endpoints': [
            '/health',
            '/api/execute',
            '/api/status',
            '/api/tables'
        ]
    }), 404

@app.errorhandler(500)
def internal_error(error):
    return jsonify({
        'error': '内部服务器错误',
        'node_id': NODE_ID,
        'timestamp': datetime.now().isoformat()
    }), 500

if __name__ == '__main__':
    logger.info(f"启动节点服务: {NODE_ID} ({node_config['name']})")
    logger.info(f"数据库: {os.getenv('DB_HOST')}:{os.getenv('DB_PORT')}/{os.getenv('DB_NAME')}")
    
    app.run(
        host='0.0.0.0', 
        port=5000,
        debug=SYSTEM_CONFIG.get('debug', False)
    )


