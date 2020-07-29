from pyftpdlib.authorizers import DummyAuthorizer
from pyftpdlib.handlers import FTPHandler
from pyftpdlib.servers import FTPServer
from PIL import Image, ImageDraw
import tensorflow as tf
from tensorflow import keras
from keras.models import load_model
import numpy as np
from io import BytesIO
import argparse
from http.server import BaseHTTPRequestHandler, HTTPServer


model = load_model('/home/roma/Desktop/CNN/models/keras_model_with_new_dataset_15.h5')


def model_predict(image):
    im = Image.open(image)
    im.thumbnail((128 , 128), Image.ANTIALIAS)
    img = (np.expand_dims(im,0))
    predictions_single = model.predict(img)
    predictions_single_string = np.array_str(predictions_single)
    return predictions_single_string

class MyHandler(FTPHandler):
    def on_login(self, username):
        print("LOGIN")
        pass

    def on_file_received(self, file):
        print("IT IS RECEIVED.")
        print("SO PROBABLY PREDICTION IS -" , model_predict(file))
        pass


    
if __name__ == "__main__":

    authorizer = DummyAuthorizer()
    authorizer.add_user("user", "12345", "/home/roma/Desktop/1", perm="elradfmw")
    authorizer.add_anonymous("/home")

    handler = MyHandler
    handler.authorizer = authorizer

    server = FTPServer(("127.0.0.1", 21), handler)
    server.serve_forever()

