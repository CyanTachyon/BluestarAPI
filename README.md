# BluestarAPI

## 模块介绍

- injector --- 完全使用java编写的一些工具
    * `java swing`的一个`layout`
    * 一些集合(`Collection`)的实现
    * `io`相关内容
    * 数学相关内容
    * 下载器和多线程下载器
    * *反射相关工具(使用`Unsafe`实现的访问器,在不调用构造函数的情况下创建空白实例)
- kotlin --- kotlin相关内容,依赖`injector`模块
    * `injector`中的内容使用`kotlin`包装方便`kotlin`调用
    * 序列化工具,支持序列化和反序列化,与`ObjectInput/OutputStream`类似,区别为不检查`Serializable`,且序列化结果为可见字符组成的字符串
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
2. **在不使用构造函数的情况下创建实例**,此功能的实现与`ObjectInputStream`创建实例类似但没有对于是否继承`Serializable`的检查.
   通过此方法创建的实例为空白的,即简单类型为`0`或`false`,复杂类型为`null`
   **但是这些功能全部基于反射、`1`中提到的`Unsafe`,以及一些不规范内容实现,并不能保证在三方jdk上稳定运行**

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
    final int a, b;

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