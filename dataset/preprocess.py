from lxml import etree
import os
import glob
import sys

context = etree.iterparse('./dataset/Posts.xml', events=('end',), tag='row')

if not os.path.exists('./dataset/docs'):
    os.makedirs('./dataset/docs')

i = 0
num_docs = -1
if len(sys.argv) > 1:
    num_docs = int(sys.argv[1])
for action, elem in context:
    new_row = etree.Element('post', attrib={attribute: elem.attrib[attribute] for attribute in ['Id', 'ParentId', 'AcceptedAnswerId', 'CreationDate', 'LastEditDate', 'Body', 'LastEditorDisplayName', 'Title', 'Tags'] if attribute in elem.attrib})
    if 'ParentId' in elem.attrib:
        filenum = elem.attrib['ParentId']
    else:
        filenum = elem.attrib['Id']
        i += 1
    with open('./dataset/docs/%s.xml' % filenum, 'ab') as doc:
        doc.write(etree.tostring(new_row) + b'\x0a')
    if i >= num_docs:
        break

# fout = open('./dataset/processed.xml', 'wb')
# fout.write(b'<posts>')
# for filename in glob.glob("./dataset/docs/*.xml"):
#     with open(filename, 'rb') as fin:
#         fout.write(fin.read())
# fout.write(b'</posts>')
# fout.close()
