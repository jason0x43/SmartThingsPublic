/**
 *  Copyright 2015 SmartThings
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
 *  SmartSense Multi Garage
 *
 *  Author: SmartThings
 *  Date: 2013-03-09
 *
 *  Modified to use contact state rather than angle to determine whether a door is closed.
 */
metadata {
	definition (name: "SmartSense Multi Garage (jason0x43)", namespace: "jason0x43", author: "Jason Cheatham") {
		capability "Acceleration Sensor"
		capability "Actuator"
		capability "Battery"
        capability "Button"
		capability "Contact Sensor"
		capability "Garage Door Control"
		capability "Sensor"
		capability "Signal Strength"
		capability "Temperature Measurement"
		capability "Three Axis"
        capability "Refresh"

		attribute "status", "string"
		attribute "door", "string"
		attribute "buttonPress", "string"

		command "actuate"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 4){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState "closed", label:'${name}', icon:"st.doors.garage.garage-closed", action: "actuate", backgroundColor:"#79b821", nextState:"opening"
				attributeState "open", label:'${name}', icon:"st.doors.garage.garage-open", action: "actuate", backgroundColor:"#ffa81e", nextState:"closing"
				attributeState "opening", label:'${name}', icon:"st.doors.garage.garage-opening", backgroundColor:"#ffe71e"
				attributeState "closing", label:'${name}', icon:"st.doors.garage.garage-closing", backgroundColor:"#ffe71e"
			}
		}
		standardTile("contact", "device.contact", width: 2, height: 2) {
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
		}
		standardTile("acceleration", "device.acceleration", decoration: "flat", width: 2, height: 2) {
			state("active", label:'${name}', icon:"st.motion.acceleration.active", backgroundColor:"#53a7c0")
			state("inactive", label:'${name}', icon:"st.motion.acceleration.inactive", backgroundColor:"#ffffff")
		}
		valueTile("temperature", "device.temperature", decoration: "flat", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°')
		}
		valueTile("3axis", "device.threeAxis", decoration: "flat", wordWrap: false, width: 2, height: 2) {
			state("threeAxis", label:'${currentValue}', unit:"")
		}
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main("status")
		details(["status", "contact", "acceleration", "temperature", "3axis", "battery"])
	}
    
	preferences {
		input description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter \"-5\". If 3 degrees too cold, enter \"+3\".", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		input "tempOffset", "number", title: "Temperature Offset", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
	}    
}

def parse(String description) {
	// log.debug "Parse: $description"
	def results = null

	if (!isSupportedDescription(description) || zigbee.isZoneType19(description)) {
		results = parseSingleMessage(description)
	}
    else if (description == 'updated') {
		results = parseOtherMessage(description)
	}
	else {
		results = parseMultiSensorMessage(description)
	}

	// log.debug "Parse result: ${results?.descriptionText}"
	return results
}

def actuate() {
	log.debug "Sending button press event"
	//sendEvent(name: "buttonPress", value: "true", isStateChange: true)
	sendEvent(name: "button", value: "pushed", data: [buttonNumber: "1"], descriptionText: "Garage door button was pushed", isStateChange: true)
}

def close() {
	if (device.currentValue("status") == "open") {
		log.debug "Sending button press event to close door"
        actuate()
	}
	else {
		log.debug "Not closing door since it is already closed"
	}
}

def open() {
	if (device.currentValue("status") == "closed") {
		log.debug "Sending button press event to open door"
        actuate()
	}
	else {
		log.debug "Not opening door since it is already open"
	}
}

private addGarageEvents(changedType, newValue, results) {
	if (changedType == "acceleration") {
		if (newValue == "active") {
			// Status is based on motion direction
			def direction;
			if (device.currentValue("status") == "closed") {
				log.debug "Acceleration is active, status is closed"
				direction = "opening";
			}
			else {
				log.debug "Acceleration is active, status is not closed"
				direction = "closing";
			}

			results << createEvent(name: "status", value: direction, unit: "")
		}
		else if (device.currentValue("contact") != "closed") {
			// When we stop, if we're not closed, we're open
			log.debug "Acceleration is not active, contact is not closed"
			results << createEvent(name: "status", value: "open", unit: "")
		}
	}
	else if (changedType == "contact") {
		if (newValue == "closed") {
			log.debug "Contact is closed"
			// Status and door are both anything -> closed
			results << createEvent(name: "status", value: "closed", unit: "")
			results << createEvent(name: "door", value: "closed", unit: "")
		}
		else {
			// Door is closed -> open
			results << createEvent(name: "door", value: "open", unit: "")

			if (device.currentValue("acceleration") == "active") {
				log.debug "Contact is open, acceleration is active"
				// If contact was just opened and the door is moving, it's opening
				results << createEvent(name: "status", value: "opening", unit: "")
			}
			else {
				log.debug "Contact is open, acceleration is not active"
				// If contact was just opened and the door isn't moving, it's
				// open (or at least not closed)
				results << createEvent(name: "status", value: "open", unit: "")
			}
		}
	}
}

