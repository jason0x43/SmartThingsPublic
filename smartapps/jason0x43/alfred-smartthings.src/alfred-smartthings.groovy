/**
 *  Copyright 2014 Jason Cheatham
 *  Based on the "Alfred Workflow" smart app by SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 *
 *  Alfred Workflow
 *
 *  Author: Jason Cheatham
 *
 */
definition(
	name:        'Alfred-SmartThings',
	namespace:   'jason0x43',
	author:      'Jason Cheatham',
	description: 'A SmartApp allowing Alfred to interact with SmartThings',
	category:    'Convenience',
	iconUrl:     'https://s3.amazonaws.com/smartapp-icons/Partner/alfred-app.png',
	iconX2Url:   'https://s3.amazonaws.com/smartapp-icons/Partner/alfred-app@2x.png',
	iconX3Url:   'https://s3.amazonaws.com/smartapp-icons/Partner/alfred-app@2x.png',
	oauth: [
		displayName: 'Alfred-SmartThings',
		displayLink: 'https://github.com/jason0x43/alfred-smartThings'
	]
)


preferences {
	section('Allow Alfred to Control These Things...') {
		input(
			name: 'hubs',
			type: 'hub',
			title: 'Which hubs?',
			multiple: true,
			required: false
		)
		input(
			name: 'switches',
			type: 'capability.switch',
			title: 'Which switches?',
			multiple: true,
			required: false
		)
		input(
			name: 'locks',
			type: 'capability.lock',
			title: 'Which locks?',
			multiple: true,
			required: false
		)
	}
}

mappings {
	path('/hubs') {
		action: [
			GET: 'listHubs',
			PUT: 'updateHubs'
		]
	}
	path('/hubs/:id') {
		action: [
			GET: 'showHub',
			PUT: 'updateHub'
		]
	}
    path('/switches') {
		action: [
			GET: 'listSwitches',
			PUT: 'updateSwitches'
		]
    }
    path('/switches/:id') {
		action: [
			GET: 'showSwitch',
			PUT: 'updateSwitch'
		]
    }
    path('/locks') {
		action: [
			GET: 'listLocks',
			PUT: 'updateLocks'
		]
    }
    path('/locks/:id') {
		action: [
			GET: 'showLock',
			PUT: 'updateLock'
		]
    }
}

def installed() {}

def updated() {}

// device handlers //////////////////////////////////////

def listHubs() {
	list(hubs, 'hub')
}

void updateHubs() {
	updateAll(hubs)
}

def showHub() {
	show(hubs, 'hub')
}

void updateHub() {
	update(hubs)
}

def listSwitches() {
	list(switches, 'switch')
}
void updateSwitches() {
	updateAll(switches)
}
def showSwitch() {
	show(switches, 'switch')
}
void updateSwitch() {
	update(switches)
}

def listLocks() {
	list(locks, 'lock')
}
void updateLocks() {
	updateAll(locks)
}
def showLock() {
	show(locks, 'lock')
}
void updateLock() {
	update(locks)
}

// helpers //////////////////////////////////////////////

private void updateAll(devices) {
	def command = request.JSON?.command
	def type = params.param1
	if (!devices) {
		httpError(404, 'Devices not found')
	}

	if (command) {
		devices.each { device ->
			executeCommand(device, type, command)
		}
	}
}

private void update(devices) {
	log.debug("update, request: ${request.JSON}, params: ${params}, devices: $devices.id")
	def command = request.JSON?.command
	def type = params.param1
	def device = devices?.find { it.id == params.id }

	if (!device) {
		httpError(404, 'Device not found')
	}

	if (command) {
		executeCommand(device, type, command)
	}
}

/**
 * Validating the command passed by the user based on capability.
 * @return boolean
 */
def validateCommand(device, deviceType, command) {
	def capabilityCommands = getDeviceCapabilityCommands(device.capabilities)
	def currentDeviceCapability = getCapabilityName(deviceType)
	if (capabilityCommands[currentDeviceCapability]) {
		return command in capabilityCommands[currentDeviceCapability] ? true : false
	} else {
		// Handling other device types here, which don't accept commands
		httpError(400, 'Bad request.')
	}
}

/**
 * Need to get the attribute name to do the lookup. Only
 * doing it for the device types which accept commands
 * @return attribute name of the device type
 */
def getCapabilityName(type) {
    switch(type) {
		case 'switches':
			return 'Switch'
		case 'locks':
			return 'Lock'
		default:
			return type
	}
}

/**
 * Constructing the map over here of
 * supported commands by device capability
 * @return a map of device capability -> supported commands
 */
def getDeviceCapabilityCommands(deviceCapabilities) {
	def map = [:]
	deviceCapabilities.collect {
		map[it.name] = it.commands.collect{ it.name.toString() }
	}
	return map
}

/**
 * Return a list of all devices of a given type 
 */
private list(devices, name) {
	devices.collect { device(it, name) }
}

/**
 * Validates and executes the command
 * on the device or devices
 */
def executeCommand(device, type, command) {
	if (validateCommand(device, type, command)) {
		device.'$command'()
	} else {
		httpError(403, 'Access denied. This command is not supported by current capability.')
	}	
}

private show(devices, name) {
	def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, 'Device not found')
	}
	else {
		device(dev, name)
	}
}

private device(it, name) {
	if (it) {
		try {
			// regular device
			def s = it.currentState(name)
			[ id: it.id, name: it.displayName, state: s ]
		}
		catch (e) {
			// hub
			[ id: it.id, name: it.name ]
		}
	}
}

// vim:ts=4
