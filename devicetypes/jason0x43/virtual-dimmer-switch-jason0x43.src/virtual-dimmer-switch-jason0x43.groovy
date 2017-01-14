/**
 * Based on SmartThings Dimmer Switch and @twack's VirtualBetterDimmer
 */
metadata {
	definition (name: "Virtual Dimmer Switch (jason0x43)", namespace: "jason0x43", author: "Jason Cheatham") {
		capability "Switch Level"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
        
		attribute "stepsize", "string"

		command "getLevel"
		command "dimmerOn"
		command "dimmerOff"        
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"dimmerOff", icon:"st.switches.switch.on", backgroundColor:"#79b821"
				attributeState "off", label:'${name}', action:"dimmerOn", icon:"st.switches.switch.off", backgroundColor:"#ffffff"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}

		standardTile("refresh", "device.switch", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch"])
		details(["switch", "refresh"])
	}
}

def initialize() {    
	if (!settings.stepsize) {
    	state.stepsize = 10
    }
    else {
		state.stepsize = settings.stepsize
	}
    
    if (!device.currentValue("level")) {
    	setLevel(100)
	}
}

def parse(String description) {
	// not a real device, nothing to parse
}

def dimmerOn() { //made our own, since event was filtered by default on Android
    log.info("on")
    sendEvent(name: "switch", value: "on")
}

def dimmerOff() { //made our own, since event was filtered by default on Android
    log.info("off")
    sendEvent(name: "switch", value: "off")
    
}

def on() {
    log.info("on")
    sendEvent(name: "switch", value: "on")
}

def off() {
    log.info("off")
    sendEvent(name: "switch", value: "off")
}

def setLevel(val) {
    log.info("setLevel $val")
    
    // Don't drive switches past allowed values
    if (val < 0) {
    	val = 0
    }
    if (val > 100) {
    	val = 100
    }
    
    if (val == 0) {
    	sendEvent(name: "level", value:val)
    	dimmerOff()
    }
    else {
    	dimmerOn()
    	sendEvent(name: "level", value: val)
        // Needed for apps subscribed to setLevel event
    	sendEvent(name: "switch.setLevel", value: val)
    }
}

def setLevel(val, dur) {
	log.info("setLevel $val, $dur")
	sendEvent(name: "setLevel", value: val)
}

def getLevel() {
	log.info('getLevel')
	log.info(device.currentValue("level"))
	log.info(device.currentValue("switch"))
}

def poll() {
    log.info "poll"
}

def refresh() {
    log.info "refresh"
}