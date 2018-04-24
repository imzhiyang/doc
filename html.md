##Html脚本注入分析
    pageId=<script>alert(123);</script>
    1. 在脚本中Page.registerPage("${pageId}", function(){});
    2. 在<div id="${pageId}"></div>
    3. <div>${pageId}</div>
    在以上三种场景中，只有1和3会出现脚本注入的问题：在2中没有出现的原因，应该是div的元素属性会按dom的方式去解析元素
    
    延伸问题：
    1. <input id="pageId" value="${pageId}"/>
    alert($("#pageId").val()); // alert("<script>123</script>");
    alert("${pageId?xhmtl}"); // alert("&lt;script&gt;123..."); 有转义的字符
    2. 所以如果在js中想获取转义后的html，可以类似下面操作
       <script>
           var div = document.createElement("div");
           div.innerTEXT = "<script>alert(21);</script>";
           console.log(div.innerHTML);
       </script>