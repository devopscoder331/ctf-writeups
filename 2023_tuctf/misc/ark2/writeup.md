### A.R.K. 2

Level: Easy

Woof woof bark bark (Note: to speed up the process, only include entries containing "dog" in your attempts)

### Solution

We have file that name "woof" and recognizing the type of data contained in a computer file.

```
$ file woof

woof: Keepass password database 2.x KDBX
```

When need hash file using the keepass2john Python tool.

```
$ keepass2john woof > woof.hash
```
We have a hint "Note: to speed up the process, only include entries containing "dog" in your attempts". 

```
$ grep -r -i dog rockyou.txt > dog.txt
```

Crack the hash of the keepass database (woof.hash) to determine its passphrase using John the Ripper.

```
$ john woof.hash -wordlist=./dog.txt --session=session_dog

Using default input encoding: UTF-8
Loaded 1 password hash (KeePass [SHA256 AES 32/64])
Cost 1 (iteration count) is 60000 for all loaded hashes
Cost 2 (version) is 2 for all loaded hashes
Cost 3 (algorithm [0=AES 1=TwoFish 2=ChaCha]) is 0 for all loaded hashes
Will run 20 OpenMP threads
Press 'q' or Ctrl-C to abort, almost any other key for status
wholetthedogsout (woof)     
1g 0:00:00:01 DONE (2023-12-07 21:40) 0.8849g/s 920.3p/s 920.3c/s 920.3C/s baldog..hot dog
Use the "--show" option to display all of the cracked passwords reliably
Session completed.
```

Get flag **TUCTF{wholetthedogsout}**