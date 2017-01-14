/**
 *  Mode Lighting
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
	name:        'Mode Lighting',
	namespace:   'jason0x43',
	author:      'Jason Cheatham',
	description: 'Control a light according to the current mode.',
	category:    'Convenience',
	iconUrl:     'https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png',
	iconX2Url:   'https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png'
)

preferences {
	page(name: 'rootPage')
}

def rootPage() {
	dynamicPage(name: 'rootPage', title: 'Light Settings', install: true, uninstall: true) {
		section {
			input(
				name: 'light',
				type: 'capability.switch',
				title: 'Control This Light',
				description: null,
				multiple: false,
				required: true,
				refreshAfterSelection: true
			)
		}

		if (light) {
			location.modes.each { mode ->
				section(mode.name) { 
					def switchVar = "switch_${mode.name}"
					input(
						name: switchVar,
						type: 'enum',
						metadata: [ values: [ 'on', 'off', 'current' ] ],
						title: 'Turn switch on or off, or keep current state',
						description: null,
						defaultValue: this[switchVar],
						required: false,
						refreshAfterSelection: true
					)
                    
					if (this["switch_${mode.name}"] != 'off') {
						if (canSetColor(light)) {
							def hueVar = "hue_${mode.name}"
							input(
								name: hueVar,
								type: 'number',
								title: 'Hue (1-99)',
								description: null,
								defaultValue: this[hueVar],
								required: false
							)

							def satVar = "sat_${mode.name}"
							input(
								name: satVar,
								type: 'number',
								title: 'Saturation (1-99)',
								description: null,
								defaultValue: this[satVar],
								required: false
							)
						}

						if (canDim(light)) {
							def dimVar = "dim_${mode.name}"
							input(
								name: dimVar,
								type: 'number',
								title: 'Brightness (1-99)',
								description: null,
								defaultValue: this[dimVar],
								required: false
							)
						}
					}
				}
			}

			section {
				label(title: 'Label this SmartApp', required: false, defaultValue: '')
			}
		}
	}
}

// Handlers ////////////////////////////////////////////////////////////

def installed() {
	log.debug("Installing with settings: ${settings}")
	initialize()
}

def updated() {
	log.debug("Updating with settings: ${settings}")
	initialize()
}

private initialize() {
	log.debug('Subscribing to location events')
	subscribe(location, locationHandler)
	updateState(location.mode)
}

def locationHandler(event) {
	log.debug("locationHandler evt: ${event.value}")
	updateState(event.value)
}

// Helpers /////////////////////////////////////////////////////////////

private updateState(modeName) {
	log.debug("updating state for mode ${modeName}")
    def isOn = "${light.currentValue('switch').value}" == "on"
    
	switch (this["switch_${modeName}"]) {
    case 'on':
    	log.trace('case "on"')
		light.on()
        pause(500)
        // set the isOn flag to true so case 'current' can check it instead
        // of requesting currentValue, which may not be current enough
        isOn = true

	case 'current':
    	log.trace('case "current"')
    	if (isOn) {
        	log.trace('light is on -- configuring')
            
            if (canSetColor(light)) {
            	log.trace('setting color')
                
                def hue = this["hue_${modeName}"]
                if (hue != null) {
                    light.setHue(hue)
                    pause(500)
                }

                def sat = this["sat_${modeName}"]
                if (sat != null) {
                    light.setSaturation(sat)
                    pause(500)
                }

                def bri = this["dim_${modeName}"]
                light.setLevel(bri == null ? 100 : bri)
            }
            else if (canDim(light)) {
                def level = this["dim_${modeName}"]
                light.setLevel(level == null ? 100 : level)
            }
        }

        break
        
    case 'off':
    	log.trace('case "off"')
		light.off()
        break;
	}
}

private canDim(device) {
	def isDimmer = false
	device.supportedCommands.each {
		if (it.name.contains('setLevel')) {
			isDimmer = true
		}
	}
	return isDimmer
}

private canSetColor(device) {
	def hasColor = false
	device.supportedCommands.each {
		if (it.name.contains('setColor')) {
			hasColor = true
		}
	}
	return hasColor
}