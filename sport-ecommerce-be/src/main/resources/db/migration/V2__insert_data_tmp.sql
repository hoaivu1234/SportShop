INSERT INTO roles(name) VALUES
                            ('ROLE_ADMIN'),
                            ('ROLE_CUSTOMER'),
                            ('ROLE_GUEST');

-- password: user12345
INSERT INTO users(email,password,first_name,last_name,phone,status) VALUES
                                                                        ('admin@sport.com','$10$caoe7sWDSU8J8CwqlJCQDO7.O0mFRK93osEXdrzfwEk14TaEoLVuS','System','Admin','0900000001','ACTIVE'),
                                                                        ('john@gmail.com','$10$caoe7sWDSU8J8CwqlJCQDO7.O0mFRK93osEXdrzfwEk14TaEoLVuS','John','Nguyen','0900000002','ACTIVE'),
                                                                        ('anna@gmail.com','$10$caoe7sWDSU8J8CwqlJCQDO7.O0mFRK93osEXdrzfwEk14TaEoLVuS','Anna','Tran','0900000003','ACTIVE'),
                                                                        ('david@gmail.com','$10$caoe7sWDSU8J8CwqlJCQDO7.O0mFRK93osEXdrzfwEk14TaEoLVuS','David','Le','0900000004','ACTIVE'),
                                                                        ('mike@gmail.com','$10$caoe7sWDSU8J8CwqlJCQDO7.O0mFRK93osEXdrzfwEk14TaEoLVuS','Mike','Pham','0900000005','ACTIVE'),
                                                                        ('staff@sport.com','$10$caoe7sWDSU8J8CwqlJCQDO7.O0mFRK93osEXdrzfwEk14TaEoLVuS','Staff','One','0900000006','ACTIVE'),
                                                                        ('user1@gmail.com','$10$caoe7sWDSU8J8CwqlJCQDO7.O0mFRK93osEXdrzfwEk14TaEoLVuS','User','One','0900000007','ACTIVE'),
                                                                        ('user2@gmail.com','$10$caoe7sWDSU8J8CwqlJCQDO7.O0mFRK93osEXdrzfwEk14TaEoLVuS','User','Two','0900000008','ACTIVE');

INSERT INTO user_roles VALUES
                           (1,1),
                           (2,2),
                           (3,2),
                           (4,2),
                           (5,2),
                           (6,3),
                           (7,2),
                           (8,2);

INSERT INTO addresses(user_id,full_name,phone,province,district,ward,address_line,is_default) VALUES
                                                                                                  (2,'John Nguyen','0900000002','Hanoi','Cau Giay','Dich Vong','12 Nguyen Phong',true),
                                                                                                  (3,'Anna Tran','0900000003','HCM','District 1','Ben Nghe','45 Le Loi',true),
                                                                                                  (4,'David Le','0900000004','Danang','Hai Chau','Thach Thang','99 Tran Phu',true),
                                                                                                  (5,'Mike Pham','0900000005','Hanoi','Dong Da','Lang Thuong','20 Chua Lang',true),
                                                                                                  (7,'User One','0900000007','Hue','Huong Tra','Ward1','12 ABC',true);

INSERT INTO categories(name,slug,parent_id) VALUES
                                                ('Shoes','shoes',NULL),
                                                ('Clothing','clothing',NULL),
                                                ('Accessories','accessories',NULL),
                                                ('Running Shoes','running-shoes',1),
                                                ('Football Shoes','football-shoes',1),
                                                ('T-Shirts','t-shirts',2),
                                                ('Shorts','shorts',2);

INSERT INTO products(name,slug,description,brand,price,discount_price,category_id,status) VALUES
                                                                                              ('Nike Air Zoom','nike-air-zoom','Running shoes','Nike',2500000,2200000,4,'ACTIVE'),
                                                                                              ('Adidas Predator','adidas-predator','Football shoes','Adidas',3000000,2800000,5,'ACTIVE'),
                                                                                              ('Puma Running Tee','puma-tee','Sport t-shirt','Puma',500000,450000,6,'ACTIVE'),
                                                                                              ('Nike Shorts','nike-shorts','Training shorts','Nike',600000,550000,7,'ACTIVE'),
                                                                                              ('Adidas Cap','adidas-cap','Sport cap','Adidas',300000,250000,3,'ACTIVE'),
                                                                                              ('Yonex Bag','yonex-bag','Sport bag','Yonex',1200000,1100000,3,'ACTIVE');

