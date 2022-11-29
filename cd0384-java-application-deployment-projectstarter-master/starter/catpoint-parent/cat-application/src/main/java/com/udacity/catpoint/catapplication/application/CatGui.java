package com.udacity.catpoint.catapplication.application;

import com.udacity.catpoint.catapplication.data.SecRepoImpl;
import com.udacity.catpoint.catapplication.data.SecRepo;
import com.udacity.catpoint.catapplication.service.SecSvc;
import com.udacity.catpoint.catservice.service.FakeImageService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * This is the primary JFrame for the application that contains all the top-level JPanels.
 *
 * We're not using any dependency injection framework, so this class also handles constructing
 * all our dependencies and providing them to other classes as necessary.
 */
public class CatGui extends JFrame {
    private SecRepo securityRepository = new SecRepoImpl();
    private FakeImageService imageService = new FakeImageService();
    private SecSvc securityService = new SecSvc(securityRepository, imageService);
    private Display displayPanel = new Display(securityService);
    private Sensor sensorPanel = new Sensor(securityService);
    private Control controlPanel = new Control(securityService, sensorPanel);
    private Image imagePanel = new Image(securityService);

    public CatGui() {
        setLocation(100, 100);
        setSize(600, 850);
        setTitle("Very Secure App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new MigLayout());
        mainPanel.add(displayPanel, "wrap");
        mainPanel.add(imagePanel, "wrap");
        mainPanel.add(controlPanel, "wrap");
        mainPanel.add(sensorPanel);

        getContentPane().add(mainPanel);

    }
}
