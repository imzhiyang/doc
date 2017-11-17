## 安装angular/cli
###设置npm的镜像信息
npm config set registry http://registry.cnpmjs.org

### node-sass安装失败
无法下载win32-x64-46_binding.node<br/>
设置SASS_BINARY_PATH的环境变量为win32-x64-46_binding.node的路径

### module、component、class的关系
    1. module中可以引入module、component，
    2. 类定义在.ts文件，使用export class
    3. 在componet中无法import component，只能在module中声明对应的component
    4. 比如在@component中定义了selector(为my-test)，那么在其它component中，可以使用<my-test>引入
    5. 由于之前在index.html中使用了<app-root>，所以app.componenet.ts的selector就必须为app-root