/**
 *  Smart Nest
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
    name:        'Smart Nest',
    namespace:   'jason0x43',
    author:      'Jason Cheatham',
    description: 'An application allowing SmartThings to manage a Nest thermostat.',
    category:    'Green Living',
    iconUrl:     'https://dl.dropboxusercontent.com/s/2vye4fpid4sp15k/Nest-4.0-for-iOS-app-icon-small.png',
    iconX2Url:   'https://dl.dropboxusercontent.com/s/2vye4fpid4sp15k/Nest-4.0-for-iOS-app-icon-small.png',
    iconX3Url:   'https://dl.dropboxusercontent.com/s/2vye4fpid4sp15k/Nest-4.0-for-iOS-app-icon-small.png'
)

preferences {
	section('Manage this Nest') {
		input(name: 'nest', type: 'capability.thermostat', required: true)
	}
	section('Set mode to "Away" when all of these people leave home and "Home" when any are at home') {
		input(name: 'people', type: 'capability.presenceSensor', multiple: true)
	}
	section('Poll the Nest for updates at this interval (default is 5 minutes)') {
		input(name: 'interval', type: 'number', required: false)
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
    subscribe(people, 'presence', handlePresence)

	def interval = this['interval'] ?: 5
	if (interval > 0) {
	    log.debug("Scheduling update for every ${interval} minutes")
		schedule("0 0/${interval} * * * ?", poll)
	}

    handlePresence(null)
}

def handlePresence(event) {
	log.debug('Handling presence event')
	if (people.find { it.currentPresence == 'present' } == null) {
        log.debug('Nobody home')
        log.debug("Current status: ${nest.currentValue('presence')}")
    	if (nest.currentValue('presence') == 'present') {
        	log.debug('Setting nest to "away"')
            nest.away()
		}
	}
    else {
    	log.debug('Someone is present')
        log.debug("Current status: ${nest.currentValue('presence')}")
        if (nest.currentValue('presence') == 'away') {
        	log.debug 'Setting nest to "present"'
    		nest.present()
		}
    }
}

def poll() {
	log.debug('polling...')
	nest.poll()
}
