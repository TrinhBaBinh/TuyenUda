package com.udacity.catpoint.catservice.service;

public interface ImageService {
    public boolean imageContainsCat(java.awt.image.BufferedImage image, float confidenceThreshhold);
}
