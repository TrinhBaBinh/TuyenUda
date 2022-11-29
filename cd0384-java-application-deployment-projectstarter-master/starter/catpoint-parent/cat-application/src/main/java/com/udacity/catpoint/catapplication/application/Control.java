package com.udacity.catpoint.catapplication.application;

import com.udacity.catpoint.catapplication.data.ArmingStatus;
import com.udacity.catpoint.catapplication.service.SecSvc;
import com.udacity.catpoint.catapplication.service.StyleSvc;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JPanel containing the buttons to manipulate arming status of the system.
 */
public class Control extends JPanel {

    private SecSvc securityService;
    private Map<ArmingStatus, JButton> buttonMap;


    public Control(SecSvc securityService, Sensor sensorPanel) {
        super();
        setLayout(new MigLayout());
        this.securityService = securityService;

        JLabel panelLabel = new JLabel("System Control");
        panelLabel.setFont(StyleSvc.HEADING_FONT);

        add(panelLabel, "span 3, wrap");

        //create a map of each status type to a corresponding JButton
        buttonMap = Arrays.stream(ArmingStatus.values())
                .collect(Collectors.toMap(status -> status, status -> new JButton(status.getDescription())));

        //add an action listener to each button that applies its arming status and recolors all the buttons
        buttonMap.forEach((k, v) -> {
            v.addActionListener(e -> {
                securityService.setArmingStatus(k);
                buttonMap.forEach((status, button) -> button.setBackground(status == k ? status.getColor() : null));
                sensorPanel.updateSensorList();
            });
        });

        //map order above is arbitrary, so loop again in order to add buttons in enum-order
        Arrays.stream(ArmingStatus.values()).forEach(status -> add(buttonMap.get(status)));

        ArmingStatus currentStatus = securityService.getArmingStatus();
        buttonMap.get(currentStatus).setBackground(currentStatus.getColor());


    }
}
