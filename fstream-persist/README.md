fStream - Persist
===

This is the fStream persistence module. It can write to HBase or InfluxDB as a backend. To configure a backend, edit `spring.profiles.active` in `application.yml` to be `hbase` or `influxdb`

If `influxdb` is chosen, it requires a running instance which can be installed by following http://influxdb.com/docs/v0.8/introduction/installation.html. 

## Examples

### Request
`curl -G 'http://localhost:8086/db/ticks/series?u=root&p=root' --data-urlencode "q=select * from /EUR\/USD/ limit 5"`

### Result
`[{"name":"EUR/USD","columns":["time","sequence_number","ask","bid"],"points":[[1429370479773,7850001,1.44248,1.31491],[1429370479436,7810001,1.4221494,1.2945795],[1429370479126,7780001,1.4200882,1.2925183],[1429370477615,7760001,1.4191265,1.2915566],[1429370476959,7680001,1.3409355,1.2133656]]}]`
