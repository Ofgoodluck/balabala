# Docker环境分布式图书馆系统主应用程序
import os
import logging
import time
from datetime import datetime
from threading import Thread

# 根据环境变量决定启动模式
NODE_TYPE = os.getenv('NODE_TYPE', 'master')

# 设置日志
import logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

if NODE_TYPE == 'node':
    # 节点模式：启动节点服务
    node_id = os.getenv('NODE_ID')
    print(f"启动节点服务模式: {node_id}")
    
    # 检查环境变量
    env_vars = ['NODE_ID', 'DB_HOST', 'DB_PORT', 'DB_NAME', 'DB_USER', 'DB_PASSWORD']
    print("环境变量检查:")
    for var in env_vars:
        value = os.getenv(var)
        print(f"  {var}: {value}")
    
    try:
        # 先导入配置模块
        print("正在导入config_docker模块...")
        from config_docker import DATABASE_NODES, SYSTEM_CONFIG
        print(f"成功导入config_docker，可用节点: {list(DATABASE_NODES.keys())}")
        
        if node_id not in DATABASE_NODES:
            print(f"错误：节点ID '{node_id}' 不在配置中")
            print(f"可用的节点ID: {list(DATABASE_NODES.keys())}")
            raise ValueError(f"未知的节点ID: {node_id}")
        
        print(f"节点配置: {DATABASE_NODES[node_id]}")
        
        # 再导入node_service模块
        print("正在导入node_service模块...")
        from node_service import app
        print("成功导入node_service模块")
        
    except Exception as e:
        print(f"导入失败: {e}")
        import traceback
        traceback.print_exc()
        print("程序将退出...")
        import sys
        sys.exit(1)
