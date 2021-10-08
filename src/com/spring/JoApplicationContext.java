package com.spring;


import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author tyd
 * @date 2021/10/7 15:46
 */
public class JoApplicationContext {
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<BeanPostProcessor> beanPostProcessorList = new CopyOnWriteArrayList<>();

    public JoApplicationContext(Class configClass) {
        // 扫描componentScan路径上，所有带有@component注解的bean-->生成beanDefinition -->beanDefinitionMap
        scan(configClass);

        //将单例对象放入单例池中
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

            if ("singleton".equals(beanDefinition.getScope())) {
                // 创建bean
                Object o = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, o);
            }
        }

    }
    // 本质上，DI,aware，这些都可以理解为初始化前。
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class beanClass = beanDefinition.getBeanClass();
        try {
            // 反射实例化bean,假设使用的是无参的构造方法，不考虑推断构造方法。
            Constructor declaredConstructor = beanClass.getDeclaredConstructor();
            Object instance = declaredConstructor.newInstance();

            // DI，其实bean的DI，也是利用的BeanPostProcessor,源码中像@AutoWired和@value注解，实际上是在一个方法里实现的
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    String fieldName = field.getName();
                    Object bean = getBean(fieldName);
                    field.setAccessible(true);
                    field.set(instance, bean);
                }
            }
            // Aware 回调，（这里就实现一个BeanNameAware）,就多加几个if
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware)instance).setBeanName(beanName);
            }

            // 初始化前，还是对当前这个普通对象的加工
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(beanName, instance);
            }

            // 查看对象上是否有实现InitializingBean接口,初始化，也可以看看有没有方法上有@PostConstruct注解。执行方法
            if (instance instanceof InitializingBean) {
                ((InitializingBean)instance).afterPropertiesSet();
            }

            // 初始化后，bean已经初始化完毕，可以开始AOP的实现了，返回的对象instance，可能就是一个代理对象
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(beanName,instance);
            }

            return instance;

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Object getBean(String beanName) {
        if (singletonObjects.containsKey(beanName)) {
            return singletonObjects.get(beanName);
        } else {
            return createBean(beanName, beanDefinitionMap.get(beanName));
        }
    }

    private void scan(Class configClass) {
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
        // 包路径(这个是带点的com.jo)
        String packagePath = componentScanAnnotation.value();
        // 通过包路径，获取到包路径下，所有的类镜像信息
        List<Class> beanClasses = getBeanClasses(packagePath);

        // 遍历beanClasses，初始化，beanDefinitionMap池
        for (Class clazz : beanClasses) {
            // 有@component注解
            if (clazz.isAnnotationPresent(Component.class)) {
                // 扫描时，将beanPostProcessor生成，添加到beanPostProcessorList中，一会其他的bean在初始化时(createBean())，需要使用到
                if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                    try {
                        BeanPostProcessor beanPostProcessor = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                        beanPostProcessorList.add(beanPostProcessor);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }else {

                    // 要么spring自己生成，要么就是从component注解上获取
                    Component component = (Component) clazz.getAnnotation(Component.class);
                    String beanName = component.value();
                    if ("".equals(beanName)) {
                        beanName = Introspector.decapitalize(clazz.getSimpleName());
                    }

                /*if ("".equals(beanName)) {
                    char[] chars = clazz.getSimpleName().toCharArray();
                    if (chars.length>0) {
                        char prefixName = chars[0];
                        chars[0] = (prefixName+"").toLowerCase().toCharArray()[0];
                        beanName = new String(chars);
                    }
                }*/
                    BeanDefinition beanDefinition = new BeanDefinition();
                    beanDefinition.setBeanClass(clazz);


                    // 解析scope
                    if (clazz.isAnnotationPresent(Scope.class)) {
                        Scope scope = (Scope) clazz.getAnnotation(Scope.class);
                        beanDefinition.setScope(scope.value());
                    } else {
                        beanDefinition.setScope("singleton");
                    }
                    beanDefinitionMap.put(beanName, beanDefinition);
                }
            }

        }
    }

    private List<Class> getBeanClasses(String packagePath) {
        // 所有扫描的bean的clazz
        List<Class> beanClasses = new ArrayList<>();

        // 转换成带斜杠的，com/jo/service,注意了，这个是相对路径
        packagePath = packagePath.replace(".", "/");
        ClassLoader classLoader = JoApplicationContext.class.getClassLoader();
        // 3种类加载器
        // boot----> jre/lib
        // etc-----> jre/etc/lib
        // app ----> 类路径上，我们在java -jar 启动时，可以配置classpath。都是他来加载的。如果你是在用ideal,控制台上“第一行”就有这个 -classpath

        // 相对于，AppClassLoader它所对应的类路径上的相对路径。
        URL resource = classLoader.getResource(packagePath);
        // 这是一个文件夹/F:/aMyStudy/design/out/production/joSpring/com/jo/service
        File file = new File(resource.getFile());

        if (file.isDirectory()) {
            // 拿里面的文件
            File[] files = file.listFiles();
            for (File f : files) {
                String fileName = f.getAbsolutePath();
                if (fileName.endsWith(".class")) {
                    String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                    className = className.replace("\\", ".");
                    // 使用类加载器，加载我们的类
                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        beanClasses.add(clazz);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return beanClasses;
    }
}
