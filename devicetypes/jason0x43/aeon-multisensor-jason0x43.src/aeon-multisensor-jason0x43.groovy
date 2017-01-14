metadata {
	definition(name: 'Aeon MultiSensor (jason0x43)', namespace: 'jason0x43', author: 'Jason Cheatham') {
		capability('Motion Sensor')
	    capability('Temperature Measurement')
		capability('Relative Humidity Measurement')
		capability('Configuration')
		capability('Illuminance Measurement')
		capability('Sensor')
		capability('Battery')

		fingerprint(deviceId: '0x2001', inClusters: '0x30,0x31,0x80,0x84,0x70,0x85,0x72,0x86')
	}

	simulator {
		// messages the device returns in response to commands it receives
		status('motion (basic)': 'command: 2001, payload: FF')
		status('no motion (basic)': 'command: 2001, payload: 00')
		status('motion (binary)': 'command: 3003, payload: FF')
		status('no motion (binary)': 'command: 3003, payload: 00')

		for (int i = 0; i <= 100; i += 20) {
			status('temperature ${i}F': new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				sensorType: 1,
				scaledSensorValue: i,
				precision: 1,
				scale: 1
			).incomingMessage())
		}

		for (int i = 0; i <= 100; i += 20) {
			status('humidity ${i}%': new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				sensorType: 5,
				scaledSensorValue: i,
				precision: 0
			).incomingMessage())
		}

		for (int i = 0; i <= 100; i += 20) {
			status('luminance ${i} lux': new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				sensorType: 3,
				scaledSensorValue: i,
				precision: 0
			).incomingMessage())
		}
		for (int i = 200; i <= 1000; i += 200) {
			status('luminance ${i} lux': new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				sensorType: 3,
				scaledSensorValue: i,
				precision: 0
			).incomingMessage())
		}
		for (int i = 0; i <= 100; i += 20) {
			status('battery ${i}%': new physicalgraph.zwave.Zwave().batteryV1.batteryReport(
				batteryLevel: i
			).incomingMessage())
		}
	}

	tiles {
		standardTile('motion', 'device.motion', width: 2, height: 2) {
			state(
				name: 'active',
				label:'motion',
				icon:'st.motion.motion.active',
				backgroundColor:'#53a7c0'
			)
			state(
				name: 'inactive',
				label:'no motion',
				icon:'st.motion.motion.inactive',
				backgroundColor:'#ffffff'
			)
		}

		valueTile('temperature', 'device.temperature', inactiveLabel: false) {
			state(name: 'temperature', label:'${currentValue}°',)
			backgroundColors: [
				[value: 31, color: '#153591'],
				[value: 44, color: '#1e9cbb'],
				[value: 59, color: '#90d2a7'],
				[value: 74, color: '#44b621'],
				[value: 84, color: '#f1d801'],
				[value: 95, color: '#d04e00'],
				[value: 96, color: '#bc2323']
			]
		}
		valueTile('humidity', 'device.humidity', inactiveLabel: false) {
			state(name: 'humidity', label:'${currentValue}% humidity', unit:'')
		}
		valueTile('illuminance', 'device.illuminance', inactiveLabel: false) {
			state(name: 'luminosity', label:'${currentValue} ${unit}', unit:'lux')
		}
		valueTile('battery', 'device.battery', inactiveLabel: false, decoration: 'flat') {
			state(name: 'battery', label:'${currentValue}% battery', unit:'')
		}
		standardTile('configure', 'device.configure', inactiveLabel: false, decoration: 'flat') {
			state(
					name: 'configure',
					label:'',
					action:'configuration.configure',
					icon:'st.secondary.configure'
				 )
		}

		main(['motion', 'temperature', 'humidity', 'illuminance'])
		details(['motion', 'temperature', 'humidity', 'illuminance', 'battery', 'configure'])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.trace("Parsing '${description}'")
	def result = []
	def cmd = zwave.parse(description, [0x31: 2, 0x30: 1, 0x84: 1])
	if (cmd) {
		if (cmd.CMD == '8407') {
        	result << new physicalgraph.device.HubAction(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
        }
		result << createEvent(zwaveEvent(cmd))
	}
	log.trace("Parsed message: ${result}")
	return result
}

// Event Generation //////////////////////////////////////////////////////////

// Battery powered devices can be configured to periodically wake up and check in. They send this
// command and stay awake long enough to receive commands, or until they get a WakeUpNoMoreInformation
// command that instructs them that there are no more commands to receive and they can stop listening
def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	log.debug('Received wake up notification')
	[descriptionText: "${device.displayName} woke up", isStateChange: false]
}

// Sent when a value from a multi-level sensor, like temp or luminance, changes
def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd) {
	log.debug("Received multi-level report for ${cmd.sensorType}")
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			// temperature
			def cmdScale = cmd.scale == 1 ? 'F' : 'C'
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			map.name = 'temperature'
			break;
		case 3:
			// luminance
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = 'lux'
			map.name = 'illuminance'
			break;
		case 5:
			// humidity
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = '%'
			map.name = 'humidity'
			break;
	}
	map
}

// Battery state event
def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	log.debug("Received battery report: ${cmd.batteryLevel}")
	def map = [:]
	map.name = 'battery'
	map.value = cmd.batteryLevel == 0 ? '1' : cmd.batteryLevel.toString()
	map.unit = '%'
    map.displayed = false
	map
}

// Report on binary events; for this sensor, that's 'motion on' or 'motion off'
def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
	log.debug("Received sensor binary report: ${cmd.sensorValue}")
	def map = [:]
	map.value = cmd.sensorValue ? 'active' : 'inactive'
	map.name = 'motion'
	if (map.value == 'active') {
		map.descriptionText = "$device.displayName detected motion"
	}
	else {
		map.descriptionText = "$device.displayName motion has stopped"
	}
	map
}

// Many sensors send BasicSet commands to associated devices. This is so you can associate them with
// a switch-type device and they can directly turn it on/off when the sensor is triggered.
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	log.debug("Received sensor basic set report: ${cmd.sensorValue}")
	def map = [:]
	map.value = cmd.value ? 'active' : 'inactive'
	map.name = 'motion'
	if (map.value == 'active') {
		map.descriptionText = "$device.displayName detected motion"
	}
	else {
		map.descriptionText = "$device.displayName motion has stopped"
	}
	map
}

// A catchall handler for otherwise unhandled events
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.trace("Catchall reached for cmd: ${cmd.toString()}")
	[:]
}

def configure() {
	log.debug('Sending configuration...')
	delayBetween([
		// send 'binary sensor report' command instead of 'basic set' command for motion
		zwave.configurationV1.configurationSet(
			parameterNumber: 5,            // which command to send on PIR motion
			size: 1,
			scaledConfigurationValue: 2    // "Sensor Binary report" enum value
		).format(),

		// send no-motion report 15 seconds after motion stops
		zwave.configurationV1.configurationSet(
			parameterNumber: 3,            // timeout period of no-motion before sending OFF state
			size: 2,
			scaledConfigurationValue: 15   // seconds
		).format(),

		// include all data (temperature, humidity, illuminance & battery) in reporting group 1
		zwave.configurationV1.configurationSet(
			parameterNumber: 101,          // which reports to send automatically in group 1
			size: 4,
			scaledConfigurationValue: 225  // 0x000000E1 -- [x][x][x][111xxxx1]
		).format(),

		// send reporting group 1 data every 5 minutes
		zwave.configurationV1.configurationSet(
			parameterNumber: 111,          // interval for sending group 1
			size: 4,
			scaledConfigurationValue: 30  // 0x0000012C
		).format()
	], 250)
}

// vim:ts=4:noet