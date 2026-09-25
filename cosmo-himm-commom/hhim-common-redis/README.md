# Redis包使用说明

## 功能一：Redis多DB切换使用方式（责任人：张耀晖）
### 第一步：引入common-redis包

### 第二步：启动类上加注解@EnableRedisMultiDB

### 第三步：配置文件添加配置
在spring.redis配置下添加：
```yml
multidb:
    redisTemplatePrefix: im-redisTemplate-
    databases: [0,1,2,3,4,5,6]
    redisClientType: jedis
```

### 第四步：代码中切换数据源

```java
// 注入Bean
@Autowired
private RedisMultiDBTemplateManager templateManager;


// 获取对应数据源的redisTemplate
RedisTemplate<Object, Object> redisTemplate = templateManager.getRedisTemplate(5);
```
