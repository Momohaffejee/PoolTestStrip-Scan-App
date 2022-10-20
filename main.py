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

gray = cv2.cvtColor(color_balanced_img,cv2.COLOR_BGR2GRAY)

''' Filter Image to reduce noise '''
Blurred = cv2.medianBlur(gray,5)

'''Adaptive Threshloding '''
th1 = cv2.adaptiveThreshold(Blurred,255,cv2.ADAPTIVE_THRESH_MEAN_C,cv2.THRESH_BINARY,15,1)

'''Apply Binarization on Adaptive Thresholded image'''
ret, thresh = cv2.threshold(th1, 150, 255, cv2.THRESH_BINARY_INV)

'''Obtain horizontal lines mask'''
horizontal_kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (25,1))
horizontal_mask = cv2.morphologyEx(thresh, cv2.MORPH_OPEN, horizontal_kernel, iterations=1)
horizontal_mask = cv2.dilate(horizontal_mask, horizontal_kernel, iterations=9)

'''Obtain vertical lines mask'''
vertical_kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (1,50))
vertical_mask = cv2.morphologyEx(thresh, cv2.MORPH_OPEN, vertical_kernel, iterations=1)
vertical_mask= cv2.dilate(vertical_mask, vertical_kernel, iterations=9)

'''Bitwise-and masks together'''
result =  cv2.bitwise_or(vertical_mask, horizontal_mask)

'''Apply dilation on src image to fil gaps around edges'''
kernel = np.ones((5,5),np.uint8)
dilated_img = cv2.dilate(result, kernel, iterations = 1)

'''Find Contours in the image '''
contours  = cv2.findContours(dilated_img, cv2.RETR_TREE,cv2.CHAIN_APPROX_NONE)[0]

'''Detecting Colors on the Reference Chart by detecting rectangles/sqaures in the image from the obtained contours'''
cnts = contours
Color_Components = [ ]

ColorBalImg = color_balance(image,2,1)

for cnt in cnts:
    if cv2.contourArea(cnt) >3000 : # filter small contours
        x,y,w,h = cv2.boundingRect(cnt) # offsets - with this you get 'mask'
        cv2.rectangle(Enhanced_image_LINE,(x,y),(x+w,y+h),(0,255,0),2)
        Color_Components.append(Enhanced_image_LINE[y:y+h,x:x+w])


cv2.imshow("Enhanced Image",Enhanced_image_LINE)

cv2.waitKey(0)
cv2.destroyAllWindows()