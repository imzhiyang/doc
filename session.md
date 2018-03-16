## Session

### session是如何产生，与验证的呢？
    1. session是以cookie中jsessionId的值，来查找对应的session

### 为何在sso中sessionId会变化，导致两次登录？
    这种情况只有出现在点击登出操作之后，才会出现
    1. 登出，执行subject.logout，旧session(123)被废弃
    2. 重定向至cas/logout，然后再由cas跳转到sso/cas
    3. sso/cas判断没有登录，登录失败，跳转至failureUrl，这时候sessionId还是保留(123)；因为没有收到response.setCookie的操作
    4. cas login成功之后，重定向至sso/cas，这时候login成功，并写入ticket-123的对应关系；而由于123session已丢失，subject.save的时候，会触发，session.create（234），然后response设置新的sessionId
    5. 跳转至home，没有principal，所以需往后台请求(sessionId=234)，接着当然是请求失败了。
    6. 接着又重新跳转至cas，重复4的流程，但是这次234的session并没有失效，所以第二次成功了。
    
    解决方法：
    1. 在logout的service配置为/home，这样就会跳转至home，经过拦截器，会触发sesssion.create，然后回到浏览器，会将session刷新
    2. 在customCasFilter中记录的subject.getSession(false).getId，而不是request的sessionId，这样就能保证，存储的是最终的sessionId
    3. 在跳转至登录页的时候，首先让其跳转至home，由于Home被authc的拦截器拦截，并会跳转至对应的登录页，这时候由于会触发WebUtils.saveRequest来记住上次密码，由于之前的session的失效，会重新创建sessionId，并触发response.setCookie回去，这样就能在前台保证下次请求的sessionId为最新的

### 在采用request.getSession(true)，为啥在经过logout之后就会产生新的sessionId，看了一下源码，默认也是先取一下request中是否存在，也是传的登出之前的sessionId，那为何session会丢失呢？
    1. subject.logout
    2. session.invalidate();导致旧session失效，但具体session是怎么保存的，没去研究
    3. request.getSession(false) = null;
    4. request.getSession(true); 从而导致session被create