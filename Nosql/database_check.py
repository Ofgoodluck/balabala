#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
数据库内容检查工具
"""

import sqlite3
import os

def check_database_content():
    print("🔍 检查数据库内容")
    print("=" * 40)
    
    db_files = [
        'scu_library.db',
        'swjtu_library.db', 
        'uestc_library.db'
    ]
    
    for db_file in db_files:
        if os.path.exists(db_file):
            print(f"\n📊 数据库: {db_file}")
            print("-" * 30)
            
            try:
                conn = sqlite3.connect(db_file)
                cursor = conn.cursor()
                
                # 获取所有表名
                cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
                tables = cursor.fetchall()
                
                print(f"表数量: {len(tables)}")
                
                for table in tables:
                    table_name = table[0]
                    print(f"\n📋 表: {table_name}")
                    
                    # 获取表结构
                    cursor.execute(f"PRAGMA table_info({table_name});")
                    columns = cursor.fetchall()
                    print(f"   列数: {len(columns)}")
                    for col in columns:
                        print(f"   - {col[1]} ({col[2]})")
                    
                    # 获取记录数
                    cursor.execute(f"SELECT COUNT(*) FROM {table_name};")
                    count = cursor.fetchone()[0]
                    print(f"   记录数: {count}")
                    
                    # 显示前几条记录
                    if count > 0:
                        cursor.execute(f"SELECT * FROM {table_name} LIMIT 3;")
                        records = cursor.fetchall()
                        print(f"   示例记录:")
                        for i, record in enumerate(records, 1):
                            print(f"     {i}. {record}")
                
                conn.close()
                
            except Exception as e:
                print(f"   ❌ 错误: {e}")
        else:
            print(f"❌ 数据库文件不存在: {db_file}")

    print("\n✨ 数据库检查完成!")

if __name__ == "__main__":
    check_database_content()
