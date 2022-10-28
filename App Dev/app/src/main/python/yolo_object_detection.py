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
    hsl_bar = cv2.cvtColor(bar,cv2.COLOR_BGR2HLS)
    LAB_bar = cv2.cvtColor(bar,cv2.COLOR_BGR2LAB)
    hue, lig,sat = hsl_bar[0][0]
    L , A , B     = LAB_bar[0][0]

    return bar, np.array((red, green, blue)),np.array((hue,lig,sat)) , np.array((L,A,B))

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

def THardness(THcolor,THardnessArray,THardnessArrayResult):
  distValues = []
  for value in THardnessArray:

   dist = np.linalg.norm(THcolor - value)
   distValues.append(dist)

  minpos = distValues.index(min(distValues))
  return THardnessArrayResult[minpos]

def TBromine(TBColor,TBromineArray,TBromineArrayResult):
  distValues = []
  for value in TBromineArray:

   dist = np.linalg.norm(TBColor - value)
   distValues.append(dist)

  minpos = distValues.index(min(distValues))
  return TBromineArrayResult[minpos]

def FChlorine(FCColor,FChlorineArray,FChlorineArrayResult):
  distValues = []
  for value in FChlorineArray:

   dist = np.linalg.norm(FCColor - value)
   distValues.append(dist)

  minpos = distValues.index(min(distValues))
  return FChlorineArrayResult[minpos]

def pH(pHColor,pHArray,pHArrayResult):
  distValues = []
  for value in pHArray:

   dist = np.linalg.norm(pHColor - value)
   distValues.append(dist)

  minpos = distValues.index(min(distValues))
  return pHArrayResult[minpos]

def TAcidity(TAColor,TAcidityArray,TAcidityArrayResult):
  distValues = []
  for value in TAcidityArray:

   dist = np.linalg.norm(TAColor - value)
   distValues.append(dist)

  minpos = distValues.index(min(distValues))
  return TAcidityArrayResult[minpos]

