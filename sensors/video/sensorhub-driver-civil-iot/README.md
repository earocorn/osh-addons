# F20 Platform

Sensor adapter for the F20 platform, otherwise known as the [Wintec WW-5H2x](https://www.win-tec.com.tw/portfolio-item/ww-5h2x/).

## Configuration

Configuring the sensor requires:
Select ```Sensors``` from the left hand accordion control and right click for context sensitive menu in accordion control
- **Module Name:** A name for the instance of the driver
- **Serial Number:** The platforms serial number, or a unique identifier
- **Auto Start:** Check the box to start this module when OSH node is launched
**Datastream IDs**
Note: If you want to specify certain Datastreams from the SensorThings API, you may add them under the **Datastream IDs** configuration.
This configuration allows you to specify the SensorThings Datastream ID, along with the rate to poll images from the Datastream.

If you would rather use a generic range of Datastream IDs (recommended if you don't know specific Datastreams), then you can specify a start and end for this range,
as well as a general poll interval (in minutes) to use for polling all Datastreams. 
It is recommended to use a poll interval of no more than 10 minutes. 
- **Datastream ID Start:** Beginning ID to use (default is 59)
- **Datastream ID End:** Ending ID to use (default is 130)
- **General Poll Interval (min):** Poll interval in minutes

## Starting Driver
It is possible that you will encounter errors regarding SSL when starting the driver.
If this is the case, then you will need to create a trust store and add a JVM argument to use the trust store when running a node.

This trust store will need up-to-date certificates from the following providers:

- `https://sta.ci.taiwan.gov.tw`
- `https://iapi.wra.gov.tw`
- `https://airtw.moenv.gov.tw`

Go to the above websites and download their SSL certificates.
Then, you can create a trust store by performing the following command on each of the certificates.

```shell
keytool -importcert -file <SSL CERTIFICATE FILE> -keystore truststore.jks -alias <ANY NAME FOR CERTIFICATE>
```

Then, you'll use this trust store and specified password (default is `changeit`) when launching the OSH node.

Example JVM arguments shown below (include these in `launch.sh` or `launch.bat`)
```shell
-Djavax.net.ssl.trustStore=/path/to/truststore.jks
-Djavax.net.ssl.trustStorePassword=changeit
```