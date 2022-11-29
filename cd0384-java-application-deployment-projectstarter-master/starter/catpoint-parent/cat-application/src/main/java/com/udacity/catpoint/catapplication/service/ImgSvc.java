package com.udacity.catpoint.catapplication.service;

import java.awt.image.BufferedImage;

public interface ImgSvc {
    public boolean imageContainsCat(BufferedImage image, float confidenceThreshhold);
}