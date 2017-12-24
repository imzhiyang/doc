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