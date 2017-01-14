/**
 *  Appliance Finished
 *
 *  Copyright 2014 SmartThings
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
    name: "Appliance Finished",
    namespace: "jason0x43",
    author: "Jason Cheatham",
    description: "Get notified when an appliance (washer, dishwasher, etc.) finishes operating",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/text.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/text@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/text@2x.png"
)

preferences {
	section {
		input(name: "meter", type: "capability.powerMeter", title: "Monitor this power meter", required: true, multiple: false, description: null)
    	input(name: "appliance", type: "string", title: "Name of appliance", required: true, description: null)
        input(name: "threshold", type: "number", title: "Active power", required: true, description: "in watts.")
        input(name: "notifyDelay", type: "number", title: "Wait this many minutes after it stops to send a notification (defaults to 2)", required: false, description: null)
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
	subscribe(meter, "power", meterHandler)
}

def meterHandler(evt) {
    def meterValue = evt.value as double
    def isActive = meterValue >= threshold;
    
    if (!atomicState.lastValue) {
    	atomicState.lastValue = meterValue
    }

	atomicState.lastValue = meterValue
    
    def lastState = atomicState.lastState;
    if (isActive) {
    	atomicState.lastState = "on";
    }
    else {
    	atomicState.lastState = "off";
	}
    
    if (!isActive && lastState == "on") {
        log.debug("${meter} became inactive")
        def delay = ((notifyDelay != null && notifyDelay != "") ? notifyDelay : 2) * 60
        log.debug("scheduling notification to run in ${delay} seconds")
        if (delay == 0) {
	    	sendNotification()
        }
        else {
    		runIn(delay, 'sendNotification')
        }
    }
    else if (isActive && lastState == "off") {
    	log.debug("${meter} became active, unscheduling notification")
    	unschedule('sendNotification')
	}
}

def sendNotification() {
	log.debug("Sending notification");
    sendPush(appliance + " is finished!")
}