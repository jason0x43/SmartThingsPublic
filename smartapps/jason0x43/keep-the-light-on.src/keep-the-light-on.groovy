/**
 *  Keep The Light On
 *
 *  Copyright 2014 Jason Cheatham
 *
 *  Licensed under the Apache License, Version 2.0 (the 'License'); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
definition(
    name: 	 	 'Keep the Light On',
    namespace:   'jason0x43',
    author:      'Jason Cheatham',
    description: 'Keep a light on as long as motion is sensed within a room or a door is open.',
    category:    'Convenience',
    iconUrl:     'https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet.png',
    iconX2Url:   'https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet@2x.png'
)


preferences {
	section('Keep these lights on...') {
		input(
			name: 'switches',
			type: 'capability.switch',
			multiple: true
		)
	}

	section('If this door is open...') {
		input(
			name: 'contacts',
			type: 'capability.contactSensor',
			title: 'Where?',
			multiple: true
		)
	}
    section('Or if there\'s motion...') {
    	input(
			name: 'motionSensor',
			type: 'capability.motionSensor',
			title: 'Where?'
		)
    }
	section('For this many minutes after motion stops or the door closes...') {
		input(
			name: 'waitMinutes',
			type: 'number',
			title: 'Minutes (default is 3)?'
		)
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
	subscribe(motionSensor, 'motion', motionHandler)
	subscribe(contacts, 'contact', contactHandler)
}

def contactHandler(event) {
	def value = "${event.value}"
	log.debug("Contact event: ${value}")
    
	if (value == "open") {
    	log.debug("Contact opened -- unschedule turnOff")
    	unschedule 'turnoff'
	}
	else {
    	log.debug("Contact closed -- schedule turnOff")
        scheduleTurnOff()
	}
}

def motionHandler(event) {
	def value = "${event.value}"
	log.debug("Motion event: ${value}")

	if (value == "active") {
		log.debug('Saw motion -- unschedule turnoff')
		unschedule 'turnOff'
	}
	else {
    	log.debug("Motion stopped -- schedule turnOff")
        scheduleTurnOff()
	}
}

private scheduleTurnOff() {
	log.trace('scheduleTurnOff')

	if (contacts.find { "${it.currentState('contact').value}" == "open" } != null) {
		log.debug('Contact is open -- doing nothing')
		return
	}

	if ("${motionSensor.currentState('motion').value}" == "active") {
		log.debug('Motion is active -- doing nothing')
		return
	}

	def seconds = (waitMinutes == null ? 3 : waitMinutes) * 60
	log.debug("Turning off in ${seconds} seconds")
	runIn(seconds, 'turnOff')
}

def turnOff() {
    log.debug('Turning off switches')
    switches.off()
}