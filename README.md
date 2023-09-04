# BluestarAPI

## 模块介绍

- injector --- 完全使用java编写的一些工具
    * `java swing`的一个`layout`
    * 一些集合(`Collection`)的实现
    * `io`相关内容
    * 数学相关内容
    * 下载器和多线程下载器
    * *反射相关工具(使用`Unsafe`实现的访问器,在不调用构造函数的情况下创建空白实例)
- kotlin --- kotlin相关内容,依赖`injector`模块,主要是将`injector`包装方便kotlin调用
- net --- 目前仅有数据包流和多频道数据包流 依赖`kotlin`模块
- Bukkit-injector --- Minecraft Bukkit插件相关工具 依赖`injector`模块
    * 命令注入工具,命令注册管理
    * 配置文件封装
    * 文字颜色和渐变颜色解析
    * `Gui`创建工具
    * 通过告示牌、聊天、铁砧获取玩家输入
    * *附魔注册
- plugin --- 将`Bukkit-injector`打包为插件

### 注意(*号内容解释)

#### 反射工具

1. 使用了`Unsafe`,在对java中某些类进行反射时,会因权限问题无法成功(例如`Class`类等),使用`Unsafe`可以避免.
   但需要注意的是,在安全管理器启用的状态下`Unsafe`可能无法工作
2. **在不使用构造函数的情况下创建实例**,此功能的实现得益于java中的序列化相关功能,在`ObjectInputStream`
   反序列化一个实例的时候,第一步便是创建一个新实例。相信很多人注意到了此时不会有构造函数被调用,而是凭空捏造出一个实例。
   翻阅源码后发现主要有一个函数专门用于在运行时创建一个空白构造函数,供序列化工具调用来创建空白实例。
   并且此方法本身没有对于是否继承`Serializable`的检查,这使得通过反射调用此函数即可达到目的.
   利用此功能配合反射实现的深克隆,可以**在运行时对任意实例进行完全复制**,且可以保证其没有副作用,也就是*
   *被复制的实例本身也不知道其自身被复制**.
   **但是这些功能全部基于反射和`1`中提到的`Unsafe`实现,并不能保证在三方jdk上稳定运行**

空白实例创建示例:

```java
import me.lanzhi.api.reflect.ReflectAccessor;

public class TEST
{
    public static void main(String[] args) throws Throwable
    {
        A a=ReflectAccessor.blankInstance(A.class);
        System.out.println(a);
    }
}

class A
{
    int a, b;

    public A()
    {
        a=1;
        b=2;
        System.out.println("构造函数被调用");
        throw new RuntimeException();
    }

    @Override
    public String toString()
    {
        return "{ a="+a+", b="+b+" }";
    }
}
```

无报错,且输出以下内容:

```
{ a=0, b=0 }
```