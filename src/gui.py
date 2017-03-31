from Tkinter import *
import ttk

def exteriorfunc():
     print "ext"

class GUI:
     def __init__(self, buttonFunc):
          self.root = Tk()
          self.initUI()
          self.setFunction(buttonFunc)

     def addModule(self, module):
          self.moduleList.insert(END, module)

     def addData(self, data):
          self.dataList.insert(END, data)

     def setFunction(self, function):
          self.analyzeButton.grid_forget()
          self.analyzeButton = Button(self.root, text="Analyze", command=lambda:function(self))
          self.analyzeButton.grid(column=0, row=4, columnspan=2)

     def initUI(self):
          self.root.geometry("600x600+100+100")
          self.root.title("Testing")

          self.mainframe = ttk.Frame(self.root, padding="3 3 12 12")
          self.mainframe.grid()
          self.moduleList = Listbox(self.root, exportselection=0)
          self.moduleList.grid(column=0, row=1, sticky=N)

          self.dataList = Listbox(self.root, exportselection=0)
          self.dataList.grid(column=1, row=1, sticky=N)

          self.analyzeButton = Button(self.root, text="Analyze")
          self.analyzeButton.grid(column=0, row=2, columnspan=2, sticky=N)
          self.dataListLabel = Label(self.root, text="Data set")
          self.dataListLabel.grid(column=1, row=0)
          self.analysisListLabel = Label(self.root, text="Analysis")
          self.analysisListLabel.grid(column=0, row=0)

          self.messageBoxLabel = Label(self.root, text="Output")
          self.messageBoxLabel.grid(column=2, row=0, columnspan=2)
          self.messagebox = Text(self.root, width=40, height=30)
          self.messagebox.insert(END, "EHYYYYYY")
          self.messagebox.grid(column=2, row=1, columnspan=2, rowspan=5)

     def getSelectedModule(self):
          if self.moduleList.curselection():
               return self.moduleList.get(self.moduleList.curselection())
          else:
               print "no module selected"
               return None
          

     def getSelectedData(self):
          if self.dataList.curselection():
               return self.dataList.get(self.dataList.curselection())
          else:
               print "no table selected"
               return None

     def getModuleAndData(self):
          return (self.getSelectedModule(), self.getSelectedData())

     def setText(self, text):
          self.messagebox.delete(1.0, END)
          self.messagebox.insert(END, text)

     #start tkinter window, hope you set everything up by this point lel
     def begin(self):
          self.root.mainloop()

if __name__ == '__main__':
     gui = GUI(exteriorfunc)
     gui.root.mainloop()