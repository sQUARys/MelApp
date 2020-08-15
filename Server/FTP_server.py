import argparse
from http.server import BaseHTTPRequestHandler, HTTPServer
from io import BytesIO

from cv2 import cv2
import imutils
import matplotlib.pyplot as plt
import numpy as np
import tensorflow as tf
from keras.models import load_model
from PIL import Image, ImageDraw
from pyftpdlib.authorizers import DummyAuthorizer
from pyftpdlib.handlers import FTPHandler
from pyftpdlib.servers import FTPServer
from skimage.measure import compare_ssim
from tensorflow import keras


model = load_model('/home/roma/Desktop/CNN/models/keras_model_with_new_dataset_15.h5')
accuracy_file = "/home/roma/Desktop/Server/accuracy.txt"
f_image = ""

def image_difference():

    imageA = cv2.imread("first.jpg")
    imageB = cv2.imread("second.jpg")

    grayA = cv2.cvtColor(imageA, cv2.COLOR_BGR2GRAY)
    grayB = cv2.cvtColor(imageB, cv2.COLOR_BGR2GRAY)

    (score, diff) = compare_ssim(grayA, grayB, full=True)
    diff = (diff * 255).astype("uint8")

    thresh = cv2.threshold(diff, 0, 255,cv2.THRESH_BINARY_INV | cv2.THRESH_OTSU)[1]
    cnts = cv2.findContours(thresh.copy(), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    cnts = imutils.grab_contours(cnts) 

    for c in cnts:
	    (x, y, w, h) = cv2.boundingRect(c)
	    cv2.rectangle(imageA, (x, y), (x + w, y + h), (0, 0, 255), 2)
	    cv2.rectangle(imageB, (x, y), (x + w, y + h), (0, 0, 255), 2)

    cv2.imshow("Original", imageA)
    cv2.imshow("Modified", imageB)
    cv2.waitKey(0)


def model_predict(image):
    im = Image.open(image)
    im.thumbnail((128 , 128), Image.ANTIALIAS)
    img = (np.expand_dims(im,0))
    predictions_single = model.predict(img)
    predictions_single_string = np.array_str(predictions_single)
    return predictions_single_string

class MyHandler(FTPHandler):
    def on_login(self, username):
        print("LOGIN...")
        pass

    def on_file_received(self, file):
        print("IT IS RECEIVED.")
        print("SO PROBABLY PREDICTION IS -" , model_predict(file))
        if accuracy_file:
            with open(accuracy_file, "r+") as accuracy_read_file: 
                accuracy_read_file.seek(0)

        with open(accuracy_file, "w+") as accuracy_write_file: 
                 accuracy_write_file.write(model_predict(file))
                 accuracy_write_file.close()
        pass


    
if __name__ == "__main__":

    authorizer = DummyAuthorizer()
    authorizer.add_user("user", "12345", "/home/roma/Desktop/Server", perm="elradfmw")
    authorizer.add_anonymous("/home")

    handler = MyHandler
    handler.authorizer = authorizer

    server = FTPServer(("192.168.1.104", 21), handler)
    image_difference()
    server.serve_forever()
