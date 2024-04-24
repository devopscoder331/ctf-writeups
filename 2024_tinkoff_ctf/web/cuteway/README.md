### Cuteway (Дип-взлом)

Category: medium, web, bypass \
Author: Александр Соколов (@alex_S0k0l0v)

#### Description:

У студентов есть подозрения, что препод продаёт их дипломные работы в онлайне. Нужны веские доказательства. Найдите любые улики, которые помогут поймать его за руку.

Получите доступ к визитной карточке препода на сайте: t-cutaway-o78hk6as.spbctf.net

Сайт работает на опенсорсном движке: cutaway_b39a911.tar.gz

### Solution

We have source code for local deployment and security research. First disabled Captcha in files:
```
- ./sources/services/nginx/frontend/static/js/app.js
- ./sources/services/app/schemas/__init__.py
```

![](img/1.png)

![](img/2.png)


Okey, lets try start application use docker-compose:

```
$ cd sources
$ docker-compose -f docker-compose.yml build
$ docker-compose -f docker-compose.yml up
```

![](img/3.png)

Then open page http://127.0.0.1:20010/register and register:

![](img/4.png)

See request and response:

![](img/5.png)


Try login on page and see request and response:

![](img/5.png)

![](img/6.png)

Server responce **access_token** (jwt) and **profile_id**:

![](img/7.png)

See nginx configs, source code, API with endpoints:

- sources/services/nginx/nginx.conf:

![](img/8.png)

- sources/services/app/api/card.py:

![](img/9.png)

- sources/services/app/utils/_init _.py

![](img/10.png)

We have to visit the page http://127.0.0.1:20010/api/card/{profile_id} with valid JWT token, try it:

![](img/11.png)

Ok, the task flag will be located /api/card/{profile_id} on the teacher’s page. When run docker-compose then create text-database:

- ./sources/services/app/database/profiles.txt

![](img/12.png)

Try look for vulnerabilities in the source code. And finding interesting code who have validation inpuy with regexp:

- ./sources/services/app/schemas/__init__.py:

![](img/13.png)

- ./sources/services/app/utils/__init__.py

![](img/14.png)


Okey, try bypass authication:

```
$ htpasswd -bnBC 10 "" YOUR_PASSWORD | tr -d ':\n'

> $2y$10$4VJm4VXOBGS26TW7aUAZm.j8lVn56lkRu9U4O6YKK4ctmArbcBRrW
```

Inject parameters into the description field to create exploit user:

![](img/15.png)

Then login use creads of exploit user and get access_token with profile_id:

![](img/16.png)

Okey get flag:

![](img/17.png)