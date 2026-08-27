### Simulated Orientation Sensor

Sensor driver providing values for true-north orientation (azimuth angle), while
also providing a configurable sensor location set to update every 1 second.

By default, the driver generates a random azimuth each second. Set the
`manualControl` configuration option to `true` to expose the
`orientationControl` command stream. Commands accept an azimuth from 0 through
360 degrees, and the observation output repeats that fixed value until another
command is received.
