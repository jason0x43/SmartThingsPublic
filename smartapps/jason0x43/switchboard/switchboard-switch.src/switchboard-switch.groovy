/**
 *  Switchboard Switch
 *
 *  Copyright 2017 Jason Cheatham
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
 *  Based on VDM Child - Switches
 *  Copyright 2015 Brian Keifer
 */
definition(
    name: "Switchboard Switch",
    namespace: "jason0x43/switchboard",
    parent: "jason0x43/switchboard:Switchboard",
    author: "Jason Cheatham",
    description: "A virtual momentary switch for controlling routines",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
    page(name: "mainPage", install: false, uninstall: true, nextPage: "devicePage")
    page(name: "devicePage", install: true, uninstall: true)
}

def namePage() {
    dynamicPage(name: "namePage", title: "New Switch", install: true, uninstall: childCreated()) {
        section {
            label(title: "Device Label:", required: true)
        }
    }
}

def devicePage() {
    dynamicPage(name: "devicePage", title: "New Switch", install: true, uninstall: childCreated()) {
        if (!childCreated()) {
            section { inputDeviceType() }
        } else {
            section { paragraph("Switches currently can not be converted to a different type after installation.\n\n${app.label}") }
        }
    }
}

def inputDeviceType() {
    input("deviceType", "enum", title: "Device Type:", required: true, options: ["Momentary Button Tile", "Simulated Button (jason0x43)"], defaultValue: "Momentary Button Tile")
}

def installed() {
    createChildDevice(app.label, settings.deviceType)
    initialize()
}


def updated() {
    log.debug("Updated with settings: ${settings}")
    unsubscribe()
    initialize()
}

def initialize() {
}

def createChildDevice(deviceLabel, deviceType) {
    app.updateLabel(deviceLabel)
    if (!childCreated()) {
        def child = addChildDevice("smartthings", deviceType, getDeviceID(), null, [name: getDeviceID(), label: deviceLabel, completedSetup: true])
    }
}

def uninstalled() {
    getChildDevices().each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

private childCreated() {
    if (getChildDevice(getDeviceID())) {
        return true
    } else {
        return false
    }
}

private getDeviceID() {
    return "SBSW_${app.id}"
}
