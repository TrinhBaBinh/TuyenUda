package com.udacity.catpoint.catapplication.application;

import com.udacity.catpoint.catapplication.data.AlarmStatus;

/**
 * Identifies a component that should be notified whenever the system status changes
 */
public interface Status {
    void notify(AlarmStatus status);
    void catDetected(boolean catDetected);
    void sensorStatusChanged();
}
