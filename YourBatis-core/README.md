# YourBatis


## 项目简介

>  本项目中有大量TODO, 未完成的部分会及时抛出UnfinishedFunctionException异常, 方便检查


- 项目进度:
  - 支持基本的CRUD
  - 有照着MyBatis源码稍微封装了一下logback
  - 有池化的连接池
  - 有xsd规范xml文件的编写
- 未完成
  - Cursor
  - Cache
  - GeneratorKey, 
    因为GeneratorKey似乎是将写操作产生的id注入到参数的Id里去
    这样感觉降低了可读性
  - Mybatis中有对xml的if元素的属性条件语句的解析, 类似于这种表达式的解析
    MyBatis采用的似乎是解释器模式, 涉及的类实在太多了, 故本项目采用了Java自带的解析JavaScript的解析器
    而没有从头开始写
  - 有好多的配置, 不能在xml中设置, 因为在代码中被写死成了默认值
    这是为了方便测试, 其实开放配置也很快
    一开始的时候为了赶进度想着少做一点功能, 后来发现这些功能不得不做, 结果差不多都写了😂



## 项目结构

```
D:\IT_STUDY\SOURCE\JDK\YOURBATIS\YOURBATIS-CORE\SRC\MAIN
├─java
│  └─org
│      └─harvey
│          └─batis
│              ├─annotation
│              │      Flush.java
│              │      Intercepts.java
│              │      Param.java
│              │      Signature.java
│              │
│              ├─binding
│              │      MapperMethod.java
│              │      MapperProxy.java
│              │      MapperProxyFactory.java
│              │      MapperRegistry.java
│              │
│              ├─builder
│              │  │  BaseBuilder.java
│              │  │  CacheRefResolver.java
│              │  │  MapperBuilderAssistant.java
│              │  │  MethodResolver.java
│              │  │  ParameterExpression.java
│              │  │  ResultMapResolver.java
│              │  │  SqlSourceBuilder.java
│              │  │
│              │  └─xml
│              │          MapperBuilder.java
│              │          XMLConfigBuilder.java
│              │          XMLIncludeTransformer.java
│              │          XMLMapperBuilder.java
│              │          XMLMapperEntityResolver.java
│              │          XMLStatementBuilder.java
│              │
│              ├─cache
│              │      Cache.java
│              │      CacheKey.java
│              │      PerpetualCache.java
│              │
│              ├─config
│              │      Configuration.java
│              │
│              ├─cursor
│              │      Cursor.java
│              │      DefaultCursor.java
│              │
│              ├─datasource
│              │      DataSourceFactory.java
│              │      DriverProxy.java
│              │      PooledConnection.java
│              │      PooledDataSource.java
│              │      PooledDataSourceFactory.java
│              │      PoolState.java
│              │      UnpooledDataSource.java
│              │      UnpooledDataSourceFactory.java
│              │
│              ├─exception
│              │  │  ExceptionFactory.java
│              │  │  PersistenceException.java
│              │  │  TooManyElementsException.java
│              │  │  UnfinishedFunctionException.java
│              │  │  YourbatisException.java
│              │  │
│              │  ├─binding
│              │  │      BindingException.java
│              │  │
│              │  ├─builder
│              │  │      BuilderException.java
│              │  │      IncompleteElementException.java
│              │  │
│              │  ├─cache
│              │  │      CacheException.java
│              │  │
│              │  ├─datasource
│              │  │      DataSourceException.java
│              │  │
│              │  ├─executor
│              │  │      ExecutorException.java
│              │  │
│              │  ├─io
│              │  │      LogException.java
│              │  │
│              │  ├─plugin
│              │  │      PluginException.java
│              │  │
│              │  ├─reflection
│              │  │      ReflectionException.java
│              │  │
│              │  ├─scripting
│              │  │      ScriptingException.java
│              │  │
│              │  ├─transaction
│              │  │      TransactionException.java
│              │  │
│              │  └─type
│              │          ResultMapException.java
│              │          TypeException.java
│              │
│              ├─executor
│              │  │  BaseExecutor.java
│              │  │  BatchResult.java
│              │  │  Executor.java
│              │  │  SimpleExecutor.java
│              │  │
│              │  ├─key
│              │  │  └─generator
│              │  │          Jdbc3KeyGenerator.java
│              │  │          KeyGenerator.java
│              │  │          NoKeyGenerator.java
│              │  │          SelectKeyGenerator.java
│              │  │
│              │  ├─loader
│              │  │      ResultLoaderMap.java
│              │  │
│              │  ├─param
│              │  │      DefaultParameterHandler.java
│              │  │      ParameterHandler.java
│              │  │
│              │  ├─result
│              │  │      DefaultMapResultHandler.java
│              │  │      DefaultResultContext.java
│              │  │      DefaultResultHandler.java
│              │  │      DefaultResultSetHandler.java
│              │  │      ResultContext.java
│              │  │      ResultExtractor.java
│              │  │      ResultHandler.java
│              │  │      ResultSetHandler.java
│              │  │      ResultSetWrapper.java
│              │  │
│              │  └─statement
│              │          BaseStatementHandler.java
│              │          PreparedStatementHandler.java
│              │          RoutingStatementHandler.java
│              │          StatementHandler.java
│              │          StatementUtil.java
│              │
│              ├─io
│              │  │  AbstractResourceAccessor.java
│              │  │  ClassLoaderWrapper.java
│              │  │  DefaultResourceAccessor.java
│              │  │  ResourceAccessor.java
│              │  │  ResourceAccessorFactory.java
│              │  │  Resources.java
│              │  │
│              │  ├─log
│              │  │  │  Log.java
│              │  │  │  LogFactory.java
│              │  │  │
│              │  │  ├─impl
│              │  │  │      Slf4jImpl.java
│              │  │  │      Slf4jLocationAwareLoggerImpl.java
│              │  │  │      Slf4jLoggerImpl.java
│              │  │  │
│              │  │  └─jdbc
│              │  │          BaseJdbcLogger.java
│              │  │          ConnectionLogger.java
│              │  │          PreparedStatementLogger.java
│              │  │          ResultSetLogger.java
│              │  │          StatementLogger.java
│              │  │
│              │  └─xml
│              │          ResolverUtil.java
│              │
│              ├─mapping
│              │  │  BoundSql.java
│              │  │  Discriminator.java
│              │  │  Environment.java
│              │  │  MappedStatement.java
│              │  │  ParameterMap.java
│              │  │  ParameterMapping.java
│              │  │  ResultMap.java
│              │  │  ResultMapping.java
│              │  │
│              │  └─sqlsource
│              │          DynamicSqlSource.java
│              │          RawSqlSource.java
│              │          SqlSource.java
│              │          StaticSqlSource.java
│              │
│              ├─parsing
│              │      ConfigXmlConstants.java
│              │      DefaultErrorHandler.java
│              │      GenericTokenParser.java
│              │      MapperXmlConstants.java
│              │      PropertyParser.java
│              │      TokenHandler.java
│              │      XNode.java
│              │      XPathParser.java
│              │
│              ├─plugin
│              │      Interceptor.java
│              │      InterceptorChain.java
│              │      Invocation.java
│              │      Plugin.java
│              │
│              ├─reflection
│              │  │  ContextMap.java
│              │  │  DefaultReflectorFactory.java
│              │  │  MetaClass.java
│              │  │  MetaObject.java
│              │  │  ParamNameResolver.java
│              │  │  ParamNameUtil.java
│              │  │  Reflector.java
│              │  │  ReflectorFactory.java
│              │  │  SystemMetaObject.java
│              │  │  TypeParameterResolver.java
│              │  │
│              │  ├─factory
│              │  │      DefaultObjectFactory.java
│              │  │      ObjectFactory.java
│              │  │
│              │  ├─invoke
│              │  │      AbstractFieldInvoker.java
│              │  │      AmbiguousMethodInvoker.java
│              │  │      Invoker.java
│              │  │      MethodInvoker.java
│              │  │      ReadableFieldInvoker.java
│              │  │      WriteableFieldInvoker.java
│              │  │
│              │  ├─property
│              │  │      FieldProperties.java
│              │  │      PropertyTokenizer.java
│              │  │
│              │  └─wrapper
│              │          BaseWrapper.java
│              │          BeanWrapper.java
│              │          CollectionWrapper.java
│              │          DefaultObjectWrapperFactory.java
│              │          MapWrapper.java
│              │          ObjectWrapper.java
│              │          ObjectWrapperFactory.java
│              │
│              ├─scripting
│              │  │  LanguageDriver.java
│              │  │  LanguageDriverRegistry.java
│              │  │
│              │  ├─defaults
│              │  ├─js
│              │  │      ExpressionEvaluator.java
│              │  │      LanguagePhaser.java
│              │  │      PropertyAccessor.java
│              │  │
│              │  └─xml
│              │      │  AbstractNodeHandler.java
│              │      │  DynamicContext.java
│              │      │  NodeHandler.java
│              │      │  SqlNode.java
│              │      │  XmlLanguageDriver.java
│              │      │  XmlScriptBuilder.java
│              │      │
│              │      ├─handler
│              │      │      ChooseHandler.java
│              │      │      ForEachHandler.java
│              │      │      IfHandler.java
│              │      │      OtherwiseHandler.java
│              │      │      SetHandler.java
│              │      │      TrimHandler.java
│              │      │      WhenHandler.java
│              │      │      WhereHandler.java
│              │      │
│              │      └─node
│              │              ChooseSqlNode.java
│              │              DynamicSqlNode.java
│              │              ForEachSqlNode.java
│              │              IfSqlNode.java
│              │              MixedSqlNode.java
│              │              SetSqlNode.java
│              │              StaticTextSqlNode.java
│              │              TextSqlNode.java
│              │              TrimSqlNode.java
│              │              WhereSqlNode.java
│              │
│              ├─session
│              │      DefaultSqlSession.java
│              │      DefaultSqlSessionFactory.java
│              │      RowBounds.java
│              │      SqlSession.java
│              │      SqlSessionFactory.java
│              │      SqlSessionFactoryBuilder.java
│              │
│              ├─transaction
│              │  │  Transaction.java
│              │  │  TransactionFactory.java
│              │  │
│              │  └─jdbc
│              │          JdbcTransaction.java
│              │          JdbcTransactionFactory.java
│              │
│              └─util
│                  │  ArrayUtil.java
│                  │  ConsoleColorfulString.java
│                  │  ErrorContext.java
│                  │  OneGetter.java
│                  │  ReflectionExceptionUnwrappedMaker.java
│                  │  SqlStringUtil.java
│                  │  StrictMap.java
│                  │  UrlBuilder.java
│                  │  XPathBuilder.java
│                  │
│                  ├─enums
│                  │      AutoMappingBehavior.java
│                  │      ExecutorType.java
│                  │      JdbcType.java
│                  │      ParameterMode.java
│                  │      ResultSetType.java
│                  │      SqlCommandType.java
│                  │      StatementType.java
│                  │      TransactionIsolationLevel.java
│                  │
│                  ├─function
│                  │      ThrowableFunction.java
│                  │      ThrowableSupplier.java
│                  │
│                  └─type
│                          BaseTypeHandler.java
│                          BigIntegerTypeHandler.java
│                          DateTypeHandler.java
│                          DoubleTypeHandler.java
│                          EnumTypeHandler.java
│                          IntegerTypeHandler.java
│                          LongTypeHandler.java
│                          ObjectTypeHandler.java
│                          SimpleTypeRegistry.java
│                          SqlTimestampTypeHandler.java
│                          StringTypeHandler.java
│                          TypeHandler.java
│                          TypeHandlerRegistry.java
│                          TypeReference.java
│                          UnknownTypeHandler.java
│
└─resources
    │  README.md
    │
    └─org
        └─harvey
            └─batis
                └─builder
                    └─xml
                        └─xsd
                                yourbatis-config.xsd
                                yourbatis-mapper.xsd

```

