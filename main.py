from tkinter import S
from DUALandLIME import enhance_image_exposure
import matplotlib.pyplot as plt
import cv2
from ImageEnhancement import *
from sklearn.cluster import KMeans

image  = cv2.imread("images/sample11.jpeg")

'''Image Enhancement Methods and Techniques'''
Enhanced_image = enhance_image_exposure(image,0.6,0.15)
Enhanced_image_LINE = enhance_image_exposure(image,0.6,0.15,False)

'''Convert RGB to HSV/LAB'''
zeros = np.zeros(image.shape[:2], dtype="uint8")
HSVimage = cv2.cvtColor(Enhanced_image_LINE,cv2.COLOR_RGB2HSV)
LABimage = cv2.cvtColor(Enhanced_image_LINE,cv2.COLOR_RGB2LAB)

(H, S, V) = cv2.split(LABimage)

HSimage = cv2.merge([H,zeros,zeros])

'''Further Image Enhancement Methods and Techniques'''
DoG_image = dog(Enhanced_image,15,100,0)
negative_img = negate(DoG_image)
contrast_stretch_img = contrast_stretch(negative_img, 2, 99.5)
blur_img = fast_gaussian_blur(contrast_stretch_img, 3, 1)
gamma_img = gamma(blur_img, 1.1)
color_balanced_img = color_balance(gamma_img, 2, 1)

cv2.imshow("Enhanced Image",color_balanced_img)

cv2.waitKey(0)
cv2.destroyAllWindows()