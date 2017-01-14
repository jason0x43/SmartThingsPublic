/**
 *  Nest Direct
 *
 *  Author: dianoga7@3dgo.net
 *  Code: https://github.com/smartthings-users/device-type.nest
 *
 * Copyright (C) 2013 Brian Steere <dianoga7@3dgo.net>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the 'Software'), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

preferences {
    input 'username', 'text', title: 'Username', description: 'Your Nest username (usually an email address)'
    input 'password', 'password', title: 'Password', description: 'Your Nest password'
    input 'serial', 'text', title: 'Serial #', description: 'The serial number of your thermostat'
}

metadata {
    definition (name: 'Nest (jason0x43)', namespace: 'jason0x43', author: 'Jason Cheatham') {
        capability 'Polling'
		capability 'Presence Sensor'
        capability 'Relative Humidity Measurement'
		capability 'Sensor'
        capability 'Thermostat'
        capability 'Temperature Measurement'

        attribute 'temperatureUnit', 'string'

        command 'away'
        command 'heatingSetpointUp'
        command 'heatingSetpointDown'
        command 'coolingSetpointUp'
        command 'coolingSetpointDown'
        command 'present'
        command 'range'
        command 'setCelsius'
        command 'setFahrenheit'
        command 'setPresence'
        command 'tempUpDown'
    }

    tiles(scale: 2) {
		multiAttributeTile(name: 'status', type: 'thermostat', width: 6, height: 4) {
            tileAttribute('device.temperature', key: 'PRIMARY_CONTROL') {
				attributeState('temperature', label: '${currentValue}°', icon: 'st.nest.empty')
			}
            tileAttribute('device.presence', key: 'PRIMARY_CONTROL') {
            	attributeState('home', icon: 'st.nest.nest-home')
                attributeState('away', icon: 'st.nest.nest-away')
			}
			tileAttribute ("device.thermostatOperatingState", key: "PRIMARY_CONTROL") {
            	attributeState('idle', action: 'polling.poll', backgroundColor: '#777777')
                attributeState('cooling', action: 'polling.poll', backgroundColor: '#003CEC')
                attributeState('heating', action: 'polling.poll', backgroundColor: '#E14902')
                attributeState('fan only', action: 'polling.poll', backgroundColor: '#999999')
			}
            tileAttribute('device.humidity', key: 'SECONDARY_CONTROL') {
				attributeState("humidity", label: '${currentValue}%', icon: 'st.Weather.weather12')
            }
            tileAttribute('device.temperature', key: 'VALUE_CONTROL') {
				attributeState("temperature", label: '${currentValue}°', action: 'tempUpDown')
			}
		}
        
        standardTile('thermostatMode', 'device.thermostatMode', decoration: 'flat') {
			state('auto', action: 'thermostat.off', icon: 'st.thermostat.auto')
			state('off', action: 'thermostat.cool', icon: 'st.thermostat.heating-cooling-off')
			state('cool', action: 'thermostat.heat', icon: 'st.thermostat.cool')
			state('heat', action: 'thermostat.auto', icon: 'st.thermostat.heat')
		}
        
        standardTile('thermostatFanMode', 'device.thermostatFanMode', decoration: 'flat') {
            state('auto', action: 'thermostat.fanOn', icon: 'st.thermostat.fan-auto')
            state('on', action: 'thermostat.fanCirculate', icon: 'st.thermostat.fan-on')
            state('circulate', action: 'thermostat.fanAuto', icon: 'st.thermostat.fan-circulate')
        }
        
        standardTile('heatingSetpointUp', 'device.heatingSetpoint', decoration: 'flat') {
            state('setHeatingSetpoint', action: 'heatingSetpointUp', icon: 'st.thermostat.thermostat-up')
        }
        valueTile('heatingSetpoint', 'device.heatingSetpoint') {
            state('default', label: '${currentValue}°', unit: 'Heat', backgroundColor: '#E14902')
        }
        controlTile('heatSliderControl', 'device.heatingSetpoint', 'slider', height: 1, width: 2) {
            state('setHeatingSetpoint', action: 'thermostat.setHeatingSetpoint', backgroundColor:'#d04e00')
        }
        standardTile('heatingSetpointDown', 'device.heatingSetpoint', decoration: 'flat') {
            state('setHeatingSetpoint', action: 'heatingSetpointDown', icon: 'st.thermostat.thermostat-down')
        }
        
        standardTile('coolingSetpointUp', 'device.coolingSetpoint', decoration: 'flat') {
            state('setCoolingSetpoint', action: 'coolingSetpointUp', icon: 'st.thermostat.thermostat-up')
        }
        valueTile('coolingSetpoint', 'device.coolingSetpoint') {
            state('default', label: '${currentValue}°', unit: 'Cool', backgroundColor: '#003CEC')
        }
        controlTile('coolSliderControl', 'device.coolingSetpoint', 'slider', height: 1, width: 2) {
            state('setCoolingSetpoint', action: 'thermostat.setCoolingSetpoint', backgroundColor: '#1e9cbb')
        }
        standardTile('coolingSetpointDown', 'device.coolingSetpoint', decoration: 'flat') {
            state('setCoolingSetpoint', action: 'coolingSetpointDown', icon: 'st.thermostat.thermostat-down')
        }
        
        standardTile('presence', 'device.presence', decoration: 'flat') {
            state('present', label: '${name}', action: 'away', icon:'st.Home.home2')
            state('away', label: '${name}', action: 'present', icon:'st.Transportation.transportation5')
        }
        
		valueTile('temperatureUnit', 'device.temperatureUnit', decoration: 'flat', canChangeIcon: true) {
			state('fahrenheit',  label: '°F', action: 'setCelsius')
			state('celsius', label: '°C', action: 'setFahrenheit')
		}
        
        standardTile('refresh', 'device.thermostatMode', decoration: 'flat') {
            state('default', action: 'polling.poll', icon: 'st.secondary.refresh')
        }
        
        main([ 'status' ])
        details([ 'status', 'heatingSetpointDown', 'heatingSetpoint', 'heatingSetpointUp', 'coolingSetpointDown', 'coolingSetpoint', 'coolingSetpointUp', 'thermostatFanMode', 'humidity', 'presence', 'temperatureUnit', 'refresh' ])
    }
}

def parse(String description) {
}

// temperature /////////////////////////////////////////////////////////////

def tempUpDown(temp) {
	log.debug("tempUpDown called with ${temp}")
    def mode = device.latestState('thermostatMode')
    log.debug('mode is $mode')

	switch (mode) {
    case 'heat':
    	setHeatingSetpoint(temp)
    	break;
    case 'cool':
    	setCoolingSetpoint(temp)
    	break;
    default:
    	break;
    }

    poll()
}

def heatingSetpointUp(){
	setHeatingSetpoint(device.currentValue('heatingSetpoint') + 1)
}

def heatingSetpointDown(){
	setHeatingSetpoint(device.currentValue('heatingSetpoint') - 1)
}

def setHeatingSetpoint(temp) {
	setTargetTemperature(temp, 'heat')
}

def coolingSetpointUp(){
	setCoolingSetpoint(device.currentValue('coolingSetpoint') + 1)
}

def coolingSetpointDown(){
	setCoolingSetpoint(device.currentValue('coolingSetpoint') - 1)
}

def setCoolingSetpoint(temp) {
	setTargetTemperature(temp, 'cool')
}

def setTargetTemperature(temp, heatCool) {
    log.debug("setting ${heatCool}ing point to ${temp}")
    
    if (temp) {
    	def tempName = 'target_temperature'
        if (device.latestState('thermostatMode').stringValue == 'range') {
        	tempName += heatCool == 'cool' ? '_high' : '_low'
        }
        
        def units = device.latestValue('temperatureUnit')
        if (units == 'fahrenheit') {
        	temp = fToC(temp)
		}
        
        log.debug("really setting temp to ${temp}")
        
        api('temperature', [ target_change_pending: true, (tempName): temp ]) {
        	log.debug('sending event')
            sendEvent(name: heatCool + 'ingSetpoint', value: temp)
            poll()
        }
    }
}

// units //////////////////////////////////////////////////////////////

def setFahrenheit() {
	setTemperatureUnit('fahrenheit')
}

def setCelsius() {
	setTemperatureUnit('celsius')
}

def setTemperatureUnit(temperatureUnit) {
	log.debug("Setting temperatureUnit to: ${temperatureUnit}")
	sendEvent(name: 'temperatureUnit',   value: temperatureUnit)
	poll()
}

// heat mode //////////////////////////////////////////////////////////

def off() {
    setThermostatMode('off')
}

def heat() {
    setThermostatMode('heat')
}

def emergencyHeat() {
    setThermostatMode('heat')
}

def cool() {
    setThermostatMode('cool')
}

def range() {
    setThermostatMode('range')
}

def setThermostatMode(mode) {
    log.debug("setting thermostat mode to ${mode}")
    mode = mode == 'emergency heat'? 'heat' : mode
    
    api('thermostat_mode', [ target_change_pending: true, target_temperature_type: mode]) {
        sendEvent(name: 'thermostatMode', value: mode == 'range' ? 'auto' : mode)
        poll()
    }
}

// fan mode //////////////////////////////////////////////////////////

def fanOn() {
    setThermostatFanMode('on')
}

def fanAuto() {
    setThermostatFanMode('auto')
}

def fanCirculate() {
    log.debug('turning fan to circulate')
    setThermostatFanMode('circulate')
}

def setThermostatFanMode(mode) {
    log.debug("setting fan mode to ${mode}")
    def modes = [
        on: [ fan_mode: 'on' ],
        auto: [ fan_mode: 'auto' ],
        circulate: [ fan_mode: 'duty-cycle', fan_duty_cycle: 900 ]
    ]

    api('fan_mode', modes.getAt(mode)) {
        sendEvent(name: 'thermostatFanMode', value: mode)
    }
}

// presence //////////////////////////////////////////////////////////

def away() {
    setPresence('away')
}

def present() {
    setPresence('present')
}

def setPresence(status) {
    log.debug("setting presence to ${status}")
    api('presence', [ away: status == 'away', away_timestamp: new Date().getTime(), away_setter: 0 ]) {
        sendEvent(name: 'presence', value: status)
    }
}

// general //////////////////////////////////////////////////////////

def poll() {
    log.debug('Polling...')
    api('status', []) {
        data.device = it.data.device.getAt(settings.serial)
        data.shared = it.data.shared.getAt(settings.serial)
        data.structureId = it.data.link.getAt(settings.serial).structure.tokenize('.')[1]
        data.structure = it.data.structure.getAt(data.structureId)
                
        data.device.fan_mode = data.device.fan_mode == 'duty-cycle' ? 'circulate' : data.device.fan_mode
        data.structure.away = data.structure.away ? 'away' : 'present'
        
        log.debug('data.shared: ' + data.shared)
        
        def humidity = data.device.current_humidity
        def fanMode = data.device.fan_mode
		def temperatureType = data.shared.target_temperature_type
		def temperatureUnit = device.latestValue('temperatureUnit')
        def temperature = getTemperature(data.shared.current_temperature, temperatureUnit)
        
        log.debug('temperature unit is ' + temperatureUnit)

        sendEvent(name: 'humidity', value: humidity)
        sendEvent(name: 'temperature', value: temperature, state: temperatureType)
        sendEvent(name: 'thermostatFanMode', value: fanMode)
        sendEvent(name: 'thermostatMode', value: temperatureType)

        def heatingSetpoint = '--'
        def coolingSetpoint = '--'
        
        if (temperatureType == 'cool') {
            coolingSetpoint = getTemperature(data.shared.target_temperature, temperatureUnit)
        }
        else if (temperatureType == 'heat') {
            heatingSetpoint = getTemperature(data.shared.target_temperature, temperatureUnit)
        }
        else if (temperatureType == 'range') {
            coolingSetpoint = getTemperature(data.shared.target_temperature_high, temperatureUnit)
            heatingSetpoint = getTemperature(data.shared.target_temperature_low, temperatureUnit)
        }
        
        sendEvent(name: 'coolingSetpoint', value: coolingSetpoint)
        sendEvent(name: 'heatingSetpoint', value: heatingSetpoint)
        
        log.debug('set setpoints')
        
        if (device.latestValue('presence') == 'present') {
        	if (data.structure.away == 'away') {
        		sendEvent(name: 'presence', value: 'not present')
			}
        }
        else {
        	if (data.structure.away == 'present') {
        		sendEvent(name: 'presence', value: 'present')
			}
		}
        
        log.debug('set presence')
        
        def operatingState = 'idle'        
        if (data.shared.hvac_ac_state) {
        	operatingState = 'cooling'
		}
        else if (data.shared.hvac_heater_state) {
        	operatingState = 'heating'
        }
        else if (data.shared.hvac_fan_state) {
        	operatingState = 'fan only'
		}
        log.debug('operating state is ' + operatingState)
		sendEvent(name: 'thermostatOperatingState', value: operatingState)
    }
}

// Call the Nest unofficial REST API
def api(method, args = [], success = {}) {
    if (!isLoggedIn()) {
        log.debug 'Need to login'
        login(method, args, success)
    }
    else {
	    def methods = [
			status: [uri: "/v2/mobile/${data.auth.user}", type: 'get'],
			fan_mode: [uri: "/v2/put/device.${settings.serial}", type: 'post'],
			thermostat_mode: [uri: "/v2/put/shared.${settings.serial}", type: 'post'],
			temperature: [uri: "/v2/put/shared.${settings.serial}", type: 'post'],
			presence: [uri: "/v2/put/structure.${data.structureId}", type: 'post']
		]

		def request = methods.getAt(method)
		log.debug('Logged in for ' + method + ': ' + request + ', ' + args)
		doRequest(request.uri, args, request.type, success)
	}
}

// Need to be logged in before this is called. So don't call this. Call api.
def doRequest(uri, args, type, success) {
    log.debug("Calling $type : $uri : $args")

    if (uri.charAt(0) == '/') {
        uri = "${data.auth.urls.transport_url}${uri}"
    }

    def params = [
        uri: uri,
        headers: [
            'X-nl-protocol-version': 1,
            'X-nl-user-id': data.auth.userid,
            'Authorization': "Basic ${data.auth.access_token}"
        ],
        body: args
    ]
    
     def postRequest = { response ->
        if (response.getStatus() == 302) {
            def locations = response.getHeaders('Location')
            def location = locations[0].getValue()
            log.debug("redirecting to ${location}")
            doRequest(location, args, type, success)
        }
        else {
            success.call(response)
        }
    }

    try {
        if (type == 'post') {
            httpPostJson(params, postRequest)
        }
        else if (type == 'get') {
            httpGet(params, postRequest)
        }
    }
    catch (Throwable e) {
        login()
    }
}

// Login to Nest.com
def login(method = null, args = [], success = {}) {
    def params = [
        uri: 'https://home.nest.com/user/login',
        body: [ username: settings.username, password: settings.password ]
    ]
    httpPost(params) {response ->
        data.auth = response.data
        data.auth.expires_in = Date.parse('EEE, dd-MMM-yyyy HH:mm:ss z', response.data.expires_in).getTime()
        log.debug(data.auth)

		if (method != null) {
        	api(method, args, success)
        }
    }
}

// Indicate whether we're logged in
def isLoggedIn() {
    if (!data.auth) {
        log.debug 'No data.auth'
        return false
    }
    def now = new Date().getTime();
    return data.auth.expires_in > now
}

// Return the proper rounded value of a temperature
def getTemperature(temp, unit) {
	if (unit == 'fahrenheit') {
    	temp = cToF(temp)
    }
    return Math.round(temp)
}

// Convert Celsius to Fahrenheit
def cToF(temp) {
    return temp * 1.8 + 32
}

// Convert Fahrenheit to Celsius
def fToC(temp) {
    return (temp - 32) / 1.8
}

// Run a handler after a delay
def delay(handler, seconds) {
	def runTime = new Date(now.getTime() + seconds * 1000)
	runOnce(runTime, handler)
}