# 分布式图书馆系统主应用程序

from flask import Flask, render_template, request, jsonify, session
import json
import time
from datetime import datetime
from threading import Thread

from distributed_db_manager import DistributedDatabaseManager
from gis_module import GISManager
from config import MASTER_NODE, SYSTEM_CONFIG

app = Flask(__name__)
app.secret_key = 'distributed_library_system_2024'

# 全局实例
db_manager = DistributedDatabaseManager()
gis_manager = GISManager()

# 系统状态
system_status = {
    'startup_time': datetime.now(),
    'active_connections': 0,
    'total_requests': 0
}

@app.before_request
def before_request():
    """请求预处理"""
    system_status['total_requests'] += 1

@app.route('/')
def index():
    """主页"""
    return render_template('index.html', 
                         master_config=MASTER_NODE,
                         system_config=SYSTEM_CONFIG)

@app.route('/dashboard')
def dashboard():
    """系统监控面板"""
    # 获取系统统计信息
    performance_stats = db_manager.get_performance_statistics()
    node_status = db_manager.get_node_status()
    recent_transactions = db_manager.get_recent_transactions(20)
    
    dashboard_data = {
        'system_status': system_status,
        'performance_stats': performance_stats,
        'node_status': node_status,
        'recent_transactions': recent_transactions,
        'gis_coverage': gis_manager.get_library_coverage_analysis()
    }
    
    return render_template('dashboard.html', data=dashboard_data)

@app.route('/query', methods=['GET', 'POST'])
def query_interface():
    """查询界面"""
    if request.method == 'GET':
        return render_template('query.html')
    
    # 处理POST请求
    sql = request.form.get('sql', '').strip()
    
    if not sql:
        return jsonify({'success': False, 'error': '请输入SQL查询语句'})
    
    try:
        # 执行分布式查询
        result = db_manager.execute_distributed_query(sql)
        
        # 添加查询历史到session
        if 'query_history' not in session:
            session['query_history'] = []
        
        session['query_history'].append({
            'sql': sql,
            'timestamp': datetime.now().isoformat(),
            'success': result['success'],
            'execution_time': result.get('execution_time', 0)
        })
        
        # 只保留最近10条历史记录
        session['query_history'] = session['query_history'][-10:]
        
        return jsonify(result)
        
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)})

@app.route('/optimization')
def optimization_viewer():
    """查询优化过程查看器"""
    return render_template('optimization.html')

@app.route('/nodes')
def nodes_monitor():
    """节点监控页面"""
    node_status = db_manager.get_node_status()
    return render_template('nodes.html', nodes=node_status)

@app.route('/api/node/<node_id>/commands')
def get_node_commands(node_id):
    """获取指定节点的命令日志"""
    limit = request.args.get('limit', 50, type=int)
    commands = db_manager.get_node_commands_log(node_id, limit)
    return jsonify(commands)

@app.route('/api/node/<node_id>/status')
def get_node_status(node_id):
    """获取指定节点的状态"""
    if node_id in db_manager.nodes:
        node = db_manager.nodes[node_id]
        status = {
            'id': node_id,
            'name': node.name,
            'location': node.location,
            'database': node.database_path,
            'commands_count': len(node.commands_log),
            'last_activity': node.commands_log[-1]['timestamp'] if node.commands_log else None,
            'recent_commands': node.commands_log[-10:]
        }
        return jsonify(status)
    
    return jsonify({'error': 'Node not found'}), 404

@app.route('/gis')
def gis_interface():
    """GIS地理信息界面"""
    highlighted_library = request.args.get('highlight', None)
    
    # 生成地图HTML
    map_html = gis_manager.create_interactive_map(highlighted_library)
    
    # 获取图书馆数据
    libraries_data = gis_manager.libraries_data
    
    return render_template('gis.html', 
                         map_html=map_html,
                         libraries=libraries_data)

@app.route('/api/gis/spatial_query', methods=['POST'])
def spatial_query():
    """空间查询API"""
    data = request.get_json()
    query_type = data.get('type')
    params = data.get('params', {})
    
    try:
        results = gis_manager.get_spatial_query_results(query_type, params)
        return jsonify({'success': True, 'results': results})
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)})

@app.route('/api/gis/heatmap')
def generate_heatmap():
    """生成热力图"""
    heatmap_html = gis_manager.create_heatmap()
    return jsonify({'heatmap_html': heatmap_html})

@app.route('/library_management')
def library_management():
    """图书管理界面"""
    return render_template('library_management.html')

