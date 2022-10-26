from DUALandLIME import enhance_image_exposure
import matplotlib.pyplot as plt
import cv2
from sklearn.cluster import KMeans
from ImageEnhancementFuncs import *
from DominantColor import *

image  = cv2.imread("images/sample15.jpeg")

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

ColorBalImg = color_balance(Enhanced_image_LINE,2,1)
colorcb = simplest_cb(image,1)

gray = cv2.cvtColor(color_balanced_img,cv2.COLOR_BGR2GRAY)

''' Filter Image to reduce noise '''
Blurred = cv2.medianBlur(gray,5)

'''Adaptive Threshloding '''
th1 = cv2.adaptiveThreshold(Blurred,255,cv2.ADAPTIVE_THRESH_MEAN_C,cv2.THRESH_BINARY,15,1)

'''Apply Binarization on Adaptive Thresholded image'''
ret, thresh = cv2.threshold(th1, 150, 255, cv2.THRESH_BINARY_INV)

#cv2.imshow("Horizontal_image",thresh)
#cv2.waitKey(0)

'''Obtain horizontal lines mask'''
horizontal_kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (25,1))
horizontal_mask = cv2.morphologyEx(thresh, cv2.MORPH_OPEN, horizontal_kernel, iterations=1)
horizontal_mask = cv2.dilate(horizontal_mask, horizontal_kernel, iterations=9)

#cv2.imshow("Horizontal_image",horizontal_mask)

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
contours  = cv2.findContours(dilated_img, cv2.RETR_TREE ,cv2.CHAIN_APPROX_NONE)[0]

'''Detecting Colors on the Reference Chart by detecting rectangles/sqaures in the image from the obtained contours'''
cnts = contours
Color_Components = [ ]

ColorBalImg = color_balance(image,2,1)

for cnt in cnts:
    if cv2.contourArea(cnt) >3000 : # filter small contours
        x,y,w,h = cv2.boundingRect(cnt) # offsets - with this you get 'mask'
        cv2.rectangle(ColorBalImg,(x,y),(x+w,y+h),(0,255,0),2)
        Color_Components.append(ColorBalImg[y:y+h,x:x+w])


patches = [ ]
LW = (0,0,221)
UW = (180,30,255)
decide = False
ColorHSV = []

for color in Color_Components:  
    
    cv2.imshow("Patch Color",color)
    cv2.waitKey(0)

    height, width, _ = np.shape(color)

    # reshape the image to be a simple list of RGB pixels
    image = color.reshape((height * width, 3))

    # we'll pick the 5 most common colors
    num_clusters = 1
    clusters = KMeans(n_clusters=num_clusters)
    clusters.fit(image)

    # count the dominant colors and put them in "buckets"
    histogram = make_histogram(clusters)

    # then sort them, most-common first
    combined = zip(histogram, clusters.cluster_centers_)
    combined = sorted(combined, key=lambda x: x[0], reverse=True)

    # finally, we'll output a graphic showing the colors in order
    for index, rows in enumerate(combined):
      bar, rgb, hsv = make_bar(100, 100, rows[1])
      if hsv[0]>LW[0] and hsv[1]>LW[1] and hsv[2]>LW[2]and hsv[0]<UW[0] and hsv[1]<UW[1] and hsv[2]<UW[2] or decide == False:
        #print('HSV value:',hsv) 
        decide = True

      else:
        print('HSV value:',hsv)
        ColorHSV.append(hsv)
        decide = False
     
      patches.append(color)


'''Resize Image'''
Image1 = ColorBalImg
scale_percent = 40 # percent of original size
width = int(Image1.shape[1] * scale_percent / 100)
height = int(Image1.shape[0] * scale_percent / 100)
dim = (width, height)
# resize image
resized = cv2.resize(Image1, dim, interpolation = cv2.INTER_AREA)
cv2.imshow("Resized Images",resized)

cv2.waitKey(0)
cv2.destroyAllWindows()