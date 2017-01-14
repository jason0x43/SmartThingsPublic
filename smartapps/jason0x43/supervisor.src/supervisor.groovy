/**
 *  Supervisor
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
 *
 */
definition(
	name:        'Supervisor',
	namespace:   'jason0x43',
	author:      'Jason Cheatham',
	description: 'Calls poll() function periodically for selected devices.',
	category:    'Convenience',
	iconUrl:     'https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png',
	iconX2Url:   'https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png'
)

preferences {
    section('About') {
		paragraph('Supervisor is an app that periodically polls devices to refresh their state.')
    }

    section('What to watch') {
		input(name: 'targets', type: 'capability.polling', title:'Select devices to be polled', multiple: true, required: true)
	    input(name: 'minutes', type: 'number', title:'Set polling interval (in minutes)', required: true)
    }
}

def poll() {
    log.debug('Polling targets...')
	targets.poll()
}

def installed() {
    log.debug('Installed...')
	initialize()
}

def updated() {
    log.debug('Updated...')
	unschedule()
	initialize()
}

private initialize() {
    log.debug("Initializing with settings: ${settings}...")
	if (minutes > 0 && targets.size() > 0) {
	    log.debug("Scheduling poller to run every ${minutes} minutes.")
		schedule("0 0/${minutes} * * * ?", poll)
	}
}
