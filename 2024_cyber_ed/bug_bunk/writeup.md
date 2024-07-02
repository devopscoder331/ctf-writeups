### Bug Bunk
Level: Easy

I wonder what Elliot's code word is?

Task on IDOR in an imaginary Bug Bank, based on a real bug bounty report, from the BI.ZONE Bug Bounty platform

### Solution

See standart start page, when there is the registration form. 

![](img/01.png)

Register the user and study source code, scripts and interested endpoints. Find an interesting script that reveals env:

```
env: {"PUBLIC_BACKEND_PORT":"8000","PUBLIC_FRONTEND_PORT":"80","PUBLIC_BACKEND_URL":"http://bug-bank.cyber-ed.space:8000"}
```

![](img/02.png)

Find an interesting page, when trying to get the code.

![](img/03.png)

Look at the request and response, we see the api address and your uid.

![](img/04.png)

We find a page with reviews where there are several contacts:

![](img/05.png)

We're trying to send Eliot a few credits:

![](img/06.png)

Look at the request and response, we see Eliot's uid:

![](img/07.png)

Okey, what if using Eliot's uid to open the page /api/auth/user/{ eliot euid}:

![](img/08.png)

Found IDOR vulnerability and get flag.