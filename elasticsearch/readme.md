##ElasticSearch
### 搜索过滤条件
+ 表达式搜索<br/>
  {"query":{"match":{"last_name":"Smith"}}} 查找last_name包含Smith的人物，这里的串是完整的，不能切割
+ 更复杂的搜索<br/>
  {"query":{"bool":{"must":{"match":{"last_name":"smith"}},"filter":{"range":{"age":{"gt":30}}}}}} last_name为Smith，并且年纪超过30
+ 全文检索<br/>
  {"query":{"match":{"about":"rock climbing"}}}
+ 部分匹配<br/>
  prefix，如果新建索引的时候，没有指定field为not_analyse的话，那么该字段无法使用prefix来过滤
### Aggerations 聚合

    `
    消息体
    {
        "aggs":{
            "all_interests":{"terms":{"field":"interests"}}
        }
    }
    但是执行以上的聚合，会报错。Fielddata is disabled on text fields by default
    解决方法：
    1. 修改消息体：filed:interests.keyword
    2. 将interests置为fieldValue设为true
    PUT magacorp/_mapping/employee   magacorp为索引；employee为类型
    {
       "properties": {
          "region":{
              "type": "text",
              "fielddata": true
          }
        }
    }    
    `
###_mapping查看数据结构
###filter过滤器
过滤器缓存
###facet切面
    `
       不会考虑过滤器的信息
       {
           "query":{"match_all":{}}
           "filter":{},
           "facet":{}
       }
    `
<br/>

    `
        {
           "query":{
                "filtered" : {"filter":{}}
           },
           "facet":{}
        }
    ` 

     


    



