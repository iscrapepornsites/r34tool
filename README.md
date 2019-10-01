# r34tool
A self written scraper / utility tool for a particular site with NSFW content

# Note
You have to change the credentials to the database in `me.mux.aps.mongo.MongoAdapter` (mainly the Host)

# Compiling
    mvn package

# Usage
### `java -jar <yourfile>.jar [wait_time] watch [timeout]`
Watches the page for new updates (waits $wait_time between requests, and $timeout between checks if there are new posts)
*Also:* this can be killed, and restarted at any time, it saves its state to the database, and can be resumed at any time.

e.g. `java -jar r34.jar 100 watch 60000`

This checks every minute for new posts, then downloads these posts with a 100ms delay between requests (to avoid ratelimit)

---
### `java -jar <yourfile>.jar dumpsplit [folder] [interval]`
Exports ALL files from GridFS into folders, always containing $interval files. Folder names are create by taking $folder and appending `_i` to the folder, where `i` is an ascending number.

e.g. `java -jar r34.jar dumpsplit /home/user/dump 10000`
produces folders like: `/home/user/dump_0` up to `/home/user/dump_n`, where `n` is the total number of posts divided by `10000`

---
### `java -jar <yourfile>.jar dump [folder] [tags...]`
Dumps all files matching ALL given tags. Tags should be given in a space seperated list. **warning: this folder might get a bit crowded**

e.g. `java -jar r34.jar output/ animated long_video`

---
### Dumpzip, Dumpallzip
This is currently broken, the zip file you get from it is not valid

---
### `java -jar <yourfile>.jar drop`
Drops the mongodb collections (Meta AND Images/Videos) 

**THIS DOES NOT ASK FOR CONFIRMATION, IT NUKES YOUR DATA IN SECONDS**