def main(img): 

 # Load Yolo
 net = cv2.dnn.readNet("yolov3_training_last.weights", "yolov3_testing.cfg")

 # Name custom object
 classes = ["Patch"]

 layer_names = net.getLayerNames()
 output_layers = [layer_names[i - 1] for i in net.getUnconnectedOutLayers()]
 colors = np.random.uniform(0, 255, size=(len(classes), 3))


 'Color balanced image'
 ColorBImg = color_balance(img,2,1)

 "Resize Images"
 img = cv2.resize(img, None, fx=0.8, fy=0.8)
 ColorBImg =cv2.resize(ColorBImg,None,fx=0.8,fy=0.8)

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
    if cv2.contourArea(cnt) >1200 : # filter small contours
        x,y,w,h = cv2.boundingRect(cnt) # offsets - with this you get 'mask'
        cv2.rectangle(ColorBImg,(x,y),(x+w,y+h),(0,255,0),2)
        Patches.append(ColorBImg[y:y+h,x:x+w])


 cv2.imshow("Patch Color",ColorBImg)
 cv2.waitKey(0)

 RGBcolors = [ ]  
 HLScolors = [ ] 
 LABcolors = [ ] 
 colorsIMG = []
 ColorHSV = []
 i = 0
 for color in Patches:  

  if i%2 == 0 and i>1:
    

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
        bar, rgb, hls, lab = make_bar(100, 100, rows[1])
        RGBcolors.append(rgb)
        HLScolors.append(hls)
        LABcolors.append(lab)
        print(lab)
        colorsIMG.append(color)
        
  i=i+1

 '''Reference color RGB values , Results and Recommendations'''

 RGBTHArray = [np.array((52,98,108)),np.array((88,105,115)),np.array((60,54,57)),np.array((90,50,69)),np.array((118,48,70))]
 HSLTHArray = [np.array((191,35,31)),np.array((202,13,40)),np.array((330,5,22)),np.array((332,29,27)),np.array((341,42,33))]
 LABTHArray = [np.array((39,-13,-11)),np.array((43,-4,-8)),np.array((23,3,-1)),np.array((26,21,-4)),np.array((30,33,2))]
 THArrayResult = ["O ppm","100 ppm","200 ppm","400 ppm","800ppm"]

 RGBTBArray = [np.array((227,219,178)),np.array((224,220,140)),np.array((200,211,145)),np.array((173,205,156)),np.array((108,201,153)),np.array((82,191,175))]
 HSLTBArray = [np.array((50,47,79)),np.array((57,58,71)),np.array((70,43,70)),np.array((99,33,71)),np.array((149,46,61)),np.array((171,46,54))]
 LABTBArray = [np.array((87,-4,21)),np.array((86,-10,40)),np.array((82,-14,32)),np.array((79,-20,21)),np.array((74,-39,16)),np.array((71,-35,-1))]
 TBArrayResult = ["O ppm","1 ppm","2 ppm","4 ppm","10 ppm","20 ppm"]

 RGBFCArray =  [np.array((228,219,141)),np.array((204,215,167)),np.array((172,206,142)),np.array((172,206,142)),np.array((126,199,142)),np.array((30,177,132))]
 HSLFCArray =  [np.array((54,62,72)),np.array((74,38,75)),np.array((92,40,68)),np.array((92,40,68)),np.array((133,39,64)),np.array((162,71,41))]
 LABFCArray =  [np.array((87,-8,39)),np.array((84,-12,22)),np.array((79,-23,28)),np.array((79,-23,28)),np.array((74,-35,22)),np.array((64,-47,13))]
 FCArrayResult = ["O ppm","1 ppm","2 ppm","3 ppm","5 ppm","10 ppm"]

 RGBpHArray = [np.array((247,179,85)),np.array((233,171,106)),np.array((245,133,86)),np.array((229,95,96)),np.array((220,82,112))]
 HSLpHArray = [np.array((35,91,65)),np.array((31,74,66)),np.array((18,89,65)),np.array((360,72,64)),np.array((347,66,59))]
 LABpHArray = [np.array((78,16,56)),np.array((75,16,42)),np.array((67,39,44)),np.array((58,52,26)),np.array((54,56,12))]
 pHArrayResult = ["6.2 ","6.8 ","7.2 ","7.8 "," 8.4 "]

 RGBTAArray =  [np.array((211,190,60)),np.array((147,147,23)),np.array((70,98,43)),np.array((28,94,69)),np.array((13,118,103)),np.array((14,111,111))]
 HSLTAArray =  [np.array((52,63,53)),np.array((60,73,33)),np.array((91,39,28)),np.array((157,54,24)),np.array((171,80,26)),np.array((180,78,25))]
 LABTAArray =  [np.array((77,-7,65)),np.array((59,-14,59)),np.array((38,-21,28)),np.array((35,-27,8)),np.array((44,-31,0)),np.array((42,-25,-7))]
 TAArrayResult = ["O ppm","40 ppm","80 ppm","120 ppm","180 ppm","240 ppm"]


 for C in colorsIMG:
  cv2.imshow("Colors",C) 
  cv2.waitKey(0)

 RGBresultTA = TAcidity(RGBcolors[0],TAcidityArray=RGBTAArray,TAcidityArrayResult=TAArrayResult)
 RGBresultpH = pH(RGBcolors[1],pHArray=RGBpHArray,pHArrayResult=pHArrayResult)
 RGBresultFC = FChlorine(RGBcolors[2],FChlorineArray=RGBFCArray,FChlorineArrayResult=FCArrayResult)
 RGBresultTB = TBromine(RGBcolors[3],TBromineArray=RGBTBArray,TBromineArrayResult= TBArrayResult)
 RGBresultTH = THardness(RGBcolors[4],THardnessArray=RGBTHArray,THardnessArrayResult= THArrayResult)

 HLSresultTA = TAcidity(HLScolors[0],TAcidityArray=HSLTAArray,TAcidityArrayResult=TAArrayResult)
 HLSresultpH = pH(HLScolors[1],pHArray=HSLpHArray,pHArrayResult=pHArrayResult)
 HLSresultFC = FChlorine(HLScolors[2],FChlorineArray=HSLFCArray,FChlorineArrayResult=FCArrayResult)
 HLSresultTB = TBromine(HLScolors[3],TBromineArray=HSLTBArray,TBromineArrayResult= TBArrayResult)
 HLSresultTH = THardness(HLScolors[4],THardnessArray=HSLTHArray,THardnessArrayResult= THArrayResult)

 LABresultTA = TAcidity(LABcolors[0],TAcidityArray=LABTAArray,TAcidityArrayResult=TAArrayResult)
 LABresultpH = pH(LABcolors[1],pHArray=LABpHArray,pHArrayResult=pHArrayResult)
 LABresultFC = FChlorine(LABcolors[2],FChlorineArray=LABFCArray,FChlorineArrayResult=FCArrayResult)
 LABresultTB = TBromine(LABcolors[3],TBromineArray=LABTBArray,TBromineArrayResult= TBArrayResult)
 LABresultTH = THardness(LABcolors[4],THardnessArray=LABTHArray,THardnessArrayResult= THArrayResult)
 
 print( "Using RBG color Space")
 print("Total Acidity : ",RGBresultTA)
 print("pH            : ",RGBresultpH)
 print("Free Chlorine : ",RGBresultFC)
 print("Total Bromine : ",RGBresultTB)
 print("Total Hardness: ",RGBresultTH)
 
 print(" ")

 print( "Using HLS color Space")
 print("Total Acidity : ",HLSresultTA)
 print("pH            : ",HLSresultpH)
 print("Free Chlorine : ",HLSresultFC)
 print("Total Bromine : ",HLSresultTB)
 print("Total Hardness: ",HLSresultTH)

 print(" ")

 print( "Using LAB color Space")
 print("Total Acidity : ",LABresultTA)
 print("pH            : ",LABresultpH)
 print("Free Chlorine : ",LABresultFC)
 print("Total Bromine : ",LABresultTB)
 print("Total Hardness: ",LABresultTH)


 cv2.imshow("Dilation Image", dilation_Image)
 cv2.waitKey(0)
 cv2.destroyAllWindows()

def getImage(path): 
 return cv2.imread(path)

if __name__ == "__main__":  
 img = getImage("NewDataset/16.jpg")

 main(img)