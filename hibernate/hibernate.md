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