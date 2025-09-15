-- 电子科技大学图书馆数据库初始化脚本

-- 创建图书表
CREATE TABLE IF NOT EXISTS books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    category VARCHAR(50),
    publication_year INTEGER,
    publisher VARCHAR(100),
    library_id VARCHAR(10) DEFAULT 'node2',
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
    university VARCHAR(100) DEFAULT '电子科技大学',
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
('node2', '电子科技大学图书馆', '成都市成华区建设北路二段四号', 30.7608, 103.9348, '028-83201999', '8:00-22:30')
ON CONFLICT (id) DO NOTHING;

-- 插入示例图书数据（偏向电子信息类）
INSERT INTO books (title, author, isbn, category, publication_year, publisher, available_copies, total_copies) VALUES 
('信号与系统', '奥本海姆', '9787121058400', '电子信息', 2013, '电子工业出版社', 7, 7),
('数字信号处理', '程佩青', '9787302151463', '电子信息', 2007, '清华大学出版社', 5, 5),
('通信原理', '樊昌信', '9787121302961', '通信工程', 2016, '电子工业出版社', 6, 6),
('电路分析基础', '李瀚荪', '9787040393453', '电子信息', 2013, '高等教育出版社', 4, 4),
('模拟电子技术基础', '童诗白', '9787040393446', '电子信息', 2013, '高等教育出版社', 8, 8),
('数字电子技术基础', '阎石', '9787040393439', '电子信息', 2013, '高等教育出版社', 6, 6),
('微机原理与接口技术', '冯博琴', '9787121302978', '计算机科学', 2016, '电子工业出版社', 4, 4),
('自动控制原理', '胡寿松', '9787030270009', '自动化', 2010, '科学出版社', 5, 5),
('电磁场与电磁波', '谢处方', '9787040393422', '电子信息', 2013, '高等教育出版社', 3, 3),
('MATLAB程序设计', '刘卫国', '9787302151456', '计算机科学', 2007, '清华大学出版社', 4, 4),
('嵌入式系统设计', '李驹光', '9787121302985', '计算机科学', 2016, '电子工业出版社', 3, 3),
('人工智能导论', '廉师友', '9787121302992', '计算机科学', 2016, '电子工业出版社', 5, 5)
ON CONFLICT (isbn) DO NOTHING;

-- 插入示例用户数据
INSERT INTO users (name, email, student_id, phone) VALUES 
('陈一', 'chenyi@uestc.edu.cn', '2021002001', '13800138011'),
('周二', 'zhouer@uestc.edu.cn', '2021002002', '13800138012'),
('吴三', 'wusan@uestc.edu.cn', '2021002003', '13800138013'),
('郑四', 'zhengsi@uestc.edu.cn', '2021002004', '13800138014'),
('孙五', 'sunwu@uestc.edu.cn', '2021002005', '13800138015'),
('朱六', 'zhuliu@uestc.edu.cn', '2021002006', '13800138016')
ON CONFLICT (email) DO NOTHING;

-- 插入示例借阅记录
INSERT INTO borrowing_records (user_id, book_id, library_id, due_date, status) VALUES 
(1, 1, 'node2', CURRENT_TIMESTAMP + INTERVAL '30 days', 'borrowed'),
(2, 2, 'node2', CURRENT_TIMESTAMP + INTERVAL '30 days', 'borrowed'),
(3, 4, 'node2', CURRENT_TIMESTAMP + INTERVAL '30 days', 'borrowed'),
(4, 6, 'node2', CURRENT_TIMESTAMP + INTERVAL '30 days', 'borrowed')
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


