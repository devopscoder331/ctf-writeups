### A.R.K. 3

Level: Easy


Meowdy. (Note: to speed up the process, only include entries containing "meow" in your attempts)

### Solution

We have file that name "meow" and recognizing the type of data contained in a computer file.

```
$  file meow
meow: Mac OS X Keychain File
```

Then need hash file using the keychain2john tool:

```
$ keychain2john meow > meow.hash
```

We have a hint "Note: to speed up the process, only include entries containing "meow" in your attempts". 

```
$ grep -r -i meow rockyou.txt | sort -u > meow.txt
```

Crack the hash of keychain (meow.hash) to determine its passphrase using John the Ripper.

```
john -format:keychain meow.hash --wordlist=./meow.txt --session=session_meow
```

