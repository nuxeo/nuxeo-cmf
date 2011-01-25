#!/usr/bin/python

import reportlab.pdfgen.canvas
from reportlab.lib import colors
from reportlab.lib.units import inch
from pyPdf import PdfFileWriter, PdfFileReader

def createPDF(file, title):
    canvas = reportlab.pdfgen.canvas.Canvas(file)
    canvas.setFont('Times-BoldItalic', 12)
    canvas.drawString(inch, 10.5 * inch, title)

    canvas.setFont('Times-Roman', 10)
    canvas.drawCentredString(4.135 * inch, 0.75 * inch,
                            'Page %d' % canvas.getPageNumber())
    canvas.showPage()
    canvas.save()

def mergePDF(input_file_1, input_file_2, output_file):
    output = PdfFileWriter()
    input1 = PdfFileReader(file(input_file_1, "rb"))
    input2 = PdfFileReader(file(input_file_2, "rb"))
    
    for i in range(0, input1.getNumPages()):
        output.addPage(input1.getPage(i))

    for i in range(0, input2.getNumPages()):
        output.addPage(input2.getPage(i))
        
    outputStream = file(output_file, "wb")
    output.write(outputStream)
    outputStream.close()


if __name__ in ('main', '__main__'):
    mergePDF("4000pages.pdf","1000pages.pdf","5000pages.pdf")





