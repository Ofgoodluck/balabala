-- 西南交通大学图书馆数据库初始化脚本

-- 创建图书表
CREATE TABLE IF NOT EXISTS books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    category VARCHAR(50),
    publication_year INTEGER,
    publisher VARCHAR(100),
    library_id VARCHAR(10) DEFAULT 'node3',
    available_copies INTEGER DEFAULT 1,
    total_copies INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    student_id VARCHAR(20) UNIQUE,
    university VARCHAR(100) DEFAULT '西南交通大学',
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'active'
);

-- 创建图书馆表
CREATE TABLE IF NOT EXISTS libraries (
    id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    phone VARCHAR(20),
    email VARCHAR(100),
    opening_hours VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建借阅记录表
CREATE TABLE IF NOT EXISTS borrowing_records (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    book_id INTEGER REFERENCES books(id),
    library_id VARCHAR(10) REFERENCES libraries(id),
    borrow_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP,
    return_date TIMESTAMP,
    status VARCHAR(20) DEFAULT 'borrowed',
    fine_amount DECIMAL(10, 2) DEFAULT 0.00
);

-- 插入图书馆信息
INSERT INTO libraries (id, name, address, latitude, longitude, phone, opening_hours) VALUES 
('node3', '西南交通大学图书馆', '成都市金牛区二环路北一段111号', 30.7569, 103.9871, '028-87600114', '8:00-22:00')
ON CONFLICT (id) DO NOTHING;

-- 插入示例图书数据（偏向交通运输类）
INSERT INTO books (title, author, isbn, category, publication_year, publisher, available_copies, total_copies) VALUES 
('交通工程学', '任福田', '9787114045981', '交通运输', 2003, '人民交通出版社', 6, 6),
('铁道工程', '易思蓉', '9787113198428', '交通运输', 2015, '中国铁道出版社', 5, 5),
('桥梁工程', '范立础', '9787114045974', '土木工程', 2003, '人民交通出版社', 4, 4),
('隧道工程', '关宝树', '9787113198435', '土木工程', 2015, '中国铁道出版社', 3, 3),
('结构力学', '龙驭球', '9787040393460', '土木工程', 2013, '高等教育出版社', 8, 8),
('材料力学', '刘鸿文', '9787040393477', '工程学', 2013, '高等教育出版社', 7, 7),
('理论力学', '哈尔滨工业大学理论力学教研室', '9787040393484', '工程学', 2013, '高等教育出版社', 6, 6),
('土力学', '东南大学', '9787114045967', '土木工程', 2003, '人民交通出版社', 4, 4),
('工程测量', '张正禄', '9787114045950', '土木工程', 2003, '人民交通出版社', 5, 5),
('城市轨道交通', '徐瑞华', '9787113198442', '交通运输', 2015, '中国铁道出版社', 4, 4),
('高速铁路概论', '易思蓉', '9787113198459', '交通运输', 2015, '中国铁道出版社', 3, 3),
('交通规划', '王炜', '9787114045943', '交通运输', 2003, '人民交通出版社', 5, 5)
ON CONFLICT (isbn) DO NOTHING;

-- 插入示例用户数据
INSERT INTO users (name, email, student_id, phone) VALUES 
('刘一', 'liuyi@swjtu.edu.cn', '2021003001', '13800138021'),
('关二', 'guaner@swjtu.edu.cn', '2021003002', '13800138022'),
('张三', 'zhangsan@swjtu.edu.cn', '2021003003', '13800138023'),
('李四', 'lisi@swjtu.edu.cn', '2021003004', '13800138024'),
('王五', 'wangwu@swjtu.edu.cn', '2021003005', '13800138025'),
('赵六', 'zhaoliu@swjtu.edu.cn', '2021003006', '13800138026'),
('孙七', 'sunqi@swjtu.edu.cn', '2021003007', '13800138027')
ON CONFLICT (email) DO NOTHING;

-- 插入示例借阅记录
INSERT INTO borrowing_records (user_id, book_id, library_id, due_date, status) VALUES 
(1, 2, 'node3', CURRENT_TIMESTAMP + INTERVAL '30 days', 'borrowed'),
(2, 4, 'node3', CURRENT_TIMESTAMP + INTERVAL '30 days', 'borrowed'),
(3, 1, 'node3', CURRENT_TIMESTAMP + INTERVAL '30 days', 'borrowed'),
(4, 6, 'node3', CURRENT_TIMESTAMP + INTERVAL '30 days', 'borrowed'),
(5, 8, 'node3', CURRENT_TIMESTAMP + INTERVAL '30 days', 'borrowed')
ON CONFLICT DO NOTHING;

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_books_title ON books(title);
CREATE INDEX IF NOT EXISTS idx_books_author ON books(author);
CREATE INDEX IF NOT EXISTS idx_books_category ON books(category);
CREATE INDEX IF NOT EXISTS idx_books_isbn ON books(isbn);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_student_id ON users(student_id);
CREATE INDEX IF NOT EXISTS idx_borrowing_records_user_id ON borrowing_records(user_id);
CREATE INDEX IF NOT EXISTS idx_borrowing_records_book_id ON borrowing_records(book_id);
CREATE INDEX IF NOT EXISTS idx_borrowing_records_status ON borrowing_records(status);

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为books表创建更新时间触发器
CREATE TRIGGER update_books_updated_at BEFORE UPDATE ON books
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();


