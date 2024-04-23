### A.R.K. 4

Level: Easy

What does the fox say?

### Solution

We have file that name "fox" and recognizing the type of data contained in a computer file.

```
$ file fox 
fox: Zip archive data, at least v1.0 to extract, compression method=store
```

```
$ mv fox fox.zip && unzip fox.zi
```

```
ls -l fox/

Archive:  fox.zip
   creating: fox/
  inflating: fox/webappsstore.sqlite  
  inflating: fox/favicons.sqlite     
   creating: fox/sessionstore-backups/
  inflating: fox/sessionstore-backups/recovery.baklz4  
  inflating: fox/sessionstore-backups/recovery.jsonlz4  
  inflating: fox/search.json.mozlz4  
  inflating: fox/compatibility.ini   
  inflating: fox/addonStartup.json.lz4  
  inflating: fox/broadcast-listeners.json  
  inflating: fox/sessionCheckpoints.json  
  inflating: fox/places.sqlite       
   creating: fox/gmp-gmpopenh264/
   creating: fox/gmp-gmpopenh264/2.3.2/
  inflating: fox/gmp-gmpopenh264/2.3.2/libgmpopenh264.so  
  inflating: fox/gmp-gmpopenh264/2.3.2/gmpopenh264.info  
  inflating: fox/cookies.sqlite-wal  
 extracting: fox/shield-preference-experiments.json  
   creating: fox/storage/
  inflating: fox/storage/ls-archive.sqlite  
   creating: fox/storage/permanent/
   creating: fox/storage/permanent/chrome/
   creating: fox/storage/permanent/chrome/idb/
  inflating: fox/storage/permanent/chrome/idb/2918063365piupsah.sqlite  
  inflating: fox/storage/permanent/chrome/idb/2823318777ntouromlalnodry--naod.sqlite  
  inflating: fox/storage/permanent/chrome/idb/1451318868ntouromlalnodry--epcr.sqlite  
  inflating: fox/storage/permanent/chrome/idb/3561288849sdhlie.sqlite  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.sqlite  
  inflating: fox/storage/permanent/chrome/idb/1657114595AmcateirvtiSty.sqlite  
   creating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/31  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/11  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/43  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/2  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/39  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/33  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/18  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/22  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/9  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/7  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/44  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/15  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/40  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/3  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/35  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/26  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/32  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/36  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/23  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/24  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/34  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/12  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/6  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/8  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/19  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/4  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/45  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/28  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/1  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/10  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/42  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/37  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/20  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/29  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/5  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/27  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/30  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/21  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/41  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/25  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/13  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/17  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/14  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/38  
  inflating: fox/storage/permanent/chrome/idb/3870112724rsegmnoittet-es.files/16  
   creating: fox/storage/default/
   creating: fox/storage/default/https+++accounts.google.com/
   creating: fox/storage/default/https+++accounts.google.com/ls/
  inflating: fox/storage/default/https+++accounts.google.com/ls/data.sqlite  
  inflating: fox/storage/default/https+++accounts.google.com/ls/usage  
   creating: fox/storage/default/https+++www.google.com/
   creating: fox/storage/default/https+++www.google.com/ls/
  inflating: fox/storage/default/https+++www.google.com/ls/data.sqlite  
  inflating: fox/storage/default/https+++www.google.com/ls/usage  
  inflating: fox/containers.json     
  inflating: fox/pkcs11.txt          
  inflating: fox/SiteSecurityServiceState.txt  
  inflating: fox/protections.sqlite  
  inflating: fox/extensions.json     
 extracting: fox/addons.json         
   creating: fox/crashes/
 extracting: fox/crashes/store.json.mozlz4  
  inflating: fox/permissions.sqlite  
  inflating: fox/times.json          
  inflating: fox/logins-backup.json  
   creating: fox/datareporting/
   creating: fox/datareporting/glean/
   creating: fox/datareporting/glean/db/
  inflating: fox/datareporting/glean/db/data.safe.bin  
  inflating: fox/datareporting/state.json  
  inflating: fox/datareporting/aborted-session-ping  
  inflating: fox/datareporting/session-state.json  
  inflating: fox/formhistory.sqlite  
  inflating: fox/cert9.db            
  inflating: fox/storage.sqlite      
  inflating: fox/AlternateServices.txt  
   creating: fox/settings/
  inflating: fox/settings/data.safe.bin  
  inflating: fox/handlers.json       
  inflating: fox/favicons.sqlite-wal  
  inflating: fox/key4.db             
  inflating: fox/extension-preferences.json  
  inflating: fox/logins.json         
  inflating: fox/places.sqlite-wal   
  inflating: fox/cookies.sqlite      
  inflating: fox/prefs.js            
  inflating: fox/content-prefs.sqlite  
  inflating: fox/xulstore.json       
```

```
$ curl https://raw.githubusercontent.com/unode/firefox_decrypt/main/firefox_decrypt.py -O
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 39242  100 39242    0     0   127k      0 --:--:-- --:--:-- --:--:--  127k
```


```
$ python3 firefox_decrypt.py fox/
2023-12-07 22:04:04,852 - WARNING - profile.ini not found in fox/
2023-12-07 22:04:04,853 - WARNING - Continuing and assuming 'fox/' is a profile location

Website:   https://www.example.com
Username: 'fox'
Password: 'TUCTF{B3w4R3_7h3_f1r3_4nd_7h3_f0x}'
```
