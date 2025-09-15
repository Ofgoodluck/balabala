# 分布式图书馆系统数据模型

import sqlite3
import json
from datetime import datetime
from typing import List, Dict, Any, Optional

class DatabaseNode:
    """数据库节点类，代表一个分布式站点"""
    
    def __init__(self, node_id: str, config: Dict[str, Any]):
        self.node_id = node_id
        self.config = config
        self.name = config['name']
        self.location = config['location']
        self.database_path = config['database']
        self.commands_log = []  # 记录接收和发送的命令
        self.init_database()
    
    def init_database(self):
        """初始化数据库表结构"""
        conn = sqlite3.connect(self.database_path)
        cursor = conn.cursor()
        
        # 创建图书馆表
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS libraries (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                address TEXT NOT NULL,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                contact_phone TEXT,
                opening_hours TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        ''')
        
        # 创建图书表
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS books (
                id INTEGER PRIMARY KEY,
                isbn TEXT UNIQUE NOT NULL,
                title TEXT NOT NULL,
                author TEXT NOT NULL,
                publisher TEXT,
                publish_year INTEGER,
                category TEXT,
                library_id INTEGER,
                total_copies INTEGER DEFAULT 1,
                available_copies INTEGER DEFAULT 1,
                location_in_library TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (library_id) REFERENCES libraries (id)
            )
        ''')
        
        # 创建用户表
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY,
                student_id TEXT UNIQUE NOT NULL,
                name TEXT NOT NULL,
                email TEXT,
                phone TEXT,
                school TEXT NOT NULL,
                major TEXT,
                grade INTEGER,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        ''')
        
        # 创建借阅记录表
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS borrowings (
                id INTEGER PRIMARY KEY,
                user_id INTEGER NOT NULL,
                book_id INTEGER NOT NULL,
                library_id INTEGER NOT NULL,
                borrow_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                due_date TIMESTAMP NOT NULL,
                return_date TIMESTAMP,
                status TEXT DEFAULT 'borrowed',
                fine_amount REAL DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users (id),
                FOREIGN KEY (book_id) REFERENCES books (id),
                FOREIGN KEY (library_id) REFERENCES libraries (id)
            )
        ''')
        
        conn.commit()
        conn.close()
        
        # 初始化示例数据
        self.init_sample_data()
    
    def init_sample_data(self):
        """初始化示例数据"""
        conn = sqlite3.connect(self.database_path)
        cursor = conn.cursor()
        
        # 检查是否已有数据
        cursor.execute("SELECT COUNT(*) FROM libraries")
        if cursor.fetchone()[0] == 0:
            # 插入当前图书馆信息
            cursor.execute('''
                INSERT INTO libraries (name, address, latitude, longitude, contact_phone, opening_hours)
                VALUES (?, ?, ?, ?, ?, ?)
            ''', (
                self.name,
                self.config['address'],
                self.location['lat'],
                self.location['lng'],
                '028-85405110',
                '08:00-22:00'
            ))
            
            library_id = cursor.lastrowid
            
            # 插入示例图书数据
            sample_books = [
                ('9787111544937', '计算机网络', '谢希仁', '电子工业出版社', 2017, '计算机科学', 5, 5),
                ('9787302257646', '数据结构', '严蔚敏', '清华大学出版社', 2011, '计算机科学', 3, 3),
                ('9787115428028', '算法导论', 'Thomas H.Cormen', '人民邮电出版社', 2012, '计算机科学', 2, 1),
                ('9787111213826', '数据库系统概念', 'Abraham Silberschatz', '机械工业出版社', 2006, '数据库', 4, 4),
                ('9787302447641', '分布式系统原理与范型', 'Andrew S.Tanenbaum', '清华大学出版社', 2016, '分布式系统', 2, 2)
            ]
            
            for book in sample_books:
                cursor.execute('''
                    INSERT INTO books (isbn, title, author, publisher, publish_year, category, 
                                     library_id, total_copies, available_copies, location_in_library)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ''', book + (library_id, f"A{book[6]:03d}"))
                
        conn.commit()
        conn.close()
    
    def log_command(self, command_type: str, command: str, status: str = "received"):
        """记录命令日志"""
        log_entry = {
            'timestamp': datetime.now().isoformat(),
            'type': command_type,
            'command': command,
            'status': status,
            'node': self.node_id
        }
        self.commands_log.append(log_entry)
        # 只保留最近100条记录
        if len(self.commands_log) > 100:
            self.commands_log = self.commands_log[-100:]
    
    def execute_query(self, query: str, params: tuple = None) -> List[Dict]:
        """执行查询并返回结果"""
        self.log_command("SQL", query, "executing")
        
        try:
            conn = sqlite3.connect(self.database_path)
            conn.row_factory = sqlite3.Row  # 返回字典格式结果
            cursor = conn.cursor()
            
            if params:
                cursor.execute(query, params)
            else:
                cursor.execute(query)
                
            if query.strip().upper().startswith('SELECT'):
                results = [dict(row) for row in cursor.fetchall()]
                self.log_command("SQL", f"查询返回 {len(results)} 条记录", "completed")
                return results
            else:
                conn.commit()
                affected_rows = cursor.rowcount
                self.log_command("SQL", f"影响 {affected_rows} 行", "completed")
                return [{'affected_rows': affected_rows}]
                
        except Exception as e:
            self.log_command("SQL", f"错误: {str(e)}", "failed")
            raise e
        finally:
            conn.close()

class Library:
    """图书馆实体类"""
    def __init__(self, id: int, name: str, address: str, latitude: float, longitude: float):
        self.id = id
        self.name = name
        self.address = address
        self.latitude = latitude
        self.longitude = longitude

class Book:
    """图书实体类"""
    def __init__(self, id: int, isbn: str, title: str, author: str, library_id: int):
        self.id = id
        self.isbn = isbn
        self.title = title
        self.author = author
        self.library_id = library_id

class User:
    """用户实体类"""
    def __init__(self, id: int, student_id: str, name: str, school: str):
        self.id = id
        self.student_id = student_id
        self.name = name
        self.school = school

class Borrowing:
    """借阅记录实体类"""
    def __init__(self, id: int, user_id: int, book_id: int, library_id: int, status: str):
        self.id = id
        self.user_id = user_id
        self.book_id = book_id
        self.library_id = library_id
        self.status = status



