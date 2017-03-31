import mysql.connector
from Tkinter import *
import ttk
from gui import GUI
import subprocess
import os
# the callback for clicking analyze on the gui

def analyze_callback(myGui):
     module, data =  myGui.getModuleAndData()
     if not module:
          print "Please select a module"
     if not data:
          print "Please select a data set"
     if module and data:
          module, language = lookupModule(module)
          print os.path.dirname(os.path.realpath(__file__))
          if module and language:
               myGui.setText("Analyzing...")
               test = subprocess.Popen([language, module, data, "test2"], cwd="bin/", stdout=subprocess.PIPE)
               output = test.stdout.read()
               myGui.setText(output)

#connect to mysql database and get list of data tables
def connect():
     connection = mysql.connector.connect(
                    host = 'localhost',
                    user = 'root',
                    passwd = 'T3rti@ry')

     cursor = connection.cursor()
     cursor.execute("USE well_2016_schema")
     cursor.execute("SHOW TABLES")

     dataList = []
     for table_name in cursor:
          dataList.append(table_name[0])

     return dataList

def lookupModule(moduleName):
     moduleFile = open("modules.txt", "r")
     for line in moduleFile:
          lineArr = line.split(",")
          if lineArr[0] == moduleName:
               return (lineArr[1].rstrip(), lineArr[2].rstrip())
     return None


def getModules():
     moduleList = []
     moduleFile = open("modules.txt", "r")
     for line in moduleFile:
          lineArr = line.split(",")
          moduleList.append(lineArr[0])
     return moduleList

gui = GUI(analyze_callback)
dataList = connect()
moduleList = getModules()
for table in dataList:
     print table

for table in dataList:
     gui.addData(table)

for module in moduleList:
     gui.addModule(module)
     print lookupModule(module)

gui.begin()