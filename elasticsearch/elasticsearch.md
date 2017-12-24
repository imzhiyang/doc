##Elasticsearch
###查看当前已有的索引
/_aliases<br/>
/_cat/indices?v<br/>
_cluster/health?level=indices&pretty（用于检测状态,level：indices、shards、cluster）<br/>
_nodes(集群节点的状态)
###搜索
- 基本语法：
- 

    {
        "query":{
            "match"：{
                "fieldName":"value"
            }
        }
    }

- term与match差别
- 
    term用于精确搜索(并不会使用分析器)
    match用于非analyze的字段(long、int、string not analyzed精确查询)。用于analysed字段，则会根据使用指定的分词器进行搜索。默认采用了standar分词器
- 问题
- 
    数据格式如下：
    "nameInitial" : {
       "type" : "text",
       "fields" : {
           "keyword" : {
           "type" : "keyword",
           "ignore_above" : 256
        }
    }
    可以看出text是被默认(standard)分词的，只有keyword类型才不会被分词。
    {
        "query":{
            "match":{
                "nameInitial":"GF"
            }
        }
    }
    {
        "query":{
            "term":{
                "nameInitial":"GF"
            }
        }
    }
    使用第一个可以找出记录(默认对nameInitial去分词，再去倒排匹配索引)
    第二条却不行。因为term需要确确找出对应的记录为(GF)。而与上面数据存入倒排索引时，不匹配
- 复杂查询
- 
    {"query":{"bool":{....}}} 基本格式
    主要使用四种
    must(and) 
    must_not(and not)
    should(or)
    filter：不仅可以用bool，也可以搭配constant_score进行过滤。性能差不多
- 查询与过滤
- 
    过滤只是用于检查包含和排除；而查询则还要计算相关性。因此过滤的效率会比查询来得高
- 分析与分词器
- 
    主要有三个以下步骤：
    过滤器： 过滤出有效信息(如html标签过滤掉)
    分词器： 对不同的文本进行分割，得到不同的词组
    分词器过滤器： 可能在分词器之后还会对词组进行过滤，转化为小写、去掉一些特殊词等
    可使用 _analyze {"analyzer":"vvv", "text":"vv dd df"} 来查看分析结果
- 验证查询(_validate)
- 
    /index/type/_validate/query?pretty -d data
    针对data的查询分析
- 索引结构(mapping)
- 
    /index/_mapping/type?pretty
    可以查看指定索引下的类型结构
- 查询分析(explain)
- 
    /index/type/_search?explain&pretty "query"
    可以查看查询的执行情况，类似explain analyse

### 索引

    索引设置：主要是分片数和主分片的拷贝数目
- number\_of_shards: 主分片的数量
- number\_of_replicas 每个主分片的拷贝数量
- 同一索引下，所有字段成扁平化效果，不跟定义的一级类型相关；如type1-title字段，那么type2不能再有title字段
### 分析器
- 字符过滤器(char_filter)
- 分词器(tokenizer)
- 词单元过滤器(filter)
> {
>     "setting": {
>         "analysis" : {
>             "char_filter":{...}
>             ...
>         }
>      }
> }

指定字段的分析器
> {"index": {"mappings": {"type1":{"fieldName":{"type":"text","analyzer":"standard"}}}}}


- 视频字母搜索的elasticsearch转化
- 
    1. 将视频拼音的首字母使用了空格分开各个词组，如LZY就变成了L Z Y，从而达到elastic可以为每个字母生成倒排索引
    2. 搜索的时候，使用Z Y进行搜索，但是会导致，只要包含其中一个都会出来，这不是想要的结果
    3. 可以在match中设置operator来表示and，这样来达到同时包含
    3. 也可以采用bool must复合查询，来设置必须全部包含，但是碰到问题L Z Y的得分会比Z Y的得分来得高。bool中的must、should会影响评分，must_not只用于排除，不会影响评分
- bool的评分公式
- 
    bool: {"must":{...1},"must":{...2},"bool":{"should":{3}}}
    1的评分+2的评分+bool的评分/3


### boost
在match中新增boost属性，来设置权重，可影响得分

### Match
- type指定搜索类型，比如phrase，必须满足词组的顺序，如match:{'a':'Z Y'}，a字段中字符顺序必须是Z Y这样顺序。等同于match_phrase
- operator 指定操作类型；比如match:{'a':'Z Y','operator':'and'}，a必须包含Z或者Y

### ngrams
好像postgresql中也是使用了gist_trgm模块，可能这个东西就是相关性的重要点吧

- edge_ngram表示边界分割器，边界min\_gram:分割的力度，max\_gram最多存储多少字符
- ngram表示分割器，min\_gram同上

postgresql中之前的gist_trgm，应该采用的就是这种过滤器，将存储的分词切割存储

但是这里有个问题：ngrams分割之后，是没有position，也就是对于match_phrase是无效的。

### 得分计算
    score=boost1*IDF(term1) * tfNorm(term1) + boost2*IDF(term2) * tfNorm(term2) + ....
    boost1:自定义的权值，默认是1
    其中IDF(term)=ln(1 + (docCount - freq + 0.5)/(freq + 0.5))
    docCount：查询中满足查询条件的所有文档(按分片)
    docFreq：满足本条term的查询文档数目(比如有两个文档满足，就是2)
    由此可以看出，如果出现的频率也高，那么相应的得分越低。
    tfNorm(term1) = (freq * (k1 + 1)) / (freq + k1 * (1 - b + b * fieldLength / avgFieldLength))
    其中: freq表示出现次数、k1=1.2、b=0.75
    avgFieldLength:平均长度，比如现在三个文档text analyze、 lai zhi yang、 wang meng jie，得出总共有8个单词/3=2.66667
    fieldLength：出现这个搜索关键词的词长度，比如搜索lai，则得出lai zhi yang；那么fieldLength就是3，但是elasticsearch存储这个长度却不是3，需要一定转化：
    byte i = SmallFloat.floatToByte315(quar(1/x)); x代表doc的term数量
    float t = SmallFloat.floatToByte315(i);
    fieldLength = 1 / t * t;
### 基于字母首字母的视频分析实现
前面本来想使用gram的分析器，后面尝试了之后，发现在经过gram分析器之后，数据就没有按照对应的位置，所以不适用于match_phrase不合适。

由于elasticsearch的得分是根据should等从句的得分之后，所以设计为首先match_phrase找出匹配顺序的，接着新增should->match_phrase_prefix，最后为了让如果全值匹配的得分较高，所以新增了constant_score->term来提升字符串相等时的评分

### cmd utf-8乱码
CHCP 65001

### 集群
    在.yml的配置文件中，配置cluster.name=laizy-elasticsearch（集群中的节点信息一样）
    node.name(节点名称)
    transport.tcp.port(节点端口，非http，比如集群中通讯端口)
    discovery.zen.ping.unicast.hosts(单播的集群节点，比如localhost:transport.tcp.port)

### Transport Client 与 Node Client       
    transport client: 需要维护自己的客户端列表，并且只是用于查询，无法新增节点；
    node Client: 新增一个协助节点加入到集群中，并作为一个节点来操作数据，自身不保存数据。     

### master、data、ingest区别
    master: 主节点；负责索引增删改、节点增删。如果没有主节点，会导致数据等搜索失败
    data: 数据节点，可进行数据存储
    ingest: 应该说是辅助节点，不能进行数据存储，但是可以进行数据搜素
    集群节点：上面的三种都不是，只能进行负责转发或者协助

### Java Api Transport Client
    client.transport.sniff: true，自动查找cluster的数据节点，这个适合用于数据操作，但是搜索的话，我觉得ingest的节点也是可以的
### network.host
    可限定访问的ip地址。
    由于测试环境做了内地ip的转换，所以默认配置下bound_address的值127.0.0.1，由于测试环境有做了ip转换，nat转换，所以不晓得最后的限制访问地址。所以后面改为0.0.0.0。