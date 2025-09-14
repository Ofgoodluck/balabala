# 查询分解和优化模块

import re
import sqlparse
from typing import List, Dict, Any, Tuple
from dataclasses import dataclass
from datetime import datetime

@dataclass
class QueryFragment:
    """查询片段"""
    sql: str
    target_nodes: List[str]
    estimated_cost: float
    data_source: str

@dataclass
class OptimizationStep:
    """优化步骤"""
    step_name: str
    before_sql: str
    after_sql: str
    explanation: str
    cost_reduction: float

class QueryOptimizer:
    """查询优化器"""
    
    def __init__(self, nodes_config: Dict[str, Any]):
        self.nodes_config = nodes_config
        self.optimization_log = []
    
    def log_optimization(self, step: OptimizationStep):
        """记录优化步骤"""
        self.optimization_log.append({
            'timestamp': datetime.now().isoformat(),
            'step': step
        })
    
    def estimate_cost(self, sql: str, node_id: str) -> float:
        """估算查询成本"""
        # 简单的成本估算模型
        base_cost = 1.0
        
        # 根据操作类型调整成本
        if 'JOIN' in sql.upper():
            base_cost *= 2.0
        if 'ORDER BY' in sql.upper():
            base_cost *= 1.5
        if 'GROUP BY' in sql.upper():
            base_cost *= 1.3
        if '*' in sql:
            base_cost *= 1.2  # 全字段查询成本较高
            
        # 根据节点位置调整成本（模拟网络延迟）
        node_costs = {'node1': 1.0, 'node2': 1.1, 'node3': 1.2}
        base_cost *= node_costs.get(node_id, 1.0)
        
        return base_cost
    
    def optimize_projection(self, sql: str) -> Tuple[str, str]:
        """投影优化 - 只选择需要的字段"""
        if 'SELECT *' in sql.upper():
            # 分析查询需要的字段
            explanation = "将 SELECT * 替换为具体字段，减少数据传输量"
            optimized = sql.replace('SELECT *', 'SELECT id, title, author, isbn')
            return optimized, explanation
        return sql, "无需投影优化"
    
    def optimize_selection(self, sql: str) -> Tuple[str, str]:
        """选择优化 - 下推WHERE条件"""
        parsed = sqlparse.parse(sql)[0]
        
        # 简单的WHERE条件下推
        if 'WHERE' in sql.upper():
            explanation = "WHERE条件已存在，在各节点本地执行过滤"
            return sql, explanation
        else:
            explanation = "建议添加WHERE条件进行数据过滤"
            return sql, explanation
    
    def optimize_join(self, sql: str) -> Tuple[str, str]:
        """连接优化"""
        if 'JOIN' in sql.upper():
            explanation = "优化JOIN顺序，小表驱动大表"
            # 这里可以实现更复杂的JOIN优化
            return sql, explanation
        return sql, "无JOIN操作"
    
    def optimize_query(self, sql: str) -> List[OptimizationStep]:
        """执行查询优化"""
        optimization_steps = []
        current_sql = sql
        
        # 1. 投影优化
        optimized_sql, explanation = self.optimize_projection(current_sql)
        if optimized_sql != current_sql:
            step = OptimizationStep(
                step_name="投影优化",
                before_sql=current_sql,
                after_sql=optimized_sql,
                explanation=explanation,
                cost_reduction=0.2
            )
            optimization_steps.append(step)
            self.log_optimization(step)
            current_sql = optimized_sql
        
        # 2. 选择优化
        optimized_sql, explanation = self.optimize_selection(current_sql)
        if optimized_sql != current_sql:
            step = OptimizationStep(
                step_name="选择优化",
                before_sql=current_sql,
                after_sql=optimized_sql,
                explanation=explanation,
                cost_reduction=0.3
            )
            optimization_steps.append(step)
            self.log_optimization(step)
            current_sql = optimized_sql
        
        # 3. 连接优化
        optimized_sql, explanation = self.optimize_join(current_sql)
        if optimized_sql != current_sql:
            step = OptimizationStep(
                step_name="连接优化",
                before_sql=current_sql,
                after_sql=optimized_sql,
                explanation=explanation,
                cost_reduction=0.4
            )
            optimization_steps.append(step)
            self.log_optimization(step)
            current_sql = optimized_sql
            
        return optimization_steps

