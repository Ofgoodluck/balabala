#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
快速功能验证测试
"""

import requests
import json

def test_basic_functions():
    base_url = "http://localhost:5000"
    
    print("🔍 快速功能验证测试")
    print("=" * 40)
    
    try:
        # 1. 测试主页
        print("1. 测试主页访问...")
        response = requests.get(base_url)
        print(f"   状态: {'✅' if response.status_code == 200 else '❌'} (HTTP {response.status_code})")
        
        # 2. 测试仪表板
        print("2. 测试仪表板...")
        response = requests.get(f"{base_url}/dashboard")
        print(f"   状态: {'✅' if response.status_code == 200 else '❌'} (HTTP {response.status_code})")
        
        # 3. 测试统计API
        print("3. 测试统计信息...")
        response = requests.get(f"{base_url}/api/statistics")
        if response.status_code == 200:
            data = response.json()
            print(f"   状态: ✅ 获取到统计数据")
            print(f"   节点数: {len(data.get('nodes', {}))}")
            print(f"   性能数据: {'有' if data.get('performance') else '无'}")
        else:
            print(f"   状态: ❌ (HTTP {response.status_code})")
        
        # 4. 测试简单SQL查询
        print("4. 测试简单查询...")
        response = requests.post(
            f"{base_url}/query",
            data={'sql': 'SELECT COUNT(*) as total FROM books'},
            headers={'Content-Type': 'application/x-www-form-urlencoded'}
        )
        if response.status_code == 200:
            result = response.json()
            print(f"   状态: ✅ 查询成功")
            print(f"   结果: {result}")
        else:
            print(f"   状态: ❌ (HTTP {response.status_code})")
        
        # 5. 测试图书馆信息查询
        print("5. 测试图书馆查询...")
        response = requests.post(
            f"{base_url}/query",
            data={'sql': 'SELECT * FROM libraries'},
            headers={'Content-Type': 'application/x-www-form-urlencoded'}
        )
        if response.status_code == 200:
            result = response.json()
            print(f"   状态: ✅ 查询成功")
            if 'results' in result:
                print(f"   图书馆数量: {len(result['results'])}")
                for lib in result['results'][:3]:  # 显示前3个
                    print(f"   - {lib}")
        else:
            print(f"   状态: ❌ (HTTP {response.status_code})")
            
        # 6. 测试GIS页面
        print("6. 测试GIS页面...")
        response = requests.get(f"{base_url}/gis")
        print(f"   状态: {'✅' if response.status_code == 200 else '❌'} (HTTP {response.status_code})")
        
        print("\n✨ 快速测试完成!")
        
    except Exception as e:
        print(f"❌ 测试出错: {e}")

if __name__ == "__main__":
    test_basic_functions()
