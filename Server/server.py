import hug
import base64
import subprocess
import spacy
from spacy.pipeline import EntityRecognizer
from spacy.gold import GoldParse
from spacy.tagger import Tagger
import random
import json

@hug.post('/upload')
def upload_file(body):
    """accepts file uploads"""
    with open("/mnt/data/body.jpeg", "wb") as f:
        f.write(body['image'])
    subprocess.call('tesseract /mnt/data/body.jpeg out -l eng', shell=True)
    nlp = spacy.load('en')
    output_directory='/contracts_out/'
    if output_directory:
       print("Loading from", output_directory)
       nlp2 = spacy.load('en', path=output_directory)
       nlp2.entity.add_label('CONTRACT_DATE')
       nlp2.entity.add_label('BORROWER')
       nlp2.entity.add_label('LOANER')
       nlp2.entity.add_label('LOAN_MONTHS')
       nlp2.entity.add_label('AMOUNT')
       nlp2.entity.add_label('INTEREST')
       file = open('/home/ill/out.txt', 'r') 
       text = file.read()
       doc2 = nlp2(text)
       for ent in doc2.ents:
           print(ent.label_, ent.text)

    AMOUNT=""
    INTEREST=""
    LOAN_MONTHS=""
    for ent in doc2.ents:
               if ent.label_ == 'AMOUNT':
                   AMOUNT =  ent.text
               if ent.label_ == 'LOAN_MONTHS':
                   LOAN_MONTHS =  ent.text
               if ent.label_ == 'INTEREST':
                   INTEREST =  ent.text
   TOTAL = (int(AMOUNT) * int(INTEREST)/100)*int(LOAN_MONTHS) + int(AMOUNT)
    print (TOTAL)
    
    json_map = {}
    json_map["AMOUNT"] = int(AMOUNT)
    json_map["LOAN_MONTHS"] = int(LOAN_MONTHS)
    json_map["INTEREST"] = int(INTEREST)
    json_map["TOTAL"] = TOTAL
    
    return result
