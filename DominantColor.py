import cv2
import numpy as np
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


def sort_hsvs(hsv_list):
    """
    Sort the list of HSV values
    :param hsv_list: List of HSV tuples
    :return: List of indexes, sorted by hue, then saturation, then value
    """
    bars_with_indexes = []
    for index, hsv_val in enumerate(hsv_list):
        bars_with_indexes.append((index, hsv_val[0], hsv_val[1], hsv_val[2]))
    bars_with_indexes.sort(key=lambda elem: (elem[1], elem[2], elem[3]))
    return [item[0] for item in bars_with_indexes]
