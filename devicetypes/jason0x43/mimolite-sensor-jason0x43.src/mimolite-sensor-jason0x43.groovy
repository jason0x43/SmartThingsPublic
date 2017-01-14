/**
 *  MIMOlite Sensor
 *
 *  Copyright 2015 Jason Cheatham
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 metadata {
    definition(name: 'MIMOlite Sensor (jason0x43)', namespace: 'jason0x43', author: 'Jason Cheatham') {
        capability('Contact Sensor')
		capability('Refresh')
		capability('Configuration')
	}

    simulator {
        // reply messages
        reply('2001FF,delay 100,2502': 'command: 2503, payload: FF')
		reply('200100,delay 100,2502': 'command: 2503, payload: 00')

		// status messages
		status('open':  'command: 2001, payload: FF')
		status('closed': 'command: 2001, payload: 00')
    }

    tiles {
        standardTile('contact', 'device.contact', inactiveLabel: false) {
            state(
				name: 'open',
				label: 'Active',
				icon: 'st.security.alarm.alarm',
				backgroundColor: '#ffa81e'
			)
			state(
				name: 'closed',
				label: 'Inactive',
				icon: 'st.security.alarm.clear'
			)
        }

		standardTile('refresh', 'device.switch', inactiveLabel: false, decoration: 'flat') {
			state(name: 'default', label:'', action:'refresh.refresh', icon: 'st.secondary.refresh')
		}

		standardTile('configure', 'device.configure', inactiveLabel: false, decoration: 'flat') {
			state(
				name: 'configure',
				label:'',
				action:'configuration.configure',
				icon:'st.secondary.configure'
			 )
		}

        main([ 'contact' ])

		details([ 'contact', 'refresh', 'configure' ])
    }
}

def parse(String description) {
    def result = null
	def cmd = zwave.parse(description, [ 0x20: 1, 0x84: 1, 0x30: 1, 0x70: 1, 0x31: 3, 0x71: 1 ])
	if (cmd) {
		result = zwaveEvent(cmd)
		log.debug("parse cmd: '${cmd}' to result: '${result.inspect()}'")
	}
	else {
		log.debug("parse failed for event: '${description}'")
	}
    result
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
    log.debug("zwaveEvent SensorBinaryReport: '${cmd}'")
	sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	// This is the event that gets sent for simluated messages and for
	// association group 1
    log.debug("zwaveEvent BasicSet: '${cmd}'")
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    log.debug("zwaveEvent ConfigurationReport: '${cmd}'")
	[:]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.debug("zwaveEvent Command not Handled: '${cmd}'")
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

private sensorValueEvent(Short value) {
	if (value) {
		log.debug("Open sensor value event: $value")
		createEvent(name: 'contact', value: 'open', descriptionText: "$device.displayName is open", isStateChange: true )
	}
	else {
		log.debug("Closed sensor value event: $value")
		createEvent(name: 'contact', value: 'closed', descriptionText: "$device.displayName is closed", isStateChange: true)
	}
}

def refresh() {
    log.debug('executing "refresh"')
	delayBetween([
		zwave.configurationV1.configurationGet(parameterNumber:3).format(),
		zwave.configurationV1.configurationGet(parameterNumber:4).format(),
		zwave.configurationV1.configurationGet(parameterNumber:5).format(),
		zwave.configurationV1.configurationGet(parameterNumber:6).format(),
		zwave.configurationV1.configurationGet(parameterNumber:7).format(),
		zwave.configurationV1.configurationGet(parameterNumber:8).format(),
		zwave.configurationV1.configurationGet(parameterNumber:9).format(),
		zwave.configurationV1.configurationGet(parameterNumber:11).format(),
		zwave.associationV1.associationGet(groupingIdentifier:1).format(),
		zwave.associationV1.associationGet(groupingIdentifier:2).format(),
		zwave.associationV1.associationGet(groupingIdentifier:3).format(),
		zwave.associationV1.associationGet(groupingIdentifier:4).format(),
		zwave.associationV1.associationGet(groupingIdentifier:5).format(),
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.alarmV1.alarmGet(alarmType:8).format(),
		zwave.sensorMultilevelV3.sensorMultilevelGet().format()
	], 100)
}

// handle commands
def configure() {
	log.debug "executing 'configure'"

	// Voltage  Measurement   Config    Config (d)
	// -------------------------------------------
	//  8.000                 11000101     197
	//  7.000                 11000001     193
	//  4.000      2741       10101011     171
	//  5.000      2892       10110100     180
	//  0.500       631       00100111      39
	//  2.000      2062       10000000     128
	// 24.000                 11101000     232
	// 23.000                 11100111     231
	//  1.500      1687       01101001     105
	//  1.250      1433       01011001      89
	//  1.125      1306       01010001      81
	//  1.000      1179       01001001      73

	def cmd = delayBetween([
		// clear pulse meter counts
		zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, configurationValue: [1]).format(),

		// sig 1 triggers relay
		zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, configurationValue: [1]).format(),

		// lower threshold, high
		zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, configurationValue: [100]).format(),

		// lower threshold, low
		zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, configurationValue: [39]).format(),

		// upper threshold, high
		zwave.configurationV1.configurationSet(parameterNumber: 6, size: 1, configurationValue: [232]).format(),

		// upper threshold, low
		zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1, configurationValue: [231]).format(),

		// set to analog, below bounds
		zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, configurationValue: [1]).format(),

		// disable periodic reports
		zwave.configurationV1.configurationSet(parameterNumber: 9, size: 1, configurationValue: [255]).format(),

		// momentary relay
		zwave.configurationV1.configurationSet(parameterNumber: 11, size: 1, configurationValue: [0]).format(),

		//subscribe to basic sets on sig1
		zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId).format(),

		//subscribe to basic multisensorreports
		zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId).format(),

		//subscribe to power alarm
		zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:zwaveHubNodeId).format(),

		//unsubscribe from binary sensor reports
		zwave.associationV1.associationRemove(groupingIdentifier:4, nodeId:zwaveHubNodeId).format(),

		//unsubscribe from pulse meter events
		zwave.associationV1.associationRemove(groupingIdentifier:5, nodeId:zwaveHubNodeId).format()
	], 100)

	// associationRemove
}

// vim:ts=4:noet: