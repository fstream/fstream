fStream - Persist
---

This is the fStream persistence module. It can write to HBase or InfluxDB as a backend. To configure a backend, edit `spring.profiles.active` in `application.yml` to be `hbase` or `influxdb`

If `influxdb` is chosen, it requires a running instance which can be installed by following http://influxdb.com/docs/v0.8/introduction/installation.html. 
