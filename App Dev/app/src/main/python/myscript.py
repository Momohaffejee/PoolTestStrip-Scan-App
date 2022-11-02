
import numpy as np
import random
import glob
import cv2
from PIL import Image
import base64
import io
from sklearn.cluster import KMeans 

def main(data):
    #net = cv2.dnn.readNet("yolov3_training_last.weights", "yolov3_testing.cfg")
    decoded_data = base64.b64decode(data)
    np_data = np.fromstring(decoded_data, np.uint8)
    img = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)
    img_gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
    pil_im = Image.fromarray(img_gray)

    buff = io.BytesIO()
    pil_im.save(buff, format="PNG")
    img_str = base64.b64decode(buff.getvalue())
    
    return "hello"