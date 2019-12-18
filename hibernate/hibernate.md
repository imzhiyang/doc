## Hibernate问题

### Transaction
    问题：在@Transaction下的方法，抛出了BmsException，但是事务没有回滚
    原因：在DefaultTransactionAttribute下，判断事务是否回滚是(ex instance Of RuntimeException || ex instanceOf Error)
    解决方法： 在@Transaction中rollback=Exception.class。或者在配置文件ap..xml中，transactionManager新增advise的配置
### 缓存问题
    class A { B b;}   class B {name} 一开始b.name=test
问题：  
<table>
	<tr>
		<td>事务1</td>
        <td>事务2</td>
    </tr>
    <tr>
        <td> 
            query A
        </td>
        <td></td>
    </tr>
    <tr>
        <td></td>
        <td>更细B的name为test1。comitted</td>
    </tr>
    <tr>
        <td>select * from B; b.name=test。没有改变</td>
        <td></td>
    </tr>
</table>
原因：
    1. 由于事务1在query A的时候，会把B的信息也加载的缓存中。
    2. select * from B； 会直接从hibernate的一级缓存中直接获取。所以还是test
    3. 在hibernate的Loader中，会通过getEntityUsingInterceptor来判断entityKey是否已经在缓存中获取。
    4. 由于事务1中已经有b的缓存，所以会第二次也会从缓存中直接获取


### join两次的问题
    TestVideo {
        @JoinColumn(column="video_id)", reference="video_id")
        VideoLibVideo vlv; 
    }
    VideoLibVideo {
        @JoinColumn(column="video_id")
        Video video;
    }
    Video {
        long id;
    }
    以上的例子会造成，在获取videoLibVideo的时候，join两次video。
    代码跟踪：
    1. 查询生成sql的过程中。JoinWalker.walkEntityTree，判断出有一个embedded属性，..._testVideo_vlv.video需要以componentType的方式去join
    2. 深层原因是：BinderHelper.createSyntheticPropertyReference(由于testVideo中使用了reference_column的方式)这时候会往RootClass中addProperty(SynthethicProperty(...TestVideo_vlv.video))属性
    解决方法：
    TestVideo {
        @Transient
        VideoLibVideo vlv;
        @Join(video_id, reference_column=video_id)
        VideoLibVideo2 vlv2;// 这个只存video_id
        @Join(video_id)
        VideoEntity video;
    }

### hibernate对象状态
	在cms中存在这样的问题，在代码中有使用@Transaction(propagation=NOT_SUPPORT)的方式，这样就会造成调用该方法和saveOrUpdate的方法不是在一个session里面，而其它session又有可能执行了数据的获取，很容易造成一个id在session中有两个实体读取
	1. 临时态，一个new的实体，可通过persist、save、saveOrUpdate，状态持久态，没有id值都是临时态，否则都是游离态
	2. 持久态（可通过saveOrUpdate、update（将对象强制刷入）、merge(合并已有对象，并返回session的对象)将一个临时态对象转为持久态）
	3. 游离态，不在session状态下，调用了evit、clear等方法，saveOrUpdate、merge、update
	4. 删除态，调用了delete方法
	cms中碰到一个问题：
	1. 在ResolutionService.delete，调用了entryService.find（注意这里的find是NOT_SUPPORT，也就是会重新开一个session去获取entry），然后遍历对象调用entryService.delete，多次调用这个ResolutionService.delete方法，导致同一个id会在delete session中存在。
	2. 因为在调用了hibernate的delete，后续所有针对该id对象的获取都会被识别为删除态，所以就会标识为空，