# Using the files in the torrent
#### I recommend that you run all of these processes in a `screen` or `tmux` session, so you can quit out of the console

#### Check the hashes of all the Files together (optional, and really slow [~1,5h on my 8core server with regular HDDs]):
```bash
cat rule34.tgz.part_?? | pv -s $(du -sb . | awk '{print $1}') | sha1sum
```
This must result in `bb587c1c5fa6bbd579b59703cd941228a65e09b7`

#### Unzip all the files (This needs a LOT  of space [~2.2TB with the raw files], CPU, time [~6h] and some installed packages)
```bash
mkdir mongodb; \
cat rule34.tgz.part_?? \
| pv -s $(du -sb . | awk '{print $1}') \
| pigz -dc - \
| tar -xf - --strip-components 3 -C mongodb
```

### Error Handling

#### If `pigz` is not found
You must install the pigz package on your system, with either `yum` or `apt`

#### If `pv` is not found
You must install the pv package on your system, with either `yum` or `apt`

#### Any other error
Please dont hesistate to open an issue

# Hosting a mongodb database with docker:

```bash
chown -R 117:117 <path to files>
chmod -R 770 <path to files>
docker pull mongo:4.2.1-bionic
docker run -d -p 27017:27017 -v <path to files>:/data/db mongo:4.2.1-bionic
```

# Querying the database
```bash
mongo --host localhost --port 27017
> use aps
> db.posts.count({})
3152187
> db.fs.files.count({})
1904217
> db.fs.chunks.count({})
10363477
```

#### If any of the counts are 0, you might have permission problems.

But I don't know how to fix them, sorry. Just try `chmod -R 777 <path to files>`
