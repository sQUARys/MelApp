import ftplib

filename = "AUG_0_307.jpeg"
path_to_file = "/home/roma/Desktop/CNN/AUG_0_307.jpeg"

ftp = ftplib.FTP("127.0.0.1")

ftp.login("user" , "12345")

my_file = open(path_to_file  , "rb")

ftp.storbinary('STOR ' + filename , my_file)