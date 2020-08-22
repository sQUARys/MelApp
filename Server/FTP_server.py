import argparse
from http.server import BaseHTTPRequestHandler, HTTPServer
from io import BytesIO
import os

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
import skimage
from tensorflow import keras
import time

model = load_model('/home/roma/Desktop/CNN/models/keras_model_with_new_dataset_15.h5')
accuracy_file = "/home/roma/Desktop/MelApp/Server/accuracy.txt"
file_for_accuracy = "/home/roma/Desktop/MelApp/Server/picture.jpeg"
file_for_im_diff = "/home/roma/Desktop/MelApp/Server/image_diff.jpeg"
modified_image = "modified_im.jpeg"
modifi_full_file = "/home/roma/Desktop/MelApp/Server/modified_im.jpeg"

images_differences = ["original_image_diff.jpeg" , "image_diff.jpeg"]



def image_difference():
    
    imageA = cv2.imread(images_differences[0])
    imageB = cv2.imread(images_differences[1])

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
    
    return imageB
    


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
        if file == file_for_accuracy:
            print("#### IT IS RECEIVED ####")
            print("SO PROBABLY PREDICTION IS -" , model_predict(file))
            if accuracy_file:
                with open(accuracy_file, "r+") as accuracy_read_file: 
                    accuracy_read_file.seek(0)

            with open(accuracy_file, "w+") as accuracy_write_file: 
                 accuracy_write_file.write(model_predict(file))
                 accuracy_write_file.close()
            pass

        if file == file_for_im_diff:
            if os.path.isfile(images_differences[0]) == False:
                print("FIRST FILE FOR IMD IS UPLOADED, PLEASE SEND A SECOND")
                os.rename(images_differences[1] , images_differences[0])
                
            if os.path.isfile(images_differences[0]) == True and os.path.isfile(images_differences[1]) == True:
                if os.path.isfile(modified_image):
                    with open(modified_image, "r+") as modified_read_file: 
                        modified_read_file.seek(0)
                        
                with open(modified_image, "w+") as modified_write_file: 
                    print("Second file is Uploaded")
                    modifi_im_method = image_difference()
                    print(modifi_im_method)
                    cv2.imwrite(modified_image , modifi_im_method)
                    print("cv2.imwrite")
                    os.remove(images_differences[0])
                    os.remove(images_differences[1])
                    modified_write_file.close()
                

    
if __name__ == "__main__":

    authorizer = DummyAuthorizer()
    authorizer.add_user("user", "12345", "/home/roma/Desktop/MelApp/Server", perm="elradfmw")
    authorizer.add_anonymous("/home")

    handler = MyHandler
    handler.authorizer = authorizer

    server = FTPServer(("192.168.1.104", 21), handler)
    server.serve_forever()
