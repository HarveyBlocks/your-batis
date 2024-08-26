# YourBatis

本项目中有大量TODO, 未完成的部分会及时抛出UnfinishFunctionException异常, 方便检查

##项目结构

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

#### Resuources

帮助读写文件, 解析字节码

####Log

封装logback包中的一些日志

####ResolveUtil/ResourceAccessor

ResourceAccessor及其实现有包扫描, 解析Jar包的功能

能在Jar包中寻找资源

ResolveUtil调用ResourceAccessor, 其中ResolveUtil中规定了目标类型应当满足的条件, 用ResourceAccessor找到类之后会进行匹配



### Annotation

####Param/ParamNameResolver

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

###Util

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

#### confix.xsd/mapper.xsd

xml限制文件

限制文件能很好的对XML的格式进行规范, 但是这与MyBatis中有众多的表达式产生了冲突

例如MyBatis对于多个IDREF组成的列表采取的是字符串分割

但是要用XSD限制IDREF, 只能用空格作为分隔符, 就很鸡肋



### Config

Configuration, 注册中心? 一个confix.xml文件对应一个Config类

有各种配置, 属性和各种Register, 同时也有很多工具类的方法的中转

便于各类之间传播数据(同时也导致其中保存的字段鱼龙混杂, 非常冗长)和调用方法(各种方法也混在一起)

里面的配置都是写死的, 一开始本来打算取缔掉这些涉及配置的字段的,这样可以让本框架的使用者不具备配置这些字段权限的(因为啥都可以配置工作量很大), 但为了可读性, 还是保留了这些字段, 导致为这些字段重新具备配置的能力提供了机会

emmm 

~~简单来说本来想偷懒, 就要在配置字段上偷工减料, 没想到最后还是写了一堆, 导致工作量反而每减少?~~

### Scriping

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

统一调用管理Excutor, 

参数的映射和Statement的准备/预编译

Statement的执行

执行结果到Entity实体类的映射注入

#### RowBounds

在JVM内存层面对获取到的结果进行分页

RowBounds字段有offset和limit

### Datasource

有池化的和非池化的, 都有实现

池化的话, 活跃的连接用光了, 就获取空闲的连接, 空闲的连接用光了就创建空闲的连接

所有连接都被使用了, 就查看是否存在连接被长期占用, 被长期占用的连接是否还能用, 调整之后返回连接池

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

## 使用流程与测试

>   以Work3的一个方法为例

###代码清单

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
sqlSession.getMapper(BillMapper.class)
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

