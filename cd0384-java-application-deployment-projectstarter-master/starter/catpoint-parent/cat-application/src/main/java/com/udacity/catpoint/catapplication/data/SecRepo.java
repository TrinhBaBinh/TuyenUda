package com.udacity.catpoint.catapplication.data;

import java.util.Set;

/**
 * Interface showing the methods our security repository will need to support
 */
public interface SecRepo {
    void addSensor(Sensor sensor);
    void removeSensor(Sensor sensor);
    void updateSensor(Sensor sensor);
    void setAlarmStatus(AlarmStatus alarmStatus);
    void setArmingStatus(ArmingStatus armingStatus);
    Set<Sensor> getSensors();
    AlarmStatus getAlarmStatus();
    ArmingStatus getArmingStatus();


}
