package org.springframework.test.context;


import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.annotation.Autowired;
import org.springframework.beans.annotation.Qualifier;
import org.springframework.beans.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ClassPathXmlApplicationContext;
import org.springframework.context.annotation.Component;
import org.springframework.context.aware.ApplicationContextAware;
import org.springframework.context.event.event.ApplicationEvent;
import org.springframework.context.event.event.ContextRefreshedEvent;
import org.springframework.context.event.listener.ApplicationListener;
import org.springframework.core.exception.BeansException;
import org.springframework.test.beans.dependency.BeanDependencyInjectTest;

public class ClassPathXmlApplicationContextTest {

    /**
     * 对比之前的{@link BeanDependencyInjectTest#testAutowired()}，直接使用ApplicationContext是不是方便多了？
     */
    @Test
    public void testClassPathXmlApplicationContext() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:5_application_context.xml");
        Writer writer = applicationContext.getBean("writer", Writer.class);
        Article article = applicationContext.getBean("articleAlias", Article.class);
        Assert.assertEquals(writer.getArticle(), article);
    }

    /**
     * 容器启停过程中会发布一些事件，比如容器刷事件，我们可以监听这些事件做一些特定的操作
     */
    @Test
    public void testApplicationContextEvent() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:6_application_event.xml");
    }

    /**
     * 所谓容器自身的事件，其实逻辑是这样的：
     * <p>
     * Spring自己定义了一些事件 ===> 把事件丢给ApplicationContext（实际是ApplicationEventPublisher），让它帮忙发布 ===> ApplicationContext再通知监听者
     * <p>
     * 也就是说，Spring利用了自己的事件发布机制。
     * 同理，我们也可以自定义一些事件，把事件丢给ApplicationContext，然后自定义ApplicationListener监听事件
     */
    @Test
    public void testApplicationContextCustomEvent() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:6_application_event.xml");
        OrderService orderService = applicationContext.getBean("orderService", OrderService.class);
        orderService.cancelOrder("123456", "24681012141618");
    }

    @Component
    public static class Writer {

        @Value("${writerName}")
        private String name;

        @Qualifier("articleAlias")
        @Autowired
        private Article article;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Article getArticle() {
            return article;
        }

        public void setArticle(Article article) {
            this.article = article;
        }
    }

    @Component("articleAlias")
    public static class Article {
        @Value("${articleTitle}")
        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }


    /************* 容器事件监听 *************/

    @Component
    public static class MyApplicationRefreshedEventListener implements ApplicationListener<ContextRefreshedEvent> {

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            System.out.println("监听到ApplicationContext刷新完毕");
        }
    }


    /************* 自定义事件监听 *************/

    @Component
    public static class OrderService implements ApplicationContextAware {

        private ApplicationContext applicationContext;

        public void cancelOrder(String userId, String orderNo) {
            System.out.println("OrderService 用户(" + userId + ")取消了订单，订单号：" + orderNo);
            applicationContext.publishEvent(new OrderCancelledEvent(orderNo));
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }
    }

    @Component
    public static class OrderMqHandler implements ApplicationListener<OrderCancelledEvent> {

        @Override
        public void onApplicationEvent(OrderCancelledEvent event) {
            System.out.println("OrderMqHandler 监听到订单取消，orderNo：" + event.getSource());
        }
    }

    public static class OrderCancelledEvent extends ApplicationEvent {

        public OrderCancelledEvent(Object source) {
            super(source);
        }
    }
}
