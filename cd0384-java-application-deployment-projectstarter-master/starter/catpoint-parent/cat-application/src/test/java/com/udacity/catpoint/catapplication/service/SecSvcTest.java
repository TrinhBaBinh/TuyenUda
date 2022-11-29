package com.udacity.catpoint.catapplication.service;

import com.udacity.catpoint.catapplication.data.*;
import com.udacity.catpoint.catservice.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecSvcTest {

    @Mock
    SecRepo securityRepository;

    @Mock
    ImageService imageService;

    private Sensor sensor;

    @InjectMocks
    private SecSvc securityService;

    @BeforeEach
    public void initSensorBeforeTest() {
        sensor = new Sensor("Test Name", SensorType.WINDOW);
    }

    private Set<Sensor> createSensors(boolean active, int quantity){
        Set<Sensor> sensors = new TreeSet<>();
        Sensor sensor;

        for (int i = 0; i < quantity; i++){
            sensor = new Sensor("S - " + i, SensorType.MOTION);
            sensor.setActive(active);
            sensors.add(sensor);
        }

        return sensors;
    }


    //test case 1
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void changeAlarmStatusFromArmedToPending_activatedSensor(ArmingStatus armingStatus){

        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);

        verify(securityRepository, times(1)).setAlarmStatus(captor.capture());
        verify(securityRepository, times(1)).updateSensor(sensor);
        assertEquals(AlarmStatus.PENDING_ALARM, captor.getValue());
    }

    //test case 2
    @Test
    void changeAlarmStatusFromArmedToPending_activatedSensor_setOffAlarm(){

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    //test case 3
    @Test
    void changeAlarmStateToNo_alarmPending_allSensorsInactive(){
        Set<Sensor> sensors = createSensors(false, 8);
        Sensor one = sensors.iterator().next();
        one.setActive(true);

        when(securityRepository.getSensors()).thenReturn(sensors);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(one, false);

        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);
        verify(securityRepository, times(1)).setAlarmStatus(captor.capture());
        assertEquals(AlarmStatus.NO_ALARM, captor.getValue());
    }

    //test case 4
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void changeSensor_alarmActive_notAffectedAlarmStatus(boolean active) {
        sensor.setActive(active);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(sensor, !active);

        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    //test case 5
    @Test
    void changeAlarmStatusAlarm_sensorActivatedWhileAlreadyActive_alarmPending(){

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);
        verify(securityRepository, times(1)).setAlarmStatus(captor.capture());
        assertEquals(AlarmStatus.ALARM, captor.getValue());
    }

    //test case 6
    @Test
    void noChangeAlarmStatus_sensorDeactivateWhileAlreadyInactive(){

        securityService.changeSensorActivationStatus(sensor, false);
        verify(securityRepository, never()).setAlarmStatus(any());
    }

    //test case 7
    @Test
    void putAlarmStatus_AlarmImageContainCat_systemArmedHome(){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        securityService.processImage(mock(BufferedImage.class));

        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);
        verify(securityRepository, times(1)).setAlarmStatus(captor.capture());
        assertEquals(AlarmStatus.ALARM, captor.getValue());
    }

    //test case 8
    @Test
    void changeSensorsNotActiveAndAlarmStatusNoAlarm_imageContainNoCat(){
        Set<Sensor> sensors = createSensors(false, 4);

        when(securityRepository.getSensors()).thenReturn(sensors);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);

        securityService.processImage(mock(BufferedImage.class));

        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);
        verify(securityRepository, times(1)).setAlarmStatus(captor.capture());
        assertEquals(AlarmStatus.NO_ALARM, captor.getValue());
    }

    //test case 9
    @Test
    void changeAlarmStatusNoAlarm_systemDisarmed(){
        securityService.setArmingStatus(ArmingStatus.DISARMED);

        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);
        verify(securityRepository, times(1)).setAlarmStatus(captor.capture());
        assertEquals(AlarmStatus.NO_ALARM, captor.getValue());
    }

    //test case 10
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void changeAllSensorsInactive_systemArmed(ArmingStatus armingStatus){
        Set<Sensor> sensors = createSensors(true, 7);

        when(securityRepository.getSensors()).thenReturn(sensors);

        securityService.setArmingStatus(armingStatus);

        sensors.forEach(s -> assertEquals(false, s.getActive()));
    }


    //test case 11
    @Test
    void changeAlarmStatusAlarm_cameraShowCat_systemArmedHome(){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        securityService.processImage(mock(BufferedImage.class));

        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);
        verify(securityRepository, times(1)).setAlarmStatus(captor.capture());
        assertEquals(AlarmStatus.ALARM, captor.getValue());
    }
}
