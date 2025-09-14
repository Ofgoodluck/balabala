#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
分布式图书馆管理系统 - 自动化测试脚本
测试所有核心功能模块
"""

import requests
import json
import time
import sys
import traceback
from typing import Dict, List, Any

class SystemTester:
    def __init__(self, base_url: str = "http://localhost:5000"):
        self.base_url = base_url
        self.session = requests.Session()
        self.test_results = []
        
    def wait_for_server(self, timeout: int = 30) -> bool:
        """等待服务器启动"""
        print("等待服务器启动...")
        for i in range(timeout):
            try:
                response = self.session.get(self.base_url, timeout=5)
                if response.status_code == 200:
                    print(f"✅ 服务器已启动 (耗时 {i+1}s)")
                    return True
            except:
                pass
            time.sleep(1)
            print(f"⏳ 等待中... ({i+1}/{timeout}s)")
        return False
    
    def log_test(self, test_name: str, success: bool, details: str = ""):
        """记录测试结果"""
        status = "✅ 通过" if success else "❌ 失败"
        print(f"{status} {test_name}")
        if details:
            print(f"   详情: {details}")
        self.test_results.append({
            'test': test_name,
            'success': success,
            'details': details
        })
    
    def test_basic_connectivity(self) -> bool:
        """测试基本连接"""
        try:
            response = self.session.get(self.base_url)
            success = response.status_code == 200
            self.log_test("基本连接测试", success, f"状态码: {response.status_code}")
            return success
        except Exception as e:
            self.log_test("基本连接测试", False, str(e))
            return False
    
    def test_database_nodes(self) -> bool:
        """测试数据库节点"""
        try:
            response = self.session.get(f"{self.base_url}/api/statistics")
            if response.status_code == 200:
                data = response.json()
                nodes = data.get('nodes', {})
                success = len(nodes) >= 3
                self.log_test("数据库节点测试", success, f"检测到 {len(nodes)} 个节点")
                return success
            else:
                self.log_test("数据库节点测试", False, f"API返回错误: {response.status_code}")
                return False
        except Exception as e:
            self.log_test("数据库节点测试", False, str(e))
            return False
    
    def test_sql_queries(self) -> bool:
        """测试SQL查询功能"""
        test_queries = [
            "SELECT COUNT(*) FROM books",
            "SELECT name FROM libraries",
            "SELECT library_id, COUNT(*) FROM books GROUP BY library_id",
            "SELECT * FROM books WHERE category = '计算机科学' LIMIT 5"
        ]
        
        all_success = True
        for query in test_queries:
            try:
                response = self.session.post(
                    f"{self.base_url}/query",
                    data={'sql': query},
                    headers={'Content-Type': 'application/x-www-form-urlencoded'}
                )
                
                if response.status_code == 200:
                    result = response.json()
                    success = 'result' in result or 'results' in result
                    self.log_test(f"SQL查询测试: {query[:30]}...", success)
                    if not success:
                        all_success = False
                else:
                    self.log_test(f"SQL查询测试: {query[:30]}...", False, f"状态码: {response.status_code}")
                    all_success = False
                    
            except Exception as e:
                self.log_test(f"SQL查询测试: {query[:30]}...", False, str(e))
                all_success = False
                
        return all_success
    
    def test_book_search(self) -> bool:
        """测试图书搜索功能"""
        try:
            search_data = {
                'search_term': '计算机',
                'search_type': 'title'
            }
            
            response = self.session.post(
                f"{self.base_url}/api/books/search",
                json=search_data,
                headers={'Content-Type': 'application/json'}
            )
            
            if response.status_code == 200:
                result = response.json()
                success = 'books' in result and isinstance(result['books'], list)
                count = len(result.get('books', []))
                self.log_test("图书搜索测试", success, f"找到 {count} 本相关图书")
                return success
            else:
                self.log_test("图书搜索测试", False, f"状态码: {response.status_code}")
                return False
                
        except Exception as e:
            self.log_test("图书搜索测试", False, str(e))
            return False
    
    def test_gis_functionality(self) -> bool:
        """测试GIS地理信息功能"""
        try:
            # 测试空间查询
            spatial_data = {
                'type': 'nearest_library',
                'params': {
                    'lat': 30.6586,
                    'lng': 104.0647
                }
            }
            
            response = self.session.post(
                f"{self.base_url}/api/gis/spatial_query",
                json=spatial_data,
                headers={'Content-Type': 'application/json'}
            )
            
            if response.status_code == 200:
                result = response.json()
                success = 'result' in result
                self.log_test("GIS空间查询测试", success)
                return success
            else:
                self.log_test("GIS空间查询测试", False, f"状态码: {response.status_code}")
                return False
                
        except Exception as e:
            self.log_test("GIS空间查询测试", False, str(e))
            return False
    
    def test_query_optimization(self) -> bool:
        """测试查询优化功能"""
        try:
            # 测试一个复杂查询的优化
            complex_query = """
            SELECT b.title, l.name, COUNT(*) as book_count
            FROM books b 
            JOIN libraries l ON b.library_id = l.id 
            WHERE b.category = '计算机科学'
            GROUP BY b.library_id
            """
            
            response = self.session.post(
                f"{self.base_url}/query",
                data={'sql': complex_query},
                headers={'Content-Type': 'application/x-www-form-urlencoded'}
            )
            
            if response.status_code == 200:
                result = response.json()
                # 检查是否包含优化信息
                has_optimization = any([
                    'optimization_steps' in result,
                    'decomposed_queries' in result,
                    'execution_plan' in result
                ])
                self.log_test("查询优化测试", has_optimization)
                return has_optimization
            else:
                self.log_test("查询优化测试", False, f"状态码: {response.status_code}")
                return False
                
        except Exception as e:
            self.log_test("查询优化测试", False, str(e))
            return False
    
    def test_node_monitoring(self) -> bool:
        """测试节点监控功能"""
        try:
            # 测试节点状态
            response = self.session.get(f"{self.base_url}/api/node/node1/status")
            
            if response.status_code == 200:
                result = response.json()
                success = 'status' in result
                self.log_test("节点监控测试", success)
                return success
            else:
                self.log_test("节点监控测试", False, f"状态码: {response.status_code}")
                return False
                
        except Exception as e:
            self.log_test("节点监控测试", False, str(e))
            return False
    
    def test_book_management(self) -> bool:
        """测试图书管理功能"""
        try:
            # 测试添加图书
            new_book = {
                'title': '测试图书 - Python编程',
                'author': '测试作者',
                'isbn': '978-0000000000',
                'category': '计算机科学',
                'library_id': 1,
                'copies': 3
            }
            
            response = self.session.post(
                f"{self.base_url}/api/books/add",
                json=new_book,
                headers={'Content-Type': 'application/json'}
            )
            
            if response.status_code == 200:
                result = response.json()
                success = result.get('success', False)
                self.log_test("图书管理测试", success, "成功添加测试图书")
                return success
            else:
                self.log_test("图书管理测试", False, f"状态码: {response.status_code}")
                return False
                
        except Exception as e:
            self.log_test("图书管理测试", False, str(e))
            return False
    
    def test_performance(self) -> bool:
        """测试系统性能"""
        try:
            start_time = time.time()
            
            # 并发执行多个查询
            queries = [
                "SELECT COUNT(*) FROM books",
                "SELECT COUNT(*) FROM libraries", 
                "SELECT COUNT(*) FROM users"
            ]
            
            for query in queries:
                self.session.post(
                    f"{self.base_url}/query",
                    data={'sql': query},
                    headers={'Content-Type': 'application/x-www-form-urlencoded'}
                )
            
            end_time = time.time()
            duration = end_time - start_time
            
            success = duration < 10.0  # 10秒内完成
            self.log_test("性能测试", success, f"查询耗时: {duration:.2f}s")
            return success
            
        except Exception as e:
            self.log_test("性能测试", False, str(e))
            return False
    
    def run_all_tests(self) -> Dict[str, Any]:
        """运行所有测试"""
        print("🚀 开始执行分布式图书馆管理系统测试")
        print("=" * 50)
        
        # 等待服务器启动
        if not self.wait_for_server():
            print("❌ 服务器启动失败，无法执行测试")
            return {'success': False, 'error': '服务器启动失败'}
        
        print("\n📋 执行功能测试...")
        print("-" * 30)
        
        # 执行所有测试
        tests = [
            ("基本连接", self.test_basic_connectivity),
            ("数据库节点", self.test_database_nodes),
            ("SQL查询", self.test_sql_queries),
            ("图书搜索", self.test_book_search),
            ("GIS功能", self.test_gis_functionality),
            ("查询优化", self.test_query_optimization),
            ("节点监控", self.test_node_monitoring),
            ("图书管理", self.test_book_management),
            ("系统性能", self.test_performance)
        ]
        
        for test_name, test_func in tests:
            try:
                test_func()
            except Exception as e:
                self.log_test(f"{test_name}测试", False, f"异常: {str(e)}")
                traceback.print_exc()
        
        # 统计结果
        total_tests = len(self.test_results)
        passed_tests = sum(1 for result in self.test_results if result['success'])
        failed_tests = total_tests - passed_tests
        
        print("\n" + "=" * 50)
        print("📊 测试结果汇总")
        print("=" * 50)
        print(f"总测试数: {total_tests}")
        print(f"✅ 通过: {passed_tests}")
        print(f"❌ 失败: {failed_tests}")
        print(f"🎯 通过率: {(passed_tests/total_tests*100):.1f}%")
        
        if failed_tests > 0:
            print("\n❌ 失败的测试:")
            for result in self.test_results:
                if not result['success']:
                    print(f"   - {result['test']}: {result['details']}")
        
        print("\n🎉 测试完成!")
        
        return {
            'success': failed_tests == 0,
            'total': total_tests,
            'passed': passed_tests,
            'failed': failed_tests,
            'pass_rate': passed_tests/total_tests*100,
            'details': self.test_results
        }

def main():
    """主函数"""
    try:
        tester = SystemTester()
        results = tester.run_all_tests()
        
        # 退出码
        exit_code = 0 if results['success'] else 1
        sys.exit(exit_code)
        
    except KeyboardInterrupt:
        print("\n⚠️ 测试被用户中断")
        sys.exit(130)
    except Exception as e:
        print(f"\n💥 测试执行出错: {e}")
        traceback.print_exc()
        sys.exit(1)

if __name__ == "__main__":
    main()
