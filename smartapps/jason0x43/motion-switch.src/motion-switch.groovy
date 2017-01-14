/**
 *  Motion Switch
 *
 *  Copyright 2014 Jason Cheatham
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
definition(
    name:        'Motion Switch',
    namespace:   'jason0x43',
    author:      'Jason Cheatham',
    description: 'Control switches with a motion sensor.',
    category:    'Green Living',
	iconUrl:     'https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet.png',
	iconX2Url:   'https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet@2x.png',
	iconX3Url:   'https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet@3x.png'
)

preferences {
	section('Manage these switches...') {
		input(name: 'switches', type: 'capability.switch', multiple: true, required: true)
	}
	section('With this motion sensor...') {
		input(name: 'motionSensor', type: 'capability.motionSensor', required: true)
	}
	section('Minimum shutoff wait time (default is 1 minute)') {
		input(name: 'minWait', type: 'number', required: false)
	}
	section('Maximum shutoff wait time (default is 60 minutes)') {
		input(name: 'maxWait', type: 'number', required: false)
	}
}

def installed() {
	log.debug("Installed with settings: ${settings}")
	initialize()
}

def updated() {
	log.debug("Updated with settings: ${settings}")
	unsubscribe()
	initialize()
}

def initialize() {
	log.debug("Initializing with settings: ${settings}")
	state.occupancyStart = 0
	subscribe(motionSensor, 'motion.active', handleMotion)
	subscribe(motionSensor, 'motion.inactive', handleStillness)
}

def handleMotion(event) {
	log.debug('Saw motion -- turning switches on')
	if (switches.find { "${it.currentState('switch').value}" == "off" } != null) {
		log.debug('Some switches off -- starting occupancy timer')
		state.occupancyStart = new Date().time
	}
	switches.on()
	unschedule 'turnOff'
}

def handleStillness(event) {
	log.debug('Motion stopped')

	def occupancyTime = (new Date().time - state.occupancyStart) / 1000
	log.debug("Room has been occupied for ${occupancyTime} seconds")

	def minWaitSeconds = (this['minWait'] ?: 1) * 60
	def maxWaitSeconds = (this['maxWait'] ?: 60) * 60
	def waitSeconds = occupancyTime
	if (waitSeconds < minWaitSeconds) {
		waitSeconds = minWaitSeconds
	}
	else if (waitSeconds > maxWaitSeconds) {
		waitSeconds = maxWaitSeconds
	}

	log.debug("Scheduling turnoff in ${waitSeconds} seconds")
	runIn(waitSeconds, 'turnOff')
}

def turnOff() {
	log.debug('Turning off switches')
	switches.off()
}

// vim:ts=4:noet:si: