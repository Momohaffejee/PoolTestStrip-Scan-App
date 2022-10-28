import cv2
import numpy as np
import base64
from os.path import dirname, join
from skimage.color import convert_colorspace
import PIL
import colorsys
import extcolors
from com.chaquo.python import Python
import io
import glob
import random
from decimal import Decimal
from sklearn.cluster import KMeans

def rgb_to_hsv(rgb):
    rgb = rgb[0]/255.0, rgb[1]/255.0, rgb[2]/255.0
    mx = max(rgb)
    mn = min(rgb)
    df = mx-mn
    if mx == mn:
        h = 0
    elif mx == rgb[0]:
        h = (60 * ((rgb[1]-rgb[2])/df) + 360) % 360
    elif mx == rgb[1]:
        h = (60 * ((rgb[2]-rgb[0])/df) + 120) % 360
    elif mx == rgb[2]:
        h = (60 * ((rgb[0]-rgb[1])/df) + 240) % 360
    if mx == 0:
        s = 0
    else:
        s = (df/mx)*100
    v = mx*100
    return np.array((int(h),int(s),int(v)))


def rgb_lab(input_color):
    rgb = [0, 0, 0]
    for i in range(0, len(input_color)):
        rgb[i] = input_color[i]/255.0

    X = rgb[0]*0.4124+rgb[1]*0.3576+rgb[2]*0.1805
    Y = rgb[0]*0.2126+rgb[1]*0.7152+rgb[2]*0.0722
    Z = rgb[0]*0.0193+rgb[1]*0.1192+rgb[2]*0.9505
    xyz = [X, Y, Z]
    xyz[0] /= 95.045/100
    xyz[1] /= 100.0/100
    xyz[2] /= 108.875/100

    L = 0
    for i in range(0, 3):
        v = xyz[i]
        if v > 0.008856:
            v = pow(v, 1.0/3)
            if i == 1:
                L = 116.0*v-16.0
        else:
            v *= 7.787
            v += 16.0/116
            if i == 1:
                L = 903.3*xyz[i]
        xyz[i] = v

    a = 500.0*(xyz[0]-xyz[1])
    b = 200.0*(xyz[1]-xyz[2])
    Lab = np.array((int(L), int(a), int(b)))
    return Lab


class ObjDetection:

    def __init__(self):
        self.classes = ['Patch']
        self.yoloWeights = self.readWeights()
        self.yoloCfg = self.readCfg()
        self.yoloNet = self.readNet()
        self.yoloLayer = self.setLayer()
        self.output_layers = []
        self.yoloUnC = self. setUnconnected()

        self.files_dir   = str(Python.getPlatform().getApplication().getFilesDir())

    def readWeights(self):
        weights = join(dirname(__file__), "yolov3_training_last.weights")
        return weights

    def readCfg(self):
        cfg = join(dirname(__file__), "yolov3_testing.cfg")
        return cfg

    def readNet(self):
        net = cv2.dnn.readNet(self.yoloWeights, self.yoloCfg)
        return net

    def setLayer(self):
        net = self.yoloNet
        layer_names = net.getLayerNames()
        return layer_names

    def setUnconnected(self):
        net = self.yoloNet
        unconnected = net.getUnconnectedOutLayers()
        return unconnected

    # def make_histogram(self,cluster):
    #   """
    #   Count the number of pixels in each cluster
    #   :param: KMeans cluster
    #   :return: numpy histogram
    #   """
    #   numLabels = np.arange(0, len(np.unique(cluster.labels_)) + 1)
    #   hist, _ = np.histogram(cluster.labels_, bins=numLabels)
    #   hist = hist.astype('float32')
    #   hist /= hist.sum()
    #   return hist

    # def make_bar(self,height, width, color):
    #   """
    #   Create an image of a given color
    #   :param: height of the image
    #   :param: width of the image
    #   :param: BGR pixel values of the color
    #   :return: tuple of bar, rgb values, and hsv values
    #   """
    #   bar = np.zeros((height, width, 3), np.uint8)
    #   bar[:] = color
    #   red, green, blue = int(color[2]), int(color[1]), int(color[0])
    #   rgb = cv2.cvtColor(bar,cv2.COLOR_BGR2RGB)
    #   hsv = cv2.cvtColor(bar,cv2.COLOR_RGB2HSV)
    #   lab = cv2.cvtColor(bar,cv2.COLOR_BGR2Lab)
    #   hsl_bar = cv2.cvtColor(bar,cv2.COLOR_BGR2HLS)
    #   hue, sat,val = hsv[0][0]
    #   L , A , B     = lab[0][0]

    # return bar, np.array((red, green, blue)),np.array((hue,sat,val)) , np.array((L,A,B))

    def color_balance(self, img, low_per, high_per):
        '''Contrast stretch image by histogram equilization with black and white cap'''

        tot_pix = img.shape[1] * img.shape[0]
        # no.of pixels to black-out and white-out
        low_count = tot_pix * low_per / 100
        high_count = tot_pix * (100 - high_per) / 100

        cs_img = []
        # for each channel, apply contrast-stretch
        for ch in cv2.split(img):
            # cummulative histogram sum of channel
            cum_hist_sum = np.cumsum(cv2.calcHist(
                [ch], [0], None, [256], (0, 256)))

            # find indices for blacking and whiting out pixels
            li, hi = np.searchsorted(cum_hist_sum, (low_count, high_count))
            if (li == hi):
                cs_img.append(ch)
                continue
            # lut with min-max normalization for [0-255] bins
            lut = np.array([0 if i < li
                            else (255 if i > hi else round((i - li) / (hi - li) * 255))
                            for i in np.arange(0, 256)], dtype='uint8')
            # constrast-stretch channel
            cs_ch = cv2.LUT(ch, lut)
            cs_img.append(cs_ch)

        return cv2.merge(cs_img)

    def THardness(self, THcolor, THardnessArray, THardnessArrayResult):
        distValues = []
        for value in THardnessArray:

            dist = np.linalg.norm(THcolor - value)
            distValues.append(dist)

            minpos = distValues.index(min(distValues))
        return THardnessArrayResult[minpos]

    def TBromine(self, TBColor, TBromineArray, TBromineArrayResult):
        distValues = []
        for value in TBromineArray:

            dist = np.linalg.norm(TBColor - value)
            distValues.append(dist)

            minpos = distValues.index(min(distValues))
        return TBromineArrayResult[minpos]

    def FChlorine(self, FCColor, FChlorineArray, FChlorineArrayResult):
        distValues = []
        for value in FChlorineArray:

            dist = np.linalg.norm(FCColor - value)
            distValues.append(dist)

            minpos = distValues.index(min(distValues))
        return FChlorineArrayResult[minpos]

    def pH(self, pHColor, pHArray, pHArrayResult):
        distValues = []
        for value in pHArray:

            dist = np.linalg.norm(pHColor - value)
            distValues.append(dist)

            minpos = distValues.index(min(distValues))
        return pHArrayResult[minpos]

    def TAcidity(self, TAColor, TAcidityArray, TAcidityArrayResult):
        distValues = []
        for value in TAcidityArray:
            dist = np.linalg.norm(TAColor - value)
            distValues.append(dist)

            minpos = distValues.index(min(distValues))
        return TAcidityArrayResult[minpos]

    def decodeData(self, data):
        decode_data = base64.b64decode(data)
        numpy_data = np.fromstring(decode_data, np.uint8)
        frame = cv2.imdecode(numpy_data, cv2.IMREAD_UNCHANGED)
        return frame

    def objectdetection(self, data):

        img = self.decodeData(data)
        #img = cv2.imread(data)
        #  Load Yolo
        net = self.yoloNet
        # net = cv2.dnn.readNet("yolov3_training_last.weights", "yolov3_testing.cfg")

        #  Name custom object
        classes = ["Patch"]

        layer_names = self.yoloLayer

        for i in self.yoloUnC:
            self.output_layers.append(layer_names[int(i)-1])

        colors = np.random.uniform(0, 255, size=(len(classes), 3))

        'Color balanced image'
        ColorBImg = self.color_balance(img, 2, 1)

        "Resize Images"
        img = cv2.resize(img, None, fx=0.8, fy=0.8)
        ColorBImg = cv2.resize(ColorBImg, None, fx=0.8, fy=0.8)

        height, width, channels = img.shape

        # Detecting objects
        blob = cv2.dnn.blobFromImage(
            img, 0.00392, (416, 416), (0, 0, 0), True, crop=False)

        net.setInput(blob)
        outs = net.forward(self.output_layers)

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
                    # print(class_id)
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
        # print(indexes)
        font = cv2.FONT_HERSHEY_PLAIN
        for i in range(len(boxes)):
            if i in indexes:
                x, y, w, h = boxes[i]
                label = str(classes[class_ids[i]])
                color = colors[class_ids[i]]
                cv2.rectangle(img, (x, y), (x + w, y + h), color, 2)
                # cv2.putText(img, label, (x, y + 30), font, 3, color, 2)

        "Canny Edge Detection of Patches"
        edges = cv2.Canny(img, 100, 200)

        "Dilation Technique"
        kernel = np.ones((5, 5), np.uint8)
        dilation_Image = cv2.dilate(edges, kernel, iterations=1)

        "Find Contours of patches"
        contours = cv2.findContours(
            dilation_Image, cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)[0]

        Patches = []
        for cnt in contours:
            if cv2.contourArea(cnt) > 1200:  # filter small contours
                # offsets - with this you get 'mask'
                x, y, w, h = cv2.boundingRect(cnt)
                cv2.rectangle(ColorBImg, (x, y), (x+w, y+h), (0, 255, 0), 2)
                Patches.append(ColorBImg[y: y+h, x: x+w])
                
                
        
        return Patches

    def colorExtraction(self, Patches):
        RGBcolors = []
        HSVcolors = []
        LABcolors = []
        colorsIMG = []
        ColorHSV = []
        i = 0

        for color in Patches:

            if i % 2 == 0 and i > 1:

              
                # Convert array to Image
                img = PIL.Image.fromarray(color)
                colors, pixel_count = extcolors.extract_from_image(img)

                i = 1
                for RGBtuple in colors:
                    if i == 1:
                        rgbdominantColor = RGBtuple[0]
                        rgb = np.array(rgbdominantColor)
                    i = 1+1
                #ct = ColorThief(img)
                #colorpalette = ct.get_palette(color_count=1)
                #rgb_dominantColor = colorpalette(colorpalette[1])
                hsv = rgb_to_hsv(rgb)
                Lab = rgb_lab(rgb)
                HSVcolors.append(hsv)
                LABcolors.append(Lab)
                RGBcolors.append(rgb)

                # finally, we'll output a graphic showing the colors in order
             #  for index, rows in enumerate(combined):
             #     bar, rgb, hsv, lab = self.make_bar(100, 100, rows[1])
             #     RGBcolors.append(rgb)
             #     HLScolors.append(hsv)
             #     LABcolors.append(lab)
             #     print(lab)
             #     colorsIMG.append(color)
 
            i = i+1
          
        return RGBcolors, LABcolors, HSVcolors

    def Comparison(self, RGBcolors, HSVcolors, LABcolors):
     ''' Reference color RGB values , Results and Recommendations '''
     if len(RGBcolors) ==5 and len(HSVcolors) == 5 and len(LABcolors)==5:
        RGBTHArray = [np.array((111, 95, 42)), np.array((121, 110, 90)), np.array((57, 55, 61)), np.array((70, 53, 87)), np.array((70, 53, 124))]
        HSVTHArray = [np.array((46, 62, 43)), np.array((38, 25, 47)), np.array((260, 9, 23)), np.array((270,39, 43)), np.array((258,59,48))]
        LABTHArray = [np.array((67, -3, 28)), np.array((71, 0, 8)), np.array((53, 2, -3)), np.array((55,12,-14)), np.array((55,22,-29))]
        THArrayResult = [0,100, 200, 400, 800]

        RGBTBArray = [np.array((173,221 ,227)), np.array((148, 220, 220)), np.array((144, 216, 203)), np.array((155, 206, 172)), np.array((155, 207, 113)), np.array((157,207, 113))]
        HSVTBArray = [np.array((186, 23, 89)), np.array((180, 32, 86)), np.array((169,33, 84)), np.array((140, 24, 80)), np.array((91, 45, 81)), np.array((91,45, 81))]
        LABTBArray = [np.array((92, -7, -4)), np.array((91, -12, -4)), np.array((90,-13, -1)), np.array((89, -12, 5)), np.array((89, -18, 23)), np.array((89, -18, 23))]
        TBArrayResult = [0.00,1.00,2.00,4.00,10.00,20.00]

        RGBFCArray = [np.array((141, 219, 226)), np.array((171, 218, 209)), np.array((143, 207, 217)), np.array((152, 203, 146)), np.array((141, 200, 125)), np.array((131, 175, 28))]
        HSVFCArray = [np.array((189, 37, 88)), np.array((168, 21, 85)), np.array((146, 30, 81)), np.array((113, 28, 79)), np.array((107, 37, 78)), np.array((77, 84, 68))]
        LABFCArray = [np.array((91, -12, -6)), np.array((92, -8, 0)), np.array((89, -15, 5)), np.array((88, -14, 12)), np.array((87, -18, 17)), np.array((82, -26, 56))]
        FCArrayResult = [ 0.00, 1.00,2.00,3.00,5.00,10.00]

        RGBpHArray = [np.array((80, 178, 248)), np.array((67, 159, 248)), np.array((87, 131, 248)), np.array((89, 89, 233)), np.array((114, 82, 230))]
        HSVpHArray = [np.array((205, 67,97)), np.array((209, 72, 97)), np.array((223, 64, 97)), np.array((240, 61, 91)), np.array((252, 65, 90))]
        LABpHArray = [np.array((83, -12,-23)), np.array((80, -9, -28)), np.array((76, 4, -34)), np.array((68, 19, -42)), np.array((68,27,-41))]
        pHArrayResult = [6.2, 6.8 , 7.2, 7.8 , 8.4]

        RGBTAArray = [np.array((12, 192, 221)), np.array((17, 147, 146)), np.array((51, 111, 80)), np.array((71, 95, 30)), np.array((104, 118, 22)), np.array((116, 119, 16))]
        HSVTAArray = [np.array((188, 94, 86)), np.array((179, 88, 57)), np.array((149, 54, 43)), np.array((82, 68, 37)), np.array((68, 81, 46)), np.array((61, 86, 46))]
        LABTAArray = [np.array((82,-36, 18)), np.array((74, -34, -9)), np.array((67, -22, 6)), np.array((64, -18, 32)), np.array((71, -17, 48)), np.array((71, -15, 54))]
        TAArrayResult = [0.00, 40.00, 80.00, 120.00,180.00,240.00]

        RGBresultTA = self.TAcidity(
            RGBcolors[0], TAcidityArray=RGBTAArray, TAcidityArrayResult=TAArrayResult)
        RGBresultpH = self.pH(
            RGBcolors[1], pHArray=RGBpHArray, pHArrayResult=pHArrayResult)
        RGBresultFC = self.FChlorine(
            RGBcolors[2], FChlorineArray=RGBFCArray, FChlorineArrayResult=FCArrayResult)
        RGBresultTB = self.TBromine(
            RGBcolors[3], TBromineArray=RGBTBArray, TBromineArrayResult=TBArrayResult)
        RGBresultTH = self.THardness(
            RGBcolors[4], THardnessArray=RGBTHArray, THardnessArrayResult=THArrayResult)

        HSVresultTA = self.TAcidity(
            HSVcolors[0], TAcidityArray=HSVTAArray, TAcidityArrayResult=TAArrayResult)
        HSVresultpH = self.pH(
            HSVcolors[1], pHArray=HSVpHArray, pHArrayResult=pHArrayResult)
        HSVresultFC = self.FChlorine(
            HSVcolors[2], FChlorineArray=HSVFCArray, FChlorineArrayResult=FCArrayResult)
        HSVresultTB = self.TBromine(
            HSVcolors[3], TBromineArray=HSVTBArray, TBromineArrayResult=TBArrayResult)
        HSVresultTH = self.THardness(
            HSVcolors[4], THardnessArray=HSVTHArray, THardnessArrayResult=THArrayResult)

        LABresultTA = self.TAcidity(
            LABcolors[0], TAcidityArray=LABTAArray, TAcidityArrayResult=TAArrayResult)
        LABresultpH = self.pH(
            LABcolors[1], pHArray=LABpHArray, pHArrayResult=pHArrayResult)
        LABresultFC = self.FChlorine(
            LABcolors[2], FChlorineArray=LABFCArray, FChlorineArrayResult=FCArrayResult)
        LABresultTB = self.TBromine(
            LABcolors[3], TBromineArray=LABTBArray, TBromineArrayResult=TBArrayResult)
        LABresultTH = self.THardness(
            LABcolors[4], THardnessArray=LABTHArray, THardnessArrayResult=THArrayResult)
        
        return [ HSVresultTH ,HSVresultTB ,HSVresultFC ,HSVresultpH ,HSVresultTA]
     else: 
        return [100,2,2,7.2,120]
        

    def run(self, data):
        Patches = self.objectdetection(data)
        rgb, lab, hsv = self.colorExtraction(Patches=Patches)
        
        result = self.Comparison(rgb,hsv,lab)
        return result

objDect = ObjDetection()


def main(data):

    result = objDect.run(data)
    #print(result)
    #result = "Hello"
    return result



