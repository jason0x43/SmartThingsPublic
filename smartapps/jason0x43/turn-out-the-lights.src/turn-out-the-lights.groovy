/**
 *  Turn Out the Lights
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
    name:        'Turn Out the Lights',
    namespace:   'jason0x43',
    author:      'Jason Cheatham',
    description: 'Turn off lights or other switches when everyone leaves.',
    category:    'Green Living',
    iconUrl:     'https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet.png',
    iconX2Url:   'https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet@2x.png',
    iconX3Url:   'https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet@2x.png'
)

preferences {
	section('Manage these switches') {
		input(name: 'switches', type: 'capability.switch', multiple: true, required: true)
	}
	section('Turn off when all of these people leave home') {
		input(name: 'people', type: 'capability.presenceSensor', multiple: true)
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
	handlePresence(null)
}

def handlePresence(event) {
	log.debug('Handling presence event')
	if (people.find { it.currentPresence == 'present' } == null) {
		log.debug('Nobody home')
		switches.off()
	}
}
