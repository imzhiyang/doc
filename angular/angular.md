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

### form不用模板检验，自定义formGroup的方式
    1. 在module中import ReactiveFormsModule，否则会导致no provider for NgControl
    2. form元素需要使用[formGroup]='xxx'，会导致formControl找不到group
    3. 在component中定义xxx的formGroup， 必须与上面的formGroup对应
    4. this.formGroup = new FormGroup({'name': new FormControl(this.hero.name, [Validators.required,Validators.minLength(4)])});
    5. 在input的元素中定义formControlName='name'

### js中get与set方法
    get xxx() { return ''};
    set xxx() { return ''};

### angular下build -prod --env中的问题
    -prod会以产品模式打包，那就不会产生js.map，这样就无法在source下面调试angular
    enableProdModel：以发布环境运行，很多插件就无法进行调试
### typescript 引入模块
    在depedencies中添加对应的js和types的引入，这样在打包的时候，会被拷贝到对应的bundle.js中，而不是引用，是内容拷贝
### 对于js的引入
    1. 将js放入assets中，这样打包会被一起打包，然后在html中引用
    2. 在对应angular-cli.json中的scripts中，写对应的js引用，这样可以被打入bundle.js中，是内容拷贝，不是引用

### typescript中调用js的方法
    1. 由于js的引入属于全局库，所以在开头部分。declare var window: any;
    2. window.xxx就是对应的方法

### 前后台部署的问题
    1. 早前java和view是放在一起开发，如果没有一起部署的话，但是这样会有跨域问题；
    解决方法：在nginx下面，使用端口一样的，但是location不一样的访问配置，这样就能解决。
    如server{location / {bms} location /view/ {root dir}}

### package-lock.json
    npm install情况下来下载对应的js库
    ^符号是在大版本号不变的情况，更新下载新版本的Js，如jquery: ^1.6.2，如果有1.7.1也会被下载
    ~符号是下载最小最版本号的最新版本，如jquery ~1.6.2，会下载1.6.8
    package-lock.json就是在npm install，将所下载的js及其下载源、签名都做了记录，以防到新环境的时候，会导致了下载了其它版本的js。