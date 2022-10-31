import cv2
import cv2
import numpy as np
import glob
import random
from sklearn.cluster import KMeans

def make_histogram(cluster):
    """
    Count the number of pixels in each cluster
    :param: KMeans cluster
    :return: numpy histogram
    """
    numLabels = np.arange(0, len(np.unique(cluster.labels_)) + 1)
    hist, _ = np.histogram(cluster.labels_, bins=numLabels)
    hist = hist.astype('float32')
    hist /= hist.sum()
    return hist


def make_bar(height, width, color):
    """
    Create an image of a given color
    :param: height of the image
    :param: width of the image
    :param: BGR pixel values of the color
    :return: tuple of bar, rgb values, and hsv values
    """
    bar = np.zeros((height, width, 3), np.uint8)
    bar[:] = color
    red, green, blue = int(color[2]), int(color[1]), int(color[0])
    hsv_bar = cv2.cvtColor(bar, cv2.COLOR_BGR2HSV)
    hue, sat, val = hsv_bar[0][0]
    return bar, (red, green, blue), (hue, sat, val)

def color_balance(img, low_per, high_per):
    '''Contrast stretch image by histogram equilization with black and white cap'''
    
    tot_pix = img.shape[1] * img.shape[0]
    # no.of pixels to black-out and white-out
    low_count = tot_pix * low_per / 100
    high_count = tot_pix * (100 - high_per) / 100
    
    cs_img = []
    # for each channel, apply contrast-stretch
    for ch in cv2.split(img):
        # cummulative histogram sum of channel
        cum_hist_sum = np.cumsum(cv2.calcHist([ch], [0], None, [256], (0, 256)))

        # find indices for blacking and whiting out pixels
        li, hi = np.searchsorted(cum_hist_sum, (low_count, high_count))
        if (li == hi):
            cs_img.append(ch)
            continue
        # lut with min-max normalization for [0-255] bins
        lut = np.array([0 if i < li 
                        else (255 if i > hi else round((i - li) / (hi - li) * 255)) 
                        for i in np.arange(0, 256)], dtype = 'uint8')
        # constrast-stretch channel
        cs_ch = cv2.LUT(ch, lut)
        cs_img.append(cs_ch)
        
    return cv2.merge(cs_img)


# Load Yolo
net = cv2.dnn.readNet("yolov3_training_last.weights", "yolov3_testing.cfg")

# Name custom object
classes = ["Patch"]


# Images path
images_path = glob.glob(r"C:\Users\User\Downloads\train_yolo_to_detect_custom_object\yolo_custom_detection\NewDataset\*.jpg")


layer_names = net.getLayerNames()
output_layers = [layer_names[i - 1] for i in net.getUnconnectedOutLayers()]
colors = np.random.uniform(0, 255, size=(len(classes), 3))

# Insert here the path of your images
random.shuffle(images_path)
# loop through all the images

# Loading image
img = cv2.imread(images_path[1])

'Color balanced image'
ColorBImg = color_balance(img,2,1)

"Resize Images"
img = cv2.resize(img, None, fx=0.9, fy=0.9)
ColorBImg =cv2.resize(ColorBImg,None,fx=0.9,fy=0.9)

height, width, channels = img.shape

# Detecting objects
blob = cv2.dnn.blobFromImage(img, 0.00392, (416, 416), (0, 0, 0), True, crop=False)

net.setInput(blob)
outs = net.forward(output_layers)

# Showing informations on the screen
class_ids = []
confidences = []
boxes = []
for out in outs:
        for detection in out:
            scores = detection[5:]
            class_id = np.argmax(scores)
            confidence = scores[class_id]
            if confidence > 0.3:
                # Object detected
                #print(class_id)
                center_x = int(detection[0] * width)
                center_y = int(detection[1] * height)
                w = int(detection[2] * width)
                h = int(detection[3] * height)

                # Rectangle coordinates
                x = int(center_x - w / 2)
                y = int(center_y - h / 2)

                boxes.append([x, y, w, h])
                confidences.append(float(confidence))
                class_ids.append(class_id)

indexes = cv2.dnn.NMSBoxes(boxes, confidences, 0.5, 0.4)
#print(indexes)
font = cv2.FONT_HERSHEY_PLAIN
for i in range(len(boxes)):
        if i in indexes:
            x, y, w, h = boxes[i]
            label = str(classes[class_ids[i]])
            color = colors[class_ids[i]]
            cv2.rectangle(img, (x, y), (x + w, y + h), color, 2)
            #cv2.putText(img, label, (x, y + 30), font, 3, color, 2)

"Canny Edge Detection of Patches"
edges = cv2.Canny(img,100,200)

"Dilation Technique"
kernel = np.ones((5,5),np.uint8)
dilation_Image = cv2.dilate(edges, kernel, iterations = 1)

"Find Contours of patches"
contours  = cv2.findContours(dilation_Image, cv2.RETR_TREE ,cv2.CHAIN_APPROX_NONE)[0]

Patches = []
for cnt in contours:
    if cv2.contourArea(cnt) >2200 : # filter small contours
        x,y,w,h = cv2.boundingRect(cnt) # offsets - with this you get 'mask'
        cv2.rectangle(ColorBImg,(x,y),(x+w,y+h),(0,255,0),2)
        Patches.append(ColorBImg[y:y+h,x:x+w])


cv2.imshow("Patch Color",ColorBImg)
cv2.waitKey(0)

colors = [ ]
ColorHSV = []
i = 0
for color in Patches:  

 if i%2 == 0 :
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
        print('RGB value:',rgb)
        colors.append(color)
 i=i+1

cv2.imshow("Dilation Image", dilation_Image)
cv2.waitKey(0)
cv2.destroyAllWindows()