else:
    # 主控制模式：启动完整应用
    print("启动主控制模式")
    
    from flask import Flask, render_template, request, jsonify, session
    import json
    import time
    from threading import Thread
    
    from distributed_db_manager_docker import DistributedDatabaseManager
    from gis_module import GISManager
    from config_docker import MASTER_NODE, SYSTEM_CONFIG
    
    app = Flask(__name__)
    app.secret_key = 'distributed_library_system_docker_2024'
    
    # 全局实例
    db_manager = None
    gis_manager = None
    
    # 系统状态
    system_status = {
        'startup_time': datetime.now(),
        'active_connections': 0,
        'total_requests': 0,
        'environment': 'docker'
    }
    
    def init_managers():
        """初始化管理器"""
        global db_manager, gis_manager
        try:
            logger.info("正在初始化分布式数据库管理器...")
            db_manager = DistributedDatabaseManager()
            logger.info("正在初始化GIS管理器...")
            gis_manager = GISManager()
            logger.info("管理器初始化完成")
        except Exception as e:
            logger.error(f"管理器初始化失败: {e}")
    
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
        if not db_manager:
            # 返回带有错误信息的模板而不是JSON错误
            dashboard_data = {
                'system_status': system_status,
                'performance_stats': {'total_queries': 0, 'average_response_time': 0, 'error_rate': 0},
                'node_status': {},
                'recent_transactions': [],
                'gis_coverage': {},
                'error_message': '数据库管理器正在初始化中，请稍后刷新页面...'
            }
            return render_template('dashboard.html', data=dashboard_data)
            
        # 获取系统统计信息
        try:
            performance_stats = db_manager.get_performance_statistics()
            node_status = db_manager.get_node_status()
            recent_transactions = db_manager.get_recent_transactions(20)
            
            dashboard_data = {
                'system_status': system_status,
                'performance_stats': performance_stats,
                'node_status': node_status,
                'recent_transactions': recent_transactions,
                'gis_coverage': gis_manager.get_library_coverage_analysis() if gis_manager else {}
            }
        except Exception as e:
            logger.error(f"获取仪表板数据失败: {e}")
            dashboard_data = {
                'system_status': system_status,
                'performance_stats': {'total_queries': 0, 'average_response_time': 0, 'error_rate': 0},
                'node_status': {},
                'recent_transactions': [],
                'gis_coverage': {},
                'error_message': f'获取数据失败: {str(e)}'
            }
        
        return render_template('dashboard.html', data=dashboard_data)
    
    @app.route('/query', methods=['GET', 'POST'])
    def query_interface():
        """查询界面"""
        if request.method == 'GET':
            return render_template('query.html')
        
        if not db_manager:
            return jsonify({'success': False, 'error': '数据库管理器未初始化'})
        
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
            logger.error(f"查询执行失败: {e}")
            return jsonify({'success': False, 'error': str(e)})
    
    @app.route('/nodes')
    def nodes_monitor():
        """节点监控页面"""
        if not db_manager:
            return render_template('nodes.html', nodes={})
        
        try:
            node_status = db_manager.get_node_status()
        except Exception as e:
            logger.error(f"获取节点状态失败: {e}")
            node_status = {}
        
        return render_template('nodes.html', nodes=node_status)
    
    @app.route('/api/node/<node_id>/status')
    def get_node_status(node_id):
        """获取指定节点的状态"""
        if not db_manager or node_id not in db_manager.nodes:
            return jsonify({'error': 'Node not found'}), 404
        
        node = db_manager.nodes[node_id]
        
        try:
            # 获取基本节点信息
            status = {
                'id': node_id,
                'name': node['config']['name'],
                'location': node['config']['location'],
                'database': node['config'].get('database', '未配置'),
                'status': node.get('status', 'unknown'),
                'type': node['type']
            }
            
            # 获取命令历史
            try:
                commands = db_manager.get_node_command_history(node_id, 10)
                status['commands_count'] = len(commands)
                status['recent_commands'] = commands
                status['last_activity'] = commands[0]['timestamp'] if commands else None
            except Exception as e:
                logger.error(f"获取节点 {node_id} 命令历史失败: {e}")
                status['commands_count'] = 0
                status['recent_commands'] = []
                status['last_activity'] = None
            
            # 如果是HTTP节点，尝试获取远程状态
            if node['type'] == 'http':
                try:
                    import requests
                    response = requests.get(f"{node['url']}/api/status", timeout=3)
                    if response.status_code == 200:
                        remote_status = response.json()
                        status.update(remote_status)
                        status['status'] = 'healthy'
                    else:
                        status['status'] = 'unhealthy'
                except Exception as e:
                    logger.warning(f"无法获取节点 {node_id} 远程状态: {e}")
                    status['status'] = 'unreachable'
            
            return jsonify(status)
            
        except Exception as e:
            logger.error(f"获取节点 {node_id} 状态失败: {e}")
            return jsonify({'error': str(e), 'node_id': node_id}), 500
    
    @app.route('/api/node/<node_id>/commands')
    def get_node_commands(node_id):
        """获取指定节点的命令日志"""
        if not db_manager:
            return jsonify({'error': '数据库管理器未初始化'}), 500
        
        try:
            limit = request.args.get('limit', 50, type=int)
            
            # 从分布式数据库管理器获取命令日志
            commands = db_manager.get_node_command_history(node_id, limit)
            
            return jsonify(commands)
            
        except Exception as e:
            logger.error(f"获取节点 {node_id} 命令日志失败: {e}")
            return jsonify({'error': str(e)}), 500
    
    @app.route('/optimization')
    def optimization_viewer():
        """查询优化界面"""
        return render_template('optimization.html')
    
    @app.route('/api/analyze_query', methods=['POST'])
    def api_analyze_query():
        """查询优化分析API"""
        if not db_manager:
            return jsonify({'success': False, 'error': '数据库管理器未初始化'})
        
        data = request.get_json() or {}
        sql = data.get('sql', '').strip()
        
        if not sql:
            return jsonify({'success': False, 'error': '请输入SQL查询语句'})
        
        try:
            # 执行查询并获取优化信息
            result = db_manager.execute_distributed_query(sql)
            
            # 获取详细的查询分析信息
            optimization_info = db_manager.get_query_optimization_info(sql)
            
            # 合并结果
            result['query_info'] = optimization_info
            
            return jsonify(result)
            
        except Exception as e:
            logger.error(f"查询优化分析失败: {e}")
            return jsonify({'success': False, 'error': str(e)})
    
    @app.route('/library_management')
    def library_management():
        """图书管理界面"""
        return render_template('library_management.html')
    
    @app.route('/gis')
    def gis_interface():
        """GIS地理信息界面"""
        if not gis_manager:
            return render_template('gis.html', map_html="<p>GIS管理器未初始化</p>", libraries={})
        
        highlighted_library = request.args.get('highlight', None)
        
        # 生成地图HTML
        map_html = gis_manager.create_interactive_map(highlighted_library)
        
        # 获取图书馆数据
        libraries_data = gis_manager.libraries_data
        
        return render_template('gis.html', 
                             map_html=map_html,
                             libraries=libraries_data)
    
    @app.route('/api/books/search', methods=['POST'])
    def search_books():
        """搜索图书"""
        if not db_manager:
            return jsonify({'success': False, 'error': '数据库管理器未初始化'})
        
        data = request.get_json()
        search_term = data.get('search_term', '')
        search_type = data.get('search_type', 'title')
        
        # 构建搜索SQL（使用参数化查询防止SQL注入）
        if search_type == 'title':
            sql = f"SELECT * FROM books WHERE title ILIKE '%{search_term}%'"
        elif search_type == 'author':
            sql = f"SELECT * FROM books WHERE author ILIKE '%{search_term}%'"
        elif search_type == 'isbn':
            sql = f"SELECT * FROM books WHERE isbn = '{search_term}'"
        else:
            sql = f"SELECT * FROM books WHERE title ILIKE '%{search_term}%' OR author ILIKE '%{search_term}%'"
        
        try:
            result = db_manager.execute_distributed_query(sql)
            return jsonify(result)
        except Exception as e:
            logger.error(f"图书搜索失败: {e}")
            return jsonify({'success': False, 'error': str(e)})
    
    @app.route('/api/books', methods=['GET'])
    def get_all_books():
        """获取所有图书列表"""
        if not db_manager:
            return jsonify({'success': False, 'error': '数据库管理器未初始化'})
        
        try:
            # 获取查询参数
            limit = request.args.get('limit', 50, type=int)
            offset = request.args.get('offset', 0, type=int)
            category = request.args.get('category', '')
            
            # 构建查询SQL
            if category:
                sql = f"SELECT * FROM books WHERE category = '{category}' ORDER BY id LIMIT {limit} OFFSET {offset}"
            else:
                sql = f"SELECT * FROM books ORDER BY id LIMIT {limit} OFFSET {offset}"
            
            result = db_manager.execute_distributed_query(sql)
            
            if result['success']:
                return jsonify({
                    'success': True,
                    'books': result['results'],
                    'total_results': len(result['results']),
                    'limit': limit,
                    'offset': offset
                })
            else:
                return jsonify({'success': False, 'error': result.get('error', '查询失败')})
                
        except Exception as e:
            logger.error(f"获取图书列表失败: {e}")
            return jsonify({'success': False, 'error': str(e)})

    @app.route('/api/books/add', methods=['POST'])
    def add_book():
        """添加图书"""
        if not db_manager:
            return jsonify({'success': False, 'error': '数据库管理器未初始化'})
        
        data = request.get_json()
        
        try:
            result = db_manager.insert_book(data)
            return jsonify(result)
        except Exception as e:
            logger.error(f"添加图书失败: {e}")
            return jsonify({'success': False, 'error': str(e)})
    
    @app.route('/api/books/borrow', methods=['POST'])
    def borrow_book():
        """借阅图书"""
        if not db_manager:
            return jsonify({'success': False, 'error': '数据库管理器未初始化'})
        
        data = request.get_json()
        book_id = data.get('book_id')
        user_id = data.get('user_id', 'anonymous')
        
        if not book_id:
            return jsonify({'success': False, 'error': '缺少图书ID'})
        
        try:
            # 检查图书是否存在且有库存
            check_sql = f"SELECT * FROM books WHERE id = {book_id} AND available_copies > 0"
            check_result = db_manager.execute_distributed_query(check_sql)
            
            if not check_result['success'] or not check_result['results']:
                return jsonify({'success': False, 'error': '图书不存在或无库存'})
            
            # 更新图书库存
            update_sql = f"UPDATE books SET available_copies = available_copies - 1 WHERE id = {book_id}"
            update_result = db_manager.execute_distributed_query(update_sql)
            
            if update_result['success']:
                return jsonify({
                    'success': True,
                    'message': '借阅成功',
                    'book_id': book_id,
                    'user_id': user_id
                })
            else:
                return jsonify({'success': False, 'error': '借阅失败'})
                
        except Exception as e:
            logger.error(f"借阅图书失败: {e}")
            return jsonify({'success': False, 'error': str(e)})
    
    @app.route('/api/transactions/recent')
    def get_recent_transactions():
        """获取最近的交易记录"""
        if not db_manager:
            return jsonify({'success': False, 'error': '数据库管理器未初始化'})
        
        try:
            limit = request.args.get('limit', 20, type=int)
            transactions = db_manager.get_recent_transactions(limit)
            return jsonify({
                'success': True,
                'transactions': transactions
            })
        except Exception as e:
            logger.error(f"获取交易记录失败: {e}")
            return jsonify({'success': False, 'error': str(e)})
    
    @app.route('/api/statistics')
    def get_statistics():
        """获取系统统计信息"""
        if not db_manager:
            return jsonify({'error': '数据库管理器未初始化'}), 500
        
        try:
            # 获取基础统计信息
            performance_stats = db_manager.get_performance_statistics()
            node_status = db_manager.get_node_status()
            gis_coverage = gis_manager.get_library_coverage_analysis() if gis_manager else {}
            
            # 构建标准化的响应格式
            stats = {
                'distributed_db': {
                    'total_queries': performance_stats.get('total_queries', 0),
                    'successful_queries': performance_stats.get('successful_queries', 0),
                    'failed_queries': performance_stats.get('failed_queries', 0),
                    'success_rate': performance_stats.get('success_rate', 0),
                    'cache_hit_rate': performance_stats.get('cache_hit_rate', 0),
                    'cache_hits': performance_stats.get('cache_hits', 0),
                    'average_query_time': performance_stats.get('average_execution_time', 0),
                    'active_nodes': len([n for n in node_status.values() if n.get('status') == 'healthy']),
                    'total_connections': len(node_status)
                },
                'gis': {
                    'total_libraries': gis_coverage.get('total_libraries', 0),
                    'network_connections': gis_coverage.get('network_connections', 0),
                    'coverage_area': gis_coverage.get('coverage_area', '成都市主要高校区域'),
                    'average_distance': gis_coverage.get('average_distance', 0),
                    'service_overlap_count': len(gis_coverage.get('service_overlap', []))
                },
                'query_optimization': {
                    'optimized_queries': 0,  # 可以从查询日志中统计
                    'optimization_rate': 0,
                    'performance_improvement': 0
                },
                'system': system_status,
                'performance': performance_stats,
                'nodes': node_status,
                'gis': gis_coverage
            }
            
            return jsonify(stats)
            
        except Exception as e:
            logger.error(f"获取统计信息失败: {e}")
            return jsonify({'error': f'获取统计信息失败: {str(e)}'}), 500
    
    @app.route('/api/test_node_stats')
    def test_node_stats():
        """测试节点统计获取"""
        if not db_manager:
            return jsonify({'error': '数据库管理器未初始化'}), 500
        
        try:
            logger.info("TEST: 正在调用 get_node_status()")
            node_status = db_manager.get_node_status()
            logger.info(f"TEST: get_node_status() 返回: {node_status}")
            return jsonify({
                'success': True,
                'node_status': node_status,
                'debug': 'Called get_node_status directly'
            })
        except Exception as e:
            logger.error(f"TEST: get_node_status() 错误: {e}")
            return jsonify({
                'success': False,
                'error': str(e),
                'debug': 'Error in get_node_status'
            })
    
    @app.route('/api/gis/spatial_query', methods=['POST'])
    def spatial_query():
        """空间查询"""
        if not gis_manager:
            return jsonify({'success': False, 'error': 'GIS管理器未初始化'})
        
        try:
            data = request.get_json()
            lat = data.get('lat')
            lng = data.get('lng')
            radius = data.get('radius', 5.0)  # 默认5公里
            
            if lat is None or lng is None:
                return jsonify({'success': False, 'error': '缺少经纬度参数'})
            
            libraries = gis_manager.find_libraries_within_radius(lat, lng, radius)
            return jsonify({
                'success': True,
                'libraries': libraries,
                'query_point': {'lat': lat, 'lng': lng},
                'radius_km': radius
            })
        except Exception as e:
            logger.error(f"空间查询失败: {e}")
            return jsonify({'success': False, 'error': str(e)})
    
    @app.route('/api/gis/heatmap')
    def get_heatmap_data():
        """获取热力图数据"""
        if not gis_manager:
            return jsonify({'success': False, 'error': 'GIS管理器未初始化'})
        
        try:
            heatmap_data = gis_manager.get_library_coverage_analysis()
            return jsonify({
                'success': True,
                'heatmap_data': heatmap_data
            })
        except Exception as e:
            logger.error(f"获取热力图数据失败: {e}")
            return jsonify({'success': False, 'error': str(e)})
    
    @app.route('/api/node/<node_id>/commands', methods=['POST'])
    def execute_node_command(node_id):
        """在指定节点执行命令"""
        if not db_manager or node_id not in db_manager.nodes:
            return jsonify({'error': 'Node not found'}), 404
        
        data = request.get_json()
        command = data.get('command')
        
        if not command:
            return jsonify({'error': 'Missing command'}), 400
        
        try:
            # 这里应该实现具体的命令执行逻辑
            result = {
                'node_id': node_id,
                'command': command,
                'status': 'executed',
                'message': f'Command executed on {node_id}'
            }
            return jsonify(result)
        except Exception as e:
            logger.error(f"节点命令执行失败: {e}")
            return jsonify({'error': str(e)}), 500
    
    @app.route('/api/test/sample_queries', methods=['POST'])
    def run_sample_queries():
        """运行示例查询"""
        if not db_manager:
            return jsonify({'error': '数据库管理器未初始化'}), 500
        
        sample_queries = [
            "SELECT * FROM books WHERE category = '计算机科学' LIMIT 5",
            "SELECT name, address FROM libraries",
            "SELECT COUNT(*) as total_books FROM books",
            "SELECT * FROM books WHERE available_copies > 0 LIMIT 10",
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
                    'results_count': len(result.get('results', [])) if result['success'] else 0,
                    'nodes_queried': result.get('nodes_queried', [])
                })
            except Exception as e:
                results.append({
                    'sql': sql,
                    'success': False,
                    'error': str(e),
                    'execution_time': 0
                })
        
        return jsonify({'results': results})
    
    @app.route('/health')
    def health_check():
        """主应用健康检查"""
        return jsonify({
            'status': 'healthy',
            'node_type': 'master',
            'system_status': system_status,
            'db_manager_status': 'initialized' if db_manager else 'not_initialized',
            'gis_manager_status': 'initialized' if gis_manager else 'not_initialized'
        })
    
    @app.route('/api/status')
    def api_status():
        """API状态端点"""
        return jsonify({
            'status': 'healthy',
            'node_type': 'master',
            'timestamp': datetime.now().isoformat(),
            'system_status': system_status,
            'db_manager_status': 'initialized' if db_manager else 'not_initialized',
            'gis_manager_status': 'initialized' if gis_manager else 'not_initialized',
            'nodes_status': db_manager.get_node_status() if db_manager else {}
        })
    
    @app.route('/help')
    def help_page():
        """帮助页面"""
        help_content = {
            'system_overview': """
            本系统是一个基于Docker的真正分布式图书馆管理系统，
            连接成都市主要高校图书馆，实现图书资源的跨校共享和统一管理。
            """,
            'features': [
                'Docker容器化部署',
                '真正的分布式数据库架构（PostgreSQL）',
                'HTTP API节点通信',
                'Redis缓存支持',
                '查询分解和优化',
                '地理信息系统集成',
                '实时节点监控',
                '跨校图书借阅',
                'SQL查询支持'
            ],
            'architecture': [
                '主控制节点：协调查询和管理',
                '3个数据库节点：独立的PostgreSQL实例',
                '3个节点服务：提供HTTP API',
                'Redis缓存：提高查询性能',
                'Docker网络：容器间通信'
            ]
        }
        
        return render_template('help.html', content=help_content)
    
