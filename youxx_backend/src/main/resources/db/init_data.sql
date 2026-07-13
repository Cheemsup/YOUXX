-- ============================================
-- YOUXX 初始化数据脚本
-- 包含: 商品分类 + 商品数据
-- 生成时间: 2026-05-21
-- ============================================

USE `youxx`;

-- ============================================
-- 1. 商品分类数据
-- ============================================
INSERT INTO `product_category` (`id`, `name`, `icon`, `sort_order`) VALUES
('drinks', '饮料', 'Coffee', 1),
('snacks', '零食', 'Sugar', 2),
('daily', '日用品', 'Goods', 3),
('fresh', '生鲜', 'Apple', 4),
('dairy', '乳制品', 'MilkTea', 5),
('instant', '速食', 'Food', 6);

-- ============================================
-- 2. 商品数据
-- ============================================
INSERT INTO `product` (`id`, `name`, `category_id`, `price`, `unit`, `stock`, `image_url`, `description`, `bar_code`, `discount`, `is_hot`, `tags`, `status`) VALUES
('P001', '农夫山泉', 'drinks', 2.00, '瓶', 500, 'assets/products/drinks/water.png', '550ml 饮用天然水', '6920552655001', 1.00, 1, '热销,解渴', 'ONSHELF'),
('P002', '可口可乐', 'drinks', 3.00, '瓶', 300, 'assets/products/drinks/cola.png', '330ml 可乐', '6920552655002', 0.95, 1, '热销,碳酸', 'ONSHELF'),
('P003', '康师傅冰红茶', 'drinks', 3.50, '瓶', 200, 'assets/products/drinks/iceTea.png', '500ml 冰红茶', '6920552655003', 1.00, 0, '茶饮', 'ONSHELF'),
('P004', '统一绿茶', 'drinks', 3.00, '瓶', 180, 'assets/products/drinks/greenTea.png', '500ml 绿茶', '6920552655004', 1.00, 0, '茶饮,健康', 'ONSHELF'),
('P005', '乐事薯片', 'snacks', 6.50, '袋', 150, 'assets/products/snacks/chips.png', '组合装 120g', '6920552655005', 1.00, 1, '热销,膨化', 'ONSHELF'),
('P006', '奥利奥饼干', 'snacks', 7.80, '盒', 120, 'assets/products/snacks/orio.png', '巧克力味夹心饼干 388g', '6920552655006', 0.90, 0, '饼干,甜点', 'ONSHELF'),
('P007', '卫龙辣条', 'snacks', 2.50, '袋', 200, 'assets/products/snacks/lajiao.png', '大面筋 65g', '6920552655007', 1.00, 1, '辣味,童年', 'ONSHELF'),
('P008', '手抽纸巾', 'daily', 9.90, '提', 100, 'assets/products/dairy/tissue.png', '抽纸 3 层 120 抽*3 包', '6920552655008', 1.00, 0, '生活用纸', 'ONSHELF'),
('P009', '雕牌洗洁精', 'daily', 12.80, '瓶', 80, 'assets/products/dairy/detergent.png', '1.5kg 柠檬香型', '6920552655009', 0.85, 0, '清洁,厨房', 'ONSHELF'),
('P010', '六神花露水', 'daily', 15.00, '瓶', 60, 'assets/products/dairy/deet.png', '195ml 驱蚊型', '6920552655010', 1.00, 0, '驱蚊,夏季', 'ONSHELF'),
('P011', '新鲜鸡蛋', 'fresh', 15.00, '盒', 50, 'assets/products/fresh/eggs.png', '土鸡蛋 12 枚', '6920552655011', 1.00, 1, '生鲜,营养', 'ONSHELF'),
('P012', '新鲜西红柿', 'fresh', 4.50, '斤', 40, 'assets/products/fresh/tomato.png', '500g 约 2-3 个', '6920552655012', 1.00, 0, '蔬菜,新鲜', 'ONSHELF'),
('P013', '伊利纯牛奶', 'dairy', 12.00, '箱', 70, 'assets/products/dairy/milk.png', '250ml*12 盒', '6920552655013', 0.95, 1, '牛奶,补钙', 'ONSHELF'),
('P014', '蒙牛酸奶', 'dairy', 15.80, '箱', 45, 'assets/products/dairy/yogurt.png', '100g*12 杯', '6920552655014', 1.00, 0, '酸奶,益生菌', 'ONSHELF'),
('P015', '康师傅红烧牛肉面', 'instant', 4.50, '袋', 200, 'assets/products/instant/noodles.png', '方便面 103g', '6920552655015', 1.00, 1, '速食,泡面', 'ONSHELF'),
('P016', '统一老坛酸菜面', 'instant', 4.50, '袋', 180, 'assets/products/instant/suancai.png', '方便面 105g', '6920552655016', 0.90, 0, '速食,泡面,酸菜', 'ONSHELF'),
('P017', '桂格燕麦片', 'instant', 22.00, '袋', 60, 'assets/products/instant/oat.png', '即食燕麦片 1kg', '6920552655017', 1.00, 0, '早餐,健康', 'ONSHELF'),
('P018', '农夫果园', 'drinks', 4.20, '瓶', 100, 'assets/products/drinks/juice.png', '100% 果汁 450ml', '6920552655018', 1.00, 0, '果汁,健康', 'ONSHELF');
