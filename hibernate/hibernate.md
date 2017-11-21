## Hibernate问题

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