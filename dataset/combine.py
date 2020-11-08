import glob

fout = open('./dataset/processed.xml', 'wb')
fout.write(b'<posts>')
for filename in glob.glob("./dataset/docs/*.xml"):
    with open(filename, 'rb') as fin:
        fout.write(fin.read())
fout.write(b'</posts>')
fout.close()
