/**
 *  Alfred-SmartThings
 *
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
 */
definition(
	name:        'Alfred-SmartThings',
	namespace:   'jason0x43',
	author:      'Jason Cheatham',
	description: 'An Alfred workflow for interacting with SmartThings',
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

def installed() {
	// nothing to do here
}

def updated() {
	// nothing to do here
}

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
	if (command) {
		def args = request.JSON?.args
		if (args) {
			devices."$command"(args)
		}
		else {
			devices."$command"()
		}
	}
}

private void update(devices) {
	log.debug("update, request: ${request.JSON}, params: ${params}, devices: $devices.id")
	def command = request.JSON?.command
	if (command) {
		def device = devices.find { it.id == params.id }
		if (!device) {
			httpError(404, 'Device not found')
		}
		else {
			def args = request.JSON?.args
			if (args) {
				device."$command"(args)
			}
			else if (command == "toggle") {
                if (device.currentValue('switch') == "on") {
					device.off();
                }
				else {
					device.on();
				}
			}
			else {
				device."$command"()
			}
		}
	}
}

private list(devices, name) {
	devices.collect { device(it, name) }
}

private show(devices, name) {
	def dev = devices.find { it.id == params.id }
	if (!dev) {
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