private List parseAccelerationMessage(String description) {
	def results = []
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('acceleration:')) {
			def event = getAccelerationResult(part, description)
			results << event
			addGarageEvents("acceleration", event.value, results);
		}
		else if (part.startsWith('rssi:')) {
			results << getRssiResult(part, description)
		}
		else if (part.startsWith('lqi:')) {
			results << getLqiResult(part, description)
		}
	}

	log.debug("Acceleration results: " + results)
	return results
}

private List parseContactMessage(String description) {
	def results = []
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('contactState:')) {
			def result = getContactResult(part, description)
			results << result
			addGarageEvents("contact", result.value, results);
		}
		else if (part.startsWith('accelerationState:')) {
			def result = getAccelerationResult(part, description)
			results << result
			addGarageEvents("acceleration", result.value, results);
		}
		else if (part.startsWith('temp:')) {
			results << getTempResult(part, description)
		}
		else if (part.startsWith('battery:')) {
			results << getBatteryResult(part, description)
		}
		else if (part.startsWith('rssi:')) {
			results << getRssiResult(part, description)
		}
		else if (part.startsWith('lqi:')) {
			results << getLqiResult(part, description)
		}
	}

	log.debug("Contact results: " + results)
	return results
}

private parseDescriptionText(String linkText, String value, String description) {
	if (!isSupportedDescription(description)) {
		return value
	}

	value ? "$linkText was ${value == 'open' ? 'opened' : value}" : ""
}

private List parseMultiSensorMessage(description) {
	def results = []
	if (isAccelerationMessage(description)) {
		results = parseAccelerationMessage(description)
	}
	else if (isContactMessage(description)) {
		results = parseContactMessage(description)
	}
	else if (isRssiLqiMessage(description)) {
		results = parseRssiLqiMessage(description)
	}
	else if (isOrientationMessage(description)) {
		results = parseOrientationMessage(description)
	}

	results
}

private String parseName(String description) {
	if (isSupportedDescription(description)) {
		return "contact"
	}
	null
}

private List parseOrientationMessage(String description) {
	def results = []
	def xyzResults = [x: 0, y: 0, z: 0]
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('x:')) {
			def unsignedX = part.split(":")[1].trim().toInteger()
			def signedX = unsignedX > 32767 ? unsignedX - 65536 : unsignedX
			xyzResults.x = signedX
		}
		else if (part.startsWith('y:')) {
			def unsignedY = part.split(":")[1].trim().toInteger()
			def signedY = unsignedY > 32767 ? unsignedY - 65536 : unsignedY
			xyzResults.y = signedY
		}
		else if (part.startsWith('z:')) {
			def unsignedZ = part.split(":")[1].trim().toInteger()
			def signedZ = unsignedZ > 32767 ? unsignedZ - 65536 : unsignedZ
			xyzResults.z = signedZ
		}
		else if (part.startsWith('rssi:')) {
			results << getRssiResult(part, description)
		}
		else if (part.startsWith('lqi:')) {
			results << getLqiResult(part, description)
		}
	}

	def xyz = getXyzResult(xyzResults, description)
	results << xyz

	// Looks for Z-axis orientation as virtual contact state
	def a = xyz.value.split(',').collect{it.toInteger()}
	def absValueX = Math.abs(a[0])
	def absValueY = Math.abs(a[1])
	def absValueZ = Math.abs(a[2])
	// log.debug "absValueX: $absValueX, absValueY: $absValueY, absValueZ: $absValueZ"
	addGarageEvents("orientation", [ absValueX, absValueY, absValueZ ], results);

	return results
}

private Map parseOtherMessage(description) {
	def name = null
	def value = description
	def linkText = getLinkText(device)
	def descriptionText = description
	def handlerName = description
	def isStateChange = isStateChange(device, name, value)

	def results = [
		name: name,
		value: value,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: handlerName,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
	// log.debug "Parse results for $device: $results"

	return results
}

private List parseRssiLqiMessage(String description) {
	def results = []
	// "lastHopRssi: 91, lastHopLqi: 255, rssi: 91, lqi: 255"
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('lastHopRssi:')) {
			results << getRssiResult(part, description, true)
		}
		else if (part.startsWith('lastHopLqi:')) {
			results << getLqiResult(part, description, true)
		}
		else if (part.startsWith('rssi:')) {
			results << getRssiResult(part, description)
		}
		else if (part.startsWith('lqi:')) {
			results << getLqiResult(part, description)
		}
	}

	return results
}

