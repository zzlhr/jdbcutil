# JdbcUtil
## 一套可单独使用或作为插件使用的jdbc工具类
###### 暂时只有存储过程操作。


使用方法简单，基于原生jdbc，操作方便，可集成到任意能够获取DataSource对象的框架/项目中。

当然同样可以通过原生jdbc集成该插件。

如何使用:
```java
    DataSource dataSource = ....;
    
    //初始化DataSource对象
    DataSourceUtil.setDataSource(dataSource);
    
    String procedureName = "selectUser";
    
    Procedure procedure = new Procedure("selectUser");
    
    procedure.commit();
    
    //获取结果集
    procedure.getResultArray();
```

更多例子请等待后续更新，都是实现的了东西，如果有兴趣可以自己先研究。

