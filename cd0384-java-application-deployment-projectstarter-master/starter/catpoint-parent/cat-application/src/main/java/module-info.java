module cat.application {
    requires cat.service;
    requires miglayout;
    requires java.desktop;
    requires com.google.common;
    requires com.google.gson;
    requires java.prefs;
    opens com.udacity.catpoint.catapplication.data to com.google.gson;
}