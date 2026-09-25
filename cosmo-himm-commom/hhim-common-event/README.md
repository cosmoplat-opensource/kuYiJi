责任人：张耀晖

## 1.前言

为了方便业务开发可以快速方便的集成第三方转发服务，并且做到业务代码与集成转发服务相关SDK代码的解耦，故采用观察者模式实现。



## 2.事件发布订阅整体模型

前提：依赖Spring的事件发布订阅模型实现。

![image-20220731174901947](https://raw.githubusercontent.com/xiaohuihui123456/picture/master/typora/image-20220731174901947.png)



## 3.Common包事件订阅封装实现



### 3.1 事件封装（`ThirdInterfaceMQEvent`）

继承Spring的`ApplicationEvent`的抽象类。



目的：

1. 方便下面采用Spring AOP对入参事件进行针对性拦截
2. 不需要业务开发去组装统一的数据源和追踪码等基础数据信息，可以自动从业务请求的`ThreadLocal`中获取相关数据源和追踪码等信息。



### 3.2 定义事件监听器的响应结果类型（`ThirdInterfaceMQResult`）

目的：定义需要业务返回的内容，根据这些内容组装接下来发送MQ的内容信息。

```java
@Data
public class ThirdInterfaceMQResult<T> {

    // 方法
    private String method;

    // 方法版本
    private String version;

    // MQ Body内容
    private T bizMqContent;

    // 有序发送遵循字段
    private String orderByKey;

}
```



属性字段解释：

| 字段         | 类型   | 必填 | 说明                                                         |
| ------------ | ------ | ---- | ------------------------------------------------------------ |
| method       | String | 是   | 转发给第三方服务的方法名。跟第三方服务约定的方法名，这样第三方服务才知道该请求的业务定义 |
| version      | String | 是   | 转发给第三方服务的方法的版本号。                             |
| bizMqContent | T      | 是   | MQ Body内容。业务发送方可以自定义此泛型类型，最终发送的MQ body的内容就是此类型对象的json格式 |
| orderByKey   | String | 否   | 有序发送所遵循业务字段。                                     |





### 3.3 监听器拦截处理

通过Spring AOP方式拦截spring的Listener，进行统一发送MQ消息内容到第三方转发服务。

拦截条件：

1. 入参为`ThirdInterfaceMQEvent`类型或子类型
2. 返回结果为`ThirdInterfaceMQResult`类型
3. 标记有`@EventListener`注解的方法进行拦截处理。



## 4.业务方对接方式



业务方开发只需要定义两个东西：

1. 事件
2. 事件监听器

之后，在需要转发数据到第三方服务时，组装事件并发送就OK啦。



下面是一个Demo。



### 4.1 事件定义

所定义的事件类需要继承Common-event包的`ThirdInterfaceMQEvent`抽象类。

```java
public class MyEvent extends ThirdInterfaceMQEvent {

    public MyEventC(Object source) {
        super(source);
    }

    private String orderNum;

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }
}
```



### 4.2 事件监听器定义

1. 在方法上标注`@EventListener`注解，标识此方法为事件监听器。
2. 方法的入参类型为上面所定义的事件类型，标识该事件监听器所关注的事件类型。
3. 方法的返回类型必须为`ThirdInterfaceMQResult<>`，泛型类为业务需要发送为第三方转发服务的类型。
   4.多个监听器可以监听同一个事件
```java
@Slf4j
@Component
public class MyListeners {

    @EventListener
    public ThirdInterfaceMQResult<MyMqBody> myListener(MyEvent event){
        log.info("listener.......");
		// 可以从event 里拿到数据后可以查询数据库 获取业务报文
        MyMqBody myMqBody = new MyMqBody();
        myMqBody.setName("xiaohuihui");
        myMqBody.setSex("man");
        myMqBody.setAge(18);

        ThirdInterfaceMQResult<MyMqBody> result = new ThirdInterfaceMQResult<>();
        // 设置方法
        result.setMethod("com.cosmo.hhim.tms.methodA");
        // 设置方法版本
        result.setVersion("versionA");
        // 设置有序发送所遵循业务字段
        result.setOrderByKey(null);
        // 设置发送的MQ body
        result.setBizMqContent(myMqBody);

        return result;
    }
}
```



注意：默认是同步的，加上@Async 是异步的 加在方法上 不建议使用异步 threadlocal 会拿不到东西




### 4.3 发送事件

#### （1）方式一
在需要发送事件的类中：
①注入`ApplicationEventPublisher`后 ---> ②调用`publishEvent()`方法即可。

```java
@Slf4j
@RestController
@RequestMapping("/hello")
public class HelloController {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    @GetMapping("/test/eventPublish")
    public AjaxResult eventPublish() {

        MyEvent myEvent = new MyEvent(this);
        myEvent.setOrderNum("1111111111");
        applicationEventPublisher.publishEvent(myEvent);

        log.info("事件发布完成...");

        return AjaxResult.success();
    }

}
```

#### （2）方式二

①实现`ApplicationContextAware`接口，实现`setApplicationContext()`获取到`applicationContext`实例对象。---> ②调用`publishEvent()`方法即可。

```java
@Slf4j
@RestController
@RequestMapping("/hello")
public class HelloController implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @GetMapping("/test/eventPublish")
    public AjaxResult eventPublish() {

        MyEvent myEvent = new MyEvent(this);
        myEvent.setOrderNum("1111111111");
        applicationContext.publishEvent(myEvent);

        log.info("事件发布完成...");

        return AjaxResult.success();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
```