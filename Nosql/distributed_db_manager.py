# 分布式数据库管理器

import threading
import time
import json
from typing import List, Dict, Any, Optional
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime

from models import DatabaseNode
from query_processor import DistributedQueryProcessor, QueryFragment
from config import DATABASE_NODES, SHARDING_CONFIG

class DistributedDatabaseManager:
    """分布式数据库管理器"""
    
    def __init__(self):
        self.nodes = {}
        self.query_processor = DistributedQueryProcessor(DATABASE_NODES, SHARDING_CONFIG)
        self.global_transaction_log = []
        self.performance_stats = {
            'total_queries': 0,
            'successful_queries': 0,
            'failed_queries': 0,
            'average_response_time': 0.0
        }
        self.init_nodes()
        self.init_data_partitioning()
    
    def init_nodes(self):
        """初始化所有数据节点"""
        for node_id, config in DATABASE_NODES.items():
            self.nodes[node_id] = DatabaseNode(node_id, config)
            print(f"初始化节点 {node_id}: {config['name']}")
    
    def init_data_partitioning(self):
        """初始化数据分片"""
        if SHARDING_CONFIG['strategy'] == 'horizontal':
            self.init_horizontal_partitioning()
        elif SHARDING_CONFIG['strategy'] == 'vertical':
            self.init_vertical_partitioning()
        elif SHARDING_CONFIG['strategy'] == 'mixed':
            self.init_mixed_partitioning()
    
    def init_horizontal_partitioning(self):
        """水平分片初始化"""
        print("初始化水平分片策略...")
        
        # 为每个节点分配不同的图书馆ID范围
        node_ranges = {
            'node1': (1, 1),  # 四川大学
            'node2': (2, 2),  # 电子科技大学  
            'node3': (3, 3)   # 西南交通大学
        }
        
        for node_id, (start_id, end_id) in node_ranges.items():
            node = self.nodes[node_id]
            # 确保该节点只存储指定范围的数据
            node.log_command("PARTITION", f"水平分片范围: library_id {start_id}-{end_id}", "configured")
    
    def init_vertical_partitioning(self):
        """垂直分片初始化"""
        print("初始化垂直分片策略...")
        
        for node_id, tables in SHARDING_CONFIG['vertical_tables'].items():
            node = self.nodes[node_id]
            node.log_command("PARTITION", f"垂直分片表: {', '.join(tables)}", "configured")
    
    def init_mixed_partitioning(self):
        """混合分片初始化"""
        print("初始化混合分片策略...")
        self.init_horizontal_partitioning()
        self.init_vertical_partitioning()
    
    def log_global_transaction(self, transaction_type: str, details: Dict[str, Any]):
        """记录全局事务日志"""
        log_entry = {
            'timestamp': datetime.now().isoformat(),
            'type': transaction_type,
            'details': details,
            'transaction_id': f"TXN_{int(time.time() * 1000)}"
        }
        self.global_transaction_log.append(log_entry)
        
        # 只保留最近1000条记录
        if len(self.global_transaction_log) > 1000:
            self.global_transaction_log = self.global_transaction_log[-1000:]
    
    def execute_distributed_query(self, sql: str) -> Dict[str, Any]:
        """执行分布式查询"""
        start_time = time.time()
        self.performance_stats['total_queries'] += 1
        
        try:
            # 1. 查询处理（分解和优化）
            query_result = self.query_processor.process_query(sql)
            
            # 2. 执行查询片段
            fragments_results = self.execute_query_fragments(query_result['fragments'])
            
            # 3. 合并结果
            merged_results = self.merge_query_results(fragments_results, query_result['execution_plan'])
            
            # 4. 记录成功
            execution_time = time.time() - start_time
            self.performance_stats['successful_queries'] += 1
            self.update_average_response_time(execution_time)
            
            # 5. 记录全局事务
            self.log_global_transaction("DISTRIBUTED_QUERY", {
                'sql': sql,
                'execution_time': execution_time,
                'nodes_involved': [f.data_source for f in query_result['fragments']],
                'result_count': len(merged_results)
            })
            
            return {
                'success': True,
                'results': merged_results,
                'query_info': query_result,
                'execution_time': execution_time,
                'nodes_involved': len(query_result['fragments'])
            }
            
        except Exception as e:
            execution_time = time.time() - start_time
            self.performance_stats['failed_queries'] += 1
            
            self.log_global_transaction("DISTRIBUTED_QUERY_ERROR", {
                'sql': sql,
                'error': str(e),
                'execution_time': execution_time
            })
            
            return {
                'success': False,
                'error': str(e),
                'execution_time': execution_time
            }
    
    def execute_query_fragments(self, fragments: List[QueryFragment]) -> Dict[str, List[Dict]]:
        """并发执行查询片段"""
        fragments_results = {}
        
        with ThreadPoolExecutor(max_workers=len(fragments)) as executor:
            # 提交所有查询任务
            future_to_fragment = {}
            for fragment in fragments:
                for node_id in fragment.target_nodes:
                    future = executor.submit(self.execute_fragment_on_node, fragment, node_id)
                    future_to_fragment[future] = (fragment, node_id)
            
            # 收集结果
            for future in as_completed(future_to_fragment):
                fragment, node_id = future_to_fragment[future]
                try:
                    result = future.result()
                    fragments_results[node_id] = result
                except Exception as e:
                    print(f"节点 {node_id} 执行片段失败: {e}")
                    fragments_results[node_id] = []
        
        return fragments_results
    
    def execute_fragment_on_node(self, fragment: QueryFragment, node_id: str) -> List[Dict]:
        """在指定节点执行查询片段"""
        node = self.nodes[node_id]
        
        try:
            node.log_command("FRAGMENT_QUERY", fragment.sql, "received")
            result = node.execute_query(fragment.sql)
            node.log_command("FRAGMENT_RESULT", f"返回 {len(result)} 条记录", "sent")
            return result
        except Exception as e:
            node.log_command("FRAGMENT_ERROR", str(e), "failed")
            raise e
    
    def merge_query_results(self, fragments_results: Dict[str, List[Dict]], execution_plan: Dict[str, Any]) -> List[Dict]:
        """合并查询结果"""
        merged_results = []
        
        if execution_plan['merge_strategy'] == 'union':
            # 联合合并 - 去重
            seen_ids = set()
            for node_id, results in fragments_results.items():
                for row in results:
                    # 使用ID或组合键去重
                    row_id = row.get('id', f"{node_id}_{hash(str(row))}")
                    if row_id not in seen_ids:
                        seen_ids.add(row_id)
                        merged_results.append(row)
        
        elif execution_plan['merge_strategy'] == 'direct':
            # 直接合并 - 保留所有结果
            for results in fragments_results.values():
                merged_results.extend(results)
        
        return merged_results
    
    def update_average_response_time(self, execution_time: float):
        """更新平均响应时间"""
        total_successful = self.performance_stats['successful_queries']
        current_avg = self.performance_stats['average_response_time']
        
        # 计算新的平均值
        new_avg = ((current_avg * (total_successful - 1)) + execution_time) / total_successful
        self.performance_stats['average_response_time'] = new_avg
    
    def get_node_status(self) -> Dict[str, Any]:
        """获取所有节点状态"""
        status = {}
        for node_id, node in self.nodes.items():
            status[node_id] = {
                'name': node.name,
                'location': node.location,
                'commands_count': len(node.commands_log),
                'last_activity': node.commands_log[-1]['timestamp'] if node.commands_log else None
            }
        return status
    
    def get_performance_statistics(self) -> Dict[str, Any]:
        """获取性能统计"""
        stats = self.performance_stats.copy()
        
        # 确保所有值都是有效数字，避免NaN
        total_queries = stats.get('total_queries', 0)
        successful_queries = stats.get('successful_queries', 0)
        failed_queries = stats.get('failed_queries', 0)
        avg_response_time = stats.get('average_response_time', 0.0)
        
        # 计算成功率
        if total_queries > 0:
            success_rate = round((successful_queries / total_queries) * 100, 2)
            error_rate = round((failed_queries / total_queries) * 100, 2)
        else:
            success_rate = 100.0
            error_rate = 0.0
        
        # 转换响应时间为毫秒
        avg_response_time_ms = round(avg_response_time * 1000, 2) if avg_response_time else 0.0
        
        return {
            'total_queries': total_queries,
            'successful_queries': successful_queries,
            'failed_queries': failed_queries,
            'success_rate': success_rate,
            'error_rate': error_rate,
            'average_response_time': avg_response_time_ms,
            'average_execution_time': avg_response_time_ms,
            'cache_hits': 0,  # 非Docker版本暂无缓存统计
            'cache_hit_rate': 0.0
        }
    
    def get_recent_transactions(self, limit: int = 50) -> List[Dict[str, Any]]:
        """获取最近的事务日志"""
        return self.global_transaction_log[-limit:] if self.global_transaction_log else []
    
    def get_node_commands_log(self, node_id: str, limit: int = 50) -> List[Dict[str, Any]]:
        """获取指定节点的命令日志"""
        if node_id in self.nodes:
            return self.nodes[node_id].commands_log[-limit:]
        return []
    
    def insert_book(self, book_data: Dict[str, Any]) -> Dict[str, Any]:
        """插入图书记录"""
        # 根据分片策略确定目标节点
        target_node = self.determine_insert_node(book_data)
        
        sql = '''
            INSERT INTO books (isbn, title, author, publisher, publish_year, category, 
                             library_id, total_copies, available_copies, location_in_library)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        '''
        
        params = (
            book_data['isbn'],
            book_data['title'], 
            book_data['author'],
            book_data.get('publisher', ''),
            book_data.get('publish_year', 2024),
            book_data.get('category', ''),
            book_data['library_id'],
            book_data.get('total_copies', 1),
            book_data.get('available_copies', 1),
            book_data.get('location_in_library', '')
        )
        
        try:
            result = self.nodes[target_node].execute_query(sql, params)
            
            self.log_global_transaction("INSERT_BOOK", {
                'book': book_data,
                'target_node': target_node,
                'success': True
            })
            
            return {'success': True, 'target_node': target_node}
            
        except Exception as e:
            self.log_global_transaction("INSERT_BOOK_ERROR", {
                'book': book_data,
                'target_node': target_node,
                'error': str(e)
            })
            return {'success': False, 'error': str(e)}
    
    def determine_insert_node(self, data: Dict[str, Any]) -> str:
        """根据分片策略确定插入节点"""
        if SHARDING_CONFIG['strategy'] == 'horizontal':
            # 水平分片：根据library_id确定
            library_id = data.get('library_id', 1)
            node_mapping = {1: 'node1', 2: 'node2', 3: 'node3'}
            return node_mapping.get(library_id, 'node1')
        
        elif SHARDING_CONFIG['strategy'] == 'vertical':
            # 垂直分片：根据表类型确定
            # 这里假设是books表
            for node_id, tables in SHARDING_CONFIG['vertical_tables'].items():
                if 'books' in tables:
                    return node_id
        
        return 'node1'  # 默认节点
    
    def borrow_book(self, user_id: int, book_id: int, library_id: int) -> Dict[str, Any]:
        """借书操作"""
        try:
            # 1. 检查图书可用性
            check_sql = "SELECT available_copies FROM books WHERE id = ? AND library_id = ?"
            target_node = self.determine_query_node('books', library_id)
            
            result = self.nodes[target_node].execute_query(check_sql, (book_id, library_id))
            
            if not result or result[0]['available_copies'] <= 0:
                return {'success': False, 'error': '图书不可用'}
            
            # 2. 更新图书可用数量
            update_sql = "UPDATE books SET available_copies = available_copies - 1 WHERE id = ? AND library_id = ?"
            self.nodes[target_node].execute_query(update_sql, (book_id, library_id))
            
            # 3. 插入借阅记录
            borrow_sql = '''
                INSERT INTO borrowings (user_id, book_id, library_id, due_date, status)
                VALUES (?, ?, ?, date('now', '+30 days'), 'borrowed')
            '''
            
            # 借阅记录可能存储在不同节点
            borrow_node = self.determine_query_node('borrowings', library_id)
            self.nodes[borrow_node].execute_query(borrow_sql, (user_id, book_id, library_id))
            
            self.log_global_transaction("BORROW_BOOK", {
                'user_id': user_id,
                'book_id': book_id,
                'library_id': library_id,
                'book_node': target_node,
                'borrow_node': borrow_node
            })
            
            return {'success': True, 'message': '借书成功'}
            
        except Exception as e:
            self.log_global_transaction("BORROW_BOOK_ERROR", {
                'user_id': user_id,
                'book_id': book_id,
                'library_id': library_id,
                'error': str(e)
            })
            return {'success': False, 'error': str(e)}
    
    def determine_query_node(self, table_name: str, library_id: int = None) -> str:
        """确定查询的目标节点"""
        if SHARDING_CONFIG['strategy'] == 'horizontal' and library_id:
            node_mapping = {1: 'node1', 2: 'node2', 3: 'node3'}
            return node_mapping.get(library_id, 'node1')
        
        elif SHARDING_CONFIG['strategy'] == 'vertical':
            for node_id, tables in SHARDING_CONFIG['vertical_tables'].items():
                if table_name in tables:
                    return node_id
        
        return 'node1'  # 默认节点


