definition(
    name:        'Light up the Dark',
    namespace:   'jason0x43',
    author:      'Jason Cheatham',
    description: 'Turns on lights when it\'s dark. Turns lights off when it becomes light.',
    category:    'Convenience',
    iconUrl:     'https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance.png',
    iconX2Url:   'https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance@2x.png'
)

preferences {
	section('Control these lights...'){
		input 'lights', 'capability.switch', multiple: true
	}
	section('Turning on when it\'s dark...'){
		input 'lightSensor', 'capability.illuminanceMeasurement'
	}
	section('This dark...') {
		input 'switchOnLux', 'number', title: 'Lux?'
	}
	section('And then off when it\'s this light...'){
		input 'switchOffLux', 'number', title: 'Lux?'
	}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
    initialize()
}

def initialize() {
	subscribe(lightSensor, 'illuminance', illuminanceHandler, [filterEvents: false])
    illuminanceHandler(null)
}

def illuminanceHandler(event) {
    def lux = lightSensor.currentValue('illuminance')
    def lightState = lights.currentState('switch')
    log.debug "Light state: ${lightState}"
	def someOn = lightState.inject(false) { current, next -> current || next.value == 'on' }
    def someOff = lightState.inject(false) { current, next -> current || next.value == 'off' }    
	log.debug "Handling illuminance event: lux: ${lux}, someOn: ${someOn}, someOff: ${someOff}"
    
    if (lux > switchOffLux && someOn) {
        log.debug 'Switching lights off'
        lights.off()
    }
    else if (lux < switchOnLux && someOff){
        log.debug 'Switching lights on'
        lights.on()
    }
}