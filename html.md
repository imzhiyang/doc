##Html脚本注入分析
    pageId=<script>alert(123);</script>
    1. 在脚本中Page.registerPage("${pageId}", function(){});
    2. 在<div id="${pageId}"></div>
    3. <div>${pageId}</div>
    在以上三种场景中，只有1和3会出现脚本注入的问题：在2中没有出现的原因，应该是div的元素属性会按dom的方式去解析元素
    
    延伸问题：
    1. <input id="pageId" value="${pageId}"/>
    alert($("#pageId").val()); // alert("<script>123</script>");
    alert("${pageId?xthml}"); // alert("&lt;script&gt;123..."); 有转义的字符
    2. 所以如果在js中想获取转义后的html，可以类似下面操作
       <script>
           var div = document.createElement("div");
           div.innerTEXT = "<script>alert(21);</script>";
           console.log(div.innerHTML);
       </script>

## html原理
    html中对于解析的原理到底是怎么样的呢？比如是怎么解析，后面又怎么渲染？css和js文件加载顺序是怎么样呢？
    html主要分为两大部分；1.解析 2. 渲染
    1. 对于解析，首先一个html从上至下的解析，css解析和html解析是分开，比如当前解析到<link href="xx.css"/> 这时候会去下载xx.css，但是不影响html继续解析，可以通过查看元素知晓。但是此时是还没有渲染的，加入目前xx.css下载完成(如e1.html中，由于base.css还在下载，这时候碰到bms.js也要下载，这时候会挂起等待，解析器阻塞，可以看到浏览器只解析到了test标签，由于base.css未下载，所以阻塞了渲染。这时候等base.css下载完成，就会完成渲染。)
    2. 如果js写在了head中，就会碰到在下载xx.css时，遇到xx.js又要进行下载，这时候就会阻塞，html也就无法继续解析，需等js文件下载完成之后，才能继续解析。所以js一般放到页面的最末尾，这样就能使解析器继续执行，当到最后解析js，就会阻塞，这时候就会开始渲染。
    3. 渲染必须是解析阻塞，这时候才会发起渲染效果(必须等待当前解析过程中的css都加载完成，才能表示渲染成功，如e2.html中，当前在js中下载引起阻塞，则会让浏览器去渲染，可是由于base.css还未下载完毕，所以会等待base.css下载完成，才能完成渲染)