@app.route('/api/books/search', methods=['POST'])
def search_books():
    """搜索图书"""
    data = request.get_json()
    search_term = data.get('search_term', '')
    search_type = data.get('search_type', 'title')
    
    # 构建搜索SQL
    if search_type == 'title':
        sql = f"SELECT * FROM books WHERE title LIKE '%{search_term}%'"
    elif search_type == 'author':
        sql = f"SELECT * FROM books WHERE author LIKE '%{search_term}%'"
    elif search_type == 'isbn':
        sql = f"SELECT * FROM books WHERE isbn = '{search_term}'"
    else:
        sql = f"SELECT * FROM books WHERE title LIKE '%{search_term}%' OR author LIKE '%{search_term}%'"
    
    try:
        result = db_manager.execute_distributed_query(sql)
        return jsonify(result)
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)})

@app.route('/api/books/add', methods=['POST'])
def add_book():
    """添加图书"""
    data = request.get_json()
    
    try:
        result = db_manager.insert_book(data)
        return jsonify(result)
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)})

@app.route('/api/books/borrow', methods=['POST'])
def borrow_book():
    """借书"""
    data = request.get_json()
    user_id = data.get('user_id')
    book_id = data.get('book_id')
    library_id = data.get('library_id')
    
    try:
        result = db_manager.borrow_book(user_id, book_id, library_id)
        return jsonify(result)
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)})

@app.route('/api/statistics')
def get_statistics():
    """获取系统统计信息"""
    stats = {
        'performance': db_manager.get_performance_statistics(),
        'system': system_status,
        'nodes': db_manager.get_node_status(),
        'gis': gis_manager.get_library_coverage_analysis()
    }
    return jsonify(stats)

@app.route('/api/transactions/recent')
def get_recent_transactions():
    """获取最近事务"""
    limit = request.args.get('limit', 50, type=int)
    transactions = db_manager.get_recent_transactions(limit)
    return jsonify(transactions)

@app.route('/testing')
def testing_interface():
    """测试界面"""
    return render_template('testing.html')

@app.route('/api/test/sample_queries', methods=['POST'])
def run_sample_queries():
    """运行示例查询"""
    sample_queries = [
        "SELECT * FROM books WHERE category = '计算机科学'",
        "SELECT name, address FROM libraries",
        "SELECT COUNT(*) as total_books FROM books",
        "SELECT * FROM books WHERE available_copies > 0",
        "SELECT library_id, COUNT(*) as book_count FROM books GROUP BY library_id"
    ]
    
    results = []
    for sql in sample_queries:
        try:
            result = db_manager.execute_distributed_query(sql)
            results.append({
                'sql': sql,
                'success': result['success'],
                'execution_time': result.get('execution_time', 0),
                'results_count': len(result.get('results', [])) if result['success'] else 0
            })
        except Exception as e:
            results.append({
                'sql': sql,
                'success': False,
                'error': str(e),
                'execution_time': 0
            })
    
    return jsonify({'results': results})

@app.route('/help')
def help_page():
    """帮助页面"""
    help_content = {
        'system_overview': """
        本系统是一个基于GIS的分布式图书馆管理系统，连接成都市主要高校图书馆，
        实现图书资源的跨校共享和统一管理。
        """,
        'features': [
            '分布式数据库架构（3个节点）',
            '查询分解和优化',
            '地理信息系统集成',
            '实时命令监控',
            '跨校图书借阅',
            'SQL查询支持'
        ],
        'sample_queries': [
            "SELECT * FROM books WHERE title LIKE '%数据库%'",
            "SELECT name, address FROM libraries",
            "SELECT COUNT(*) FROM books GROUP BY library_id"
        ]
    }
    
    return render_template('help.html', content=help_content)

def init_sample_data():
    """初始化示例数据"""
    print("正在初始化示例数据...")
    
    # 可以在这里添加更多示例数据的初始化逻辑
    time.sleep(1)  # 模拟初始化延迟
    
    print("示例数据初始化完成")

if __name__ == '__main__':
    print("启动分布式图书馆系统...")
    print(f"主控制节点: {MASTER_NODE['host']}:{MASTER_NODE['port']}")
    print(f"数据节点数量: {len(db_manager.nodes)}")
    
    # 在后台线程中初始化示例数据
    init_thread = Thread(target=init_sample_data)
    init_thread.daemon = True
    init_thread.start()
    
    # 启动Flask应用
    app.run(
        host=MASTER_NODE['host'],
        port=MASTER_NODE['port'],
        debug=SYSTEM_CONFIG['debug'],
        threaded=True
    )