INSERT INTO product_images(product_id,image_url,is_main,sort_order) VALUES
                                                                        (1,'img1.jpg',true,1),
                                                                        (1,'img2.jpg',false,2),
                                                                        (2,'img3.jpg',true,1),
                                                                        (3,'img4.jpg',true,1),
                                                                        (4,'img5.jpg',true,1),
                                                                        (5,'img6.jpg',true,1),
                                                                        (6,'img7.jpg',true,1);

INSERT INTO product_variants(product_id,sku,size,color,price,stock) VALUES
                                                                        (1,'SKU1','40','Red',2200000,50),
                                                                        (1,'SKU2','41','Black',2200000,30),
                                                                        (2,'SKU3','42','Blue',2800000,40),
                                                                        (3,'SKU4','M','White',450000,100),
                                                                        (4,'SKU5','L','Black',550000,70),
                                                                        (5,'SKU6','Free','Black',250000,80),
                                                                        (6,'SKU7','Free','Red',1100000,20);

INSERT INTO inventory_transactions(product_variant_id,quantity,type) VALUES
                                                                         (1,100,'IMPORT'),
                                                                         (2,50,'IMPORT'),
                                                                         (3,70,'IMPORT'),
                                                                         (4,150,'IMPORT'),
                                                                         (5,120,'IMPORT'),
                                                                         (6,90,'IMPORT'),
                                                                         (7,40,'IMPORT');

INSERT INTO carts(user_id) VALUES
                               (2),(3),(4),(5),(7);

INSERT INTO cart_items(cart_id,product_variant_id,quantity) VALUES
                                                                (1,1,2),
                                                                (1,3,1),
                                                                (2,4,3),
                                                                (3,2,1),
                                                                (4,5,2);

INSERT INTO orders(user_id,order_number,total_price,status,payment_status,shipping_address) VALUES
                                                                                                (2,'ORD001',4400000,'CONFIRMED','PAID','Hanoi'),
                                                                                                (3,'ORD002',2800000,'SHIPPING','PAID','HCM'),
                                                                                                (4,'ORD003',450000,'DELIVERED','PAID','Danang'),
                                                                                                (5,'ORD004',1100000,'PENDING','UNPAID','Hanoi'),
                                                                                                (7,'ORD005',550000,'CANCELLED','REFUND','Hue');

INSERT INTO order_items(order_id,product_variant_id,product_name,price,quantity,subtotal) VALUES
                                                                                              (1,1,'Nike Air Zoom',2200000,2,4400000),
                                                                                              (2,3,'Adidas Predator',2800000,1,2800000),
                                                                                              (3,4,'Puma Running Tee',450000,1,450000),
                                                                                              (4,7,'Yonex Bag',1100000,1,1100000),
                                                                                              (5,5,'Nike Shorts',550000,1,550000);

INSERT INTO payments(order_id,payment_method,amount,status,transaction_id) VALUES
                                                                               (1,'COD',4400000,'SUCCESS','TXN001'),
                                                                               (2,'BANK',2800000,'SUCCESS','TXN002'),
                                                                               (3,'COD',450000,'SUCCESS','TXN003'),
                                                                               (4,'BANK',1100000,'PENDING','TXN004'),
                                                                               (5,'COD',550000,'REFUND','TXN005');

INSERT INTO reviews(product_id,user_id,rating,comment) VALUES
                                                           (1,2,5,'Very good'),
                                                           (2,3,4,'Nice shoes'),
                                                           (3,4,5,'Comfortable'),
                                                           (4,5,3,'Ok'),
                                                           (5,7,4,'Good quality');

INSERT INTO wishlists(user_id,product_id) VALUES
                                              (2,1),
                                              (3,2),
                                              (4,3),
                                              (5,4),
                                              (7,5);

INSERT INTO coupons(code,discount_type,discount_value,min_order_value,start_date,end_date,status) VALUES
                                                                                                      ('SALE10','PERCENT',10,1000000,'2024-01-01','2025-01-01','ACTIVE'),
                                                                                                      ('SALE20','PERCENT',20,2000000,'2024-01-01','2025-01-01','ACTIVE'),
                                                                                                      ('SHIPFREE','FIXED',30000,500000,'2024-01-01','2025-01-01','ACTIVE'),
                                                                                                      ('WELCOME','FIXED',50000,700000,'2024-01-01','2025-01-01','ACTIVE'),
                                                                                                      ('VIP','PERCENT',30,3000000,'2024-01-01','2025-01-01','ACTIVE');

INSERT INTO product_views(product_id,user_id) VALUES
                                                  (1,2),
                                                  (2,2),
                                                  (3,3),
                                                  (4,4),
                                                  (5,5),
                                                  (6,7);