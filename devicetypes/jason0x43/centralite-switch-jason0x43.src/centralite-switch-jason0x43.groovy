/**
 *	CentraLite Switch
 *
 *	Author: SmartThings
 *	Date: 2013-12-02
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "CentraLite Switch (jason0x43)", namespace: "jason0x43", author: "Jason Cheatham") {
		capability "Actuator"
		capability "Switch"
		capability "Power Meter"
		capability "Configuration"
		capability "Refresh"
		capability "Sensor"

		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B04,0B05", outClusters: "0019"
	}

	// simulator metadata
	simulator {
		// status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"

		// reply messages
		reply "zcl on-off on": "on/off: 1"
		reply "zcl on-off off": "on/off: 0"
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
		valueTile("power", "device.power", decoration: "flat") {
			state "power", label:'${currentValue} W'
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch","power","refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "Parse description $description"
	def name = null
	def value = null
    def result = null
    
	if (description?.startsWith("read attr -")) {
		def descMap = parseDescriptionAsMap(description)
		if (descMap.cluster == "0006" && descMap.attrId == "0000") {
            result = createEvent(name: "switch", value: descMap.value.endsWith("01") ? "on" : "off")
		} else {
			def reportValue = description.split(",").find {it.split(":")[0].trim() == "value"}?.split(":")[1].trim()
			// assume 16 bit signed for encoding and power divisor is 10
            def power = Integer.parseInt(reportValue, 16) / 10
            result = createEvent(name: "power", value: power)
		}
	} else if (description?.startsWith("on/off:")) {
		log.debug "Switch command"
        result = createEvent(name: "switch", value: description?.endsWith(" 1") ? "on" : "off")
	} else if (description?.startsWith("catchall: ")) {
		log.debug "Catchall command"
        def parts = (description - "catchall: ").split()
        if (parts[1] == "0006") {
            log.debug "Switch state"
            def payloadType = parts[10]
            if (payloadType == "0B") {
                result = createEvent(name: "switch", value: description?.endsWith(" 0100") ? "on" : "off")
            } else if (payloadType == "01") {
                result = createEvent(name: "switch", value: description?.endsWith("01001") ? "on" : "off")
            }
        }
	}

	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def parseDescriptionAsMap(description) {
	(description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

// Commands to device
def on() {
	'zcl on-off on'
}

def off() {
	'zcl on-off off'
}

def meter() {
	"st rattr 0x${device.deviceNetworkId} 1 0xB04 0x50B"
}

def refresh() {
	log.debug "refreshing..."
	[
        "st rattr 0x${device.deviceNetworkId} 1 0xB04 0x50B",
        "delay 200",
        "st rattr 0x${device.deviceNetworkId} 1 6 0"
    ]
}

def configure() {
	log.debug "sending configuration..."
	[
		"zdo bind 0x${device.deviceNetworkId} 1 1 6 {${device.zigbeeId}} {}",
        "delay 200",
		"zdo bind 0x${device.deviceNetworkId} 1 1 0xB04 {${device.zigbeeId}} {}"
	]
}