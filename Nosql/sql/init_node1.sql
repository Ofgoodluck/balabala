-- 四川大学图书馆数据库初始化脚本

-- 创建图书表
CREATE TABLE IF NOT EXISTS books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    category VARCHAR(50),
    publication_year INTEGER,
    publisher VARCHAR(100),
    library_id VARCHAR(10) DEFAULT 'node1',
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
    university VARCHAR(100) DEFAULT '四川大学',
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
('node1', '四川大学图书馆', '成都市武侯区一环路南一段24号', 30.6336, 104.0834, '028-85405110', '8:00-22:00')
ON CONFLICT (id) DO NOTHING;

-- 插入示例图书数据
INSERT INTO books (title, author, isbn, category, publication_year, publisher, available_copies, total_copies) VALUES 
('数据结构与算法分析', '马克·艾伦·维斯', '9787111407010', '计算机科学', 2013, '机械工业出版社', 5, 5),
('计算机网络', '谢希仁', '9787121302954', '计算机科学', 2017, '电子工业出版社', 3, 3),
('操作系统概念', 'Abraham Silberschatz', '9787111544043', '计算机科学', 2016, '机械工业出版社', 4, 4),
('数据库系统概念', 'Abraham Silberschatz', '9787111375296', '计算机科学', 2012, '机械工业出版社', 6, 6),
('软件工程', 'Ian Sommerville', '9787111551904', '计算机科学', 2017, '机械工业出版社', 2, 2),
('高等数学', '同济大学数学系', '9787040396638', '数学', 2014, '高等教育出版社', 8, 8),
('线性代数', '同济大学数学系', '9787040396911', '数学', 2014, '高等教育出版社', 6, 6),
('概率论与数理统计', '盛骤', '9787040238969', '数学', 2008, '高等教育出版社', 4, 4),
('大学物理', '张三慧', '9787302392859', '物理学', 2015, '清华大学出版社', 5, 5),
('工程力学', '范钦珊', '9787040264623', '工程学', 2009, '高等教育出版社', 3, 3)
ON CONFLICT (isbn) DO NOTHING;

-- 插入示例用户数据
INSERT INTO users (name, email, student_id, phone) VALUES 
('张三', 'zhangsan@scu.edu.cn', '2021001001', '13800138001'),
('李四', 'lisi@scu.edu.cn', '2021001002', '13800138002'),
('王五', 'wangwu@scu.edu.cn', '2021001003', '13800138003'),
('赵六', 'zhaoliu@scu.edu.cn', '2021001004', '13800138004'),
('钱七', 'qianqi@scu.edu.cn', '2021001005', '13800138005')
ON CONFLICT (email) DO NOTHING;

-- 插入示例借阅记录
INSERT INTO borrowing_records (user_id, book_id, library_id, due_date, status) VALUES 
(1, 1, 'node1', CURRENT_TIMESTAMP + INTERVAL '30 days', 'borrowed'),
(2, 3, 'node1', CURRENT_TIMESTAMP + INTERVAL '30 days', 'borrowed'),
(3, 5, 'node1', CURRENT_TIMESTAMP + INTERVAL '30 days', 'borrowed')
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