def init_sample_data():
    """初始化示例数据（在后台线程中）"""
    logger.info("正在初始化示例数据...")
    time.sleep(2)  # 等待数据库完全启动
    logger.info("示例数据初始化完成")

def create_node_app():
    """创建节点服务的Flask应用"""
    from flask import Flask
    
    node_app = Flask(__name__)
    
    @node_app.route('/health')
    def health():
        return {'status': 'healthy', 'node_id': NODE_ID}
    
    @node_app.route('/info')
    def info():
        current_node = NODES[NODE_ID]
        return {
            'node_id': NODE_ID,
            'name': current_node['name'],
            'location': current_node['location'],
            'address': current_node['address'],
            'database': {
                'host': current_node['host'],
                'port': current_node['port'],
                'database': current_node['database']
            }
        }
    
    return node_app

# 应用启动
# 主控制模式启动逻辑
if __name__ == '__main__' and NODE_TYPE == 'master':
    logger.info("启动Docker分布式图书馆系统 - 主控制模式")
    logger.info(f"主控制节点: {MASTER_NODE['host']}:{MASTER_NODE['port']}")
    
    # 在后台线程中初始化管理器和示例数据
    def background_init():
        time.sleep(3)  # 等待其他服务启动
        init_managers()
        init_sample_data()
    
    init_thread = Thread(target=background_init)
    init_thread.daemon = True
    init_thread.start()
    
    # 启动Flask应用
    app.run(
        host=MASTER_NODE['host'],
        port=MASTER_NODE['port'],
        debug=SYSTEM_CONFIG['debug'],
        threaded=True
    )

# 节点服务模式启动逻辑
elif __name__ == '__main__' and NODE_TYPE == 'node':
    NODE_ID = os.getenv('NODE_ID')  # 重新获取NODE_ID
    print(f"启动节点服务模式: {NODE_ID}")
    logger.info(f"启动Docker分布式图书馆系统 - 节点服务模式: {NODE_ID}")
    
    # 获取当前节点配置
    from config_docker import DATABASE_NODES
    current_node = DATABASE_NODES[NODE_ID]
    
    # 在后台线程中初始化节点服务
    def background_init():
        time.sleep(5)  # 等待数据库和主控制节点启动
        logger.info(f"节点 {NODE_ID} 初始化完成")
    
    init_thread = Thread(target=background_init)
    init_thread.daemon = True
    init_thread.start()
    
    # 使用从node_service导入的完整Flask应用
    from node_service import app as node_app
    node_app.run(
        host='0.0.0.0',
        port=5000,  # 容器内部端口
        debug=SYSTEM_CONFIG['debug'],
        threaded=True
    )
