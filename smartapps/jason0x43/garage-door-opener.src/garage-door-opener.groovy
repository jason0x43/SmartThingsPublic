/**
 *  Garage Door Opener
 *
 *  Author: Jason Cheatham
 *  Date: 2014-11-08
 *
 * Monitors arrival and departure of people and opens the door when a person
 * arrives. 
 */


// Automatically generated. Make future change here.
definition(
    name: "Garage Door Opener",
    namespace: "jason0x43",
    author: "Jason Cheatham",
    description: "Open a garage door when someone arrives home.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section('Garage door') {
		input 'doorSensor', 'capability.threeAxis', title: 'Which sensor?'
		input 'doorSwitch', 'capability.switch', title: 'Which switch?'
	}

	section('People using this garage door') {
		input 'people', 'capability.presenceSensor', title: 'Presence sensor', description: 'Who?', multiple: true, required: true
	}

	section('Interior door (optional)') {
		input 'interiorDoorSensor', 'capability.contactSensor', title: 'Contact sensor?', required: false
		input 'interiorDoorWindow', 'number', title: 'Number of minutes to check door (defaults to 10)', required: false
	}

	section('False alarm threshold') {
		input 'falseAlarmThreshold', 'number', title: 'Number of minutes (defaults to 10)', required: false
	}
}

def installed() {
	log.trace 'installed()'
	initialize()
}

def updated() {
	log.trace 'updated()'
	unsubscribe()
	initialize()
}

def initialize() {
	log.debug "initializing: ${people.collect { it.displayName + ': ' + it.currentPresence }}"
	log.debug "present: ${people.collect { it.displayName + ': ' + it.currentPresence }}"

	subscribe people, 'presence', handlePresence

	if (interiorDoorSensor) {
		subscribe interiorDoorSensor, 'contact.open', handleInteriorDoorOpened
	}
}

def handlePresence(event) {
	log.trace "$event.name: $event.value"

	// time in which there must be no 'not present' events in order to open the door
	final openDoorAwayInterval = falseAlarmThreshold != null ? falseAlarmThreshold * 60 : 600

	if (event.value == 'present') {
		def person = people.find { it.id == event.deviceId }

		// check to see if there is a 'not present' event at some point within the false alarm window
		def windowStart = new Date(now() - (openDoorAwayInterval * 1000))
        log.trace "Checking if ${person} was not present since ${windowStart}"
		def states = person.statesSince('presence', windowStart)
		def recentNotPresentState = states.find { it.value == 'not present' }
        log.trace "Contact value: ${doorSensor.currentState('contact').value}"

		if (recentNotPresentState) {
			log.debug "Not opening ${doorSwitch.displayName} since someone was not present at ${recentNotPresentState.date}"
		}
        else if (state.appOpenedDoor && now() - state.appOpenedDoor < openDoorAwayInterval) {
			log.debug "Not opening ${doorSwitch.displayName} since someone arrived recently"
        }
        else if (doorSensor.currentState('acceleration').value == 'active') {
        	log.debug "Not opening ${doorSwitch.displayName} since it's currently active"
        }
		else if (doorSensor.currentState('contact').value == 'closed') {
			log.debug 'Door is closed -- opening'
			openDoor()
			state.appOpenedDoor = now()
		}
		else {
			log.debug 'Door is not closed -- doing nothing'
		}
	}
}

def handleInteriorDoorOpened(event) {
	log.trace 'Interior door opened'

	// time during which closing the interior door will shut the garage door, if the app opened it
    final window = interiorDoorWindow == null ? 10 : interiorDoorWindow
	final threshold = window * 60 * 1000
    
	if (state.appOpenedDoor && now() - state.appOpenedDoor < threshold) {
		state.appOpenedDoor = 0
		closeDoor()
	}
	else {
		log.debug "App didn't open garage door within ${window} minutes -- doing nothing"
	}
}

private isDoorOpen() {
	log.trace 'Checking if garage door is open'
	def latestThreeAxisState = doorSensor.threeAxisState // e.g.: 0,0,-1000
	if (latestThreeAxisState) {
		// 250 should work in most cases
        log.debug "3-axis state: ${latestThreeAxisState.xyzValue}"
		return Math.abs(latestThreeAxisState.xyzValue.z) > 250
	}
	else {
		log.warn "Couldn't get latest state for ${multisensor}"
		return false
	}
}

private openDoor() {
	if (doorSensor.currentContact == 'closed') {
		log.info 'Opening garage door'
		doorSwitch.on()
	}
}

private closeDoor() {
	if (isDoorOpen()) {
		log.info 'Closing garage door'
		doorSwitch.on()
	}
}