class QueryDecomposer:
    """查询分解器"""
    
    def __init__(self, nodes_config: Dict[str, Any], sharding_config: Dict[str, Any]):
        self.nodes_config = nodes_config
        self.sharding_config = sharding_config
        self.decomposition_log = []
    
    def log_decomposition(self, message: str):
        """记录分解过程"""
        self.decomposition_log.append({
            'timestamp': datetime.now().isoformat(),
            'message': message
        })
    
    def analyze_query(self, sql: str) -> Dict[str, Any]:
        """分析查询语句"""
        parsed = sqlparse.parse(sql)[0]
        
        analysis = {
            'type': 'SELECT',
            'tables': [],
            'conditions': [],
            'joins': [],
            'requires_all_nodes': False
        }
        
        # 提取表名
        tokens = list(parsed.flatten())
        in_from = False
        for token in tokens:
            if token.ttype is sqlparse.tokens.Keyword and token.value.upper() == 'FROM':
                in_from = True
                continue
            elif in_from and token.ttype is None and token.value.strip():
                analysis['tables'].append(token.value.strip())
                in_from = False
        
        # 检查是否需要跨节点查询
        if 'JOIN' in sql.upper() or len(analysis['tables']) > 1:
            analysis['requires_all_nodes'] = True
            
        self.log_decomposition(f"查询分析完成: {analysis}")
        return analysis
    
    def determine_target_nodes(self, query_analysis: Dict[str, Any]) -> List[str]:
        """确定目标节点"""
        if query_analysis['requires_all_nodes']:
            target_nodes = list(self.nodes_config.keys())
            self.log_decomposition(f"跨节点查询，目标节点: {target_nodes}")
        else:
            # 根据分片策略确定节点
            if self.sharding_config['strategy'] == 'horizontal':
                # 水平分片：根据分片键确定
                target_nodes = list(self.nodes_config.keys())
            else:
                # 垂直分片：根据表分布确定
                table_name = query_analysis['tables'][0] if query_analysis['tables'] else 'books'
                target_nodes = []
                for node_id, tables in self.sharding_config['vertical_tables'].items():
                    if table_name in tables:
                        target_nodes.append(node_id)
                        
            self.log_decomposition(f"单节点查询，目标节点: {target_nodes}")
        
        return target_nodes if target_nodes else ['node1']
    
    def decompose_query(self, sql: str) -> List[QueryFragment]:
        """分解查询"""
        self.log_decomposition(f"开始分解查询: {sql}")
        
        query_analysis = self.analyze_query(sql)
        target_nodes = self.determine_target_nodes(query_analysis)
        
        fragments = []
        
        if query_analysis['requires_all_nodes']:
            # 跨节点查询分解
            for node_id in target_nodes:
                fragment_sql = self.adapt_sql_for_node(sql, node_id)
                fragment = QueryFragment(
                    sql=fragment_sql,
                    target_nodes=[node_id],
                    estimated_cost=self.estimate_fragment_cost(fragment_sql, node_id),
                    data_source=node_id
                )
                fragments.append(fragment)
                self.log_decomposition(f"创建片段 {node_id}: {fragment_sql}")
        else:
            # 单节点查询
            for node_id in target_nodes:
                fragment = QueryFragment(
                    sql=sql,
                    target_nodes=[node_id],
                    estimated_cost=self.estimate_fragment_cost(sql, node_id),
                    data_source=node_id
                )
                fragments.append(fragment)
                self.log_decomposition(f"单节点查询片段 {node_id}: {sql}")
        
        return fragments
    
    def adapt_sql_for_node(self, sql: str, node_id: str) -> str:
        """为特定节点适配SQL"""
        # 根据节点的数据分布调整SQL
        if self.sharding_config['strategy'] == 'vertical':
            # 垂直分片：只查询该节点拥有的表
            node_tables = self.sharding_config['vertical_tables'].get(node_id, [])
            # 这里可以实现更复杂的SQL重写逻辑
            return sql
        elif self.sharding_config['strategy'] == 'horizontal':
            # 水平分片：添加分片条件
            # 例如：WHERE library_id = node_id
            if 'WHERE' in sql.upper():
                return sql + f" AND library_id = {node_id[-1]}"
            else:
                return sql + f" WHERE library_id = {node_id[-1]}"
        
        return sql
    
    def estimate_fragment_cost(self, sql: str, node_id: str) -> float:
        """估算片段成本"""
        optimizer = QueryOptimizer(self.nodes_config)
        return optimizer.estimate_cost(sql, node_id)

class DistributedQueryProcessor:
    """分布式查询处理器"""
    
    def __init__(self, nodes_config: Dict[str, Any], sharding_config: Dict[str, Any]):
        self.nodes_config = nodes_config
        self.sharding_config = sharding_config
        self.optimizer = QueryOptimizer(nodes_config)
        self.decomposer = QueryDecomposer(nodes_config, sharding_config)
        self.processing_log = []
    
    def log_processing(self, message: str):
        """记录处理过程"""
        self.processing_log.append({
            'timestamp': datetime.now().isoformat(),
            'message': message
        })
    
    def process_query(self, sql: str) -> Dict[str, Any]:
        """处理分布式查询"""
        self.log_processing(f"接收查询: {sql}")
        
        # 1. 查询优化
        optimization_steps = self.optimizer.optimize_query(sql)
        optimized_sql = sql
        if optimization_steps:
            optimized_sql = optimization_steps[-1].after_sql
            self.log_processing(f"查询优化完成，优化步骤数: {len(optimization_steps)}")
        
        # 2. 查询分解
        fragments = self.decomposer.decompose_query(optimized_sql)
        self.log_processing(f"查询分解完成，片段数: {len(fragments)}")
        
        # 3. 生成执行计划
        execution_plan = self.generate_execution_plan(fragments)
        self.log_processing(f"执行计划生成完成")
        
        return {
            'original_sql': sql,
            'optimized_sql': optimized_sql,
            'optimization_steps': optimization_steps,
            'fragments': fragments,
            'execution_plan': execution_plan,
            'total_estimated_cost': sum(f.estimated_cost for f in fragments),
            'processing_log': self.processing_log.copy(),
            'optimization_log': self.optimizer.optimization_log.copy(),
            'decomposition_log': self.decomposer.decomposition_log.copy()
        }
    
    def generate_execution_plan(self, fragments: List[QueryFragment]) -> Dict[str, Any]:
        """生成执行计划"""
        # 按成本排序片段
        sorted_fragments = sorted(fragments, key=lambda f: f.estimated_cost)
        
        plan = {
            'execution_order': [f.data_source for f in sorted_fragments],
            'parallel_execution': len(fragments) > 1,
            'merge_strategy': 'union' if len(fragments) > 1 else 'direct',
            'estimated_total_time': sum(f.estimated_cost for f in fragments) / len(fragments)
        }
        
        return plan