### Reflection

![image-20240826154819533](../../../../blog/Java/typora-user-images/README/image-20240826154819533.png)

框架的基础, 用来反射, 依据Getter和Setter方法注入字段

#### Reflector

由ReflectorFactory构造

解析一个字节码文件的Getter和Setter, 并将信息存储在本类字段

其解析的范围包含父类, 泛型参数列表

#### MetaObject/ObjectWrapper

依靠MetaObject能获取一个实例对象的一系列信息和其内部的值(依靠反射)

其内部调用ObjectWrapper, ObjectWrapper依靠MetaClass反射实体

ObjectWrapper的实现类有BeanWrapper, MapWrapper和CollectionWrapper, 有点类似组合模式

#### MetaClass

用Reflector解析一个类的字节码信息, 并存储

#### ParamNameResolver

帮助解析的工具类

#### invoker

![image-20240826154956173](../../../../blog/Java/typora-user-images/README/image-20240826154956173.png)

使用反射(Method#invoke)来实现方法的代理, 以此帮助执行Getter和Setter的逻辑

`Ambiguous`, 意味着Setter的参数类型不同(且不存在继承关系)但set的对象(依据函数名)是相同的

这种函数可以存在, 但不能被执行

#### Properties

本处Properties, 是Mybatis中反射类型的方法, 或者说表达式

例如

```
school.students[12].score[math].level
```

表示的对象是: school对象中的students集合, students集合中的索引12的元素student, 该元素中score字段, score是Map, 依据key: "math"获取到的Score对象的level排名

以此有FieldProperties工具类来获取Properties语句, 有PropertyTokenizer工具类来解析Properties语句

### IO

#### Resources

帮助读写文件, 解析字节码

#### Log

封装logback包中的一些日志

#### ResolveUtil/ResourceAccessor

ResourceAccessor及其实现有包扫描, 解析Jar包的功能

能在Jar包中寻找资源

ResolveUtil调用ResourceAccessor, 其中ResolveUtil中规定了目标类型应当满足的条件, 用ResourceAccessor找到类之后会进行匹配



### Annotation

#### Param/ParamNameResolver

Param用来注解方法的参数, 其中的值会作为参数名

ParamNameResolver用来解析被Param注解的参数(当然不被注解的将采用默认的参数名)

#### Intercepts/Signature

用来注解在Interceptor上, 表示需要被监听的类的目标对象(Intercepts)和方法签名(Signature)

详见plugin

#### SQL有关注解

未实现

### Plugin

#### Interceptor/InterceptorChain

一条链上多个Interceptor进行逐层的增强

#### Plugin

依靠解析Interceptor上的注解Intercepts和Signature来找被监听的目标对象和需要被代理执行的目标方法

### Cache

仅实现PerpetualCache, 永久缓存, 就是用Map

### Util

#### Type

-   **TypeHandler**

    实现了一部分

    实现Java类到JDBC类型的转换

    内置一部分基本类型的TypeHandler,但是只实现了一部分呢

-   **TypeHandlerRegistry**

    注册TypeHandler, 同时完成Java类型到TypeHandler的映射和JDBCType到TypeHandler的映射

-   **TypeAliasRegistry**

    "int"字符串对应到java.lang.Integer类型, "int[]" 类型能对应到java.lang.Integer的数组类型/List集合

    未实现

#### Enum

-   **AutoMappingBehavior**

    未实现AutoMapping

-   **ExecutorType**

    未实现Reuse和Batch

-   **ParameterMode**

    向SQL语句的Statement填充数据的状态

-   **ResultSetType**

    不能设置ResultSet

-   **SqlCommandType**

    sql语句的类型

-   **StatementType**

    如何解析SQL语句

    是预编译防止注入? 还是直接字符串拼接?

    CALLABLE未实现, STATEMENT未实现

    本处采取预编译

-   **TransactionIsolationLevel**

    事务隔离级别

    JDBC原生的是int code, 魔数, 不具备在编译期就检查代码正确性的能力

-   **JdbcType**

    JDBC原生的是int code, 魔数, 不具备在编译期就检查代码正确性的能力

#### 其他

-   **XPathBuilder**

    XPath语句生成器

-   **UrlBuilder**

    Url字符串生成器

    ~~Java原生那个太鸡肋了~~

-   **OneGetter**

    集合中只有一个元素的情况, 就从集合中取出这一个元素

-   ArrayUtil

    对包括数组在内的所有类型给出hashCode,equals和toString方法

### Transaction

包装了JDBC的Transaction

能配置是否启用自动提交事务

### Cursor

未实现

### binding

Mapper接口代理, 抽象接口就依据XML文件的SQL语句进行实现

MethodHandler对方法进行代理运行

### parsing

解析XML字段的各个节点

#### XNode

原生node的封装

#### XNodeParser

原生XPath封装, 同时也具有读取XML文件并解析出document的能力

#### GenericTokenParser/TokenHandler

GenericTokenParser解析一段文本, 如果文本中出现被特定字符(例如`${`在前, `}`在后)包围的内容, 该内容将会被交由TokenHandler处理

#### ConfigXmlConstants/MapperXmlConstants

字符常量, 对应XML文件中的元素名和属性名

#### config.xsd/mapper.xsd

xml限制文件

限制文件能很好的对XML的格式进行规范, 但是这与MyBatis中有众多的表达式产生了冲突

例如MyBatis对于多个IDREF组成的列表采取的是字符串分割

但是要用XSD限制IDREF, 只能用空格作为分隔符, 就很鸡肋



### Config

Configuration, 注册中心? 一个config.xml文件对应一个Config类

有各种配置, 属性和各种Register, 同时也有很多工具类的方法的中转

便于各类之间传播数据(同时也导致其中保存的字段鱼龙混杂, 非常冗长)和调用方法(各种方法也混在一起)

里面的配置都是写死的, 一开始本来打算取缔掉这些涉及配置的字段的,这样可以让本框架的使用者不具备配置这些字段权限的(因为啥都可以配置工作量很大), 但为了可读性, 还是保留了这些字段, 导致为这些字段重新具备配置的能力提供了机会

呃

~~简单来说本来想偷懒, 就要在配置字段上偷工减料, 没想到最后还是写了一堆, 导致工作量反而每减少?~~

### Scripting

#### xml

对获取到的XML的Node中的SQL节点进行解析, 同时将的参数位置和运行时的函数的参数值进行映射

似乎是命令模式?

-   XmlScriptBuilder

    -> handler

    -> node

    -> DynamicContext

    -> StringJoiner

    -> String

#### js

其中, SQL节点中的if节点, 有bool表达式

解析bool表达式的工作是由原生的API: ScriptEngineManager来完成的

MyBatis源码似乎用的是编译器模式

其实我的MyBatis源码, 在这里, 也就是ognl包下的所有文件, 字节码和源码不一致, 导致阅读源码极其困难, 及时重新下载源码写没有解决

### Session

#### SqlSession

统一调用管理Executor, 

参数的映射和Statement的准备/预编译

Statement的执行

执行结果到Entity实体类的映射注入

#### RowBounds

在JVM内存层面对获取到的结果进行分页

RowBounds字段有offset和limit

### Datasource

有池化的和非池化的, 都有实现

池化的话, 活跃的连接用光了, 就获取空闲的连接, 空闲的连接用光了就创建空闲的连接

所有连接都被使用了, 就查看是否存在连接被长期占用, 被长期占用的连接,是否还能用, 调整之后返回连接池

### mapping

#### BoundSql

统筹协调参数和结果和SQL语句本身

#### ParameterMap/ParameterMapping

映射参数

存有参数映射的方法, 类型的对应关系

#### ResultMap/ResultMapping

映射结果

存有结果映射的方法, JAVA的Entity字段和JDBC的Table字段类型的对应关系

#### SqlSource

将XML中的Sql分为多个小段, 各个小段为一个StaticSqlSource, 方便进行解析

### Executor

#### StatementHandler

如何解析SQL语句

是预编译防止注入? 还是直接字符串拼接?

CALLABLE未实现, STATEMENT未实现

本处采取预编译

#### KeyGenerator

未实现, 将结果注入到参数的字段中去, 我觉得参数含有返回值的意味很反人类啊

#### ResultLoaderMap

涉及Lazy懒加载, 未实现

#### DefaultParameterHandler

参数注入

#### Result

支持多语句

-   Statement中取出ResultSet

    -> ResultSet/ResultMap 一一对应

    -> ResultSetHandler

    -> 记录

    -> 字段

    -> TypeHandler

    -> 获取到结果

    -> ResultHandler辅助, 反射注入到Entity

    -> Entity存入ResultContext

    -> ResultContext存入ResultSetHandler

## 项目所用到的技术栈

### Java技术

泛型, 反射, JDBC, logback

### 设计模式

单例(VFS), 工厂, 建造者, 命令, 责任链, 组合

## 项目亮点

照着MyBatis写, 来学习MyBatis的源码的啊? 与其说是本项目的亮点, 不如说是MyBatis的亮点?

MyBatis里有一些钻石被我改过来了? 

### 代码风格

阿里巴巴开发规范

## 使用流程与测试

>   以Work3的一个方法为例

### 代码清单

```java
public class SqlSessionFactoryUtils {
    private static SqlSessionFactory sqlSessionFactory;

    static {
        try {
            String resource = "yourbatis-config.xml";//mybatis核心配置文件
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public static SqlSessionFactory getFactory() {
        return sqlSessionFactory;
    }
}
```

```java
public int update(Bill bill) throws Exception {
    if (bill == null) {
        return 0;
    }
    int num;
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
        // 获取BillMapper接口的代理对象
        num = sqlSession.getMapper(BillMapper.class).update(bill);
        sqlSession.commit();
    } catch (Exception e) {
        e.printStackTrace(System.err);
        throw new ForeignKeyException();
    }
    return num;
}
```

### 获取SqlSessionFactory

解析Config.xml文件

```java
String resource = "yourbatis-config.xml";//mybatis核心配置文件
// 解析config.xml文件
InputStream inputStream = Resources.getResourceAsStream(resource);
// 构建Datasource, Transaction, 加载配置
// 获取Mapper接口所在位置, 完成Mapper.xml和Mapper接口的映射
sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
```
### 获取SqlSession

获取连接

```java
SqlSession sqlSession = sqlSessionFactory.openSession()
```

### 获取Mapper代理

获取Mapper接口的Mapper.xml, 

完成Mapper.xml的解析

完成SQL语句的解析, 将SQL节点语句转化成节点类

完成SQL语句和方法的对应

```java
sqlSession.getMapper(BillMapper.class);
```

### 执行代理方法

解析参数, 反射从参数中获取值

解析SQL动态节点和动态语句, 获取最终的SQL

完成参数值和参数位置的对应

```java
mapper.update(bill);
```

执行SQL语句

-   对于查询语句
    -   获取结果集合(可能多个)ResultSet, 遍历各ResultSet
    -   遍历ResultSet的每一条记录的每一各字段, 依据ResultMap将结果注入到结果的实例对象中
    -   将结果的实例对象组合成集合返回
-   对于写语句
    -   获取结果(int), 返回

## 总结

只有写过MyBatis的源码才知道原来MyBatis的功能如此强大, 好多功能都没能用到

~~虽然不知道这些功能在实际开发中是否有用, 例如xml里一个SQL元素里可以有多个SQL语句, 为此MyBatis写了大量的代码来处理多个结果, 但是实际生产中明显一个SQL一个XML元素, 一个Mapper方法对应可读性更高更好嘛~~

其中, 在尝试开发框架之后, 设计模式和反射这两个方面, 感觉对我的帮助最大, 因为MyBatis中用了大量的反射和设计模式

我甚至学到了MethodHolder, 如果不是MyBatis, 我都不知道有这种东西

在阅读MyBatis源码的过程中, 我发现我常常被拘束于一个类的实现, 而很容易忽略类之间的调用关系

当我尝试将重点放在类之间的关系时(设计模式), 我发现我很难理清思路

类于类之间常常以一种诡异的方式组合在一起, 我尝试去找对应的设计模式, 却没办法找到最合的适设计模式去对应, 让我无法去分析这样构造代码的优势

我只能理解"他是这么构造代码的", 却很难想出"这样构造代码有什么好处"

我想这是因为我写的代码还不够多的缘故吧? 

还有, MyBatis中会有使用protected 或者 default 来直接给字段读写访问权限, 我觉得封装性不好, 为什么MyBatis认为这是可以接收的?

还有, MyBatis源码的注释也太少了, 看得我好痛苦(不过就算有注释, 也会因为中英文翻译带来的歧义而导致走进死胡同就是了😳)