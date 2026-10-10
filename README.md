# newbee-mall 商城系统（学习实践版）

一个基于 **Spring Boot 2.7.5 + MyBatis + MySQL** 的 B2C 电商系统，包含前台商城与后台管理两大模块。

> **说明**：本项目基于开源项目 [newbee-mall](https://github.com/newbee-ltd/newbee-mall) 学习开发。
> 在跑通主体功能的基础上，我重点梳理了**订单核心链路**，并做了若干自主优化（见下方「我做的部分」）。

---

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 后端框架 | Spring Boot 2.7.5 |
| ORM | MyBatis |
| 数据库 | MySQL 8.x（HikariCP 连接池） |
| 模板引擎 | Thymeleaf |
| 其他 | Hutool-captcha（验证码）、AdminLTE（后台 UI） |

## 功能模块

- **前台商城**：首页门户、商品分类、商品搜索、商品详情、购物车、订单结算、个人订单管理、会员中心
- **后台管理**：数据面板、轮播图管理、商品管理、订单管理、会员管理、分类管理

## 我做的部分

### 1. 订单核心链路（两阶段设计）

**验证阶段**——从数据库读取商品最新状态（不信任前端传值），校验商品是否下架、库存是否充足：

- 检查商品上下架状态（`goods_sell_status != 0` 则拒绝）
- 检查库存是否充足

**执行阶段**——全部在单个 `@Transactional` 事务内完成，任一步失败全部回滚：

1. 删除购物车中已选商品
2. 扣减库存（乐观锁）
3. 生成订单号、计算总价
4. 写入订单主表
5. 写入订单项快照

**订单项快照**：下单时把商品名称、价格、数量冗余存入订单项表，使商家后续修改商品信息不影响历史订单。

**乐观锁防超卖**：

```sql
UPDATE tb_newbee_mall_goods_info
SET stock_num = stock_num - #{goodsCount}
WHERE goods_id = #{goodsId}
  AND stock_num >= #{goodsCount}      -- 并发下只有一个能更新成功
  AND goods_sell_status = 0;
```

影响行数为 0 时抛出异常，触发事务回滚。

### 2. SQL 索引优化

`selectByOrderNo` 查询未命中索引，`EXPLAIN` 显示 `type=ALL`（全表扫描）。
为 `order_no` 建立唯一索引后，`type` 降为 `const`，扫描行数由全表降至 1。

```sql
ALTER TABLE tb_newbee_mall_order ADD UNIQUE INDEX idx_order_no (order_no);
```

### 3. 秒杀方案设计（方案设计，未全量落地）

针对高并发秒杀场景设计的方案：**Redis 预减库存 + 消息队列异步下单**

1. 商品库存预热到 Redis
2. 请求先经 `DECR` 原子扣减 Redis 库存
3. 扣减成功 → 写消息队列，异步落库；扣减失败 → 回补并返回售罄
4. 降级方案：Redis 不可用时熔断到数据库乐观锁兜底，防止超卖

## 本地运行

1. 创建数据库 `newbee_mall_db`，导入 `src/main/resources/newbee_mall_schema.sql`
2. 复制 `src/main/resources/application-local.properties.example` 为
   `application-local.properties`，填入你的数据库账号密码
   （该文件已在 `.gitignore` 中，不会被提交）
3. 启动：`mvn spring-boot:run`
4. 访问前台：<http://localhost:28089> ｜ 后台：<http://localhost:28089/admin/login>

> 后台管理账号见 `newbee_mall_schema.sql` 中 `tb_newbee_mall_admin_user` 表。

## 页面展示

> 自行运行项目并截图补充。

## 声明

本项目为个人学习实践，代码主体来自开源项目
[newbee-mall](https://github.com/newbee-ltd/newbee-mall)，遵循其 GPL-3.0 协议。
