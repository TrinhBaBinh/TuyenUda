package com.udacity.catpoint.catapplication.service;

import com.udacity.catpoint.catapplication.application.Status;
import com.udacity.catpoint.catapplication.data.AlarmStatus;
import com.udacity.catpoint.catapplication.data.ArmingStatus;
import com.udacity.catpoint.catapplication.data.SecRepo;
import com.udacity.catpoint.catapplication.data.Sensor;
import com.udacity.catpoint.catservice.service.ImageService;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service that receives information about changes to the security system. Responsible for
 * forwarding updates to the repository and making any decisions about changing the system state.
 *
 * This is the class that should contain most of the business logic for our system, and it is the
 * class you will be writing unit tests for.
 */
public class SecSvc {

    private ImageService imageService;
    private SecRepo securityRepository;
    private Set<Status> statusListeners = new HashSet<>();

    private boolean isCatFounding = false;

    public SecSvc(SecRepo securityRepository, ImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    /**
     * Sets the current arming status for the system. Changing the arming status
     * may update both the alarm status.
     * @param armingStatus
     */
    public void setArmingStatus(ArmingStatus armingStatus) {
        if(armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        } else {
            if (armingStatus == ArmingStatus.ARMED_HOME && isCatFounding) {
                setAlarmStatus(AlarmStatus.ALARM);
            }
            Iterator<Sensor> sensorIterator = getSensors().stream().collect(Collectors.toList()).iterator();

            while (sensorIterator.hasNext()) {
                Sensor sensor = sensorIterator.next();
                sensor.setActive(false);
                securityRepository.updateSensor(sensor);
            }

        }
        securityRepository.setArmingStatus(armingStatus);
    }

    /**
     * Internal method that handles alarm status changes based on whether
     * the camera currently shows a cat.
     * @param cat True if a cat is detected, otherwise false.
     */
    private void catDetected(Boolean cat) {
        if(cat && getArmingStatus() == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else {
            if(getSensors()
                    .stream()
                    .allMatch(s -> !s.getActive())
            ) {
                setAlarmStatus(AlarmStatus.NO_ALARM);
            }
        }
        isCatFounding = cat;

        statusListeners.forEach(sl -> sl.catDetected(cat));
    }

    /**
     * Register the StatusListener for alarm system updates from within the SecurityService.
     * @param statusListener
     */
    public void addStatusListener(Status statusListener) {
        statusListeners.add(statusListener);
    }

    public void removeStatusListener(Status statusListener) {
        statusListeners.remove(statusListener);
    }

    /**
     * Change the alarm status of the system and notify all listeners.
     * @param status
     */
    public void setAlarmStatus(AlarmStatus status) {
        securityRepository.setAlarmStatus(status);
        statusListeners.forEach(sl -> sl.notify(status));
    }

    /**
     * Internal method for updating the alarm status when a sensor has been activated.
     */
    private void handleSensorActivated() {
        if(securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
            return; //no problem if the system is disarmed
        }
        switch(securityRepository.getAlarmStatus()) {
            case NO_ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
            case PENDING_ALARM -> setAlarmStatus(AlarmStatus.ALARM);
        }
    }

    /**
     * Internal method for updating the alarm status when a sensor has been deactivated
     */
    private void handleSensorDeactivated(Sensor sensor) {
        if (securityRepository.getArmingStatus() == ArmingStatus.DISARMED){
            return;
        }

        if(securityRepository.getAlarmStatus() == AlarmStatus.PENDING_ALARM){
            if(getSensors()
                    .stream()
                    .filter(s -> s != sensor)
                    .allMatch(s -> !s.getActive())
            ) {
                setAlarmStatus(AlarmStatus.NO_ALARM);
            }
        }
    }

    /**
     * Change the activation status for the specified sensor and update alarm status if necessary.
     * @param sensor
     * @param active
     */
    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
        if(!sensor.getActive() && active) {
            handleSensorActivated();
        } else if (sensor.getActive() && !active) {
            handleSensorDeactivated(sensor);
        }
        sensor.setActive(active);
        securityRepository.updateSensor(sensor);
    }

    /**
     * Send an image to the SecurityService for processing. The securityService will use its provided
     * ImageService to analyze the image for cats and update the alarm status accordingly.
     * @param currentCameraImage
     */
    public void processImage(java.awt.image.BufferedImage currentCameraImage) {
        catDetected(imageService.imageContainsCat(currentCameraImage, 50.0f));
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }
}
