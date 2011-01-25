	# -*- coding: iso-8859-15 -*-
"""Creation of file to be imported into cmf

$Id: $
"""
import sys
import os 
import zipfile
import argparse
import random
import shutil
import time
from datetime import datetime
import pdf

class Document:
    pass


if __name__ in ('main', '__main__'):

    today = datetime.today()
    counter = 0
    # parse the arguments
    parser = argparse.ArgumentParser(description='Create file to be imported into Nuxeo.')
    parser.add_argument('-n', '--number-of-pdf', default=14, type=int, help="The number of envelopes created per iteration")
    parser.add_argument('-t', '--temp-directory', default="import_tmp", help="The directory in which we'll create documents")
    parser.add_argument('-p', '--pdf-file-name', default="CASE-{timestamp}-{nb}", help="The name of the created xml file")
    parser.add_argument('-i', '--interval', type=int, default=600, help="The script will run every interval seconds. The script won't be rerun if interval<=0")

    opts = parser.parse_args()

    # Retrieving pdf files
    z = zipfile.ZipFile('./test_resources.zip')
    z.extractall()
    pdf_files = open('./pdf-list.txt', 'r').readlines()

    while 1:
        try:
            for n in range(0, opts.number_of_pdf):
                counter += 1
                file_name = opts.pdf_file_name.format(timestamp=today.strftime('%Y%m%d-%H%M'), nb=n)
                pdf_file_src = random.choice(pdf_files).strip()
                pdf.createPDF(opts.temp_directory + "/temp.pdf", file_name + ": change checksum by adding some text to the portable document format")
                pdf.mergePDF(pdf_file_src, opts.temp_directory + "/temp.pdf", opts.temp_directory + "/" + file_name)
                print "Creating pdf : " + file_name
                os.remove(opts.temp_directory + "/temp.pdf")
            if opts.interval <= 0:
                print "Exiting: Interval time <= 0"
                sys.exit(0)
            print today.strftime('%d/%m/%Y-%Hh%Mm%Ss - ') + "Sleeping for %d seconds" % opts.interval
            time.sleep(opts.interval)
            
        except KeyboardInterrupt:
            print "bye"
            sys.exit(0)

