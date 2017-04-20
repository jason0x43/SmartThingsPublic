/**
 *  Button Factory
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
 *  Based on Virtual Device Manager
 *  Copyright 2015 Brian Keifer
 */
definition(
    name: "Button Factory",
    namespace: "jason0x43",
    author: "Jason Cheatham",
    description: "Create momentary switches and buttons for controlling routines",
    category: "My Apps",
    singleInstance: true,
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches@2x.png"
)

preferences {
    page(name: "mainPage", title: "Installed Buttons", install: true, uninstall: true, submitOnChange: true) {
        section {
            app(name: "buttons", appName: "Button Factory Button", namespace: "jason0x43", title: "New Button", multiple: true)
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    log.debug("there are ${childApps.size()} child smartapps")
    childApps.each { child ->
        log.debug("child app: ${child.label}")
    }
}
