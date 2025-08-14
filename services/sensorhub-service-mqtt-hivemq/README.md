## MQTT Support based on HiveMQ

This module exposes common MQTT resources that can be used by other components (such as SensorThings or SensorWeb API). These resources are:

- Common MQTT API (for creating topics and connecting them to internal OSH queues)
- MQTT server
- MQTT client

The default MQTT server and client implementations provided by this module are based on HiveMQ.

### TCP
topic: /sensorhub/api/datastreams/{dsId}/observations
host address: <ip>:<port>
protocol: tcp

### WSS (https)
protocol: wss
topic: /api/datastreams/{dsId}/observations
host address: <ip>:<port>/sensorhub