private Map parseSingleMessage(description) {
	def name = parseName(description)
	def value = parseValue(description)
	def linkText = getLinkText(device)
	def descriptionText = parseDescriptionText(linkText, value, description)
	def handlerName = value == 'open' ? 'opened' : value
	def isStateChange = isStateChange(device, name, value)

	def results = [
		name: name,
		value: value,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: handlerName,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
	// log.debug "Parse results for $device: $results"

	return results
}

private String parseValue(String description) {
	if (!isSupportedDescription(description)) {
		return description
	}
	else if (zigbee.translateStatusZoneType19(description)) {
		return "open"
	}
	else {
		return "closed"
	}
}

private getAccelerationResult(part, description) {
	def name = "acceleration"
	def value = part.endsWith("1") ? "active" : "inactive"
	def linkText = getLinkText(device)
	def descriptionText = "$linkText ${name} was $value"
	def isStateChange = isStateChange(device, name, value)

	return [
		name: name,
		value: value,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: value,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
}

private getContactResult(part, description) {
	def name = "contact"
	def value = part.endsWith("1") ? "open" : "closed"
	def handlerName = value == 'open' ? 'opened' : value
	def linkText = getLinkText(device)
	def descriptionText = "$linkText was $handlerName"
	def isStateChange = isStateChange(device, name, value)

	return [
		name: name,
		value: value,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: handlerName,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
}

private getTempResult(part, description) {
	def name = "temperature"
	def temperatureScale = getTemperatureScale()
	def value = zigbee.parseSmartThingsTemperatureValue(part, "temp: ", temperatureScale)
	if (tempOffset) {
		def offset = tempOffset as int
		def v = value as int
		value = v + offset
	}
	def linkText = getLinkText(device)
	def descriptionText = "$linkText was $value°$temperatureScale"
	def isStateChange = isTemperatureStateChange(device, name, value.toString())

	return [
		name: name,
		value: value,
		unit: temperatureScale,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: name,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
}

private getXyzResult(results, description) {
	def name = "threeAxis"
	def value = "${results.x},${results.y},${results.z}"
	def linkText = getLinkText(device)
	def descriptionText = "$linkText ${name} was $value"
	def isStateChange = isStateChange(device, name, value)

	return [
		name: name,
		value: value,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: name,
		isStateChange: isStateChange,
		displayed: false
	]
}

private getBatteryResult(part, description) {
	def batteryDivisor = description.split(",").find {it.split(":")[0].trim() == "batteryDivisor"} ? description.split(",").find {it.split(":")[0].trim() == "batteryDivisor"}.split(":")[1].trim() : null
	def name = "battery"
	def value = zigbee.parseSmartThingsBatteryValue(part, batteryDivisor)
	def unit = "%"
	def linkText = getLinkText(device)
	def descriptionText = "$linkText ${name} was ${value}${unit}"
	def isStateChange = isStateChange(device, name, value)

	return [
		name: name,
		value: value,
		unit: unit,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: name,
		isStateChange: isStateChange,
		displayed: false
	]
}

/**
 * RSSI == received signal strength indication
 */
private getRssiResult(part, description, lastHop=false) {
	def name = lastHop ? "lastHopRssi" : "rssi"
	def valueString = part.split(":")[1].trim()
	def value = (Integer.parseInt(valueString) - 128).toString()
	def linkText = getLinkText(device)
	def descriptionText = "$linkText ${name} was $value dBm"

	def isStateChange = isStateChange(device, name, value)

	return [
		name: name,
		value: value,
		unit: "dBm",
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: null,
		isStateChange: isStateChange,
		displayed: false
	]
}

/**
 * Use LQI (Link Quality Indicator) as a measure of signal strength. The values
 * are 0 to 255 (0x00 to 0xFF) and higher values represent higher signal
 * strength. Return as a percentage of 255.
 *
 * Note: To make the signal strength indicator more accurate, we could combine
 * LQI with RSSI.
 */
private getLqiResult(part, description, lastHop=false) {
	def name = lastHop ? "lastHopLqi" : "lqi"
	def valueString = part.split(":")[1].trim()
	def percentageOf = 255
	def value = Math.round((Integer.parseInt(valueString) / percentageOf * 100)).toString()
	def unit = "%"
	def linkText = getLinkText(device)
	def descriptionText = "$linkText ${name} was: ${value}${unit}"

	def isStateChange = isStateChange(device, name, value)

	return [
		name: name,
		value: value,
		unit: unit,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: null,
		isStateChange: isStateChange,
		displayed: false
	]
}

private Boolean isAccelerationMessage(String description) {
	// "acceleration: 1, rssi: 91, lqi: 255"
	return description ==~ /acceleration:.*rssi:.*lqi:.*/
}

private Boolean isContactMessage(String description) {
	// "contactState: 1, accelerationState: 0, temp: 14.4 C, battery: 28, rssi: 59, lqi: 255"
	return description ==~ /contactState:.*accelerationState:.*temp:.*battery:.*rssi:.*lqi:.*/
}

private Boolean isRssiLqiMessage(String description) {
	// "lastHopRssi: 91, lastHopLqi: 255, rssi: 91, lqi: 255"
	return description ==~ /lastHopRssi:.*lastHopLqi:.*rssi:.*lqi:.*/
}

private Boolean isOrientationMessage(String description) {
	// "x: 0, y: 33, z: 1017, rssi: 102, lqi: 255"
	return description ==~ /x:.*y:.*z:.*rssi:.*lqi:.*/
}